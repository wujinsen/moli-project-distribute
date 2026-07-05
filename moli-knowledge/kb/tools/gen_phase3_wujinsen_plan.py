#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate Phase 3 closure plan for uncited wujinsen raw."""
from __future__ import annotations

import os
import re
from collections import defaultdict
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
RAW = KB / "raw" / "wujinsen_markdown"
WIKI = KB / "wiki"
OUT = Path(__file__).resolve().parent / "WUJINSEN_INGEST_PLAN_Phase3_收口.md"

# Already decided skip (Phase 1/2 + taxonomy)
SKIP_PREFIXES = [
    "AI/",
    "产品/",
    "写作/",
    "硬件/",
    "操作系统/",
    "EnglishDoc/",
    "Full Stack/",
    "IM通讯/",
    "学习方法/",
    "英语学习/",
    "开源项目/",
    "DataBase/Oracle/",
    "架构/Git/",
    "架构/SAML/",
    "架构/区块链/",
    "架构/开发工具/",
    "架构/消息队列/ActiveMQ/",
    "架构/腾讯云/",
    "架构/通信协议/Thrift/",
    "面试笔试/2020程序员内推/",
    "面试笔试/2020面试题整理/",
    "面试笔试/面试公司/",
    "面试笔试/面试要求/",
    "面试笔试/大数据/",
    "大数据资料-王/QA/",
    "大数据资料-王/loadrunner/",
    "大数据资料-王/selecnium/",
    "大数据资料-王/thrift/",
    "大数据资料-王/mahout/",
    "大数据资料-王/nutch/",
    "大数据资料-王/lucene&solr/",
]

SKIP_NAME_SUBSTR = ["无标题笔记", "同步发生冲突", "dfsdfa.note.md", ".note.attach"]

# prefix or exact file -> (action, target_slug or reason)
ENRICH_RULES: list[tuple[str, str, str]] = [
    # Phase 1 tail
    ("架构/DevOps/jenkins/", "enrich", "ops/jenkins-ci入门"),
    ("架构/DevOps/nexus/", "enrich", "ops/maven-多模块与依赖管理"),
    ("架构/MicroServer/", "enrich", "middleware/feign-开发踩坑|middleware/dubbo-调用原理与分层|spring/spring-cloud-gateway"),
    ("架构/DDD领域驱动/", "enrich", "middleware/分布式事务"),
    ("架构/Lambda架构/", "skip", "架构范式单篇，非主栈面试"),
    ("架构/NaiXue/", "skip", "外链/课程剪藏"),
    ("架构/云原生/quarkus/", "skip", "Quarkus 非主栈"),
    ("架构/埋点/", "enrich", "bigdata/kafka-大数据管道"),
    ("架构/分库分表/", "enrich", "database/sharding-分库分表入门"),
    ("架构/文件存储/fastdfs/", "skip", "MinIO 已覆盖对象存储主栈"),
    ("架构/轻量级分布式 RPC 框架", "enrich", "middleware/dubbo-调用原理与分层"),
    ("架构/性能监控/", "enrich", "ops/skywalking-安装与链路追踪"),
    ("架构/容器/k8s/", "enrich", "ops/k8s入门与容器编排"),
    ("DataBase/postgresql/", "skip", "PostgreSQL 非主栈"),
    ("DataBase/MySQL查询语句练习题", "skip", "练习题，非 KB 正文"),
    ("DataBase/MySQL外键设置", "skip", "外键专题，MySQL 主栈少用"),
    ("DataBase/mysql5.6修改编码", "skip", "版本过旧安装备忘"),
    ("DataBase/left join", "enrich", "database/mysql-索引面试题"),
    ("DataBase/Redis/Redis夺命16问(同步发生冲突)", "delete", "冲突副本"),
    ("DataBase/mysql/dfsdfa", "delete", "空壳/测试文件"),
    ("面试笔试/JVM/", "enrich", "java/jvm-面试题"),
    ("面试笔试/mianshi", "skip", "个人面试记录"),
    ("面试笔试/简历", "skip", "个人简历"),
    ("面试笔试/京东商城", "skip", "JD/公司向"),
    ("面试笔试/入门教程", "skip", "泛教程，已有专题页"),
    ("面试笔试/面试小结/2018", "skip", "个人面试记录"),
    ("面试笔试/面试小结/面试小结之综合篇", "enrich", "java/java-并发面试题|database/mysql-索引面试题"),
    ("面试笔试/海量数据处理", "enrich", "bigdata/spark-核心概念与实践|patterns/算法面试题精选"),
    ("面试笔试/教你如何迅速秒杀", "enrich", "patterns/算法面试题精选|bigdata/hadoop-面试题"),
    ("面试笔试/使用logstash", "enrich", "bigdata/elk-日志分析栈"),
    ("面试笔试/你不得不懂的", "enrich", "bigdata/hadoop-生态入门"),
    ("面试笔试/大数据处理", "enrich", "bigdata/spark-核心概念与实践"),
    ("javaweb/", "enrich", "database/mybatis-与-druid持久层|security/认证与会话机制"),
    ("源码分析/", "enrich", "见 Phase2 源码映射"),
    ("jvm/", "enrich", "java/jvm-面试题"),
    ("BigData/2023/无标题", "delete", "空壳"),
    ("BigData/集群管理/无标题", "delete", "空壳"),
    ("BigData/用户画像/无标题", "delete", "空壳"),
    ("BigData/架构设计/大数据总体架构设计/无标题", "delete", "空壳"),
    ("BigData/版本问题/", "skip", "版本踩坑单篇"),
    ("BigData/知识图谱/", "skip", "业务案例单篇"),
    ("BigData/用户画像/", "skip", "业务案例（非无标题）"),
    ("BigData/集群管理/", "skip", "空壳目录"),
    ("大数据资料-王/a安装文档/", "enrich-keywords", "按组件挂已有 slug（见 §安装稿）"),
    ("大数据资料-王/concurent编程器", "skip", "拼写错误/单篇"),
    ("大数据资料-王/x线程", "enrich", "java/java-并发面试题"),
    ("大数据资料-王/scala/", "skip", "Spark 生态可选，非 Java 主栈"),
    ("大数据资料-王/rpc/", "enrich", "middleware/dubbo-调用原理与分层"),
    ("大数据资料-王/log4j/", "skip", "日志配置碎片"),
    ("大数据资料-王/nutch/", "skip", "已列 skip"),
]

# a安装文档 keyword -> slugs
INSTALL_KEYWORDS: list[tuple[str, str]] = [
    (r"(?i)redis", "cache/redis-集群与哨兵实践"),
    (r"(?i)mysql", "database/mysql-索引"),
    (r"(?i)hadoop|hdfs", "bigdata/hadoop-生态入门"),
    (r"(?i)hive", "bigdata/hive-数仓与-sql"),
    (r"(?i)spark", "bigdata/spark-核心概念与实践"),
    (r"(?i)kafka", "bigdata/kafka-大数据管道"),
    (r"(?i)flume", "bigdata/flume-与-数据采集"),
    (r"(?i)(zookeeper|\bzk\b)", "middleware/zookeeper-与协调服务"),
    (r"(?i)elastic", "search/elasticsearch-搜索"),
    (r"(?i)nginx", "ops/nginx-反向代理与负载"),
    (r"(?i)flink", "bigdata/flink-流批一体入门"),
    (r"(?i)hbase", "bigdata/hbase-列式存储入门"),
    (r"(?i)storm", "bigdata/flink-流批一体入门"),
    (r"(?i)yarn", "bigdata/hadoop-生态入门"),
    (r"(?i)solr|lucene", "search/elasticsearch-搜索"),
]


def norm(p: str) -> str:
    return p.replace("\\", "/")


def load_cited() -> set[str]:
    cited: set[str] = set()
    for p in WIKI.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        text = p.read_text(encoding="utf-8")
        for s in re.findall(r"raw/wujinsen_markdown/([^\n]+)", text):
            cited.add(norm(s.strip()))
    return cited


def is_prefix_skip(rel: str) -> bool:
    for p in SKIP_PREFIXES:
        if rel == p.rstrip("/") or rel.startswith(p):
            return True
    return False


def is_name_skip(rel: str) -> bool:
    return any(x in rel for x in SKIP_NAME_SUBSTR)


def match_rule(rel: str) -> tuple[str, str, str] | None:
    for prefix, action, target in ENRICH_RULES:
        if rel == prefix.rstrip("/") or rel.startswith(prefix) or prefix in rel:
            return action, target, prefix
    return None


def find_ainstall_prefix() -> str:
    wang = RAW / "大数据资料-王"
    if not wang.is_dir():
        return "大数据资料-王/a安装文档"
    for d in wang.iterdir():
        if d.is_dir() and "安装" in d.name:
            return f"大数据资料-王/{d.name}"
    return "大数据资料-王/a安装文档"


def classify(rel: str) -> dict:
    if is_prefix_skip(rel) or is_name_skip(rel):
        reason = "prefix-skip" if is_prefix_skip(rel) else "name-skip"
        action = "delete" if is_name_skip(rel) and ("冲突" in rel or "dfsdfa" in rel or "无标题" in rel) else "skip"
        return {"action": action, "target": "—", "cluster": reason, "note": "Phase1/2 已决策 skip"}

    if "/a安装文档/" in rel or rel.startswith(find_ainstall_prefix()):
        for pat, slug in INSTALL_KEYWORDS:
            if re.search(pat, rel):
                return {"action": "enrich", "target": slug, "cluster": "a安装文档", "note": "关键词挂接"}
        return {"action": "skip", "target": "—", "cluster": "a安装文档", "note": "无组件关键词，安装碎片 skip"}

    rule = match_rule(rel)
    if rule:
        action, target, prefix = rule
        if action == "enrich-keywords":
            return classify(rel)  # retry through install path above
        return {"action": action, "target": target, "cluster": prefix, "note": ""}

    top = rel.split("/")[0]
    if top == "BigData":
        return {"action": "enrich", "target": "bigdata/* 就近", "cluster": "BigData/零散", "note": "并入最近 bigdata 或 search/middleware slug"}
    if top == "大数据资料-王":
        return {"action": "skip", "target": "—", "cluster": "王树/剩余", "note": "低价值/重复，不单独 enrich"}
    if top in ("架构", "面试笔试", "DataBase"):
        return {"action": "enrich", "target": "邻近 Phase1 slug", "cluster": top, "note": "单篇补 sources"}
    return {"action": "skip", "target": "—", "cluster": "other", "note": "未识别，默认 skip"}


def main() -> None:
    cited = load_cited()
    raw_all = sorted(
        norm(os.path.relpath(str(f), str(RAW)))
        for f in RAW.rglob("*.md")
    )
    uncited = [r for r in raw_all if r not in cited]

    by_action: dict[str, list[tuple[str, dict]]] = defaultdict(list)
    for rel in uncited:
        info = classify(rel)
        by_action[info["action"]].append((rel, info))

    lines = [
        "# wujinsen_markdown Ingest 规划 · Phase 3 收口",
        "",
        "> **空间**：`enterprise-kb` only · **批次建议** `#1330`",
        "> **目标**：370 篇未 cited → **enrich 补挂 / skip 定案 / delete 空壳**，不再新建分类/体裁",
        f"> **审计**：raw **{len(raw_all)}** · cited **{len(cited)}** · 未 cited **{len(uncited)}**",
        "",
        "## 动作统计",
        "",
        "| 动作 | 篇数 | 说明 |",
        "|------|------|------|",
    ]
    action_labels = {
        "enrich": "补挂 `sources` 到已有 slug（不新建页）",
        "skip": "定案 skip，可选物理删 raw",
        "delete": "物理删除 raw（空壳/冲突副本）",
    }
    for act in ("enrich", "skip", "delete"):
        n = len(by_action.get(act, []))
        lines.append(f"| **{act}** | {n} | {action_labels[act]} |")

    lines.extend(
        [
            "",
            "## 执行顺序",
            "",
            "1. **#1330-delete**：物理删 `delete` 清单（冲突副本/无标题/dfsdfa）",
            "2. **#1330-enrich**：按 §规划表 enrich（脚本 `run_phase3_wujinsen_ingest.py`）",
            "3. **#1330-skip**：`WUJINSEN_SKIP_MANIFEST.md` 定案，可选删 raw",
            "4. `lint.py --strict` → `sync_to_db.py --space enterprise-kb`",
            "5. 复跑 `_audit_wujinsen_coverage.py`，未 cited 应 ≈ skip 清单",
            "",
            "## 规划表（按动作 · 簇）",
            "",
            "| 动作 | raw 簇 / 规则 | 篇数≈ | 目标 slug | 说明 |",
            "|------|---------------|-------|-----------|------|",
        ]
    )

    # Aggregate by cluster+action+target
    agg: dict[tuple, list[str]] = defaultdict(list)
    for act, items in by_action.items():
        for rel, info in items:
            key = (act, info["cluster"], info["target"], info["note"])
            agg[key].append(rel)

    for (act, cluster, target, note), rels in sorted(agg.items(), key=lambda x: (-len(x[1]), x[0][0])):
        lines.append(f"| **{act}** | `{cluster}` | {len(rels)} | `{target}` | {note or '—'} |")

    lines.extend(["", "## enrich 明细 · 按目标 slug", ""])
    slug_groups: dict[str, list[str]] = defaultdict(list)
    for rel, info in by_action.get("enrich", []):
        for t in info["target"].split("|"):
            slug_groups[t.strip()].append(rel)
    for slug in sorted(slug_groups, key=lambda s: -len(slug_groups[s])):
        rels = slug_groups[slug]
        lines.append(f"### `{slug}`（+{len(rels)} sources）")
        for r in rels[:8]:
            lines.append(f"- `{r}`")
        if len(rels) > 8:
            lines.append(f"- … 还有 {len(rels) - 8} 篇")
        lines.append("")

    lines.extend(["", "## skip 定案（不再 ingest）", ""])
    lines.append("以下 prefix 与 Phase 1/2 一致；Phase 3 仅 **定案 + 可选删 raw**：")
    lines.append("")
    for p in SKIP_PREFIXES:
        n = sum(1 for r in uncited if r.startswith(p) or r == p.rstrip("/"))
        if n:
            lines.append(f"- `{p}` — {n} 篇")
    lines.append("")
    lines.append("王树剩余低价值（无关键词安装稿外）：**~43 篇** → skip，不 enrich。")

    lines.extend(["", "## delete 清单（物理删 raw）", ""])
    for rel, info in sorted(by_action.get("delete", [])):
        lines.append(f"- `{rel}`")

    lines.extend(
        [
            "",
            "## 验收",
            "",
            "- [ ] 规划内 **enrich** 簇已补 sources",
            "- [ ] **delete** 已从 raw 删除",
            "- [ ] **skip** 写入 manifest（可选删 raw）",
            "- [ ] 未 cited 仅剩 skip 簇（AI/QA/产品/王树低价值等）",
            "- [ ] sync 无未分类文档",
            "",
            "## conflicts",
            "",
            "- **BigData/零散 27 篇**：优先挂最近 bigdata slug，不 create 新页",
            "- **王树 206 uncited**：QA(115) + 安装 + 低价值；Phase 3 **不** bulk enrich QA",
            "- **install 无关键词 21 篇**：默认 skip，不全文 ingest",
        ]
    )

    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {OUT}")
    for act in ("enrich", "skip", "delete"):
        print(act, len(by_action.get(act, [])))


if __name__ == "__main__":
    main()
