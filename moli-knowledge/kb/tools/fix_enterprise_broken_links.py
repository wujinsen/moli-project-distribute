#!/usr/bin/env python3
"""Fix enterprise-kb broken [[wikilinks]] and related entries (batch #1332)."""
from __future__ import annotations

import re
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
WIKI = KB / "wiki"
TODAY = "2026-07-05"

# [[slug]] -> [[enterprise/slug]]
WIKI_LINK: dict[str, str] = {
    "dubbo-负载均衡与集群容错": "middleware/dubbo-面试题",
    "elasticsearch-写入调优": "search/elasticsearch-搜索",
    "gateway-断言与请求改写": "spring/spring-cloud-gateway",
    "分库分表入门": "database/sharding-分库分表入门",
    "spring-事务面试题": "database/mysql-事务面试题",
    "redis-热key与大key治理": "cache/redis-面试题",
    "sentinel-热点参数限流": "middleware/sentinel-限流与熔断",
    "nacos-配置灰度发布": "middleware/nacos-config动态配置实践",
    "tomcat-连接器调优": "java/tomcat与-servlet容器",
    "java-类加载与双亲委派": "java/jvm-内存与gc",
    "mysql-explain-执行计划进阶": "database/mysql-索引面试题",
    "oauth2-与开放接口": "security/api-接口安全设计",
    "shiro-rememberme-安全": "security/认证与会话机制",
    "websocket-实时通信": "middleware/sse-服务端推送",
    "reactor-mono与-flux": "java/completablefuture-异步编排",
    "spring-事件机制": "spring/spring-application启动流程",
    "spring-scheduled-定时任务": "spring/spring-async与线程池",
    "idea-远程调试与断点": "java/arthas-在线诊断",
    "junit5-单元测试": "database/testcontainers-集成测试",
    "mockito-测试实战": "java/java-编码规范与CodeReview要点",
    "ldap-与企业账号": "security/认证与会话机制",
    "mysql-备份与恢复": "database/flyway-数据库版本迁移",
    "openapi3-与接口文档": "middleware/openfeign-与-http客户端",
    "xxl-job-分布式定时任务": "cache/redis-实现延迟队列",
    "微服务-优雅停机": "ops/蓝绿与滚动发布",
    "字段级加密存储": "security/bcrypt-密码哈希与加盐",
    "配置-敏感信息与加密": "middleware/nacos-config动态配置实践",
    "java-虚拟线程": "java/异步编程面试题",
    "logback-日志配置": "java/mdc-日志链路上下文",
    "日志脱敏规范": "security/api-接口安全设计",
    "混沌工程入门": "ops/蓝绿与滚动发布",
}

# [[slug]] -> `moli-knowledge/kb/wiki-moli/...`（跨空间不用 wikilink）
PATH_REF: dict[str, str] = {
    "bi服务": "moli-knowledge/kb/wiki-moli/develop/bi服务.md",
    "git协作指南": "moli-knowledge/kb/wiki-moli/guides/git协作指南.md",
    "docker-compose-依赖栈": "moli-knowledge/kb/wiki-moli/develop/docker-compose-茉莉依赖栈.md",
    "dubbo-分组版本与环境": "moli-knowledge/kb/wiki-moli/develop/茉莉-dubbo-group版本.md",
    "gateway-接入-sentinel规划": "moli-knowledge/kb/wiki-moli/develop/茉莉-演进-sentinel网关.md",
    "moli生产部署拓扑备忘": "moli-knowledge/kb/wiki-moli/ops/生产部署拓扑备忘.md",
    "事故复盘-postmortem": "moli-knowledge/kb/wiki-moli/guides/事故复盘-postmortem.md",
    "代码审查-checklist": "moli-knowledge/kb/wiki-moli/guides/代码审查-checklist.md",
    "压测报告解读指南": "moli-knowledge/kb/wiki-moli/test/压测报告解读指南.md",
    "可观测性与运维体系汇总": "moli-knowledge/kb/wiki-moli/develop/outputs/茉莉可观测性与运维体系汇总.md",
    "数据库初始化指南": "moli-knowledge/kb/wiki-moli/guides/数据库初始化指南.md",
    "权限管理操作指南": "moli-knowledge/kb/wiki-moli/guides/权限管理操作指南.md",
    "知识库使用指南": "moli-knowledge/kb/wiki-moli/guides/知识库使用指南.md",
    "订单服务": "moli-knowledge/kb/wiki-moli/develop/订单服务.md",
    "登录与鉴权故障根因汇总": "moli-knowledge/kb/wiki-moli/develop/outputs/茉莉登录与鉴权故障根因汇总.md",
    "高并发架构模式汇总": "moli-knowledge/kb/wiki-moli/develop/outputs/茉莉高并发架构模式汇总.md",
    "架构决策-adr": "moli-knowledge/kb/wiki-moli/develop/茉莉-规范-adr.md",
    "ci-知识库同步门禁": "moli-knowledge/kb/wiki-moli/ops/wiki同步指南.md",
    "知识库-全文检索规划": "moli-knowledge/kb/wiki-moli/develop/知识库-meilisearch接入规划.md",
    "知识库-混合检索规划": "moli-knowledge/kb/wiki-moli/develop/知识库-meilisearch接入规划.md",
}

ALL_BROKEN = set(WIKI_LINK) | set(PATH_REF)


def stem(slug: str) -> str:
    return slug.split("/")[-1]


def replace_body(text: str) -> str:
    for old, new in WIKI_LINK.items():
        text = text.replace(f"[[{old}]]", f"[[{new}]]")
    for old, path in PATH_REF.items():
        text = text.replace(f"[[{old}]]", f"`{path}`")
    return text


def fix_related_line(line: str) -> str:
    m = re.match(r"(related:\s*\[)([^\]]*)(\])", line)
    if not m:
        return line
    prefix, inner, suffix = m.groups()
    items = [x.strip() for x in inner.split(",") if x.strip()]
    out: list[str] = []
    for item in items:
        if item in PATH_REF:
            continue
        if item in WIKI_LINK:
            out.append(stem(WIKI_LINK[item]))
        elif item in ALL_BROKEN:
            continue
        else:
            out.append(item)
    # dedupe preserving order
    seen: set[str] = set()
    deduped: list[str] = []
    for item in out:
        if item in seen:
            continue
        seen.add(item)
        deduped.append(item)
    return f"{prefix}{', '.join(deduped)}{suffix}"


def fix_file(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    if text.startswith("---"):
        parts = text.split("---", 2)
        if len(parts) < 3:
            return False
        fm_lines = parts[1].splitlines()
        new_fm = [
            fix_related_line(ln) if ln.strip().startswith("related:") else ln for ln in fm_lines
        ]
        body = replace_body(parts[2])
        new = "---\n" + "\n".join(new_fm) + "\n---" + body
    else:
        new = replace_body(text)
    if new == text:
        return False
    path.write_text(new, encoding="utf-8")
    return True


def main() -> None:
    changed = 0
    for p in sorted(WIKI.rglob("*.md")):
        if p.name in ("index.md", "log.md"):
            continue
        if fix_file(p):
            print("fixed", p.relative_to(WIKI))
            changed += 1
    print("files changed", changed)


if __name__ == "__main__":
    main()
