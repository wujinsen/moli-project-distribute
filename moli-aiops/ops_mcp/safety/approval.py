"""人工审批令牌。

令牌把「人点了同意」这件事变成可验证、可审计、不可挪用的凭据。三条约束：

1. **绑定命令哈希**：令牌只对被审批的那一条命令有效。否则 Agent 拿到某步的批准后
   换一条命令执行，人工确认就形同虚设——这是人机协同链路上最容易被忽略的攻击面。
2. **限时**：默认 10 分钟过期，避免批准被囤积到之后的事故里复用。
3. **一次性**：消费后记入 nonce 表，重放直接拒绝。
"""

from __future__ import annotations

import base64
import hashlib
import hmac
import json
import secrets
import time
import uuid
from typing import Any

from .. import config
from ..errors import OPS_APPROVAL_REQUIRED, OpsToolError

# 未配置 OPS_APPROVAL_SECRET 时用进程内随机密钥：重启即全部失效，比落一个默认弱密钥安全
_RUNTIME_SECRET = config.APPROVAL_SECRET or secrets.token_hex(32)

_CONSUMED: set[str] = set()


def command_fingerprint(host: str, command: str) -> str:
    """审批绑定到 (目标主机, 命令原文)，换主机或改一个字符都会失配。"""
    raw = f"{host}\n{command.strip()}".encode()
    return hashlib.sha256(raw).hexdigest()


def _sign(payload_b64: str) -> str:
    return hmac.new(_RUNTIME_SECRET.encode(), payload_b64.encode(), hashlib.sha256).hexdigest()


def issue(
    *,
    host: str,
    command: str,
    risk: str,
    incident_id: str,
    step_id: str,
    approver: str,
    ttl_s: int | None = None,
) -> dict[str, Any]:
    now = int(time.time())
    ttl = ttl_s if ttl_s is not None else config.APPROVAL_TTL_S
    payload = {
        "jti": uuid.uuid4().hex,
        "fp": command_fingerprint(host, command),
        "host": host,
        "risk": risk,
        "incident_id": incident_id,
        "step_id": step_id,
        "approver": approver,
        "iat": now,
        "exp": now + ttl,
    }
    payload_b64 = base64.urlsafe_b64encode(
        json.dumps(payload, separators=(",", ":"), sort_keys=True).encode()
    ).decode()
    token = f"{payload_b64}.{_sign(payload_b64)}"
    return {"token": token, "expires_at": payload["exp"], "jti": payload["jti"]}


def verify(token: str, *, host: str, command: str) -> dict[str, Any]:
    """校验通过返回 payload，否则抛 OpsToolError。校验成功即消费该令牌。"""
    if not token:
        raise OpsToolError(OPS_APPROVAL_REQUIRED, "该操作需要人工审批令牌")

    try:
        payload_b64, signature = token.rsplit(".", 1)
    except ValueError as exc:
        raise OpsToolError(OPS_APPROVAL_REQUIRED, "审批令牌格式非法") from exc

    if not hmac.compare_digest(_sign(payload_b64), signature):
        raise OpsToolError(OPS_APPROVAL_REQUIRED, "审批令牌签名校验失败")

    try:
        payload = json.loads(base64.urlsafe_b64decode(payload_b64.encode()))
    except Exception as exc:  # noqa: BLE001
        raise OpsToolError(OPS_APPROVAL_REQUIRED, "审批令牌载荷无法解析") from exc

    if int(payload.get("exp", 0)) < int(time.time()):
        raise OpsToolError(
            OPS_APPROVAL_REQUIRED, "审批令牌已过期，请重新确认",
            detail={"expired_at": payload.get("exp")},
        )

    expected_fp = command_fingerprint(host, command)
    if not hmac.compare_digest(str(payload.get("fp", "")), expected_fp):
        raise OpsToolError(
            OPS_APPROVAL_REQUIRED,
            "审批令牌与待执行命令不匹配：该令牌批准的是另一条命令或另一台主机",
            detail={"expected_fingerprint": expected_fp},
        )

    jti = str(payload.get("jti", ""))
    if jti in _CONSUMED:
        raise OpsToolError(OPS_APPROVAL_REQUIRED, "审批令牌已被使用，不可重放")
    _CONSUMED.add(jti)

    return payload


def reset_consumed() -> None:
    """仅供测试：清空已消费令牌。"""
    _CONSUMED.clear()
