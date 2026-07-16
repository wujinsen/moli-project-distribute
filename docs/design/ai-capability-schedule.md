# 茉莉 AI 能力升级 · 排期（Sprint 周计划）

> **状态**：plan · 2026-07-17
> **PRD**：[`../product/ai-capability-prd.md`](../product/ai-capability-prd.md)
> **路线总纲**：[`ai-capability-roadmap.md`](ai-capability-roadmap.md)
> **技术方案**：检索 [`kb-hybrid-retrieval.md`](kb-hybrid-retrieval.md) · ChatBI [`bi-chatbi-nl2sql.md`](bi-chatbi-nl2sql.md)

---

## 1. 总体节奏

- **起始**：2026-07-20（周一） · **口径**：全职折算，单人推进，每周一个可交付增量。
- **四波 = 四版本**，每波 4 周，共 16 周（含缓冲）；时间紧可按 [`ai-capability-roadmap.md`](ai-capability-roadmap.md) 压缩为「评测先行的最小组合 AI-1→AI-2→AI-4」。
- **每周五**：跑一次评测 + 更新指标 + 提交；**每波末**：回填任务状态、更新 README 指标区、对应 wiki-moli enrich + sync。

| 波次 | 版本 | 周次 | 日期窗口 | 任务 |
|------|------|------|----------|------|
| 第 1 波 | **v1.1 语义检索** | W1–W4 | 2026-07-20 ~ 08-14 | AI-1 · AI-2 · AI-3 |
| 第 2 波 | **v1.2 ChatBI** | W5–W8 | 2026-08-17 ~ 09-11 | AI-4 |
| 第 3 波 | **v1.3 差异化检索** | W9–W12 | 2026-09-14 ~ 10-09 | AI-5 · AI-6 · AI-7 |
| 第 4 波 | **v2.0 可信与前沿** | W13–W16 | 2026-10-12 ~ 11-06 | AI-8 · AI-9 · AI-10 |

---

## 2. 第 1 波 · v1.1 语义检索（W1–W4）

| 周 | 任务 | 交付物 | 出口标准 |
|----|------|--------|----------|
| **W1** | AI-1 评测扩容 | golden ≥50 题（分层标注 + 拒答负样本）；`eval_ask.py` 支持 `difficulty`/`expect_answerable`；**扩容基线报告** | 基线报告归档 `kb/eval/reports/` |
| **W2** | AI-2 向量检索 | `kb-retrieval` sidecar（`/embed` `/search`）+ Chroma；Sync 后增量建索引 | 向量单路召回可跑通，脏 query 有召回 |
| **W3** | AI-2 Hybrid+Rerank | `KbSearchProperties` 三档开关；RRF 融合 + bge-reranker；ngram 档零回退验证 | 三档可切换；三档对比表产出 |
| **W4** | AI-3 看板+门禁 | `kb_eval_run` 落库；Dashboard「检索质量趋势」；CI `--gate-at-k` 门禁 | 看板可见曲线；CI 能拦人为回退 |

**v1.1 里程碑（M1，~08-14）**：hybrid 相对 ngram 完整集 hit@3 不回退、语义子集显著提升；对比数据写入 README。

---

## 3. 第 2 波 · v1.2 ChatBI（W5–W8）

| 周 | 任务 | 交付物 | 出口标准 |
|----|------|--------|----------|
| **W5** | 骨架 + 安全底座 | bi-server 只读数据源 + 独立只读账号；SQL AST 白名单校验（JSqlParser）；`bi_chat_trace` 表 | 危险 SQL 单测 100% 拦截 |
| **W6** | Agent MVP | `bi-agent` sidecar（schema 检索 + NL→SQL + 自纠错）；Java 只读执行 | Gradio 输入 → 出结果表 |
| **W7** | 图表 + 解读 + API | 结果图表建议 + 自然语言解读；`/bi/chat/ask` SSE；`bi:chat:*` 鉴权 | 端到端走通网关 `/BiServer/**` |
| **W8** | 评测 + 打磨 | `nl2sql_testset.jsonl` ≥30 题；执行/拒答正确率 | 执行正确率 ≥80%、拦截 100% |

**v1.2 里程碑（M2，~09-11）**：ChatBI 端到端 Demo，接 order/user 真实只读库。

---

## 4. 第 3 波 · v1.3 差异化检索（W9–W12）

| 周 | 任务 | 交付物 | 出口标准 |
|----|------|--------|----------|
| **W9** | AI-5 GraphRAG | 检索沿 `kb_relation` 扩 N 跳，按边类型加权并入候选 | multi-hop 子集对比提升 |
| **W10** | AI-6 MCP Server | `kb.search`/`kb.ask`/`kb.graph` MCP 工具，复用 REST + 鉴权 | Cursor 中 `@moli-kb` 问答演示 |
| **W11** | AI-7 Agentic RAG（上） | 查询改写/拆解 + 多轮检索编排 | 复杂问题多跳检索跑通 |
| **W12** | AI-7 Agentic RAG（下） | 答案自检 + 引用校验 + trace；单轮/Agentic 开关 | 引用覆盖率提升、延迟 <2× |

**v1.3 里程碑（M3，~10-09）**：多跳/脏 query 子集指标可量化提升；MCP 演示可复现。

---

## 5. 第 4 波 · v2.0 可信与前沿（W13–W16）

| 周 | 任务 | 交付物 | 出口标准 |
|----|------|--------|----------|
| **W13** | AI-8 网关路由 | `KbLlmClient` 多 provider 路由 + 失败降级 | 路由/降级可用 |
| **W14** | AI-8 语义缓存+看板 | Redis 语义缓存；成本/命中率看板 | 缓存命中率、省钱数据可见 |
| **W15** | AI-9 Guardrails | grounding 校验 + 注入检测 + PII 脱敏 | 幻觉率/引用覆盖率前后对比 |
| **W16** | AI-10 DeepResearch | Planner/Retriever/Writer/Reviewer；产物走 Ingest 回写 | 带引用报告回写 `wiki-moli/develop/outputs/` |

**v2.0 里程碑（M4，~11-06）**：成本看板上线；引用覆盖率对比产出；DeepResearch 报告可复现。

---

## 6. 依赖与关键路径

```
AI-1（评测基线）──→ AI-2（检索）──→ AI-3（看板门禁）
                        │
                        ├──→ AI-5（GraphRAG）
                        └──→ AI-7（Agentic RAG）──→ AI-9（Guardrails）
AI-4（ChatBI）  独立并行（依赖 AI-2 检索思路，非硬阻塞）
AI-6（MCP）     独立并行
AI-8（网关）    独立并行
AI-10（DeepResearch）依赖 AI-2/AI-5
```

- **关键路径**：AI-1 → AI-2 → AI-3（尺子先行，后续检索改动都靠它回归）。
- AI-4 可与第 1 波并行启动（若资源允许），但排在第 2 波以保证 AI-2 检索经验先沉淀。

---

## 7. 风险与缓冲

| 风险 | 影响 | 缓解 |
|------|------|------|
| embedding/rerank 模型部署耗时 | AI-2 延期 | W2 预留半周做模型选型与本地部署验证；失败回退纯向量无 rerank |
| golden 扩容质量不足 | 对比数据不可信 | 优先用 `kb_qa_log` 真实问；负样本人工评审 |
| ChatBI 安全边界 | 数据泄露风险 | 只读账号 + 白名单为硬门槛，W5 先做安全再做功能 |
| sidecar 稳定性 | 问答/BI 抖动 | Java 侧超时降级；sidecar 无状态可重启 |
| 单人推进节奏 | 整体顺延 | 每波末评估，必要时砍 P2（AI-8/9/10）保 P0/P1 |

---

## 8. 进度看板（滚动更新）

| 任务 | 计划周 | 建议模型 | 状态 | 实际完成 | 备注 |
|------|--------|----------|------|----------|------|
| AI-1 | W1 | 🟢 Composer | 🔜 | — | 标准由 Opus 定 |
| AI-2 | W2–W3 | 🔵 混合 | 🔜 | — | 融合/精排 Opus，脚手架 Composer |
| AI-3 | W4 | 🟢 Composer | 🔜 | — | — |
| AI-4 | W5–W8 | 🟣 Opus 主导 | 🔵 | — | 安全+Agent Opus，壳 Composer |
| AI-5 | W9 | 🔵 混合 | 🔵 | — | 加权算法 Opus |
| AI-6 | W10 | 🟢 Composer | 🔵 | — | — |
| AI-7 | W11–W12 | 🟣 Opus 主导 | 🔵 | — | prompt/自检 Opus |
| AI-8 | W13–W14 | 🔵 混合 | ⚪ | — | 路由策略 Opus |
| AI-9 | W15 | 🟣 Opus 主导 | ⚪ | — | 安全关键 |
| AI-10 | W16 | 🟣 Opus 主导 | ⚪ | — | 多 Agent 编排 |

> 图例：🔜 即将开工 · 🔵 排队 · ⚪ 远期 · ✅ 完成。完成后回填并同步 `ai-capability-roadmap.md` §3 状态列。
> 建议模型图例：🟣 Opus 主导 · 🔵 混合（Opus 定契约、Composer 落地）· 🟢 Composer 主导。分工细则见 §9。

---

## 9. 模型分工（Opus vs Composer）与双对话协作

判断口径：**Opus-4.8 这类强推理模型**负责「架构决策 / 安全关键 / Agent 编排 / prompt 与评测方法论 / 模糊需求」；**Composer 这类快速编码模型**负责「成熟模式的样板、CRUD、配置类、接线、CI、测试录入、文档」。多数任务是混合：Opus 出契约与关键逻辑，Composer 铺量落地。

### 9.1 逐任务分工

| 任务 | Opus 做（设计/安全/推理） | Composer 做（样板/接线） | 主导 |
|------|---------------------------|---------------------------|------|
| **AI-1** | 负样本/脏 query 分层、难度标签体系、拒答判定标准 | `golden.jsonl` 录入、`eval_ask.py` 分层统计、`kb_qa_log` 导出脚本 | 🟢 Composer |
| **AI-2** | 双路召回架构、RRF 融合权重、精排阈值、回退策略、sidecar 接口契约 | `kb-retrieval` FastAPI 脚手架、Chroma 索引、`KbSearchProperties`、Java HTTP client、离线索引任务 | 🔵 混合 |
| **AI-3** | 门禁基线阈值策略 | `kb_eval_run` 表+mapper+落库、`KbOpsService` 卡片、CI yaml | 🟢 Composer |
| **AI-4** | **SQL 白名单/AST 校验/注入防护**、NL2SQL prompt、LangGraph 工作流、自纠错重试、schema 检索与解读 prompt | `bi-server` Java 壳（Controller/Service/鉴权仿 order）、`bi_chat_trace` 审计、只读数据源+缓存、图表序列化、`bi-agent` 脚手架、`nl2sql_testset.jsonl` 录入 | 🟣 Opus 主导 |
| **AI-5** | 沿边扩跳加权/剪枝算法、多跳候选融合 | 复用 `KbWikiGraphService` 遍历、候选合并、配置开关 | 🔵 混合 |
| **AI-6** | 工具 schema 语义 | MCP server 脚手架、REST 包成 `kb.search/ask/graph`、token 鉴权接线 | 🟢 Composer |
| **AI-7** | 编排层设计、query 改写/拆解 prompt、答案自检（陈述↔引用对齐）、回补检索决策 | 状态机代码、trace 表、开关、各步接线 | 🟣 Opus 主导 |
| **AI-8** | 多 provider 路由策略（成本/延迟/降级）、语义缓存相似度阈值与失效 | provider 适配器、Redis 缓存读写、成本看板卡片 | 🔵 混合 |
| **AI-9** | grounding 校验逻辑、注入检测规则、PII 识别策略、置信度标注 | 校验管道接线、PII 正则/脱敏工具、前后指标统计 | 🟣 Opus 主导 |
| **AI-10** | Planner/Retriever/Writer/Reviewer 多 Agent 编排、大纲生成、审校与回写质量判定 | 各 Agent 骨架、接 Ingest 回写链路、报告模板渲染 | 🟣 Opus 主导 |

### 9.2 红线

- **安全（AI-4 SQL 白名单、AI-9 注入/PII）与 Agent 编排/prompt（AI-4/7/10）一律 Opus + 人审**，不交快模型顺手写。
- 样板量最大的三块（建表+mapper+VO、看板卡片、CI/文档）尽量给 Composer。

### 9.3 双对话协作协议（一个窗口 Opus、一个窗口 Composer）

核心：**两个对话不共享上下文，靠一个"契约文件"传递**。Opus 先产出契约落到 `docs/design/`，Composer 只读这个文件施工。

```
Opus 窗口（架构/安全）  ──产出──►  docs/design/<任务>-contract.md  ──输入──►  Composer 窗口（施工）
   ①接口签名/DTO                （接口契约 + 关键算法/prompt          ②按契约实现壳/表/接线/测试
   ②关键算法/prompt              + 安全约束清单，不含样板）              ③不改动安全校验逻辑本身
   ③安全约束清单                                                      ④回写实现进度到契约文件末尾
```

**Opus 窗口开场（复制）**：

```
你是本任务的架构与安全负责人。基于 docs/design/ai-capability-roadmap.md 与对应技术方案，
为 <AI-N 任务名> 产出「施工契约」，写入 docs/design/<任务>-contract.md：
① REST/DTO 接口签名与错误码
② 关键算法 / LangGraph 节点划分 / prompt 草案
③ 安全与不变量约束清单（如 SQL 白名单规则、只读边界）
④ 验收用例要点
只出设计与关键逻辑，不要写样板 CRUD / 建表 / 配置类——那些留给 Composer。
```

**Composer 窗口开场（复制）**：

```
严格按 docs/design/<任务>-contract.md 施工 <AI-N 任务名>：
- 实现契约里的接口签名、DTO、建表+mapper+VO、配置类、HTTP 接线、单元/集成测试
- 鉴权与代码风格仿 moli-order / moli-knowledge 现有模式
- 不得改动契约中的安全校验逻辑与接口签名；如需调整，先回到 Opus 窗口改契约
完成后在契约文件末尾追加「实现清单 + 未决问题」。
```

**回环**：Composer 施工中发现契约有歧义/漏洞 → 不自行拍板安全相关决定 → 回 Opus 窗口补契约 → Composer 再继续。每个任务出口前，Opus 窗口再做一轮 review（尤其 AI-4/7/9/10）。
