"""故障注入。

演练脚本**刻意绕过安全层**直连 SSH：它是混沌工具，代表运维人员主动搞破坏，
不是 Agent 的动作。让它走审批闸门既没有意义，也会把演练记录混进事故审计里。

注入的是真故障——真的把 CPU 打满、真的把 tmpfs 写满、真的停掉进程——
所以诊断链路拿到的是真实指标，不是伪造的数据。

    python -m drills.scenarios list
    python -m drills.scenarios inject oom
    python -m drills.scenarios heal oom
    python -m drills.scenarios status
"""

from __future__ import annotations

import argparse
import sys
from dataclasses import dataclass
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from ops_mcp import config as ops_config  # noqa: E402
from ops_mcp.cmdb.base import Inventory  # noqa: E402
from ops_mcp.evidence.ssh import SshPool  # noqa: E402


@dataclass(frozen=True)
class Scenario:
    name: str
    target: str
    description: str
    symptom: str
    inject: str
    heal: str


SCENARIOS: tuple[Scenario, ...] = (
    Scenario(
        name="service_down",
        target="sandbox-app",
        description="网关进程被停掉",
        symptom="端口无监听，健康检查失败，上游 502",
        inject="systemctl stop moli-gateway",
        heal="systemctl start moli-gateway",
    ),
    Scenario(
        name="oom",
        target="sandbox-app",
        description="网关内存泄漏直至 OOM",
        symptom="内存占用持续攀升，日志刷 OutOfMemoryError，健康检查转 DOWN",
        inject="echo oom > /var/run/moli/gateway.fault",
        heal="rm -f /var/run/moli/gateway.fault && systemctl restart moli-gateway",
    ),
    Scenario(
        name="cpu",
        target="sandbox-app",
        description="网关 CPU 打满",
        symptom="CPU 接近 100%，负载升高，请求排队 p99 飙升",
        inject="echo cpu > /var/run/moli/gateway.fault",
        heal="rm -f /var/run/moli/gateway.fault && systemctl restart moli-gateway",
    ),
    Scenario(
        name="disk_full",
        target="sandbox-app",
        description="/data 挂载点被写满",
        symptom="磁盘使用率 100%，日志写入失败 No space left on device",
        # dd 到写满为止；tmpfs 只有 32MB，几秒完成
        inject="echo disk > /var/run/moli/gateway.fault; "
               "dd if=/dev/zero of=/data/filler bs=1M count=64 2>/dev/null; true",
        heal="rm -f /data/filler /var/run/moli/gateway.fault",
    ),
    Scenario(
        name="db_unreachable",
        target="sandbox-app",
        description="下游数据库不可达",
        symptom="日志刷 Communications link failure，上游超时",
        inject="echo db > /var/run/moli/gateway.fault",
        heal="rm -f /var/run/moli/gateway.fault",
    ),
    Scenario(
        name="port_conflict",
        target="sandbox-app",
        description="端口被其他进程占用导致服务起不来",
        symptom="服务反复启动失败，端口被无关进程占用",
        inject="systemctl stop moli-gateway; "
               "nohup python3 -c \"import socket,time;s=socket.socket();"
               "s.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1);"
               "s.bind(('0.0.0.0',8080));s.listen(1);time.sleep(86400)\" "
               ">/var/log/moli/squatter.log 2>&1 & echo started",
        heal="pkill -f 'bind..0.0.0.0.,8080' || true; systemctl start moli-gateway",
    ),
)

BY_NAME = {s.name: s for s in SCENARIOS}


def _run(scenario_target: str, command: str) -> tuple[int | None, str, str]:
    inventory = Inventory.load(ops_config.INVENTORY_PATH)
    entry = inventory.resolve(scenario_target)
    pool = SshPool()
    try:
        result = pool.run(entry.ssh_target(), command, timeout=60)
        return result.exit_code, result.stdout, result.stderr
    finally:
        pool.close_all()


def cmd_list() -> None:
    print(f"{'场景':<16} {'目标':<14} 说明")
    print("-" * 78)
    for scenario in SCENARIOS:
        print(f"{scenario.name:<16} {scenario.target:<14} {scenario.description}")
        print(f"{'':<31} 症状：{scenario.symptom}")


def cmd_apply(name: str, heal: bool) -> int:
    scenario = BY_NAME.get(name)
    if scenario is None:
        print(f"未知场景 {name}，可选：{', '.join(BY_NAME)}", file=sys.stderr)
        return 2

    command = scenario.heal if heal else scenario.inject
    verb = "恢复" if heal else "注入"
    print(f"[{verb}] {scenario.name} → {scenario.target}")
    print(f"  命令：{command}")
    exit_code, stdout, stderr = _run(scenario.target, command)
    if stdout.strip():
        print(f"  输出：{stdout.strip()[:400]}")
    if stderr.strip():
        print(f"  stderr：{stderr.strip()[:400]}")
    print(f"  退出码：{exit_code}")
    if not heal:
        print(f"\n预期症状：{scenario.symptom}")
        print("现在到 http://127.0.0.1:8099 发起一次诊断，看它能不能自己找出来。")
    return 0


def cmd_status() -> int:
    probe = (
        "echo '--- 服务 ---'; systemctl is-active moli-gateway; "
        "echo '--- 故障标记 ---'; cat /var/run/moli/gateway.fault 2>/dev/null || echo '(无)'; "
        "echo '--- 磁盘 ---'; df -PTk /data | tail -1; "
        "echo '--- 内存 ---'; grep -E 'MemTotal|MemAvailable' /proc/meminfo"
    )
    exit_code, stdout, stderr = _run("sandbox-app", probe)
    print(stdout or stderr)
    return 0 if exit_code == 0 else 1


def main() -> int:
    parser = argparse.ArgumentParser(description="moli-aiops 故障演练场")
    sub = parser.add_subparsers(dest="command", required=True)
    sub.add_parser("list", help="列出全部场景")
    inject = sub.add_parser("inject", help="注入故障")
    inject.add_argument("name")
    heal = sub.add_parser("heal", help="恢复故障")
    heal.add_argument("name")
    sub.add_parser("status", help="查看沙箱当前状态")

    args = parser.parse_args()
    if args.command == "list":
        cmd_list()
        return 0
    if args.command == "status":
        return cmd_status()
    return cmd_apply(args.name, heal=args.command == "heal")


if __name__ == "__main__":
    raise SystemExit(main())
