"""CMDB 抽象与本地 inventory。

拓扑元数据可以来自 user-center REST 或本地文件，但 **SSH 凭据始终来自本地
inventory**：user-center 把私钥 AES-GCM 加密存在库里且不对外暴露，诊断平台
也不应该通过 HTTP 去捞私钥。凭据留在本地是更清晰的安全边界。
"""

from __future__ import annotations

from pathlib import Path
from typing import Any, Protocol

import yaml

from ..errors import OPS_INVALID_INPUT, OPS_TARGET_NOT_FOUND, OpsToolError
from ..evidence.ssh import SshTarget
from ..schemas import ChangeRecord, TopologyGraph


class ServiceSpec:
    """inventory 里声明的一个服务，用于日志白名单与处置动作。"""

    def __init__(self, raw: dict[str, Any]) -> None:
        self.name: str = str(raw.get("name") or "").strip()
        self.port: int | None = int(raw["port"]) if raw.get("port") else None
        self.systemd_unit: str = str(raw.get("systemd_unit") or "").strip()
        self.container: str = str(raw.get("container") or "").strip()
        self.log_paths: list[str] = [str(p) for p in (raw.get("log_paths") or [])]
        self.depends_on: list[str] = [str(d) for d in (raw.get("depends_on") or [])]
        self.health_url: str = str(raw.get("health_url") or "").strip()


class InventoryEntry:
    def __init__(self, raw: dict[str, Any]) -> None:
        if not raw.get("host"):
            raise OpsToolError(OPS_INVALID_INPUT, f"inventory 条目缺少 host：{raw!r}")
        self.raw = raw
        self.id: str = str(raw.get("id") or raw.get("server_id") or raw["host"])
        self.server_id: str = str(raw.get("server_id") or self.id)
        self.name: str = str(raw.get("name") or "")
        self.host: str = str(raw["host"])
        self.ssh_port: int = int(raw.get("ssh_port") or 22)
        self.role: str = str(raw.get("role") or "")
        self.tags: list[str] = [str(t) for t in (raw.get("tags") or [])]
        self.services: list[ServiceSpec] = [ServiceSpec(s) for s in (raw.get("services") or [])]
        self._log_paths: list[str] = [str(p) for p in (raw.get("log_paths") or [])]

    @property
    def log_paths(self) -> list[str]:
        """主机级路径 + 各服务声明的路径，去重后作为日志检索白名单。"""
        merged = list(self._log_paths)
        for svc in self.services:
            for path in svc.log_paths:
                if path not in merged:
                    merged.append(path)
        return merged

    def ssh_target(self) -> SshTarget:
        return SshTarget(
            id=self.id,
            host=self.host,
            port=self.ssh_port,
            user=str(self.raw.get("user") or "root"),
            password=self.raw.get("password"),
            private_key_path=self.raw.get("private_key_path"),
            private_key_text=self.raw.get("private_key_text"),
            passphrase=self.raw.get("passphrase"),
            name=self.name,
            role=self.role,
            tags=self.tags,
        )


class Inventory:
    def __init__(self, entries: list[InventoryEntry]) -> None:
        self.entries = entries
        self._by_id = {e.id: e for e in entries}
        self._by_server_id = {e.server_id: e for e in entries}
        self._by_host = {e.host: e for e in entries}

    @classmethod
    def load(cls, path: Path) -> Inventory:
        if not path.exists():
            return cls([])
        data = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
        raw_targets = data.get("targets") or []
        if not isinstance(raw_targets, list):
            raise OpsToolError(OPS_INVALID_INPUT, f"{path} 的 targets 必须是数组")
        return cls([InventoryEntry(t) for t in raw_targets])

    def resolve(self, key: str | int) -> InventoryEntry:
        text = str(key)
        for table in (self._by_id, self._by_server_id, self._by_host):
            hit = table.get(text)
            if hit is not None:
                return hit
        # 允许用 "server:1" 这种拓扑节点 id 反查
        if ":" in text:
            _, _, suffix = text.partition(":")
            hit = self._by_server_id.get(suffix)
            if hit is not None:
                return hit
        raise OpsToolError(
            OPS_TARGET_NOT_FOUND,
            f"inventory 中找不到目标 {text}（SSH 凭据必须在本地 inventory 声明）",
            detail={"known": sorted(self._by_id)},
        )

    def find_service(self, entry: InventoryEntry, service_name: str) -> ServiceSpec:
        for svc in entry.services:
            if svc.name == service_name:
                return svc
        raise OpsToolError(
            OPS_TARGET_NOT_FOUND,
            f"主机 {entry.id} 上未声明服务 {service_name}",
            detail={"known": [s.name for s in entry.services]},
        )

    def __len__(self) -> int:
        return len(self.entries)


class CmdbSource(Protocol):
    """拓扑元数据来源。"""

    name: str

    def topology(self) -> TopologyGraph: ...

    def recent_changes(self, server_id: str | int | None, limit: int) -> list[ChangeRecord]: ...

    def available(self) -> bool: ...
