# 茉莉 AI 能力升级 · 产品需求文档（PRD）

> **状态**：active · 2026-07-17（初稿，规划确认，未开工）
> **技术路线总纲**：[`docs/design/ai-capability-roadmap.md`](../design/ai-capability-roadmap.md)
> **技术方案**：检索 [`docs/design/kb-hybrid-retrieval.md`](../design/kb-hybrid-retrieval.md) · ChatBI [`docs/design/bi-chatbi-nl2sql.md`](../design/bi-chatbi-nl2sql.md)
> **排期**：[`docs/design/ai-capability-schedule.md`](../design/ai-capability-schedule.md)
> **上级索引**：[`knowledge-module-requirements.md`](knowledge-module-requirements.md) · [`moli-v1-release-scope.md`](moli-v1-release-scope.md)

---

## 1. 背景与定位

### 1.1 问题

茉莉平台的 RAG 基线（chunk 切段 → ngram 全文召回 → LLM 生成带引用答案 → golden 评测）已闭环并上线，
但对照 2026 年企业级 LLM 应用形态，存在三处结构性差距：

| 痛点 | 影响 | 用户感知 |
|------|------|----------|
| 检索只有 MySQL ngram 全文，无语义/向量召回 | 换个说法、口语化、错别字的问题召回不稳 | "明明知识库里有，却问不出来" |
| `/kb/ask` 单轮 retrieve→answer | 跨页、多跳、需要拆解的问题答不全 | "复杂问题只能拆成好几次问" |
| `moli-ai-server`（服务名 bi-server）是占位骨架 | 平台空有 order/user 业务库，却不能用自然语言查 | "看数要么等报表、要么找开发写 SQL" |

### 1.2 产品定义

**AI 能力升级** 面向 **知识库使用者 / 业务分析人员 / 平台管理员 / Agent 开发者**，交付四类价值：

1. **问得准** — 语义 + 关键词混合检索 + 精排，让"换个说法也能命中"。
2. **问得深** — Agentic RAG 与 GraphRAG，支持复杂、多跳、跨页问题。
3. **能问数** — ChatBI / NL2SQL，用自然语言查业务库并出图表解读。
4. **可信可控** — 评测回归看板、引用校验、成本/缓存可观测。

### 1.3 非目标（本轮不做）

- 不做模型训练 / 微调（仅调用 OpenAI 兼容 API）。
- 不做多模态（图像/语音）问答。
- ChatBI 不做写操作、不做交易下单、不做跨库联邦查询（只读单库聚合分析）。
- 不引入重型独立检索服务（ES 集群等）；向量先用轻量方案（Chroma / pgvector 级别）。

---

## 2. 用户与场景

| 角色 | 场景 | 期望 |
|------|------|------|
| **知识库使用者** | 用口语、换说法提问 | 语义命中，不必背关键词 |
| **知识库使用者** | 问跨多页、需要推理的问题 | 系统自动多跳检索并综合作答 |
| **业务分析人员** | "上月各渠道订单量 Top5？" | 自然语言 → 图表 + 数据 + 解读，无需写 SQL |
| **平台管理员** | 关注检索质量与 LLM 成本 | 看板可见指标趋势、成本、缓存命中率 |
| **Agent 开发者** | 在 Cursor / Claude 里查 moli 知识 | 通过 MCP `@moli-kb` 直接调用 |
| **CI** | 防止检索改动导致质量回退 | 评测门禁（hit@k 低于基线则拦截） |

---

## 3. 产品路线（版本节奏）

> 四个版本对应技术路线的四波；每版都有**可演示能力 + 可量化指标**。

| 版本 | 主题 | 交付能力（对应任务） | 关键指标 | 目标窗口 |
|------|------|----------------------|----------|----------|
| **v1.1** | 语义检索 | 评测扩容（AI-1）· 向量+Hybrid+Rerank（AI-2）· Eval 回归看板（AI-3） | golden ≥50 题；hybrid 相对 ngram hit@3 不回退、语义子集显著提升；CI 门禁生效 | 第 1 波 |
| **v1.2** | ChatBI | NL2SQL Agent（AI-4），接 order/user 只读库 | NL2SQL 执行正确率 ≥80%；危险 SQL 拦截 100% | 第 2 波 |
| **v1.3** | 差异化检索 | GraphRAG（AI-5）· MCP Server（AI-6）· Agentic RAG（AI-7） | 多跳/脏 query 子集指标可量化提升；MCP 演示可复现 | 第 3 波 |
| **v2.0** | 可信与前沿 | LLM 网关路由/语义缓存（AI-8）· Guardrails（AI-9）· Multi-Agent DeepResearch（AI-10） | 成本看板上线；引用覆盖率提升；DeepResearch 报告回写 kb | 第 4 波 |

具体周排期见 [`ai-capability-schedule.md`](../design/ai-capability-schedule.md)。

---

## 4. 功能需求与优先级

> ID 与技术路线 [`ai-capability-roadmap.md`](../design/ai-capability-roadmap.md) §3 任务总表一致。

### P0 — v1.1 语义检索（先做，立刻产出对比数据）

| ID | 需求 | 用户价值 | 验收要点 |
|----|------|----------|----------|
| **AI-1** | golden 评测集扩容至 50~100 题（含口语化/错别字/跨语种脏 query + 5~10 条应拒答负样本） | 让"问得准"可被量化 | golden ≥50 题、按空间与难度标签分层；跑出扩容后基线报告 |
| **AI-2** | 向量检索 + Hybrid Search + Rerank | 换说法也能命中 | 三档（ngram/hybrid/hybrid+rerank）可配置切换；同一 golden 上 hybrid 相对 ngram hit@3 不回退，语义子集显著提升 |
| **AI-3** | Eval 回归看板 + CI 门禁 | 检索改动不再"凭感觉"、防回退 | 评测报告落库、`KbOpsService` 可见趋势；CI `--gate-at-k 3 --min-hit <基线>` 拦截回退 |

### P0 — v1.2 ChatBI（求职主打，业务落地）

| ID | 需求 | 用户价值 | 验收要点 |
|----|------|----------|----------|
| **AI-4** | ChatBI / NL2SQL Agent（bi-server 0→1） | 业务自助查数，不等报表、不找开发 | 端到端 Demo 走通 `/BiServer/**`；NL2SQL 测试集执行正确率 ≥80%；**只读账号 + SQL 白名单，危险 SQL 拦截 100%**；结果含图表 + 自然语言解读 |

### P1 — v1.3 差异化检索

| ID | 需求 | 用户价值 | 验收要点 |
|----|------|----------|----------|
| **AI-5** | GraphRAG（沿 `kb_relation` 扩 N 跳） | 多跳/跨页问题答得全 | multi-hop 子集上 hybrid+graph 相对 hybrid hit@3 可量化提升 |
| **AI-6** | 知识库 MCP Server（`kb.search`/`kb.ask`/`kb.graph`） | 在 Cursor/Claude 直接查 moli 知识 | MCP 客户端完成一次带引用问答演示；工具走现有鉴权 |
| **AI-7** | Agentic RAG（查询改写/多跳/自检/引用校验） | 复杂问题自动拆解、自我纠偏 | dirty+multi-hop 子集单轮 vs Agentic 对比；引用覆盖率提升，平均延迟增幅 <2× |

### P2 — v2.0 可信与前沿

| ID | 需求 | 用户价值 | 验收要点 |
|----|------|----------|----------|
| **AI-8** | LLM 网关升级（多 provider 路由 + 语义缓存 + 成本看板） | 成本可控、稳定性可观测 | 路由/降级可用；缓存命中率与省钱数据可见 |
| **AI-9** | Guardrails（grounding 校验 + 注入检测 + PII 脱敏） | 答案可信、输入安全 | 幻觉率/引用覆盖率前后对比；注入与 PII 用例拦截 |
| **AI-10** | Multi-Agent DeepResearch | 一键生成带引用调研报告 | 报告可复现且带引用；产物走 Ingest 回写 `wiki-moli/develop/outputs/` |

---

## 5. 与现有产品线的关系

| 产品线 | 关系 |
|--------|------|
| **知识库问答（/kb/ask）** | AI-2/5/7 升级其召回与编排；对外 API 兼容，新增能力以开关控制 |
| **知识库内容管道运维（KBOPS）** | AI-3 评测看板并入 `KbOpsService`；与 Sync/Lint 看板同域 |
| **BI 模块（v1 骨架）** | AI-4 把占位 bi-server 升级为 ChatBI，路由 `/BiServer/**` 不变 |
| **平台 LLM 配置（T19）** | AI-4/8 复用"DB 优先 + yaml 兜底"的 LLM 配置与调用日志模式 |
| **Ingest 工作台** | AI-10 报告产物走现有 Ingest → wiki → Sync 链路 |

**分工铁律**：知识只在 `kb/wiki*` 产生（AI-10 报告经 Ingest 回写，不直写 DB）；ChatBI 只读业务库，永不写。

---

## 6. 权限（新增/复用）

| 权限码 | 用途 | 备注 |
|--------|------|------|
| `bi:chat:query` | ChatBI 提问 | AI-4 新增；绑定只读数据域 |
| `bi:chat:trace` | 查看 ChatBI 决策/SQL 链路 | AI-4 新增 |
| `kb:eval:run` | 触发/查看评测 | AI-3；管理员 |
| `kb:ask` | 知识库问答 | 已有，AI-2/5/7 沿用 |
| 空间 ACL | 检索/图谱结果按空间过滤 | 已有，MCP（AI-6）继承 |

---

## 7. 验收（产品级门槛）

### 7.1 v1.1 发布门槛

- [ ] golden ≥50 题且分层标注，扩容后基线报告归档
- [ ] 检索三档可切换；hybrid 相对 ngram 在完整 golden 上 hit@3 不回退
- [ ] 语义改写子集 hit@3 显著提升（提升幅度写入 README 指标区）
- [ ] CI 评测门禁可拦截人为制造的回退

### 7.2 v1.2 发布门槛

- [ ] Gradio/Web 端到端 Demo：自然语言 → SQL → 结果 + 图表 + 解读
- [ ] NL2SQL 测试集（≥30 题）执行正确率 ≥80%
- [ ] 危险 SQL（DML/DDL/越权/超量）100% 拦截，审计日志可查
- [ ] 鉴权走 Shiro（session 由 user-center 签发），与 order 一致

### 7.3 v1.3 / v2.0 发布门槛

- [ ] AI-5：multi-hop 子集 hybrid+graph 相对 hybrid 有正向提升
- [ ] AI-6：Cursor 中 MCP 问答演示可复现
- [ ] AI-7：Agentic 相对单轮引用覆盖率提升、延迟增幅 <2×
- [ ] AI-8/9：成本看板与引用覆盖率对比产出
- [ ] AI-10：DeepResearch 报告带引用并回写 kb

---

## 8. 文档地图

| 类型 | 路径 |
|------|------|
| **本 PRD** | `docs/product/ai-capability-prd.md` |
| 技术路线总纲 | `docs/design/ai-capability-roadmap.md` |
| 技术方案 · 混合检索 | `docs/design/kb-hybrid-retrieval.md` |
| 技术方案 · ChatBI | `docs/design/bi-chatbi-nl2sql.md` |
| 排期（Sprint 周计划） | `docs/design/ai-capability-schedule.md` |
| 架构图 | `docs/diagrams/moli-kb-hybrid-retrieval.drawio` · `moli-bi-chatbi-flow.drawio` · `moli-ai-capability-roadmap.drawio` |
| HTTP 契约 | `docs/api/KNOWLEDGE_API.md`（AI-2/3/5/7）· `docs/api/bi-api.md`（AI-4） |
| 评测说明 | `moli-knowledge/kb/eval/README.md` |

---

## 9. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-17 | 初稿：AI-1~AI-10 需求、四版本产品路线、验收门槛；配套技术方案与排期 |
