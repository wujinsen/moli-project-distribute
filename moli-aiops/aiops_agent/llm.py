"""多厂商模型路由。

三层降级，每层都有明确的触发条件：

  单次调用超时/报错  → 同厂商指数退避重试
  该厂商重试耗尽      → 切下一个厂商（换 base_url、换模型）
  全部厂商都不可用    → 抛 LlmUnavailable，由节点走规则兜底

最后一层很关键：没有任何 API key 时整条诊断链路依然能跑完，只是结论质量下降。
这样演示不依赖外部服务，也让「兜底」这件事是可验证的，而不是写在文档里的承诺。

按节点职责分档而不是全局一个模型：根因推理和证伪需要强模型，
分诊和摘要用便宜的就够，成本差异在高频巡检场景下会很明显。
"""

from __future__ import annotations

import json
import logging
import os
import re
import time
from dataclasses import dataclass, field

import httpx

log = logging.getLogger("aiops.llm")

# 节点 → 能力档位
TIER_BY_NODE: dict[str, str] = {
    "triage": "fast",
    "investigator": "fast",
    "diagnostician": "reasoning",
    "critic": "reasoning",
    "planner": "reasoning",
    "reporter": "fast",
}

DEFAULT_TIER = "fast"

_TIER_TIMEOUT_S = {
    "reasoning": float(os.environ.get("AIOPS_LLM_REASONING_TIMEOUT_S", "90")),
    "fast": float(os.environ.get("AIOPS_LLM_FAST_TIMEOUT_S", "45")),
}

_MAX_ATTEMPTS_PER_PROVIDER = int(os.environ.get("AIOPS_LLM_ATTEMPTS", "2"))
_BACKOFF_BASE_S = float(os.environ.get("AIOPS_LLM_BACKOFF_S", "1.0"))


class LlmUnavailable(RuntimeError):
    """全部厂商都不可用。节点据此切换到规则兜底，而不是让整次诊断失败。"""


@dataclass
class ProviderConfig:
    name: str
    base_url: str
    api_key: str
    models: dict[str, str] = field(default_factory=dict)

    def model_for(self, tier: str) -> str:
        return self.models.get(tier) or self.models.get(DEFAULT_TIER) or ""


@dataclass
class LlmResult:
    text: str
    provider: str
    model: str
    prompt_tokens: int = 0
    completion_tokens: int = 0
    latency_ms: int = 0
    attempts: int = 1
    fallback_used: bool = False


def load_providers() -> list[ProviderConfig]:
    """从环境变量按约定加载厂商链，顺序即优先级。

    AIOPS_PROVIDERS=deepseek,openai
    AIOPS_DEEPSEEK_BASE_URL / _API_KEY / _MODEL_REASONING / _MODEL_FAST
    """
    names = [n.strip() for n in os.environ.get("AIOPS_PROVIDERS", "").split(",") if n.strip()]
    providers: list[ProviderConfig] = []
    for name in names:
        prefix = f"AIOPS_{name.upper().replace('-', '_')}"
        api_key = os.environ.get(f"{prefix}_API_KEY", "")
        base_url = os.environ.get(f"{prefix}_BASE_URL", "").rstrip("/")
        if not api_key or not base_url:
            log.warning("厂商 %s 缺少 BASE_URL 或 API_KEY，已跳过", name)
            continue
        providers.append(
            ProviderConfig(
                name=name,
                base_url=base_url,
                api_key=api_key,
                models={
                    "reasoning": os.environ.get(f"{prefix}_MODEL_REASONING", ""),
                    "fast": os.environ.get(f"{prefix}_MODEL_FAST", ""),
                },
            )
        )

    # 兼容 deep_research 已有的 OPENAI_* 约定，省得同一台机器配两套
    if not providers and os.environ.get("OPENAI_API_KEY"):
        model = os.environ.get("OPENAI_MODEL", "gpt-4o-mini")
        providers.append(
            ProviderConfig(
                name="openai",
                base_url=os.environ.get("OPENAI_BASE_URL", "https://api.openai.com/v1").rstrip("/"),
                api_key=os.environ["OPENAI_API_KEY"],
                models={"reasoning": model, "fast": model},
            )
        )
    return providers


_JSON_FENCE = re.compile(r"```(?:json)?\s*(.*?)```", re.S)


def extract_json(text: str) -> dict:
    """LLM 经常把 JSON 包在 markdown 围栏里或前后带解释，这里逐级放宽地抠出来。"""
    candidate = text.strip()
    fenced = _JSON_FENCE.search(candidate)
    if fenced:
        candidate = fenced.group(1).strip()

    try:
        parsed = json.loads(candidate)
        return parsed if isinstance(parsed, dict) else {"value": parsed}
    except json.JSONDecodeError:
        pass

    start = candidate.find("{")
    end = candidate.rfind("}")
    if start != -1 and end > start:
        try:
            parsed = json.loads(candidate[start : end + 1])
            return parsed if isinstance(parsed, dict) else {"value": parsed}
        except json.JSONDecodeError:
            pass
    raise ValueError(f"无法从模型输出中解析 JSON：{text[:300]}")


class LlmRouter:
    def __init__(self, providers: list[ProviderConfig] | None = None) -> None:
        self.providers = providers if providers is not None else load_providers()

    @property
    def configured(self) -> bool:
        return bool(self.providers)

    def tier_for(self, node: str) -> str:
        return TIER_BY_NODE.get(node, DEFAULT_TIER)

    def chat(
        self,
        *,
        node: str,
        system: str,
        user: str,
        temperature: float = 0.2,
        json_mode: bool = False,
    ) -> LlmResult:
        if not self.providers:
            raise LlmUnavailable("未配置任何模型厂商")

        tier = self.tier_for(node)
        timeout = _TIER_TIMEOUT_S.get(tier, 45.0)
        total_attempts = 0
        failures: list[str] = []

        for index, provider in enumerate(self.providers):
            model = provider.model_for(tier)
            if not model:
                failures.append(f"{provider.name}: {tier} 档位未配置模型")
                continue

            for attempt in range(1, _MAX_ATTEMPTS_PER_PROVIDER + 1):
                total_attempts += 1
                started = time.monotonic()
                try:
                    result = self._call(
                        provider, model, system, user, temperature, timeout, json_mode
                    )
                except Exception as exc:  # noqa: BLE001
                    failures.append(f"{provider.name}/{model} 第{attempt}次: {exc}")
                    log.warning("LLM 调用失败 provider=%s model=%s attempt=%d: %s",
                                provider.name, model, attempt, exc)
                    if attempt < _MAX_ATTEMPTS_PER_PROVIDER:
                        time.sleep(_BACKOFF_BASE_S * (2 ** (attempt - 1)))
                    continue

                result.latency_ms = int((time.monotonic() - started) * 1000)
                result.attempts = total_attempts
                result.fallback_used = index > 0
                if index > 0:
                    log.info("已切换到兜底厂商 %s（前 %d 家不可用）", provider.name, index)
                return result

        raise LlmUnavailable("全部厂商均不可用：" + "; ".join(failures[-4:]))

    def _call(
        self,
        provider: ProviderConfig,
        model: str,
        system: str,
        user: str,
        temperature: float,
        timeout: float,
        json_mode: bool,
    ) -> LlmResult:
        payload: dict = {
            "model": model,
            "temperature": temperature,
            "messages": [
                {"role": "system", "content": system},
                {"role": "user", "content": user},
            ],
        }
        if json_mode:
            payload["response_format"] = {"type": "json_object"}

        with httpx.Client(timeout=timeout) as client:
            response = client.post(
                f"{provider.base_url}/chat/completions",
                json=payload,
                headers={
                    "Authorization": f"Bearer {provider.api_key}",
                    "Content-Type": "application/json",
                },
            )
            response.raise_for_status()
            body = response.json()

        choices = body.get("choices") or []
        if not choices:
            raise RuntimeError("模型返回空 choices")
        content = str((choices[0].get("message") or {}).get("content") or "")
        if not content.strip():
            raise RuntimeError("模型返回空内容")

        usage = body.get("usage") or {}
        return LlmResult(
            text=content,
            provider=provider.name,
            model=model,
            prompt_tokens=int(usage.get("prompt_tokens") or 0),
            completion_tokens=int(usage.get("completion_tokens") or 0),
        )

    def chat_json(
        self,
        *,
        node: str,
        system: str,
        user: str,
        temperature: float = 0.1,
    ) -> tuple[dict, LlmResult]:
        """要求结构化输出。解析失败时追加一次「只输出 JSON」的纠正重试。"""
        result = self.chat(node=node, system=system, user=user,
                           temperature=temperature, json_mode=True)
        try:
            return extract_json(result.text), result
        except ValueError:
            log.warning("节点 %s 的 JSON 解析失败，发起一次纠正重试", node)

        retry = self.chat(
            node=node,
            system=system + "\n\n严格只输出一个 JSON 对象，不要任何解释文字或 markdown 围栏。",
            user=user,
            temperature=0.0,
            json_mode=True,
        )
        retry.attempts += result.attempts
        return extract_json(retry.text), retry
