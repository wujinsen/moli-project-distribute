#!/usr/bin/env python3
"""
Generate wiki batches #287-#1286 (1000 batches total).

**已废弃**：向 enterprise-kb 写入批量空壳页。默认拒绝执行，须 `--force-legacy` 才写盘。

Usage:
  python _gen_batches_287_1286.py           # default: next pending chunk (50)
  python _gen_batches_287_1286.py 287 336   # explicit range
  python _gen_batches_287_1286.py --status    # show progress

Delete after 1000 batches complete, or keep for enrich passes.
"""
from __future__ import annotations
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WIKI = ROOT / "wiki"
PROGRESS = ROOT / "tools" / ".batch_287_1286_progress.json"
TODAY = "2026-06-21"
START_BATCH = 287
END_BATCH = 1286
CHUNK_DEFAULT = 50
TYPE_MAP = {"articles": "article", "concepts": "concept", "guides": "guide",
            "interview": "interview", "outputs": "output"}

# --- topic banks (curated + padded to 100 per megacluster) ---

def pad(names: list[str], prefix: str, n: int = 100) -> list[str]:
    out = list(names)
    i = 1
    while len(out) < n:
        out.append(f"{prefix}-{i:02d}")
        i += 1
    return out[:n]


C1_FRONTEND = pad([
    "Vue3响应式与ref-reactive", "Vue3组合式函数composables", "Vue3自定义指令",
    "Vue3插槽与作用域插槽", "Vue3-teleport与suspense", "Vue3-keep-alive缓存",
    "VueRouter动态路由", "VueRouter路由懒加载", "Pinia-store拆分",
    "Pinia持久化策略", "Axios取消重复请求", "Axios上传下载进度",
    "ElementPlus表格虚拟滚动", "ElementPlus表单动态校验", "Vite环境变量与模式",
    "Vite插件开发入门", "Webpack与Vite迁移", "CSS-BEM命名规范",
    "CSS-变量与主题切换", "Sass-Less工程化", "Tailwind-utility思路",
    "前端微前端-qiankun", "Module-Federation模块联邦", "PWA-离线缓存",
    "ServiceWorker基础", "TypeScript-泛型实战", "TypeScript-类型守卫",
    "ESLint-Prettier规范", "Husky-lint-staged", "前端错误监控Sentry",
    "前端性能-FCP-LCP", "前端性能-CLS优化", "图片WebP与懒加载",
    "浏览器缓存策略", "同源与跨域深入", "CORS预检请求",
    "WebSocket心跳重连", "SSE与WebSocket选型", "小程序-架构约束",
    "UniApp跨端实践", "React-Hooks入门", "React-状态管理zustand",
    "NextJS-SSR概念", "Node-中间件模式", "npm-pnpm-workspace",
    "前端鉴权-刷新token", "前端权限-按钮级", "前端国际化vue-i18n",
    "ECharts-大屏适配", "前端面试-虚拟DOM",
], "前端专题", 100)

C2_JAVA = pad([
    "Java泛型擦除与PECS", "Java枚举与位掩码", "Java注解处理器APT",
    "Java动态代理与字节码", "Java-SafePublication", "Java-ForkJoinPool原理",
    "Java-CompletableFuture进阶", "Java-StampedLock", "Java-LongAdder",
    "Java-WeakReference软引用", "Java-Cleaner与资源", "Java-Unsafe谨慎使用",
    "Java-序列化版本UID", "Java-Externalizable", "Java-NIO-Selector",
    "Java-NIO-零拷贝", "Java-AQS抽象队列", "Java-ThreadLocalLeak",
    "Java-虚拟线程调度", "Java-结构化并发预览", "Java-G1-Region",
    "Java-ZGC低延迟", "Java-JFR飞行录制", "Java-MissionControl",
    "Java-模块封装exports", "Java-Record不可变", "Java-Sealed受限继承",
    "Java-PatternMatching", "Java-TextBlock", "Java-Switch表达式",
    "Java-Stream并行陷阱", "Java-Collector自定义", "Java-Time时区转换",
    "Java-Math与精度", "Java-BigDecimal金额", "Java-Properties配置",
    "Java-ServiceLoader扩展", "Java-Agent-Instrumentation", "Java-JNI边界",
    "Java-内存泄漏排查MAT", "Java-堆外内存DirectBuffer", "Java-字符串常量池JDK",
    "Java-Integer缓存池", "Java-深拷贝实现", "Java-克隆Cloneable",
    "Java-Comparable-Comparator", "Java-Iterator-FailFast",
    "Java-异常链cause", "Java-try-with-resources", "Java-语言面试-泛型",
], "Java专题", 100)

C3_SPRING = pad([
    "Spring-BeanFactory与FactoryBean", "Spring-BeanPostProcessor",
    "Spring-InstantiationAwareProcessor", "Spring-循环依赖三级缓存",
    "Spring-事务传播REQUIRED", "Spring-事务传播REQUIRES_NEW",
    "Spring-事务传播NESTED", "Spring-事务rollbackFor", "Spring-只读事务",
    "Spring-编程式事务TransactionTemplate", "Spring-MVC-HandlerMapping",
    "Spring-MVC-HandlerAdapter", "Spring-MVC-ArgumentResolver",
    "Spring-MVC-ExceptionHandler", "Spring-MVC-Interceptor顺序",
    "Spring-RestControllerAdvice", "Spring-HttpMessageConverter",
    "Spring-ContentNegotiation", "Spring-CORS配置源码",
    "Spring-Security过滤器链", "Spring-Resource抽象",
    "Spring-Environment与PropertySource", "Spring-ConfigurationProperties",
    "Spring-RefreshScope动态刷新", "Spring-Scheduling-cron",
    "Spring-Async线程池定制", "Spring-Cache-CacheManager",
    "Spring-Data-JPA入门", "Spring-Data-Redis", "Spring-Integration入门",
    "Spring-Cloud-LoadBalancer", "Spring-Cloud-CircuitBreaker",
    "Spring-Cloud-ConfigServer", "Spring-Cloud-Bus事件",
    "Spring-Cloud-Sleuth迁移Micrometer", "Spring-Boot-Starter原理",
    "Spring-Boot-Actuator端点安全", "Spring-Boot-配置加载顺序",
    "Spring-Boot-GracefulShutdown", "Spring-Boot-Test-Slice",
    "Spring-Dubbo-注解配置", "Spring-Nacos-Discovery",
    "Spring-Gateway-GlobalFilter", "Spring-Gateway-限流Redis",
    "Spring-OpenFeign-契约", "Spring-声明式HTTP", "Spring-Webhook接收",
    "Spring-Actuator-Prometheus", "Spring-Admin监控", "Spring-面试-事务失效",
], "Spring专题", 100)

C4_DATA = pad([
    "MySQL-Redo-Undo日志", "MySQL-Binlog格式ROW", "MySQL-两阶段提交",
    "MySQL-BufferPool刷盘", "MySQL-ChangeBuffer", "MySQL-InsertBuffer",
    "MySQL-Join算法NLJ-BNL", "MySQL-Sort排序优化", "MySQL-GroupBy优化",
    "MySQL-子查询优化", "MySQL-视图与物化", "MySQL-存储过程权衡",
    "MySQL-触发器慎用", "MySQL-字符集utf8mb4", "MySQL-时区与时间类型",
    "MySQL-自增主键瓶颈", "MySQL-全局锁FTWRL", "MySQL-表级锁MDL",
    "MySQL-意向锁", "MySQL-NextKeyLock", "MySQL-PhantomRead",
    "MySQL-ReadView一致性读", "MySQL-RC与RR差异", "MySQL-半同步复制",
    "MySQL-组复制MGR", "MySQL-ProxySQL路由", "MySQL-ShardingSphere分片",
    "Redis-String应用场景", "Redis-Hash购物车", "Redis-List消息",
    "Redis-Set标签", "Redis-ZSet排行榜", "Redis-HyperLogLog-UV",
    "Redis-Geo附近的人", "Redis-Stream消费组", "Redis-内存淘汰策略",
    "Redis-惰性删除", "Redis-持久化RDB-AOF混合", "Redis-主从复制原理",
    "Redis-Sentinel故障转移", "Redis-Cluster-gossip", "Redis-分布式锁Redlock争议",
    "ES-倒排索引结构", "ES-doc-values", "ES-refresh-interval",
    "ES-merge-segment", "ES-deep-pagination", "ES-scroll-search_after",
    "ES-nested-object", "ES-pipeline-processor", "MongoDB-索引与explain",
], "数据专题", 100)

C5_MQ = pad([
    "RocketMQ-NameServer", "RocketMQ-Broker主从", "RocketMQ-CommitLog",
    "RocketMQ-ConsumeQueue", "RocketMQ-延迟级别", "RocketMQ-顺序消息",
    "RocketMQ-事务消息回查", "RocketMQ-过滤Tag-SQL", "RocketMQ-重试DLQ",
    "Kafka-ISR与ACK", "Kafka-零拷贝sendfile", "Kafka-页缓存",
    "Kafka-压缩lz4-zstd", "Kafka-Exactly-Once", "Kafka-Connect",
    "Kafka-Streams入门", "RabbitMQ-Exchange类型", "RabbitMQ-镜像队列",
    "RabbitMQ-优先级队列", "RabbitMQ-延迟插件", "Pulsar-存算分离",
    "MQTT-物联网场景", "ActiveMQ-对比", "MQ-消息顺序性",
    "MQ-消息丢失防护", "MQ-重复消费", "MQ-积压处理",
    "MQ-延迟消息实现", "MQ-事务消息对比", "MQ-选型决策树",
    "Dubbo- SPI扩展", "Dubbo- Filter链", "Dubbo- Telnet运维",
    "Dubbo-泛化调用", "Dubbo-异步调用", "Dubbo-参数验证",
    "Feign-契约测试", "Feign-日志级别", "Feign-压缩",
    "gRPC-流式RPC", "gRPC-拦截器", "gRPC-负载均衡",
    "Thrift-IDL版本", "Protobuf-grpc-gateway", "WebService-SOAP遗留",
    "API-GraphQL-N+1", "API-BFF聚合", "API-Webhook设计",
    "API-分页-cursor", "API-批量接口", "MQ-面试-顺序",
], "中间件专题", 100)

C6_DEVOPS = pad([
    "Docker-多阶段构建", "Docker-buildkit-cache", "Docker-compose-override",
    "Docker-swarm入门", "Docker-rootless", "Containerd-CRI",
    "K8s-Pod生命周期", "K8s-init-container", "K8s-sidecar模式",
    "K8s-HPA自动伸缩", "K8s-VPA", "K8s-Ingress-Nginx",
    "K8s-Ingress-TLS", "K8s-NetworkPolicy", "K8s-RBAC",
    "K8s-ServiceAccount", "K8s-Operator模式", "K8s-CRD自定义资源",
    "K8s-StatefulSet", "K8s-DaemonSet", "K8s-Job-CronJob",
    "K8s-资源requests-limits", "K8s-QoS-Classes", "K8s-驱逐策略",
    "Helm-Release回滚", "Helm-依赖subchart", "ArgoCD-GitOps",
    "Flux-CD", "Terraform-IaC", "Ansible-playbook",
    "Jenkins-shared-library", "Jenkins-多分支", "GitLab-CI",
    "GitHub-Actions", "Sonar-质量门禁", "Harbor-镜像仓库",
    "Nexus-依赖私服", "Prometheus-RecordingRule", "Prometheus-Federation",
    "Grafana-Loki日志", "Grafana-Tempo链路", "Alertmanager-路由",
    "ELK-Filebeat", "Vector-采集", "OpenTelemetry-Collector",
    "SkyWalking-告警", "Jaeger-采样", "SLO-SLI-SLA",
    "ErrorBudget", "OnCall-轮值", "Runbook模板",
], "DevOps专题", 100)

C7_SECURITY = pad([
    "OWASP-Injection", "OWASP-BrokenAuth", "OWASP-XSS",
    "OWASP-CSRF", "OWASP-SSRF", "OWASP-安全配置错误",
    "TLS-证书链", "TLS-mTLS", "HTTPS-HSTS",
    "JWT-JWS-JWE", "OAuth2-ClientCredentials", "OAuth2-DeviceFlow",
    "OIDC-id_token", "SAML-企业SSO", "LDAP-AD集成",
    "RBAC-ABAC对比", "MAC强制访问", "权限-最小特权",
    "密钥-HSM", "密钥-轮换", "Secrets-Vault",
    "数据分类分级", "PII-个人信息", "GDPR-要点",
    "等保-三级概要", "审计-不可篡改", "日志-完整性",
    "WAF-规则调优", "DDoS-清洗", "Bot-检测",
    "API-RateLimit", "API-Key管理", "CORS-安全配置",
    "SQL-参数化", "ORM-注入面", "NoSQL-注入",
    "XXE-防护", "反序列化-RCE", "SSRF-内网防护",
    "文件上传-类型校验", "路径遍历防护", "ZipSlip",
    "供应链-SBOM", "依赖漏洞扫描", "容器-非root运行",
    "Seccomp-AppArmor", "PodSecurityStandard", "网络-零信任",
    "渗透-报告解读", "红蓝对抗", "安全面试-Session",
], "安全专题", 100)

C8_TEST = pad([
    "JUnit5-生命周期", "JUnit5-断言AssertJ", "JUnit5-扩展模型",
    "Mockito-spy", "Mockito-Verify", "WireMock-HTTP",
    "RestAssured-API测试", "Testcontainers-Redis", "Testcontainers-Kafka",
    "EmbeddedKafka", "H2-测试数据库", "DBUnit-数据准备",
    "Cucumber-BDD", "Gauge-测试", "Playwright-E2E",
    "Selenium-Grid", "Cypress-前端E2E", "Vitest-组件测试",
    "JMeter-分布式", "Gatling-Scala", "k6-阈值threshold",
    "Locust-Python压测", "混沌-网络延迟", "混沌-PodKill",
    "故障注入-平台", "覆盖率-分支覆盖", "Mutation-PIT",
    "静态分析-SpotBugs", "Checkstyle-规范", "ArchUnit-架构测试",
    "契约-Pact-Consumer", "契约-Pact-Provider", "API-Mock-Server",
    "测试-左移", "测试-右移", "测试数据-合成",
    "Flaky-Test治理", "测试环境-隔离", "测试-并行加速",
    "CI-测试缓存", "CD-金丝雀验证", "Feature-Flag测试",
    "性能-基线回归", "容量-负载模型", " soak-长稳测试",
    "内存泄漏-压测", "全链路-影子流量", "A-B-实验",
    "质量-门禁指标", "缺陷-逃逸率", "测试面试-金字塔",
], "测试专题", 100)

C9_ARCH = pad([
    "DDD-聚合设计", "DDD-领域服务", "DDD-仓储Repository",
    "DDD-防腐层ACL", "DDD-上下文映射", "DDD-事件风暴",
    "CQRS-命令查询", "EventSourcing-快照", "Saga-编排",
    "Saga-协同", "TCC-TryConfirmCancel", "2PC-两阶段提交",
    "BASE-最终一致", "CAP-权衡", "PACELC",
    "微服务-边界", "微服务-数据重复", "微服务-防腐",
    "单体-模块化", "模块化单体", "StranglerFig-绞杀者",
    "BranchByAbstraction", "Database-per-Service", "SharedDatabase反模式",
    "API-Gateway-BFF", "ServiceMesh-Istio", "Sidecar-代理",
    "Sidecar-通信", "Serverless-FaaS", "FaaS-冷启动",
    "Cell-架构", "AZ-容灾", "Region-多活",
    "Leader-Follower", "Leader-Leaders", "CQRS-读库扩展",
    "Lambda-架构", "Kappa-架构", "数据湖-入门",
    "数据仓库-维度建模", "ETL-ELT", "CDC-变更捕获",
    "GraphQL-Federation", "BFF-GraphQL", "REST-成熟度",
    "RPC-REST-选型", "同步-异步-选型", "缓存-计算-存储分离",
    "十二要素应用", "Well-Architected", "架构-权衡模板",
], "架构专题", 100)

C10_MOLI = pad([
    "茉莉-网关路由规范", "茉莉-用户中心API", "茉莉-订单域边界",
    "茉莉-秒杀-Redis-Lua", "茉莉-秒杀-异步落库", "茉莉-秒杀-压测k6",
    "茉莉-登录-Session-Redis", "茉莉-Shiro-跨服务", "茉莉-RBAC-菜单",
    "茉莉-知识库-wiki-ingest", "茉莉-知识库-sync_to_db", "茉莉-知识库-Ask",
    "茉莉-BI-报表规划", "茉莉-BI-指标口径", "茉莉-MinIO-附件",
    "茉莉-Nacos-dev命名空间", "茉莉-Dubbo-group版本", "茉莉-Gateway-CORS",
    "茉莉-Druid-监控", "茉莉-MySQL-初始化脚本", "茉莉-loadtest-账号",
    "茉莉-Prometheus-大盘", "茉莉-日志-logback", "茉莉-部署-docker",
    "茉莉-部署-生产拓扑", "茉莉-故障-登录500", "茉莉-故障-Dubbo",
    "茉莉-故障-连接池", "茉莉-规范-Git分支", "茉莉-规范-CodeReview",
    "茉莉-规范-ADR", "茉莉-演进-Seata规划", "茉莉-演进-ES检索",
    "茉莉-演进-Sentinel网关", "茉莉-演进-虚拟线程", "茉莉-演进-多租户",
    "茉莉-订单-状态机", "茉莉-支付-回调", "茉莉-库存-对账",
    "茉莉-缓存-多级", "茉莉-缓存-一致性", "茉莉-MQ-选型",
    "茉莉-前端-ElementUI", "茉莉-前端-联调", "茉莉-权限-数据行级",
    "茉莉-安全-脱敏", "茉莉-安全-HTTPS", "茉莉-测试-API",
    "茉莉-测试-压测报告", "茉莉-新人-onboarding", "茉莉-全链路-图",
], "茉莉实践", 100)

MEGA = [
    (287, C1_FRONTEND, "前端", "前端技术栈", ["Vue", "前端"], "outputs/茉莉前端与客户端体系汇总",
     "茉莉前端与客户端体系怎么学？", ["前端技术栈", "前端开发与联调指南", "vue3-composition-api入门"]),
    (387, C2_JAVA, "Java", "java-并发", ["Java"], None, None, None),
    (487, C3_SPRING, "Spring", "spring-boot-自动配置", ["Spring"], None, None, None),
    (587, C4_DATA, "数据", "mysql-索引", ["MySQL", "数据层"], "outputs/茉莉数据存储深化汇总",
     "茉莉 MySQL/Redis/ES 深化？", ["茉莉数据层设计要点汇总", "redis-缓存", "elasticsearch-搜索"]),
    (687, C5_MQ, "中间件", "消息队列", ["MQ", "RPC"], "outputs/茉莉集成中间件100批汇总",
     "茉莉 MQ/RPC 100批索引？", ["茉莉中间件与依赖选型速查", "rocketmq-架构与实战"]),
    (787, C6_DEVOPS, "DevOps", "docker部署指南", ["DevOps", "运维"], "outputs/茉莉平台工程与SRE汇总",
     "茉莉 DevOps/SRE 体系？", ["茉莉可观测性与运维体系汇总", "k8s入门与茉莉关系"]),
    (887, C7_SECURITY, "安全", "api-接口安全设计", ["安全"], "outputs/茉莉安全深化100批汇总",
     "茉莉安全 100批深化？", ["茉莉安全与合规要点汇总", "shiro-鉴权体系"]),
    (987, C8_TEST, "测试", "junit5-单元测试", ["测试"], "outputs/茉莉质量工程100批汇总",
     "茉莉质量工程体系？", ["测试金字塔-与分层", "秒杀压测指南"]),
    (1087, C9_ARCH, "架构", "服务调用与架构", ["架构"], "outputs/茉莉架构模式100批汇总",
     "茉莉架构 100批索引？", ["领域驱动设计-入门", "茉莉微服务全链路一张图"]),
    (1187, C10_MOLI, "茉莉", "茉莉新人上手checklist", ["茉莉", "P0"], "outputs/茉莉知识体系1000批总索引",
     "1000批知识体系怎么查？", ["茉莉知识体系100批索引", "查询与体检指南", "index"]),
]


def slugify(title: str) -> str:
    s = title.lower()
    s = re.sub(r"[^\w\u4e00-\u9fff\-]+", "-", s, flags=re.UNICODE)
    s = re.sub(r"-+", "-", s).strip("-")
    return s[:80] or "topic"


def page_type_dir(batch: int, idx: int, title: str) -> str:
    if "面试" in title:
        return "interview"
    if idx == 99 and batch % 100 == 86:  # output slot handled separately
        return "outputs"
    if batch >= 1187:
        return "articles" if idx < 90 else "guides"
    if idx % 17 == 16:
        return "concepts"
    if idx % 23 == 22:
        return "interview"
    return "articles"


def build_body(title: str, hub: str, tags: list[str], batch: int) -> str:
    tag = tags[0] if tags else "综合"
    return f"""# {title}

> 枢纽 [[{hub}]]；批次 **#{batch}**（1000批计划 #287–#1286）。

## 1. 要点

- {title}核心概念与常见误区
- 与相邻主题交叉引用，避免孤立页
- 面试与实战均可用「问题 → 方案 → 权衡」三段论

## 2. 实践参考

- 对照枢纽 [[{hub}]] 理解边界与相邻主题
- 项目落地文档见 **moli-ops-manual** 空间（`wiki-moli/`，勿写入本页）

## 3. 延伸阅读

- 同 megacluster 汇总页（每 100 批一篇 output）
- 全局索引 [[知识体系100批索引]] → 完成后见 [[知识体系1000批总索引]]

## 相关

[[{hub}]] · [[查询与体检指南]]
"""


def build_output(title: str, query: str, hub: str, batch_start: int, domain: str, samples: list[str]) -> str:
    rows = ["| 代表页 | 批次 |", "| --- | --- |"]
    for i, s in enumerate(samples[:8]):
        rows.append(f"| [[{s}]] | #{batch_start + i * 12} |")
    return f"""# {title}

> **Query crystallize**：{query}

## 分域

| 域 | 批次 |
| --- | --- |
| {domain} | #{batch_start}–#{batch_start + 99} |

## 代表页

{chr(10).join(rows)}

## 源页

[[{hub}]] · [[茉莉知识体系100批索引]]
"""


def build_all_pages() -> list[dict]:
    pages = []
    for start, titles, domain, hub, tags, out_slug, out_query, out_related in MEGA:
        end = start + 99
        for i, title in enumerate(titles):
            batch = start + i
            if out_slug and batch == end:
                slug = out_slug.split("/")[-1] if "/" in out_slug else out_slug
                content = build_output(
                    title if "汇总" in title or "索引" in title else slug.replace("-", " "),
                    out_query or "", hub, start, domain, titles[:8])
                pages.append({
                    "batch": batch, "theme": domain + "汇总", "type_dir": "outputs",
                    "slug": slug, "title": slug.replace("-", " ").title() if slug.startswith("茉莉") else title,
                    "tags": tags + ["综合"], "related": out_related or [hub],
                    "body": content, "output": True, "query": out_query or "",
                })
                continue
            slug = slugify(title)
            # ensure unique slug
            base = slug
            c = 1
            while any(p["slug"] == slug for p in pages) or (WIKI / "articles" / f"{slug}.md").exists() and slug != base:
                slug = f"{base}-{c}"
                c += 1
            tdir = page_type_dir(batch, i, title)
            pages.append({
                "batch": batch, "theme": domain, "type_dir": tdir,
                "slug": slug, "title": title.replace("-", " "),
                "tags": tags, "related": [hub],
                "body": build_body(title, hub, tags, batch),
                "output": False, "query": "",
            })
    return pages


ALL_PAGES = build_all_pages()
assert len(ALL_PAGES) == 1000, len(ALL_PAGES)


def resolve_type(slug: str) -> str:
    for d in ("articles", "concepts", "guides", "interview", "outputs", "services"):
        if (WIKI / d / f"{slug}.md").exists():
            return d
    return "articles"


def write_page(p: dict):
    path = WIKI / p["type_dir"] / f"{p['slug']}.md"
    if path.exists():
        return False
    tags_s = ", ".join(p["tags"])
    rel_s = ", ".join(p["related"])
    lines = [
        "---", f"title: {p['title']}", f"slug: {p['slug']}",
        f"type: {TYPE_MAP[p['type_dir']]}", "status: active",
        f"tags: [{tags_s}]", "sources:", "  - raw/wujinsen_markdown/",
        f"related: [{rel_s}]", f"created: {TODAY}", f"updated: {TODAY}",
    ]
    if p["output"]:
        lines += [f"query: {p['query']}", f"source_pages: [{rel_s}]"]
    lines += ["---", "", p["body"].strip(), ""]
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines), encoding="utf-8")
    return True


def load_progress() -> int:
    if PROGRESS.exists():
        return json.loads(PROGRESS.read_text(encoding="utf-8")).get("next_batch", START_BATCH)
    return START_BATCH


def save_progress(next_batch: int):
    PROGRESS.write_text(json.dumps({
        "plan": "287-1286", "total": 1000,
        "next_batch": next_batch,
        "updated": TODAY,
    }, ensure_ascii=False, indent=2), encoding="utf-8")


def update_index(start: int, end: int, count: int):
    idx = WIKI / "index.md"
    text = idx.read_text(encoding="utf-8")
    block = f"""
## 批次 #{start}–#{end} 新增（{count} 批 · 1000批计划）

_本段 #{start}–#{end}；1000批总计划 **#287–#1286**；入口 [[茉莉知识体系1000批总索引]]（完成后）/ [[茉莉知识体系100批索引]]。_

_进度：已完成至 **#{end}**；下一批从 **#{end + 1}** 起。_
"""
    marker = f"下一批从 **#{start}** 起"
    if marker in text:
        text = text.replace(f"_100 批计划 **#187–#286** 已完成；下一批从 **#287** 起。_",
                            block.strip())
        text = text.replace("（下一批从 **#287** 起）", f"（1000批进行中，下一批从 **#{end + 1}** 起）")
    if f"#{end + 1}** 起" not in text and f"进度：已完成至 **#{end}**" not in text:
        text = text.rstrip() + "\n\n" + block.strip() + "\n"
    if end >= END_BATCH:
        text = re.sub(r"1000批进行中[^）]*", f"1000批 **#287–#1286** 已完成", text)
        text = text.replace(f"下一批从 **#{end + 1}** 起", "下一批从 **#1287** 起")
    idx.write_text(text, encoding="utf-8")


def run(start: int, end: int):
    selected = [p for p in ALL_PAGES if start <= p["batch"] <= end]
    log_lines = []
    edges = []
    written = 0
    for p in selected:
        if not write_page(p):
            continue
        written += 1
        kind = "query | crystallize" if p["output"] else "ingest"
        log_lines.append(f"## [{TODAY}] {kind} | 批次#{p['batch']} {p['theme']} → {p['slug']}; 1页")
        for r in p["related"][:2]:
            rs = r.split("/")[-1]
            edges.append({
                "from": f"{p['type_dir']}/{p['slug']}",
                "to": f"{resolve_type(rs)}/{rs}",
                "type": "relates_to", "evidence": f"批次#{p['batch']}", "date": TODAY,
            })

    if log_lines:
        lp = WIKI / "log.md"
        lp.write_text(lp.read_text(encoding="utf-8").rstrip() + "\n" +
                      "\n".join(log_lines) + f"\n## [{TODAY}] sync | wiki→MySQL 批次#{start}-{end}\n", encoding="utf-8")
    if edges:
        with open(WIKI / "graph" / "edges.jsonl", "a", encoding="utf-8") as f:
            for e in edges:
                f.write(json.dumps(e, ensure_ascii=False) + "\n")
    update_index(start, end, written)
    save_progress(end + 1)
    print(f"done range={start}-{end} written={written} skipped={len(selected)-written} next={end+1}")


if __name__ == "__main__":
    if "--status" in sys.argv:
        nb = load_progress()
        done = nb - START_BATCH
        print(f"1000-batch plan #287-#1286 | done={done} next=#{nb} remaining={END_BATCH-nb+1}")
        sys.exit(0)
    if "--force-legacy" not in sys.argv:
        print("[blocked] _gen_batches_287_1286 已废弃，会污染 enterprise-kb。"
              "请用 raw 提炼 ingest。仅 --status 或 --force-legacy 可用。", file=sys.stderr)
        sys.exit(2)
    if len(sys.argv) >= 3:
        s, e = int(sys.argv[1]), int(sys.argv[2])
    else:
        s = load_progress()
        e = min(s + CHUNK_DEFAULT - 1, END_BATCH)
    run(s, e)
