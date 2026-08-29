"""处置执行。

这是整条链路上唯一会改变生产状态的地方，所以把闸门全压在这一个函数里：
熔断开关 → 危险分级 → 人工审批令牌 → 干跑 → 执行 → 审计。

一个容易搞反的细节：**熔断开关只卡变更类命令**。只读取证必须在开关关闭时
依然可用，否则一出事故把开关一关，诊断能力也跟着没了，正好在最需要的时候失灵。
"""

from __future__ import annotations

import time

from .. import config
from ..cmdb.base import Inventory, InventoryEntry
from ..errors import (
    OPS_COMMAND_BLOCKED,
    OPS_EXEC_DISABLED,
    OPS_INVALID_INPUT,
    OpsToolError,
)
from ..evidence.ssh import SshPool
from ..safety import approval, audit
from ..safety.classifier import assess
from ..schemas import ExecResult, RiskLevel


def execute(
    entry: InventoryEntry,
    pool: SshPool,
    *,
    command: str,
    incident_id: str = "",
    step_id: str = "",
    approval_token: str = "",
    dry_run: bool = False,
    timeout: float | None = None,
) -> ExecResult:
    target = entry.ssh_target()
    host = target.host
    assessment = assess(command, allow_destructive=config.ALLOW_DESTRUCTIVE)

    def _audit(outcome: str, **kwargs) -> str:
        return audit.record(
            tool="ops_exec_command",
            outcome=outcome,
            incident_id=incident_id,
            step_id=step_id,
            host=host,
            command=command,
            risk=assessment.risk.value,
            dry_run=dry_run,
            **kwargs,
        )

    if assessment.blocked:
        _audit("blocked", error_code=OPS_COMMAND_BLOCKED,
               detail={"reason": assessment.reason, "rule": assessment.matched_rule})
        raise OpsToolError(
            OPS_COMMAND_BLOCKED,
            f"命令被安全策略拒绝：{assessment.reason}",
            detail={"risk": assessment.risk.value, "rule": assessment.matched_rule},
        )

    needs_change_gate = assessment.risk is not RiskLevel.READ_ONLY

    if needs_change_gate and not config.EXEC_ENABLED:
        _audit("blocked", error_code=OPS_EXEC_DISABLED, detail={"reason": "熔断开关关闭"})
        raise OpsToolError(
            OPS_EXEC_DISABLED,
            "处置执行总开关已关闭（OPS_EXEC_ENABLED=false），仅允许只读取证",
            detail={"risk": assessment.risk.value},
        )

    approver = ""
    approval_jti = ""
    if needs_change_gate and not dry_run:
        try:
            payload = approval.verify(approval_token, host=host, command=command)
        except OpsToolError as exc:
            # 「Agent 试图绕过人工确认」是复盘时最该看到的记录，不能让它静默冒泡
            _audit("blocked", error_code=exc.code, detail={"reason": exc.message})
            raise
        approver = str(payload.get("approver") or "")
        approval_jti = str(payload.get("jti") or "")

    if dry_run:
        audit_id = _audit("dry_run", approver=approver, approval_jti=approval_jti,
                          detail={"reason": assessment.reason})
        return ExecResult(
            host=host, command=command, risk=assessment.risk,
            stdout="[dry-run] 未实际执行", dry_run=True, audit_id=audit_id,
        )

    started = time.monotonic()
    try:
        output = pool.run(target, command, timeout=timeout)
    except OpsToolError as exc:
        _audit("failed", approver=approver, approval_jti=approval_jti,
               duration_ms=int((time.monotonic() - started) * 1000),
               error_code=exc.code, detail={"message": exc.message})
        raise

    audit_id = _audit(
        "success" if output.exit_code == 0 else "nonzero_exit",
        approver=approver,
        approval_jti=approval_jti,
        exit_code=output.exit_code,
        duration_ms=output.duration_ms,
        detail={"stdout_head": output.stdout[:500], "stderr_head": output.stderr[:500]},
    )

    return ExecResult(
        host=host,
        command=command,
        risk=assessment.risk,
        exit_code=output.exit_code,
        stdout=output.stdout,
        stderr=output.stderr,
        duration_ms=output.duration_ms,
        truncated=output.truncated,
        audit_id=audit_id,
    )


_SERVICE_ACTIONS = frozenset({"start", "stop", "restart", "status"})


def service_command(inventory: Inventory, entry: InventoryEntry, service: str, action: str) -> str:
    """把「重启 moli-gateway」翻译成具体命令，不让 LLM 自由拼服务管理命令。"""
    if action not in _SERVICE_ACTIONS:
        raise OpsToolError(
            OPS_INVALID_INPUT,
            f"不支持的服务动作 {action}",
            detail={"allowed": sorted(_SERVICE_ACTIONS)},
        )
    spec = inventory.find_service(entry, service)
    if spec.systemd_unit:
        return f"systemctl {action} {spec.systemd_unit}"
    if spec.container:
        docker_action = "ps" if action == "status" else action
        if docker_action == "ps":
            return f"docker ps -a --filter name={spec.container}"
        return f"docker {docker_action} {spec.container}"
    raise OpsToolError(
        OPS_INVALID_INPUT,
        f"服务 {service} 未声明 systemd_unit 或 container，无法生成管理命令",
    )
