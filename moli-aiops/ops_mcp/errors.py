"""工具层类型化错误。沿用 moli-knowledge/mcp/errors.py 的约定。

`retryable` 用于告诉编排层：换参数重试有没有意义。诊断 Agent 据此决定
是回补取证（可重试）还是直接放弃这条证据线（不可重试）。
"""

from __future__ import annotations

import json
from typing import Any

OPS_INVALID_INPUT = "OPS_INVALID_INPUT"
OPS_TARGET_NOT_FOUND = "OPS_TARGET_NOT_FOUND"
OPS_CMDB_UNAVAILABLE = "OPS_CMDB_UNAVAILABLE"
OPS_SSH_ERROR = "OPS_SSH_ERROR"
OPS_TIMEOUT = "OPS_TIMEOUT"
# 命令被危险分级拦下，改写命令可能通过
OPS_COMMAND_BLOCKED = "OPS_COMMAND_BLOCKED"
# 需要人工审批令牌才能执行
OPS_APPROVAL_REQUIRED = "OPS_APPROVAL_REQUIRED"
# 全局熔断开关关闭（对应 user-center 的 ops.command.enabled）
OPS_EXEC_DISABLED = "OPS_EXEC_DISABLED"
OPS_UNKNOWN_TOOL = "OPS_UNKNOWN_TOOL"
# SkyWalking OAP / Loki 不可达或返回非 2xx
OPS_OBSERVABILITY_UNAVAILABLE = "OPS_OBSERVABILITY_UNAVAILABLE"

# 换参数重试不会有不同结果，编排层不应重试
NON_RETRYABLE = frozenset(
    {
        OPS_EXEC_DISABLED,
        OPS_APPROVAL_REQUIRED,
        OPS_UNKNOWN_TOOL,
        OPS_TARGET_NOT_FOUND,
    }
)


class OpsToolError(Exception):
    def __init__(self, code: str, message: str, *, detail: dict[str, Any] | None = None) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.detail = detail or {}

    @property
    def retryable(self) -> bool:
        return self.code not in NON_RETRYABLE

    def to_payload(self) -> dict[str, Any]:
        return {
            "ok": False,
            "code": self.code,
            "message": self.message,
            "retryable": self.retryable,
            "detail": self.detail,
        }


def success_payload(data: dict[str, Any]) -> dict[str, Any]:
    return {"ok": True, **data}


def dumps(payload: dict[str, Any]) -> str:
    return json.dumps(payload, ensure_ascii=False, default=str)
