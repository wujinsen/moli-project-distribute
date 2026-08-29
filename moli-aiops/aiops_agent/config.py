"""编排层配置。"""

from __future__ import annotations

import os
from pathlib import Path

_ROOT = Path(__file__).resolve().parent.parent


def _flag(name: str, default: bool = False) -> bool:
    raw = os.environ.get(name)
    if raw is None:
        return default
    return raw.strip().lower() in {"1", "true", "yes", "on"}


SERVER_HOST = os.environ.get("AIOPS_HOST", "127.0.0.1")
SERVER_PORT = int(os.environ.get("AIOPS_PORT", "8099"))

# --- 入站鉴权（meiling-ui 整合）-------------------------------------------
# 默认开启；独立演示页可设 AIOPS_AUTH_DISABLED=true
AUTH_ENABLED = not _flag("AIOPS_AUTH_DISABLED", False)
# user-center GET /auth/capabilities；dev 直连 :8888，生产可走网关 UserCenter
AUTH_VALIDATE_URL = os.environ.get(
    "AIOPS_AUTH_VALIDATE_URL", "http://127.0.0.1:8888/auth/capabilities"
)
AUTH_TIMEOUT_S = float(os.environ.get("AIOPS_AUTH_TIMEOUT_S", "8"))
CORS_ORIGINS = [
    o.strip()
    for o in os.environ.get(
        "AIOPS_CORS_ORIGINS",
        "http://localhost:5141,http://127.0.0.1:5141",
    ).split(",")
    if o.strip()
]

# trace 与工具层审计共用一个 sqlite 文件，方便按 incident_id 把两边串起来看
DB_PATH = Path(os.environ.get("OPS_AUDIT_DB", str(_ROOT / "data" / "aiops.db")))
# LangGraph checkpoint：单步重试与回放依赖它
CHECKPOINT_PATH = Path(os.environ.get("AIOPS_CHECKPOINT_DB", str(_ROOT / "data" / "checkpoints.db")))

# 单次诊断的时间预算，超了就出降级结论而不是无限查下去
LATENCY_BUDGET_MS = int(os.environ.get("AIOPS_LATENCY_BUDGET_MS", "180000"))
# Critic 判定证据不足时最多回补几轮
MAX_INVESTIGATE_ROUNDS = int(os.environ.get("AIOPS_MAX_INVESTIGATE_ROUNDS", "2"))
MAX_HYPOTHESES = int(os.environ.get("AIOPS_MAX_HYPOTHESES", "4"))

# 即使人已审批，执行阶段是否仍走干跑。首次接生产时建议先开着
FORCE_DRY_RUN = _flag("AIOPS_FORCE_DRY_RUN", False)
