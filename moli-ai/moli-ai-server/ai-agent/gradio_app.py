#!/usr/bin/env python3
"""W7 Gradio 冒烟：自然语言 → Java /bi/chat/ask（支持网关 + Authorization）。

  set BI_CHAT_BASE=http://127.0.0.1:21000/AiServer
  set BI_CHAT_TOKEN=<login token>
  python gradio_app.py
"""
from __future__ import annotations

import json
import os

import gradio as gr
import httpx

AI_SERVER = os.environ.get("BI_CHAT_BASE", "http://127.0.0.1:1128")
AUTH_TOKEN = os.environ.get("BI_CHAT_TOKEN", "")


def _headers() -> dict[str, str]:
    h: dict[str, str] = {}
    if AUTH_TOKEN:
        h["Authorization"] = AUTH_TOKEN
    return h


def ask_chat(question: str, use_stream: bool) -> str:
    if not question or not question.strip():
        return "请输入问题"
    try:
        with httpx.Client(timeout=120.0) as client:
            url = f"{AI_SERVER.rstrip('/')}/bi/chat/ask"
            payload = {"question": question.strip(), "stream": bool(use_stream)}
            if use_stream:
                lines: list[str] = []
                with client.stream(
                    "POST", url, json=payload, headers=_headers(), timeout=120.0
                ) as resp:
                    if resp.status_code != 200:
                        return f"HTTP {resp.status_code}: {resp.text[:500]}"
                    for line in resp.iter_lines():
                        if not line or not line.startswith("data:"):
                            continue
                        data_raw = line[5:].strip()
                        try:
                            data = json.loads(data_raw)
                        except json.JSONDecodeError:
                            lines.append(data_raw)
                            continue
                        if isinstance(data, dict) and "status" in data:
                            lines.append(f"done status={data.get('status')} trace={data.get('traceId')}")
                            lines.append(f"sql: {data.get('sql')}")
                            lines.append(f"explanation: {data.get('explanation')}")
                            chart = data.get("chart") or {}
                            lines.append(f"chart: {chart.get('type')} x={chart.get('x')}")
                        elif isinstance(data, dict) and "delta" in data:
                            lines.append(data["delta"])
                        elif isinstance(data, dict) and "stage" in data:
                            lines.append(f"[stage {data.get('stage')}]")
                        elif isinstance(data, dict) and "code" in data:
                            lines.append(f"error {data.get('code')}: {data.get('message')}")
                        else:
                            lines.append(str(data)[:200])
                return "\n".join(lines) if lines else "(无 SSE 事件)"
            resp = client.post(url, json=payload, headers=_headers())
            if resp.status_code != 200:
                return f"HTTP {resp.status_code}: {resp.text[:500]}"
            body = resp.json()
            if body.get("code") != 200:
                return f"错误 {body.get('code')}: {body.get('msg')}"
            data = body.get("data") or {}
            lines = [
                f"status: {data.get('status')}",
                f"traceId: {data.get('traceId')}",
                f"sql: {data.get('sql')}",
                f"rowCount: {data.get('rowCount')}",
                f"explanation: {data.get('explanation')}",
            ]
            chart = data.get("chart") or {}
            if chart:
                lines.append(f"chart: type={chart.get('type')} x={chart.get('x')} y={chart.get('y')}")
            rows = data.get("rows") or []
            if rows:
                lines.append("rows:")
                lines.append(json.dumps(rows[:20], ensure_ascii=False, indent=2))
            if data.get("rejectCode"):
                lines.append(f"reject: {data.get('rejectCode')} - {data.get('rejectReason')}")
            return "\n".join(lines)
    except Exception as e:
        return f"请求失败: {e}"


demo = gr.Interface(
    fn=ask_chat,
    inputs=[
        gr.Textbox(label="自然语言问数", placeholder="例如：秒杀订单有多少？"),
        gr.Checkbox(label="SSE 流式 (stream=true)", value=False),
    ],
    outputs=gr.Textbox(label="ChatBI 结果", lines=22),
    title="Moli ChatBI W7 冒烟",
    examples=[
        ["秒杀订单有多少？", False],
        ["每个活动的订单数量", False],
        ["请删除所有订单", False],
    ],
)

if __name__ == "__main__":
    demo.launch(server_name="127.0.0.1", server_port=int(os.environ.get("GRADIO_PORT", "7860")))
