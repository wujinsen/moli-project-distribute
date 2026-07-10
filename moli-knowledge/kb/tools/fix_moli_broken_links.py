#!/usr/bin/env python3
"""Fix wiki-moli broken [[wikilinks]] pointing at enterprise-kb or missing slugs.

Batch: #moli-link-governance-20260710
"""
from __future__ import annotations

import json
import re
from datetime import date
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
WIKI_MOLI = KB / "wiki-moli"
WIKI_ENT = KB / "wiki"
LOG = WIKI_MOLI / "log.md"
TODAY = date.today().isoformat()
BATCH = "#moli-link-governance-20260710"

# [[old]] -> [[new-stem]] (target exists in wiki-moli)
MOLI_WIKILINK: dict[str, str] = {
    "ci-知识库同步门禁": "wiki同步指南",
    "gateway-接入-sentinel规划": "茉莉-演进-sentinel网关",
    "git-分支与发布策略": "茉莉-规范-git分支",
    "java-虚拟线程": "茉莉-演进-虚拟线程",
    "logback-日志配置": "茉莉-日志-logback",
    "micrometer-与指标暴露": "茉莉-prometheus-大盘",
    "架构决策-adr": "茉莉-规范-adr",
    "技术债-管理": "茉莉-规范-adr",
    "用户中心-扩展能力规划": "用户中心",
    "知识库-混合检索规划": "知识库-meilisearch接入规划",
    "知识库-全文检索规划": "知识库-meilisearch接入规划",
    "秒杀-库存对账校正": "茉莉-库存-对账",
    "库存-超卖防护": "茉莉-秒杀-redis-lua",
    "商品-sku与spu设计": "订单秒杀-概要设计",
    "订单-对账与补偿": "茉莉-订单-状态机",
    "订单-状态机设计": "茉莉-订单-状态机",
    "购物车-设计与缓存": "茉莉-缓存-多级",
    "双链": "知识库设计哲学-docs-as-code",
    # second pass: missing_concept slugs (≥3 refs)
    "bi报表服务演进路线": "茉莉-bi-报表规划",
    "druid连接池与监控": "茉莉-druid-监控",
    "dubbo-与-nacos": "茉莉-dubbo-group版本",
    "elasticsearch-搜索": "知识库-meilisearch接入规划",
    "java-cpu-100排查实战": "故障排查指南",
    "kafka-与-mq选型": "茉莉-mq-选型",
    "loadtest-profile与压测登录": "茉莉-loadtest-账号",
    "nacos-注册与配置": "茉莉-nacos-dev命名空间",
    "redis-缓存": "茉莉-缓存-多级",
    "shiro-starter与跨服务校验": "茉莉-shiro-跨服务",
    "shiro-鉴权体系": "认证与会话机制",
    "spring-cloud-gateway": "网关",
    "sso与系统门户": "认证与会话机制",
    "压测监控与prometheus": "茉莉-prometheus-大盘",
    "消息队列": "茉莉-mq-选型",
    "跨域与前后端分离": "茉莉-gateway-cors",
}

# [[old]] -> `moli-knowledge/kb/wiki/...` (manual when stem != enterprise filename)
MANUAL_ENTERPRISE_PATH: dict[str, str] = {
    "mysql-explain-执行计划进阶": "moli-knowledge/kb/wiki/database/mysql-索引面试题.md",
    "oauth2-与开放接口": "moli-knowledge/kb/wiki/security/annex-OAuth2实现单点登录SSO.md",
    "skywalking-链路追踪": "moli-knowledge/kb/wiki/ops/skywalking-安装与链路追踪.md",
    "tomcat-连接器调优": "moli-knowledge/kb/wiki/java/tomcat与-servlet容器.md",
    "线程池-实战调优": "moli-knowledge/kb/wiki/spring/spring-async与线程池.md",
    "分布式限流实现": "moli-knowledge/kb/wiki/middleware/sentinel-限流与熔断.md",
    "优惠券-高并发领取": "moli-knowledge/kb/wiki/middleware/sentinel-限流与熔断.md",
    "混沌工程入门": "moli-knowledge/kb/wiki/ops/蓝绿与滚动发布.md",
    "支付回调与安全验签": "moli-knowledge/kb/wiki/security/api-接口安全设计.md",
    "日志脱敏规范": "moli-knowledge/kb/wiki/security/api-接口安全设计.md",
    # second pass: enterprise-only concept slugs
    "mysql-索引": "moli-knowledge/kb/wiki/database/mysql-索引.md",
    "production-jvm启动参数": "moli-knowledge/kb/wiki/java/production-jvm启动参数.md",
    "redis-热key与大key治理": "moli-knowledge/kb/wiki/cache/redis-面试题.md",
    "sentinel-限流与熔断": "moli-knowledge/kb/wiki/middleware/sentinel-限流与熔断.md",
    "接口幂等性实践": "moli-knowledge/kb/wiki/middleware/接口幂等性实践.md",
    "生产环境服务启停脚本": "moli-knowledge/kb/wiki/ops/生产环境服务启停脚本.md",
}


def build_enterprise_stem_paths() -> dict[str, str]:
    out: dict[str, str] = {}
    for p in WIKI_ENT.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        rel = p.relative_to(WIKI_ENT).as_posix()
        out[p.stem] = f"moli-knowledge/kb/wiki/{rel}"
    return out


def build_fix_map() -> dict[str, str]:
    """old slug -> replacement text (without brackets)."""
    ent = build_enterprise_stem_paths()
    fixes: dict[str, str] = {}
    for old, new_stem in MOLI_WIKILINK.items():
        fixes[old] = f"[[{new_stem}]]"
    for old, path in MANUAL_ENTERPRISE_PATH.items():
        fixes[old] = f"`{path}`"
    lint_map = json.loads((KB / "tools" / "moli_broken_link_map.json").read_text(encoding="utf-8"))
    for old, info in lint_map.items():
        if old in fixes:
            continue
        if info["fix"]["type"] == "enterprise_slug" and info["enterprise"]:
            stem = info["enterprise"].split("/")[-1]
            path = ent.get(stem)
            if path:
                fixes[old] = f"`{path}`"
    return fixes


ALL_BROKEN = set(build_fix_map().keys())


def fix_related_line(line: str, fixes: dict[str, str]) -> str:
    m = re.match(r"(related:\s*\[)([^\]]*)(\])", line)
    if not m:
        return line
    prefix, inner, suffix = m.groups()
    out: list[str] = []
    for item in [x.strip() for x in inner.split(",") if x.strip()]:
        if item in ALL_BROKEN:
            repl = fixes.get(item, "")
            if repl.startswith("[["):
                out.append(repl[2:-2])
            # path refs: drop from related (not a wiki slug)
            continue
        out.append(item)
    seen: set[str] = set()
    deduped: list[str] = []
    for item in out:
        if item in seen:
            continue
        seen.add(item)
        deduped.append(item)
    return f"{prefix}{', '.join(deduped)}{suffix}"


def replace_body(text: str, fixes: dict[str, str]) -> str:
    for old in sorted(fixes, key=len, reverse=True):
        text = text.replace(f"[[{old}]]", fixes[old])
    return text


def fix_file(path: Path, fixes: dict[str, str]) -> bool:
    text = path.read_text(encoding="utf-8")
    if text.startswith("---"):
        parts = text.split("---", 2)
        if len(parts) < 3:
            return False
        fm_lines = [
            fix_related_line(ln, fixes) if ln.strip().startswith("related:") else ln
            for ln in parts[1].splitlines()
        ]
        body = replace_body(parts[2], fixes)
        new = "---\n" + "\n".join(fm_lines) + "\n---" + body
    else:
        new = replace_body(text, fixes)
    if new == text:
        return False
    path.write_text(new, encoding="utf-8")
    return True


def append_log(changed: int) -> None:
    line = f"## [{TODAY}] maintenance | {BATCH} wiki-moli 断链治理 → 修复 {changed} 文件（enterprise 路径 + moli 内链）\n"
    if LOG.exists():
        LOG.write_text(LOG.read_text(encoding="utf-8").rstrip() + "\n\n" + line, encoding="utf-8")
    else:
        LOG.write_text(line, encoding="utf-8")


def main() -> int:
    fixes = build_fix_map()
    unresolved = set(json.loads((KB / "tools" / "moli_broken_link_map.json").read_text(encoding="utf-8"))) - set(fixes)
    if unresolved:
        print("WARN unresolved (no fix):", ", ".join(sorted(unresolved)))
    changed = 0
    for p in sorted(WIKI_MOLI.rglob("*.md")):
        if p.name in ("index.md", "log.md"):
            continue
        if fix_file(p, fixes):
            print("fixed", p.relative_to(WIKI_MOLI))
            changed += 1
    append_log(changed)
    print(f"files changed: {changed}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
