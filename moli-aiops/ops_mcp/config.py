"""工具层配置。沿用 deep_research/config.py 的「模块级常量读环境变量」约定。"""

from __future__ import annotations

import os
from pathlib import Path

_HERE = Path(__file__).resolve().parent.parent


def _flag(name: str, default: bool = False) -> bool:
    raw = os.environ.get(name)
    if raw is None:
        return default
    return raw.strip().lower() in {"1", "true", "yes", "on"}


# --- CMDB 数据源 ---------------------------------------------------------
# auto: 先试 REST，不可用时回退 inventory 文件；rest / file 可强制指定
CMDB_MODE = os.environ.get("OPS_CMDB_MODE", "auto").strip().lower()
USER_CENTER_BASE_URL = os.environ.get(
    "OPS_USER_CENTER_BASE_URL", "http://127.0.0.1:21000/UserCenterServer"
).rstrip("/")
# user-center 登录后的 Shiro sessionId，同 moli-knowledge/mcp 的 MCP_KB_TOKEN 约定
OPS_AUTH_TOKEN = os.environ.get("OPS_AUTH_TOKEN", "")
INVENTORY_PATH = Path(os.environ.get("OPS_INVENTORY", str(_HERE / "inventory.yaml")))
CMDB_TIMEOUT_S = float(os.environ.get("OPS_CMDB_TIMEOUT_S", "10"))

# --- SSH 取证 / 处置 -----------------------------------------------------
SSH_CONNECT_TIMEOUT_S = float(os.environ.get("OPS_SSH_CONNECT_TIMEOUT_S", "8"))
SSH_COMMAND_TIMEOUT_S = float(os.environ.get("OPS_SSH_COMMAND_TIMEOUT_S", "30"))
# 单条命令回传上限，防止 tail 一个巨型日志把内存和 LLM 上下文打爆
SSH_MAX_OUTPUT_BYTES = int(os.environ.get("OPS_SSH_MAX_OUTPUT_BYTES", str(256 * 1024)))

# --- 安全 ---------------------------------------------------------------
# 处置执行总开关。对应 user-center 的 ops.command.enabled，默认关闭，事故时可立即关停
EXEC_ENABLED = _flag("OPS_EXEC_ENABLED", False)
# 即便拿到审批令牌，DESTRUCTIVE 级命令是否放行。默认永不放行
ALLOW_DESTRUCTIVE = _flag("OPS_ALLOW_DESTRUCTIVE", False)
# 审批令牌签名密钥。未配置时用进程内随机值，重启即失效
APPROVAL_SECRET = os.environ.get("OPS_APPROVAL_SECRET", "")
APPROVAL_TTL_S = int(os.environ.get("OPS_APPROVAL_TTL_S", "600"))

# --- 知识库检索（复用 moli-knowledge）------------------------------------
KB_BASE_URL = os.environ.get("KB_BASE_URL", "http://127.0.0.1:8090").rstrip("/")
KB_AUTH_TOKEN = os.environ.get("KB_AUTH_TOKEN", "")
KB_TIMEOUT_S = float(os.environ.get("OPS_KB_TIMEOUT_S", "20"))

# --- 审计 ---------------------------------------------------------------
AUDIT_DB = Path(os.environ.get("OPS_AUDIT_DB", str(_HERE / "data" / "aiops.db")))
