"""知识库检索。

复用 moli-knowledge 已有的 `/kb/ask`，让诊断能引用历史事故记录和 Runbook，
而不是每次都从零推理。这也是两个项目串起来的地方：诊断时读知识库，
复盘时把结论写回知识库。

知识库不可用不应该让诊断中断——它是加分证据，不是必需证据。
"""

from __future__ import annotations

from typing import Any

import httpx

from .. import config
from ..schemas import KbAnswer, KbHit


class KbClient:
    def __init__(self, base_url: str | None = None, token: str | None = None) -> None:
        self.base_url = (base_url or config.KB_BASE_URL).rstrip("/")
        self.token = token if token is not None else config.KB_AUTH_TOKEN

    def configured(self) -> bool:
        return bool(self.base_url and self.token)

    def ask(
        self,
        question: str,
        *,
        space_ids: list[int] | None = None,
        top_k: int = 6,
        retrieval_strategy: str = "hybrid",
        use_llm: bool = False,
    ) -> KbAnswer:
        """默认 use_llm=False：诊断阶段要的是可引用的原文片段，
        生成式回答交给编排层自己的模型路由，避免两层 LLM 各说各话。"""
        if not self.configured():
            return KbAnswer(mode="unconfigured")

        body: dict[str, Any] = {
            "question": question[:500],
            "useLlm": use_llm,
            "topK": top_k,
            "retrievalStrategy": retrieval_strategy,
        }
        if space_ids:
            body["spaceIds"] = space_ids

        try:
            with httpx.Client(timeout=config.KB_TIMEOUT_S) as client:
                response = client.post(
                    f"{self.base_url}/kb/ask",
                    json=body,
                    headers={"Authorization": self.token, "Content-Type": "application/json"},
                )
                response.raise_for_status()
                payload = response.json()
        except Exception as exc:  # noqa: BLE001
            return KbAnswer(mode=f"unavailable: {exc}")

        code = payload.get("code") if isinstance(payload, dict) else None
        if code is not None and str(code) not in ("0", "200"):
            return KbAnswer(mode=f"error: {payload.get('msg') or code}")

        data = (payload or {}).get("data") or {}
        citations = [
            KbHit(
                slug=str(c.get("slug") or ""),
                title=str(c.get("title") or ""),
                snippet=str(c.get("snippet") or "")[:600],
                space_id=c.get("spaceId"),
            )
            for c in (data.get("citations") or [])
            if isinstance(c, dict)
        ]
        return KbAnswer(
            answer=str(data.get("answer") or ""),
            mode=str(data.get("mode") or "retrieval"),
            citations=citations,
            provider=str(data.get("provider") or ""),
            model=str(data.get("model") or ""),
        )
