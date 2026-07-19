from __future__ import annotations

import httpx

from .config import OPENAI_API_KEY, OPENAI_BASE_URL, OPENAI_MODEL


def chat(system: str, user: str, *, timeout: float = 90.0) -> str:
    if not OPENAI_API_KEY:
        raise RuntimeError("OPENAI_API_KEY not configured")
    url = f"{OPENAI_BASE_URL}/chat/completions"
    payload = {
        "model": OPENAI_MODEL,
        "temperature": 0.2,
        "messages": [
            {"role": "system", "content": system},
            {"role": "user", "content": user},
        ],
    }
    headers = {"Authorization": f"Bearer {OPENAI_API_KEY}", "Content-Type": "application/json"}
    with httpx.Client(timeout=timeout) as client:
        resp = client.post(url, json=payload, headers=headers)
        resp.raise_for_status()
        body = resp.json()
    choices = body.get("choices") or []
    if not choices:
        raise RuntimeError("empty LLM response")
    return str((choices[0].get("message") or {}).get("content") or "")
