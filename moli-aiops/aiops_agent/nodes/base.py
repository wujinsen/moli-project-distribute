"""节点共用设施：trace 埋点、LLM 调用包装、规则兜底。

每个 LLM 节点都遵循同一个模式：先试模型，模型不可用就走规则兜底并把这一步
标成 degraded。所以整条链路在没有任何 API key 的情况下也能跑完，
只是结论粗糙——这让「兜底」是可以被跑出来验证的，而不是文档里的一句承诺。
"""

from __future__ import annotations

import time
from contextlib import contextmanager
from typing import Any

from .. import trace as trace_store
from ..llm import LlmResult, LlmRouter, LlmUnavailable
from ..models import NodeTrace


def now() -> str:
    return time.strftime("%Y-%m-%dT%H:%M:%S")


@contextmanager
def node_span(state: dict, node: str):
    """计时、落 trace。异常也会落，状态标成 error。"""
    node_trace = NodeTrace(node=node, iteration=int(state.get("iteration") or 0), started_at=now())
    started = time.monotonic()
    try:
        yield node_trace
    except Exception as exc:  # noqa: BLE001
        node_trace.status = "error"
        node_trace.error = f"{type(exc).__name__}: {exc}"
        raise
    finally:
        node_trace.duration_ms = int((time.monotonic() - started) * 1000)
        run_id = str(state.get("run_id") or "")
        if run_id:
            trace_store.add_node_trace(run_id, node_trace)


def apply_llm_result(node_trace: NodeTrace, result: LlmResult) -> None:
    node_trace.model = result.model
    node_trace.provider = result.provider
    node_trace.prompt_tokens = result.prompt_tokens
    node_trace.completion_tokens = result.completion_tokens
    node_trace.fallback_used = result.fallback_used
    node_trace.attempts = result.attempts


def try_llm_json(
    router: LlmRouter,
    node_trace: NodeTrace,
    *,
    node: str,
    system: str,
    user: str,
) -> dict[str, Any] | None:
    """成功返回解析后的 dict，模型不可用或输出不可解析时返回 None 由调用方兜底。"""
    try:
        payload, result = router.chat_json(node=node, system=system, user=user)
    except LlmUnavailable as exc:
        node_trace.status = "degraded"
        node_trace.error = f"模型不可用，已降级为规则模式：{exc}"
        return None
    except ValueError as exc:
        node_trace.status = "degraded"
        node_trace.error = f"模型输出无法解析，已降级为规则模式：{exc}"
        return None
    apply_llm_result(node_trace, result)
    return payload


def progress(state: dict, phase: str, message: str, pct: int, **detail: Any) -> list[dict]:
    """进度事件累积在 state 里，供 SSE 流式推给前端。"""
    events = list(state.get("progress") or [])
    events.append({"phase": phase, "message": message, "pct": pct, "detail": detail})
    return events


def over_budget(state: dict, budget_ms: int) -> bool:
    started = int(state.get("started_ms") or 0)
    if not started:
        return False
    return (int(time.time() * 1000) - started) >= budget_ms
