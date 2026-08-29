"""CMDB 层：拓扑元数据可插拔，SSH 凭据恒定来自本地 inventory。"""

from __future__ import annotations

import logging

from .. import config
from .base import CmdbSource, Inventory, InventoryEntry, ServiceSpec
from .file_source import FileCmdbSource
from .rest_source import RestCmdbSource

log = logging.getLogger("ops-mcp.cmdb")


def build_source(inventory: Inventory, mode: str | None = None) -> CmdbSource:
    """按 OPS_CMDB_MODE 选择拓扑源。

    auto 模式先探测 user-center，不可用时回退本地 inventory——演示时 Java 栈
    没起来不至于整个平台跑不动。
    """
    selected = (mode or config.CMDB_MODE).strip().lower()

    if selected == "file":
        return FileCmdbSource(inventory)
    if selected == "rest":
        return RestCmdbSource()

    rest = RestCmdbSource()
    if rest.available():
        log.info("CMDB 使用 user-center REST：%s", rest.base_url)
        return rest
    log.warning("user-center 不可达，CMDB 回退本地 inventory（%d 个目标）", len(inventory))
    return FileCmdbSource(inventory)


__all__ = [
    "CmdbSource",
    "FileCmdbSource",
    "Inventory",
    "InventoryEntry",
    "RestCmdbSource",
    "ServiceSpec",
    "build_source",
]
