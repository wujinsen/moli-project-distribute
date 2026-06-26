# kb · 茉莉企业知识库（LLM-Wiki）

一个由 AI Agent 维护的、持久互链的 markdown 知识库。
范式：Karpathy「LLM-Wiki」为主、AutoSci 为辅。

## 它是什么

不是「上传文档 + 每次提问临时检索」的朴素 RAG，而是：

> 投喂源 → Agent 读取并抽取 → 写进结构化 wiki 页、建立交叉引用 → 知识持续沉淀保鲜。

人负责投喂源、提问、定方向；Agent 负责总结、交叉引用、归档等所有琐活。

## 怎么用（在 Cursor 里直接对我说）

- **吸收**：把文档放进 `raw/` 后说「ingest raw/docs/xxx.md」→ 我读它、写/更新 wiki 页。
- **提问**：「query：订单服务怎么本地启动？」→ 我读 index 选页、带 `[[页]]` 引用作答；好答案可回写 `wiki/outputs/`。
- **体检**：「lint」→ 我扫全库找矛盾/孤儿页/断链/缺来源，给修复建议。
- **Enrich**：已有 slug 追加章节 → `python kb/tools/enrich.py` 或 Web Wiki 编辑页 **Enrich 治理**（见 [[Wiki在线编辑与AI协助改稿]] §2.2）。

规则细节见 [AGENTS.md](AGENTS.md)（Agent 工作前必读）。
功能规划见 [ROADMAP.md](ROADMAP.md)（企业级知识库要做哪些功能、做到哪了）。

## 结构

- `AGENTS.md` —— 知识库契约（结构、页面格式、三操作工作流）。
- `raw/` —— 只读源头（docs / articles / interview）。
- `wiki/` —— Agent 维护的知识页 + `index.md` 目录 + `log.md` 时间线 + `graph/edges.jsonl` 关系边。

## 当前进度

- 已初始化骨架与契约。
- 已示范 ingest 顶层 `README.zh-CN.md` → 5 个服务/指导/概念页（见 `wiki/index.md`）。
- 下一步：批量 ingest `docs/zh-CN/*` 与各微服务文档，完善 P0 用户指导手册场景。
