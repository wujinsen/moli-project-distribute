"""工具层输入/输出模型。ops_mcp 与 aiops_agent 共用。

字段命名统一 snake_case；对外 MCP JSON 也用 snake_case，避免编排层再做一层映射。
"""

from __future__ import annotations

from enum import StrEnum
from typing import Any

from pydantic import BaseModel, Field


# --- CMDB ---------------------------------------------------------------


class ServerRef(BaseModel):
    id: int | str
    name: str = ""
    ip: str = ""
    inner_ip: str = ""
    ssh_port: int = 22
    role: str = ""
    tags: list[str] = Field(default_factory=list)
    # 0 未知 / 1 可达 / 2 不可达 / 3 跳过，与 operation_server_info.status 对齐
    status: int = 0
    last_check_time: str | None = None


class ProjectRef(BaseModel):
    id: int | str
    name: str = ""
    server_id: int | str | None = None
    server_ip: str = ""
    deploy_path: str = ""
    port: int | None = None
    deploy_running: bool | None = None
    service_key: str = ""


class ComponentRef(BaseModel):
    id: int | str
    name: str = ""
    server_id: int | str | None = None
    server_ip: str = ""
    port: int | None = None
    version: str = ""
    status: int = 0


class TopologyEdge(BaseModel):
    source: str  # "server:1" / "project:3" / "component:2"
    target: str
    kind: str  # deploys | depends_on


class TopologyGraph(BaseModel):
    servers: list[ServerRef] = Field(default_factory=list)
    projects: list[ProjectRef] = Field(default_factory=list)
    components: list[ComponentRef] = Field(default_factory=list)
    edges: list[TopologyEdge] = Field(default_factory=list)
    source: str = ""  # rest | file，让 Agent 知道证据来自哪


# --- 主机指标（SSH facts）------------------------------------------------


class CpuFacts(BaseModel):
    cores: int | None = None
    usage_pct: float | None = None
    load1: float | None = None
    load5: float | None = None
    load15: float | None = None
    iowait_pct: float | None = None


class MemoryFacts(BaseModel):
    total_mb: float | None = None
    used_mb: float | None = None
    available_mb: float | None = None
    usage_pct: float | None = None
    swap_total_mb: float | None = None
    swap_used_mb: float | None = None


class DiskFacts(BaseModel):
    mount: str
    filesystem: str = ""
    total_gb: float | None = None
    used_gb: float | None = None
    avail_gb: float | None = None
    usage_pct: float | None = None
    inode_usage_pct: float | None = None


class ProcessFacts(BaseModel):
    pid: int
    user: str = ""
    cpu_pct: float | None = None
    mem_pct: float | None = None
    rss_mb: float | None = None
    command: str = ""


class PortFacts(BaseModel):
    proto: str = ""
    local_addr: str = ""
    port: int | None = None
    pid: int | None = None
    process: str = ""


class HostFacts(BaseModel):
    server_id: int | str
    host: str
    collected_at: str
    uptime_s: float | None = None
    cpu: CpuFacts = Field(default_factory=CpuFacts)
    memory: MemoryFacts = Field(default_factory=MemoryFacts)
    disks: list[DiskFacts] = Field(default_factory=list)
    top_processes: list[ProcessFacts] = Field(default_factory=list)
    listen_ports: list[PortFacts] = Field(default_factory=list)
    # 部分采集项失败不影响整体返回，诊断可以用残缺证据继续推理
    partial_errors: list[str] = Field(default_factory=list)


# --- 服务存活 -------------------------------------------------------------


class ServiceVerdict(StrEnum):
    UP = "up"
    DOWN = "down"
    # 信号缺失（没声明端口、没有 systemd、SSH 探测失败）与信号为负是两件事，
    # 不能把「判不了」混进 down，否则会造出大量假告警
    UNKNOWN = "unknown"


class ServiceHealth(BaseModel):
    """把 inventory 里「这个服务应该长什么样」和机器上「它现在什么样」对上。

    主机指标只能回答「机器累不累」，回答不了「服务在不在」。服务挂了这类事故里，
    「声明端口 8080 无监听 + 进程不存在」才是决定性证据，缺了它诊断只能给出
    「证据不足」——这正是没有这层检查时的实际表现。
    """

    service: str
    target: str = ""
    expected_port: int | None = None
    port_listening: bool | None = None
    listening_pid: int | None = None
    unit: str = ""
    unit_state: str = ""  # active | inactive | failed | unknown | ""（未声明）
    process_matches: int | None = None
    verdict: ServiceVerdict = ServiceVerdict.UNKNOWN
    reason: str = ""


# --- 日志 ---------------------------------------------------------------


class LogHit(BaseModel):
    path: str
    line_no: int | None = None
    ts: str | None = None
    level: str | None = None
    text: str


class LogSearchResult(BaseModel):
    host: str
    hits: list[LogHit] = Field(default_factory=list)
    truncated: bool = False
    scanned_paths: list[str] = Field(default_factory=list)
    partial_errors: list[str] = Field(default_factory=list)


# --- 变更 ---------------------------------------------------------------


class ChangeRecord(BaseModel):
    task_id: int | str
    task_type: str  # deploy | upload | command | health_probe
    action: str = ""
    server_id: int | str | None = None
    project_id: int | str | None = None
    status: str = ""
    operator: str = ""
    create_time: str | None = None
    finish_time: str | None = None
    message: str = ""


# --- 安全与处置 -----------------------------------------------------------


class RiskLevel(StrEnum):
    """用 StrEnum 而不是 (str, Enum)：后者在 3.11+ 下 str() 返回 'RiskLevel.MUTATING'，
    跨工具边界序列化时会悄悄写出错误的风险标签。"""


    # 只读取证，不改变系统状态，可自动执行
    READ_ONLY = "read_only"
    # 改变系统状态但可逆（重启服务、清缓存），需人工审批
    MUTATING = "mutating"
    # 不可逆或影响面极大（rm -rf、mkfs、reboot），默认直接拒绝
    DESTRUCTIVE = "destructive"


class CommandAssessment(BaseModel):
    command: str
    risk: RiskLevel
    reason: str
    matched_rule: str = ""
    requires_approval: bool = True
    blocked: bool = False


class ExecResult(BaseModel):
    host: str
    command: str
    risk: RiskLevel
    exit_code: int | None = None
    stdout: str = ""
    stderr: str = ""
    duration_ms: int = 0
    truncated: bool = False
    dry_run: bool = False
    audit_id: str = ""


# --- 知识库 -------------------------------------------------------------


class KbHit(BaseModel):
    slug: str = ""
    title: str = ""
    snippet: str = ""
    space_id: int | None = None


class KbAnswer(BaseModel):
    answer: str = ""
    mode: str = ""
    citations: list[KbHit] = Field(default_factory=list)
    provider: str = ""
    model: str = ""


def model_dump_compact(model: BaseModel) -> dict[str, Any]:
    """去掉 None 字段再交给 LLM，省 token 且避免模型被空值干扰。"""
    return model.model_dump(exclude_none=True)
