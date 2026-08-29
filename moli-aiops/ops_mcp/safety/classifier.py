"""命令危险分级。

分级结果决定人机协同的边界：
  READ_ONLY   取证阶段自动执行，不打扰人
  MUTATING    可逆变更，必须拿到人工审批令牌才能执行
  DESTRUCTIVE 不可逆或影响面极大，默认直接拒绝，审批也不放行

三条设计原则：

1. **失败关闭**：识别不出的命令一律降级为 MUTATING，绝不因为「没匹配到危险规则」
   就当成只读。允许清单是白名单语义，不是黑名单的补集。
2. **逐段判定**：按 shell 操作符拆段（引号感知），每段单独定级，整体取最高。
   否则 `ps aux; rm -rf /` 会因为开头是 ps 被误判为只读。命令替换 `$(...)`
   与反引号里的内容一并展开定级。
3. **整条优先**：跨段才成立的攻击（`curl x | bash`）必须在拆段前判掉，
   因为拆完之后两段单独看都不致命。
"""

from __future__ import annotations

import re
from collections.abc import Callable
from dataclasses import dataclass

from ..schemas import CommandAssessment, RiskLevel

MAX_COMMAND_CHARS = 4096

# 只读命令白名单。value 为 None 表示整条只读；否则为允许的子命令集合
_READ_ONLY: dict[str, frozenset[str] | None] = {
    # 文件与文本
    "cat": None, "head": None, "tail": None, "grep": None, "egrep": None,
    "fgrep": None, "zgrep": None, "zcat": None, "wc": None, "sort": None,
    "uniq": None, "cut": None, "tr": None, "strings": None, "file": None,
    "stat": None, "ls": None, "readlink": None, "basename": None,
    "dirname": None, "md5sum": None, "sha256sum": None, "diff": None,
    # 主机状态
    "uptime": None, "free": None, "df": None, "du": None, "vmstat": None,
    "iostat": None, "mpstat": None, "sar": None, "nproc": None, "lscpu": None,
    "lsblk": None, "lsof": None, "dmesg": None, "uname": None, "hostname": None,
    "whoami": None, "id": None, "date": None, "w": None, "last": None,
    "env": None, "printenv": None, "getconf": None, "echo": None, "true": None,
    "sleep": None,
    # 进程
    "ps": None, "pgrep": None, "pidof": None, "pstree": None, "top": None,
    # 网络
    "ss": None, "netstat": None, "ping": None, "traceroute": None,
    "dig": None, "nslookup": None, "host": None, "arp": None,
    # JVM 诊断
    "jps": None, "jstat": None, "jstack": None, "jinfo": None, "jcmd": None,
    # 带子命令的
    "systemctl": frozenset({"status", "is-active", "is-enabled", "is-failed",
                            "show", "list-units", "list-unit-files", "cat"}),
    "journalctl": None,
    "docker": frozenset({"ps", "logs", "inspect", "stats", "images", "top",
                         "port", "version", "info", "diff"}),
    "kubectl": frozenset({"get", "describe", "logs", "top", "explain", "version"}),
    "redis-cli": frozenset({"info", "ping", "dbsize", "slowlog"}),
    "git": frozenset({"status", "log", "show", "diff", "branch", "remote"}),
    "ip": frozenset({"addr", "a", "link", "route", "r", "neigh"}),
}

# 命令名即致命，不看参数
_DESTRUCTIVE_HEADS = frozenset({
    "shutdown", "reboot", "halt", "poweroff",
    "fdisk", "parted", "sgdisk", "wipefs",
    "userdel", "groupdel", "deluser",
    "killall5",
})

# 需要看参数才能定级的命令
_CONDITIONAL = frozenset({"sed", "awk", "find", "curl", "wget", "mysql", "psql"})

# 保护目录：递归改权限或删除命中即致命
_PROTECTED_ROOT = re.compile(r"^/(etc|usr|var|bin|sbin|boot|lib|lib64|opt|root|home)?/?$|"
                             r"^/(etc|usr|bin|sbin|boot|lib|lib64)(/|$)")


@dataclass(frozen=True)
class RegexRule:
    name: str
    pattern: re.Pattern[str]
    reason: str


@dataclass(frozen=True)
class PredicateRule:
    name: str
    predicate: Callable[[str], bool]
    reason: str


def _tokens_after_head(segment: str) -> tuple[str, list[str]]:
    """返回 (命令名, 其后全部 token)，跳过 sudo / nohup 等前缀。"""
    tokens = segment.split()
    idx = 0
    while idx < len(tokens) and tokens[idx] in {"sudo", "nohup", "time", "nice", "env", "exec"}:
        idx += 1
    if idx >= len(tokens):
        return "", []
    return tokens[idx].rsplit("/", 1)[-1], tokens[idx + 1:]


def _has_flag(tokens: list[str], short: str, *long_names: str) -> bool:
    for t in tokens:
        if t.startswith("--"):
            if t.lstrip("-").split("=")[0] in long_names:
                return True
        elif t.startswith("-") and len(t) > 1:
            if any(c in short for c in t[1:]):
                return True
    return False


def _rm_destructive(segment: str) -> bool:
    """rm 带递归或强制标志才算致命。按 token 判定，避免文件名首字母误伤。"""
    head, rest = _tokens_after_head(segment)
    if head != "rm":
        return False
    return _has_flag(rest, "rRf", "recursive", "force")


def _chmod_chown_destructive(segment: str) -> bool:
    """对系统目录递归改权限/属主。"""
    head, rest = _tokens_after_head(segment)
    if head not in {"chmod", "chown", "chgrp"}:
        return False
    if not _has_flag(rest, "R", "recursive"):
        return False
    operands = [t for t in rest if not t.startswith("-")]
    # 第一个操作数是权限位或属主，其余才是路径
    return any(_PROTECTED_ROOT.match(p) for p in operands[1:])


def _init_runlevel_destructive(segment: str) -> bool:
    head, rest = _tokens_after_head(segment)
    return head == "init" and bool(rest) and rest[0] in {"0", "6"}


def _mkfs_destructive(segment: str) -> bool:
    head, _ = _tokens_after_head(segment)
    return head == "mkfs" or head.startswith("mkfs.")


def _kill_all_destructive(segment: str) -> bool:
    head, rest = _tokens_after_head(segment)
    if head not in {"kill", "pkill"}:
        return False
    return "-1" in rest and _has_flag(rest, "9")


# 必须在拆段前对整条命令判定的规则
_WHOLE_RULES: tuple[RegexRule, ...] = (
    RegexRule("remote_pipe_shell",
              re.compile(r"\b(curl|wget)\b[^\n]*\|\s*(sudo\s+)?(ba|z|k)?sh\b", re.I),
              "下载并直接执行远程脚本"),
    RegexRule("fork_bomb",
              re.compile(r":\s*\(\s*\)\s*\{.*\|.*&.*\}\s*;?\s*:", re.S),
              "fork 炸弹"),
)

_DESTRUCTIVE_PREDICATES: tuple[PredicateRule, ...] = (
    PredicateRule("rm_recursive_or_force", _rm_destructive, "递归或强制删除，不可逆"),
    PredicateRule("chmod_chown_recursive_system", _chmod_chown_destructive, "对系统目录递归改权限"),
    PredicateRule("init_runlevel", _init_runlevel_destructive, "切换运行级别关机/重启"),
    PredicateRule("mkfs", _mkfs_destructive, "格式化文件系统"),
    PredicateRule("kill_all", _kill_all_destructive, "杀死所有进程"),
)

_DESTRUCTIVE_REGEX: tuple[RegexRule, ...] = (
    RegexRule("dd_write", re.compile(r"\bdd\b[^\n]*\bof=", re.I), "dd 直写设备或文件"),
    # 排除 2>&1 这类描述符重定向，以及 /dev/null 这类标准丢弃口
    RegexRule("write_system_dir",
              re.compile(r"(?<![0-9<>&])>{1,2}\s*/(etc|boot|sys|proc|bin|sbin|lib|lib64)/"
                         r"|(?<![0-9<>&])>{1,2}\s*/dev/(?!null\b|stdout\b|stderr\b|tty\b)", re.I),
              "向系统目录或块设备写入"),
    RegexRule("firewall_flush",
              re.compile(r"\biptables\b[^\n]*\s-F\b|\bufw\s+disable\b", re.I),
              "清空防火墙规则"),
    RegexRule("crontab_remove", re.compile(r"\bcrontab\b[^\n]*\s-r\b", re.I), "清空定时任务"),
    RegexRule("sql_drop",
              re.compile(r"\b(drop\s+(database|table|schema)|truncate\s+table)\b", re.I),
              "删库删表"),
    RegexRule("docker_prune_all",
              re.compile(r"\bdocker\b[^\n]*\b(system|volume)\s+prune\b[^\n]*\s-\w*[af]", re.I),
              "清理全部 Docker 资源"),
    RegexRule("find_delete",
              re.compile(r"\bfind\b[^\n]*\s(-delete|-exec\s+rm)\b", re.I),
              "find 批量删除"),
    RegexRule("history_wipe",
              re.compile(r"\bhistory\s+-c\b|>\s*~?/?\.bash_history", re.I),
              "清除操作历史"),
)

# 命中即至少 MUTATING
_MUTATING_MARKERS: tuple[RegexRule, ...] = (
    # 排除 2>&1 这类描述符重定向
    RegexRule("redirect_write", re.compile(r"(?<![0-9<>&])>{1,2}(?![&>])"), "输出重定向写文件"),
    RegexRule("tee", re.compile(r"\btee\b"), "tee 写文件"),
    RegexRule("package_mgr",
              re.compile(r"\b(apt|apt-get|yum|dnf|apk|pip|pip3|npm)\b\s+(install|remove|upgrade|update)", re.I),
              "包管理器变更"),
)

# 顺序敏感：长操作符必须排在其前缀之前。裸 & 不拆，避免打断 2>&1
_SHELL_OPERATORS = (";", "&&", "||", "|", "\n")


def _split_segments(command: str) -> list[str]:
    """引号感知地按 shell 操作符拆段，并展开命令替换的内容。"""
    segments: list[str] = []
    buf: list[str] = []
    quote: str | None = None
    i = 0
    n = len(command)
    while i < n:
        ch = command[i]
        if quote:
            buf.append(ch)
            if ch == quote and command[i - 1] != "\\":
                quote = None
            i += 1
            continue
        if ch in "'\"":
            quote = ch
            buf.append(ch)
            i += 1
            continue
        matched = next((op for op in _SHELL_OPERATORS if command.startswith(op, i)), None)
        if matched:
            segments.append("".join(buf))
            buf = []
            i += len(matched)
            continue
        buf.append(ch)
        i += 1
    segments.append("".join(buf))

    # $(rm -rf /) 不能因为外层是 echo 就放行
    expanded: list[str] = []
    for seg in segments:
        expanded.append(seg)
        for inner in re.findall(r"\$\(([^()]*)\)", seg):
            expanded.extend(_split_segments(inner) if inner.strip() else [])
        for inner in re.findall(r"`([^`]*)`", seg):
            expanded.extend(_split_segments(inner) if inner.strip() else [])
    return [s.strip() for s in expanded if s.strip()]


def _conditional_risk(head: str, segment: str, rest: list[str]) -> RiskLevel:
    if head == "sed":
        return RiskLevel.MUTATING if _has_flag(rest, "i", "in-place") else RiskLevel.READ_ONLY
    if head == "awk":
        return RiskLevel.READ_ONLY
    if head == "find":
        return RiskLevel.MUTATING if re.search(r"\s(-exec|-execdir|-ok)\b", segment) else RiskLevel.READ_ONLY
    if head == "wget":
        return RiskLevel.MUTATING
    if head == "curl":
        if _has_flag(rest, "oO", "output", "remote-name"):
            return RiskLevel.MUTATING
        if re.search(r"\s(-X|--request)\s+(POST|PUT|DELETE|PATCH)\b", segment, re.I):
            return RiskLevel.MUTATING
        return RiskLevel.READ_ONLY
    if head in {"mysql", "psql"}:
        if re.search(r"\b(insert|update|delete|drop|alter|create|truncate|grant|revoke)\b", segment, re.I):
            return RiskLevel.MUTATING
        if re.search(r"\b(select|show|describe|desc|explain)\b", segment, re.I):
            return RiskLevel.READ_ONLY
        return RiskLevel.MUTATING
    return RiskLevel.MUTATING


def _classify_segment(segment: str) -> tuple[RiskLevel, str, str]:
    head, rest = _tokens_after_head(segment)

    if head in _DESTRUCTIVE_HEADS:
        return RiskLevel.DESTRUCTIVE, f"{head} 属高危操作", f"head:{head}"
    for prule in _DESTRUCTIVE_PREDICATES:
        if prule.predicate(segment):
            return RiskLevel.DESTRUCTIVE, prule.reason, prule.name
    for rrule in _DESTRUCTIVE_REGEX:
        if rrule.pattern.search(segment):
            return RiskLevel.DESTRUCTIVE, rrule.reason, rrule.name

    marker = next((r for r in _MUTATING_MARKERS if r.pattern.search(segment)), None)

    if not head:
        return RiskLevel.MUTATING, "无法解析命令名", "unparsed"

    if head in _CONDITIONAL:
        risk = _conditional_risk(head, segment, rest)
        if risk is RiskLevel.READ_ONLY and marker:
            return RiskLevel.MUTATING, marker.reason, marker.name
        reason = "只读取证命令" if risk is RiskLevel.READ_ONLY else f"{head} 带写入参数"
        return risk, reason, f"conditional:{head}"

    if head in _READ_ONLY:
        allowed_subs = _READ_ONLY[head]
        if allowed_subs is not None:
            sub = next((t for t in rest if not t.startswith("-")), "")
            if sub not in allowed_subs:
                return RiskLevel.MUTATING, f"{head} {sub} 不在只读子命令清单内", f"subcommand:{head}"
        if marker:
            return RiskLevel.MUTATING, marker.reason, marker.name
        return RiskLevel.READ_ONLY, "只读取证命令", f"allowlist:{head}"

    if marker:
        return RiskLevel.MUTATING, marker.reason, marker.name
    return RiskLevel.MUTATING, f"{head} 不在只读白名单内，按变更处理", "fail_closed"


_ORDER = {RiskLevel.READ_ONLY: 0, RiskLevel.MUTATING: 1, RiskLevel.DESTRUCTIVE: 2}


def assess(command: str, *, allow_destructive: bool = False) -> CommandAssessment:
    text = (command or "").strip()
    if not text:
        return CommandAssessment(
            command=text, risk=RiskLevel.DESTRUCTIVE, reason="命令为空",
            matched_rule="empty", requires_approval=True, blocked=True,
        )
    if len(text) > MAX_COMMAND_CHARS:
        return CommandAssessment(
            command=text[:200] + "...", risk=RiskLevel.DESTRUCTIVE,
            reason=f"命令超过 {MAX_COMMAND_CHARS} 字符上限",
            matched_rule="too_long", requires_approval=True, blocked=True,
        )

    for rule in _WHOLE_RULES:
        if rule.pattern.search(text):
            return CommandAssessment(
                command=text, risk=RiskLevel.DESTRUCTIVE, reason=rule.reason,
                matched_rule=rule.name, requires_approval=True,
                blocked=not allow_destructive,
            )

    worst = RiskLevel.READ_ONLY
    worst_reason = "只读取证命令"
    worst_rule = "allowlist"
    for segment in _split_segments(text):
        risk, reason, rule_name = _classify_segment(segment)
        if _ORDER[risk] > _ORDER[worst]:
            worst, worst_reason, worst_rule = risk, reason, rule_name

    return CommandAssessment(
        command=text,
        risk=worst,
        reason=worst_reason,
        matched_rule=worst_rule,
        requires_approval=worst is not RiskLevel.READ_ONLY,
        blocked=worst is RiskLevel.DESTRUCTIVE and not allow_destructive,
    )
