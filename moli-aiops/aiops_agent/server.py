"""HTTP 层：诊断入口、SSE 进度流、人工审批。

**审批令牌只在这里签发。** 工具层不提供签发函数，Agent 也拿不到密钥，
所以「人点了同意」这件事无法被自动化伪造。签发时把令牌绑定到
(目标主机, 命令原文)，Agent 拿到之后只能用于那一条命令。

诊断在后台线程里跑，进度用 LangGraph 的 stream 逐节点收集，SSE 推给前端。
"""

from __future__ import annotations

import asyncio
import json
import logging
import threading
import time
import uuid
from typing import Any

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from pydantic import BaseModel, Field
from sse_starlette.sse import EventSourceResponse

from langgraph.types import Command

from ops_mcp import config as ops_config
from ops_mcp.errors import OpsToolError
from ops_mcp.safety import approval as approval_mod

from . import alert_webhook, config, trace
from .auth import ShiroAuthMiddleware
from .graph import DiagnosisEngine
from .models import Alert

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger("aiops.server")

app = FastAPI(title="moli-aiops · 故障诊断多智能体平台", version="0.1.0")
app.add_middleware(ShiroAuthMiddleware)
app.add_middleware(
    CORSMiddleware,
    allow_origins=config.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
ENGINE = DiagnosisEngine()


class RunChannel:
    """一次诊断的事件缓冲。SSE 端按游标增量取，避免维护复杂的订阅关系。"""

    def __init__(self, run_id: str) -> None:
        self.run_id = run_id
        self.events: list[dict[str, Any]] = []
        self.done = False
        self.error: str = ""
        self._lock = threading.Lock()

    def emit(self, event: dict[str, Any]) -> None:
        with self._lock:
            event.setdefault("ts", time.strftime("%H:%M:%S"))
            self.events.append(event)

    def since(self, cursor: int) -> tuple[list[dict[str, Any]], int]:
        with self._lock:
            return list(self.events[cursor:]), len(self.events)


CHANNELS: dict[str, RunChannel] = {}


def _channel(run_id: str) -> RunChannel:
    channel = CHANNELS.get(run_id)
    if channel is None:
        channel = RunChannel(run_id)
        CHANNELS[run_id] = channel
    return channel


def _pump(run_id: str, stream_input: Any) -> None:
    """在后台线程里驱动图，把每个节点的产出转成前端事件。"""
    channel = _channel(run_id)
    cfg = {"configurable": {"thread_id": run_id}}
    try:
        for chunk in ENGINE.graph.stream(stream_input, cfg, stream_mode="updates"):
            for node, update in (chunk or {}).items():
                if node == "__interrupt__":
                    payloads = [getattr(i, "value", i) for i in (update or ())]
                    channel.emit({
                        "type": "approval_required",
                        "payload": payloads[0] if payloads else {},
                    })
                    continue
                if not isinstance(update, dict):
                    continue
                progress_events = update.get("progress") or []
                latest = progress_events[-1] if progress_events else None
                channel.emit({
                    "type": "node",
                    "node": node,
                    "message": (latest or {}).get("message", ""),
                    "pct": (latest or {}).get("pct", 0),
                })
    except Exception as exc:  # noqa: BLE001
        log.exception("诊断 %s 异常终止", run_id)
        channel.error = f"{type(exc).__name__}: {exc}"
        channel.emit({"type": "error", "message": channel.error})
    finally:
        snapshot = ENGINE.snapshot(run_id)
        status = "awaiting_approval" if snapshot["interrupts"] else \
            str((snapshot.get("values") or {}).get("status") or "finished")
        trace.update_run(run_id, status=status)
        channel.emit({"type": "phase_done", "status": status})
        # 等待审批时通道保持开启，人确认后同一通道继续推后续节点
        channel.done = not snapshot["interrupts"]


# --- 请求模型 -------------------------------------------------------------


class DiagnoseRequest(BaseModel):
    title: str = Field(..., description="告警标题")
    target: str = Field(..., description="inventory 中的主机 id")
    service: str = ""
    description: str = ""
    source: str = "manual"
    trace_id: str = ""
    labels: dict[str, str] = Field(default_factory=dict)


class ApproveRequest(BaseModel):
    approver: str = Field(..., min_length=1)
    approved_step_ids: list[str] = Field(default_factory=list)
    comment: str = ""


class RejectRequest(BaseModel):
    approver: str = Field(..., min_length=1)
    comment: str = ""


class RerunRequest(BaseModel):
    node: str
    patch: dict[str, Any] = Field(default_factory=dict)


# --- 接口 ---------------------------------------------------------------


def _start_diagnose(alert: Alert) -> dict[str, str]:
    run_id = f"run-{uuid.uuid4().hex[:12]}"
    incident_id = alert.id or f"inc-{uuid.uuid4().hex[:8]}"
    alert.id = incident_id
    if not alert.fired_at:
        alert.fired_at = time.strftime("%Y-%m-%dT%H:%M:%S")
    trace.start_run(run_id, incident_id, alert)
    _channel(run_id).emit({"type": "started", "message": f"开始诊断：{alert.title}"})
    initial = {
        "run_id": run_id, "incident_id": incident_id, "alert": alert.model_dump(mode="json"),
        "evidence": [], "hypotheses": [], "progress": [], "iteration": 0,
        "status": "running", "started_ms": int(time.time() * 1000),
    }
    threading.Thread(target=_pump, args=(run_id, initial), daemon=True).start()
    return {"run_id": run_id, "incident_id": incident_id}


@app.get("/health")
def health() -> dict[str, Any]:
    return {
        "ok": True,
        "llm_configured": ENGINE.router.configured,
        "llm_providers": [p.name for p in ENGINE.router.providers],
        "cmdb_source": ENGINE.ctx.cmdb.name,
        "inventory_targets": [e.id for e in ENGINE.ctx.inventory.entries],
        "exec_enabled": ops_config.EXEC_ENABLED,
        "force_dry_run": config.FORCE_DRY_RUN,
        "alert_webhook_configured": bool(config.ALERT_WEBHOOK_SECRET),
        "prometheus_url": ops_config.PROMETHEUS_URL,
    }


@app.post("/hooks/alertmanager")
def alertmanager_hook(payload: dict[str, Any]) -> dict[str, Any]:
    """Alertmanager webhook。鉴权在中间件用 Bearer，不走 Shiro。"""
    alerts = alert_webhook.alerts_from_payload(payload, inventory=ENGINE.ctx.inventory)
    started: list[dict[str, str]] = []
    skipped: list[str] = []
    for alert in alerts:
        fp = alert_webhook.fingerprint(alert.labels)
        if alert_webhook.should_skip_duplicate(fp, ttl_s=config.ALERT_DEDUP_TTL_S):
            skipped.append(fp)
            continue
        started.append(_start_diagnose(alert))
    return {
        "ok": True,
        "received": len(payload.get("alerts") or []),
        "started": started,
        "skipped": skipped,
    }


@app.post("/diagnose")
def diagnose(body: DiagnoseRequest) -> dict[str, Any]:
    alert = Alert(
        title=body.title, description=body.description,
        target=body.target, service=body.service, source=body.source,
        trace_id=body.trace_id, labels=body.labels,
    )
    return _start_diagnose(alert)


@app.get("/runs/{run_id}/stream")
async def stream(run_id: str, cursor: int = 0):
    channel = CHANNELS.get(run_id)
    if channel is None:
        raise HTTPException(404, f"未知的诊断 {run_id}")

    async def generator():
        position = cursor
        idle_ticks = 0
        while True:
            events, position = channel.since(position)
            for event in events:
                yield {"event": "message", "data": json.dumps(event, ensure_ascii=False)}
            if channel.done and not events:
                yield {"event": "done", "data": json.dumps({"run_id": run_id})}
                return
            idle_ticks = 0 if events else idle_ticks + 1
            # 等待人工审批期间可能长时间无事件，靠心跳保持连接
            if idle_ticks >= 40:
                idle_ticks = 0
                yield {"event": "ping", "data": "{}"}
            await asyncio.sleep(0.25)

    return EventSourceResponse(generator())


@app.get("/runs")
def list_runs(limit: int = 30) -> dict[str, Any]:
    return {"runs": trace.list_runs(limit)}


@app.get("/runs/{run_id}")
def get_run(run_id: str) -> dict[str, Any]:
    record = trace.get_run(run_id)
    if record is None:
        raise HTTPException(404, f"未知的诊断 {run_id}")
    snapshot = ENGINE.snapshot(run_id)
    return {
        "run": record,
        "next": snapshot["next"],
        "interrupts": snapshot["interrupts"],
        "values": snapshot["values"],
        "trace": trace.get_traces(run_id),
        "trace_summary": trace.summarize(run_id),
    }


@app.post("/runs/{run_id}/approve")
def approve(run_id: str, body: ApproveRequest) -> dict[str, Any]:
    snapshot = ENGINE.snapshot(run_id)
    if not snapshot["interrupts"]:
        raise HTTPException(409, "该诊断当前没有待确认的预案")

    request = snapshot["interrupts"][0]["value"] or {}
    steps = {str(s.get("id")): s for s in (request.get("steps") or [])}
    approved_ids = body.approved_step_ids or list(steps)
    unknown = [s for s in approved_ids if s not in steps]
    if unknown:
        raise HTTPException(400, f"预案中不存在这些步骤：{unknown}")

    tokens: dict[str, str] = {}
    for step_id in approved_ids:
        step = steps[step_id]
        if not step.get("requires_approval"):
            continue
        try:
            entry = ENGINE.ctx.inventory.resolve(str(step.get("target") or ""))
        except OpsToolError as exc:
            raise HTTPException(400, exc.message) from exc
        # 令牌绑定到 (主机, 命令原文)：换命令、换主机都会校验失败
        tokens[step_id] = approval_mod.issue(
            host=entry.host,
            command=str(step.get("command") or ""),
            risk=str(step.get("risk") or "mutating"),
            incident_id=str(request.get("incident_id") or ""),
            step_id=step_id,
            approver=body.approver,
        )["token"]

    decision = {
        "approved": True, "approver": body.approver, "comment": body.comment,
        "approved_step_ids": approved_ids, "tokens": tokens,
    }
    _channel(run_id).emit({
        "type": "approved",
        "message": f"{body.approver} 批准了 {len(approved_ids)} 步，签发 {len(tokens)} 张令牌",
    })
    _channel(run_id).done = False
    threading.Thread(target=_pump, args=(run_id, Command(resume=decision)), daemon=True).start()
    return {"run_id": run_id, "approved_step_ids": approved_ids, "tokens_issued": len(tokens)}


@app.post("/runs/{run_id}/reject")
def reject(run_id: str, body: RejectRequest) -> dict[str, Any]:
    snapshot = ENGINE.snapshot(run_id)
    if not snapshot["interrupts"]:
        raise HTTPException(409, "该诊断当前没有待确认的预案")

    decision = {"approved": False, "approver": body.approver, "comment": body.comment}
    _channel(run_id).emit({"type": "rejected", "message": f"{body.approver} 否决了预案"})
    _channel(run_id).done = False
    threading.Thread(target=_pump, args=(run_id, Command(resume=decision)), daemon=True).start()
    return {"run_id": run_id, "approved": False}


@app.get("/runs/{run_id}/history")
def history(run_id: str, limit: int = 40) -> dict[str, Any]:
    return {"history": ENGINE.history(run_id, limit)}


@app.post("/runs/{run_id}/rerun")
def rerun(run_id: str, body: RerunRequest) -> dict[str, Any]:
    try:
        ENGINE.rerun_from(run_id, body.node, body.patch or None)
    except ValueError as exc:
        raise HTTPException(400, str(exc)) from exc
    return {"run_id": run_id, "reran_from": body.node}


@app.get("/", response_class=HTMLResponse)
def index() -> str:
    from pathlib import Path

    return (Path(__file__).parent / "static" / "index.html").read_text(encoding="utf-8")


def main() -> None:
    import uvicorn

    uvicorn.run(app, host=config.SERVER_HOST, port=config.SERVER_PORT)


if __name__ == "__main__":
    main()
