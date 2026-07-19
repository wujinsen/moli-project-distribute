# moli-project-distribute · 项目速读

> 作者：吴锦森 ｜ 开源协议：Apache 2.0 ｜ GitHub：`<仓库链接>` ｜ 在线 Demo：`<部署后填>`
> 一句话：一套「**Spring Cloud 微服务 + 企业级 LLM 知识库**」的开源系统，把大模型能力落地为**生产级 AI 应用**。

---

## 为什么值得看（差异化）


![知识库双轨架构](docs/diagrams/png/moli-kb-architecture.png)


---

## 核心能力

| 能力 | 落地情况 | 对应岗位关键词 |
|------|----------|----------------|
| 🔎 RAG 检索问答 | Chunk 切段 → 检索（`/kb/ask`）→ **LLM 生成式带引用回答**；封装 OpenAI 兼容客户端（`KbLlmClient`：DB 配置优先 + yaml 兜底 + 调用日志） | RAG、大模型应用、Prompt |
| 📊 检索评测 | `golden.jsonl` → **hit@k / MRR / coverage**；支持检索式 vs 生成式对比（`eval_ask.py`） | RAG 评测 / 可观测性 |
| 🧠 LLM-Wiki 知识治理 | Karpathy「LLM-Wiki」范式：Ingest / Lint / Enrich，知识「编译一次、持续保鲜」；kb→DB 单向增量幂等同步 | 知识工程 |
| 🤝 Agentic Coding | 多层 `AGENTS.md` 规则 + 自建 Cursor Skills（架构图 / SQL 迁移 / KB Ingest），沉淀可复用 AI 研发工作流 | AI 编程 / 研发提效 |
| 🏗️ 生产级后端 | Spring Cloud Alibaba（Nacos / Dubbo / Sentinel / Gateway）+ Shiro 分布式鉴权 + MyBatis-Plus + GitHub Actions CI + 压测 | 分布式 / 微服务 / 高并发 |

---

## 技术栈

- **AI / RAG**：LLM（OpenAI 兼容）、Chunking、检索问答、hit@k/MRR 评测、LLM-Wiki
- **后端**：Java 8、Spring Boot、Spring Cloud、Spring Cloud Alibaba（Nacos/Dubbo/Sentinel/Gateway）、Shiro
- **数据 / 存储**：MySQL、Redis、MyBatis-Plus、MinIO；向量检索（pgvector/Chroma）规划中
- **工具链**：Python（KB tooling）、Cursor（AGENTS/Skills/Rules）、GitHub Actions、draw.io
- **可观测性**：ELK / SkyWalking / Prometheus（方案）

---

## 关键指标

> 由 `python moli-knowledge/kb/tools/fill_eval_metrics.py --run` 自动跑评测并回填。

<!-- KB_METRICS_TABLE:START -->
| 指标 | 数值 |
|------|------|
| hit@1 | `66.7%` |
| hit@3 | `100.0%` |
| hit@5 | `100.0%` |
| hit@8 | `100.0%` |
| MRR | `0.833` |
| coverage | `100.0%` |
| 平均响应 | `0.47s` |
| 知识页 / 测试题量 | `700` 页 / `12` 题 |
| 评测时间 | `2026-07-16` |
<!-- KB_METRICS_TABLE:END -->

---


---

从 **架构设计 → 后端开发 → LLM 应用 → RAG 评测 → Agentic Coding 工作流** 全链路独立完成，并输出中/英/日三语文档与 draw.io 架构图。

---


1. 为什么用「LLM-Wiki」而不是朴素 RAG？（答：投喂即治理，去重/提炼/矛盾检测有抓手，元数据预过滤先行、向量库按需叠加）
2. 检索质量怎么衡量？（答：golden.jsonl + hit@k / MRR / coverage，检索式 vs 生成式对比）
3. LLM 配置如何管理与降级？（答：DB 优先 + yaml 兜底，`KbLlmClient` 统一调用 + 日志）
4. kb→DB 如何保证一致性？（答：单向、增量、幂等；content_hash 比对；`kb_sync_log` 审计）
5. Agentic Coding 如何提效？（答：AGENTS 分层规则 + Skills 固化多步流程，减少重复沟通与返工）

---

## 相关文档

- 完整说明：[README.md](README.md)
- 知识库模块：[moli-knowledge/README.md](moli-knowledge/README.md)
- 功能规划：[moli-knowledge/kb/ROADMAP.md](moli-knowledge/kb/ROADMAP.md)
- 架构图集：[docs/diagrams/README.md](docs/diagrams/README.md)
