"""SSH 执行器。对应 Java 侧 OperationSshClient 用的 JSch。

连接按主机复用：一次取证要跑六七条命令，每条都重连会让取证阶段平白多出几秒。
输出有硬上限，防止 tail 一个巨型日志把内存和后续 LLM 上下文一起打爆。
"""

from __future__ import annotations

import io
import threading
import time
from dataclasses import dataclass, field
from pathlib import Path

import paramiko

from .. import config
from ..errors import OPS_SSH_ERROR, OPS_TIMEOUT, OpsToolError


@dataclass
class SshTarget:
    id: str
    host: str
    port: int = 22
    user: str = "root"
    password: str | None = None
    private_key_path: str | None = None
    private_key_text: str | None = None
    passphrase: str | None = None
    name: str = ""
    role: str = ""
    tags: list[str] = field(default_factory=list)

    @property
    def label(self) -> str:
        return self.name or f"{self.user}@{self.host}:{self.port}"


@dataclass
class CommandOutput:
    exit_code: int | None
    stdout: str
    stderr: str
    duration_ms: int
    truncated: bool


def _load_pkey(target: SshTarget) -> paramiko.PKey | None:
    text = target.private_key_text
    if not text and target.private_key_path:
        path = Path(target.private_key_path).expanduser()
        if not path.exists():
            raise OpsToolError(
                OPS_SSH_ERROR, f"私钥文件不存在：{path}", detail={"target": target.id}
            )
        text = path.read_text(encoding="utf-8")
    if not text:
        return None

    last_error: Exception | None = None
    # 不同密钥类型只能逐个试，paramiko 没有统一的自动识别入口
    for key_cls in (paramiko.Ed25519Key, paramiko.ECDSAKey, paramiko.RSAKey, paramiko.DSSKey):
        try:
            return key_cls.from_private_key(io.StringIO(text), password=target.passphrase)
        except Exception as exc:  # noqa: BLE001
            last_error = exc
    raise OpsToolError(
        OPS_SSH_ERROR, "私钥无法解析（格式不支持或口令错误）",
        detail={"target": target.id, "cause": str(last_error)},
    )


class SshSession:
    """单主机 SSH 会话，线程安全，可复用。"""

    def __init__(self, target: SshTarget) -> None:
        self.target = target
        self._client: paramiko.SSHClient | None = None
        self._lock = threading.Lock()

    def _connect(self) -> paramiko.SSHClient:
        client = paramiko.SSHClient()
        client.load_system_host_keys()
        # 实验环境默认放行未知主机；生产应配置 known_hosts 并改为 RejectPolicy
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        try:
            client.connect(
                hostname=self.target.host,
                port=self.target.port,
                username=self.target.user,
                password=self.target.password,
                pkey=_load_pkey(self.target),
                timeout=config.SSH_CONNECT_TIMEOUT_S,
                banner_timeout=config.SSH_CONNECT_TIMEOUT_S,
                auth_timeout=config.SSH_CONNECT_TIMEOUT_S,
                look_for_keys=False,
                allow_agent=False,
            )
        except OpsToolError:
            raise
        except Exception as exc:  # noqa: BLE001
            raise OpsToolError(
                OPS_SSH_ERROR, f"SSH 连接 {self.target.label} 失败：{exc}",
                detail={"target": self.target.id, "host": self.target.host},
            ) from exc
        return client

    def _ensure(self) -> paramiko.SSHClient:
        if self._client is not None:
            transport = self._client.get_transport()
            if transport is not None and transport.is_active():
                return self._client
            self.close()
        self._client = self._connect()
        return self._client

    def run(self, command: str, *, timeout: float | None = None) -> CommandOutput:
        limit = config.SSH_MAX_OUTPUT_BYTES
        deadline_s = timeout if timeout is not None else config.SSH_COMMAND_TIMEOUT_S
        started = time.monotonic()

        with self._lock:
            client = self._ensure()
            transport = client.get_transport()
            if transport is None:
                raise OpsToolError(OPS_SSH_ERROR, "SSH transport 不可用", detail={"target": self.target.id})
            channel = transport.open_session()
            channel.settimeout(deadline_s)
            try:
                channel.exec_command(command)
                out, err, truncated = self._drain(channel, limit, started, deadline_s)
                exit_code = channel.recv_exit_status() if channel.exit_status_ready() else None
            finally:
                channel.close()

        return CommandOutput(
            exit_code=exit_code,
            stdout=out,
            stderr=err,
            duration_ms=int((time.monotonic() - started) * 1000),
            truncated=truncated,
        )

    def _drain(
        self, channel: paramiko.Channel, limit: int, started: float, deadline_s: float
    ) -> tuple[str, str, bool]:
        out = bytearray()
        err = bytearray()
        truncated = False
        while True:
            if time.monotonic() - started > deadline_s:
                raise OpsToolError(
                    OPS_TIMEOUT,
                    f"命令在 {self.target.label} 上超过 {deadline_s:.0f}s 未返回",
                    detail={"target": self.target.id},
                )
            progressed = False
            if channel.recv_ready():
                chunk = channel.recv(32768)
                progressed = bool(chunk)
                if len(out) < limit:
                    out.extend(chunk)
                else:
                    truncated = True
            if channel.recv_stderr_ready():
                chunk = channel.recv_stderr(32768)
                progressed = progressed or bool(chunk)
                if len(err) < limit:
                    err.extend(chunk)
                else:
                    truncated = True
            if channel.exit_status_ready() and not channel.recv_ready() and not channel.recv_stderr_ready():
                break
            if not progressed:
                time.sleep(0.02)

        if len(out) > limit:
            out = out[:limit]
            truncated = True
        if len(err) > limit:
            err = err[:limit]
            truncated = True
        return (
            out.decode("utf-8", errors="replace"),
            err.decode("utf-8", errors="replace"),
            truncated,
        )

    def close(self) -> None:
        if self._client is not None:
            try:
                self._client.close()
            except Exception:  # noqa: BLE001, S110
                pass
            self._client = None


class SshPool:
    """按 target id 复用会话。"""

    def __init__(self) -> None:
        self._sessions: dict[str, SshSession] = {}
        self._lock = threading.Lock()

    def session(self, target: SshTarget) -> SshSession:
        with self._lock:
            existing = self._sessions.get(target.id)
            if existing is None:
                existing = SshSession(target)
                self._sessions[target.id] = existing
            return existing

    def run(self, target: SshTarget, command: str, *, timeout: float | None = None) -> CommandOutput:
        return self.session(target).run(command, timeout=timeout)

    def close_all(self) -> None:
        with self._lock:
            for session in self._sessions.values():
                session.close()
            self._sessions.clear()


POOL = SshPool()
