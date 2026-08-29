"""本地 inventory 拓扑源。

user-center 没起、或在别人机器上演示时的兜底。拓扑由 inventory 里声明的
services 与 depends_on 推导，形态与 REST 源保持一致，编排层无需区分。
"""

from __future__ import annotations

from ..schemas import ChangeRecord, ProjectRef, ServerRef, TopologyEdge, TopologyGraph
from .base import Inventory


class FileCmdbSource:
    name = "file"

    def __init__(self, inventory: Inventory) -> None:
        self.inventory = inventory

    def available(self) -> bool:
        return len(self.inventory) > 0

    def topology(self) -> TopologyGraph:
        servers: list[ServerRef] = []
        projects: list[ProjectRef] = []
        edges: list[TopologyEdge] = []
        # 服务名 → 节点 id，用于把 depends_on 解析成边
        service_node: dict[str, str] = {}

        for entry in self.inventory.entries:
            server_node = f"server:{entry.server_id}"
            servers.append(
                ServerRef(
                    id=entry.server_id,
                    name=entry.name,
                    ip=entry.host,
                    ssh_port=entry.ssh_port,
                    role=entry.role,
                    tags=entry.tags,
                )
            )
            for svc in entry.services:
                node_id = f"project:{entry.server_id}:{svc.name}"
                service_node[svc.name] = node_id
                projects.append(
                    ProjectRef(
                        id=node_id,
                        name=svc.name,
                        server_id=entry.server_id,
                        server_ip=entry.host,
                        port=svc.port,
                        service_key=svc.systemd_unit or svc.container or svc.name,
                    )
                )
                edges.append(TopologyEdge(source=server_node, target=node_id, kind="deploys"))

        for entry in self.inventory.entries:
            for svc in entry.services:
                source = service_node.get(svc.name)
                if source is None:
                    continue
                for dep in svc.depends_on:
                    target = service_node.get(dep)
                    if target:
                        edges.append(TopologyEdge(source=source, target=target, kind="depends_on"))

        return TopologyGraph(servers=servers, projects=projects, edges=edges, source=self.name)

    def recent_changes(self, server_id: str | int | None, limit: int) -> list[ChangeRecord]:
        # 本地 inventory 没有变更流水，operation_task 只在 user-center 里
        return []
