"""日志检索。

现有运维模块没有日志平台，但已经有 SSH，所以做成「受约束的远程 grep」。
两处约束必须同时成立，缺一个就等于给 LLM 开了任意文件读取和命令注入：

1. **路径白名单**：只能检索 inventory 里为该主机声明过的日志路径。
   LLM 传任何其他路径一律拒绝，杜绝 `/etc/shadow` 这类越权读取。
2. **模式转义**：检索模式由 LLM 生成，必须 shell 转义后再拼命令。
   否则一个 `foo'; rm -rf /; echo '` 就绕过了整个安全层。

检索先 tail 出一个窗口再 grep，把扫描代价钉死在窗口大小上，
避免 LLM 一句「查所有错误」把几 GB 的日志全扫一遍。
"""

from __future__ import annotations

import re
import shlex

from ..errors import OPS_INVALID_INPUT, OpsToolError
from ..schemas import LogHit, LogSearchResult
from .ssh import SshPool, SshTarget

DEFAULT_SCAN_LINES = 5000
MAX_SCAN_LINES = 50000
DEFAULT_MAX_HITS = 80
MAX_HITS_CAP = 400
MAX_PATTERN_CHARS = 200

_TS = re.compile(
    r"(\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}:\d{2}(?:[.,]\d{1,6})?)"
)
_LEVEL = re.compile(r"\b(FATAL|ERROR|WARN(?:ING)?|INFO|DEBUG|TRACE)\b")


def _validate_pattern(pattern: str) -> str:
    text = (pattern or "").strip()
    if not text:
        return ""
    if len(text) > MAX_PATTERN_CHARS:
        raise OpsToolError(
            OPS_INVALID_INPUT, f"检索模式超过 {MAX_PATTERN_CHARS} 字符上限"
        )
    if "\n" in text or "\r" in text:
        raise OpsToolError(OPS_INVALID_INPUT, "检索模式不能包含换行")
    return text


def _resolve_paths(requested: list[str] | None, allowed: list[str]) -> list[str]:
    if not allowed:
        raise OpsToolError(
            OPS_INVALID_INPUT,
            "该主机在 inventory 中未声明任何日志路径，无法检索",
        )
    if not requested:
        return list(allowed)

    allowed_set = set(allowed)
    resolved: list[str] = []
    rejected: list[str] = []
    for path in requested:
        if path in allowed_set:
            resolved.append(path)
        else:
            rejected.append(path)
    if rejected:
        raise OpsToolError(
            OPS_INVALID_INPUT,
            "以下日志路径不在该主机的白名单内，已拒绝",
            detail={"rejected": rejected, "allowed": allowed},
        )
    return resolved


def _parse_hit(path: str, raw: str) -> LogHit:
    ts_match = _TS.search(raw)
    level_match = _LEVEL.search(raw)
    return LogHit(
        path=path,
        ts=ts_match.group(1) if ts_match else None,
        level=level_match.group(1).upper() if level_match else None,
        text=raw[:1000],
    )


def search(
    target: SshTarget,
    pool: SshPool,
    *,
    allowed_paths: list[str],
    paths: list[str] | None = None,
    pattern: str = "",
    scan_lines: int = DEFAULT_SCAN_LINES,
    max_hits: int = DEFAULT_MAX_HITS,
    timeout: float = 25.0,
) -> LogSearchResult:
    pattern = _validate_pattern(pattern)
    resolved = _resolve_paths(paths, allowed_paths)
    scan_lines = max(100, min(int(scan_lines), MAX_SCAN_LINES))
    max_hits = max(1, min(int(max_hits), MAX_HITS_CAP))

    hits: list[LogHit] = []
    errors: list[str] = []
    truncated = False

    for path in resolved:
        quoted_path = shlex.quote(path)
        tail = f"tail -n {scan_lines} -- {quoted_path} 2>/dev/null"
        if pattern:
            # -I 跳过二进制，-E 扩展正则，-- 终止选项解析防止模式被当成 flag
            command = f"{tail} | grep -EI -- {shlex.quote(pattern)} | tail -n {max_hits}"
        else:
            command = f"{tail} | tail -n {max_hits}"

        try:
            result = pool.run(target, command, timeout=timeout)
        except OpsToolError as exc:
            errors.append(f"{path}: {exc.message}")
            continue

        if result.truncated:
            truncated = True
        if result.stderr.strip():
            errors.append(f"{path}: {result.stderr.strip()[:200]}")

        lines = [ln for ln in result.stdout.splitlines() if ln.strip()]
        if len(lines) >= max_hits:
            truncated = True
        for line in lines:
            hits.append(_parse_hit(path, line))

    return LogSearchResult(
        host=target.host,
        hits=hits,
        truncated=truncated,
        scanned_paths=resolved,
        partial_errors=errors,
    )
