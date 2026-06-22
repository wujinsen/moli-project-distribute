# 企业知识库 · 待办任务清单（可并行开工）

> 更新：2026-06-22
> 用途：每个任务**自包含、文件边界清晰**，可在不同对话框/工作区并行开工，尽量不互相冲突。
> 范式与分工见 [`kb/ROADMAP.md`](kb/ROADMAP.md)；表结构见 [`../docs/sql/KNOWLEDGE_SCHEMA.md`](../docs/sql/KNOWLEDGE_SCHEMA.md)。
>
> 开一个新对话时：**先贴该任务的「开工提示词」**，它会让那个对话先读必要文档再动手。

---

## 现状速览

| 模块 | 已完成 | 未完成 |
|------|--------|--------|
| 表结构 | ✅ 14 张表 SQL + 设计文档；**14 个 entity/mapper 全部就绪**（T1 完成） | — |
| 同步 | ✅ `kb/tools/sync_to_db.py`（dry-run 通过） | 真正写库验证、挂自动触发 |
| Java API | ✅ CRUD、`/kb/graph`、`/kb/lint`、**`/kb/ask`(T2)**、**`/kb/index`+`/kb/page`(T3)** | 附件上传(T4)、关系/体检落库(T5)、ACL(T9)、全文检索 |
| kb 知识 | ✅ 17 页示范 + 关系边 | 批量 ingest、Query 闭环、全库 lint、面试题系列 |
| 前端 meiling-ui | ❌ 无知识库页面 | 浏览/Query/图谱/体检全套 |

---

## 并行分组建议（避免文件冲突）

```
第一波（可同时开 4 个对话，互不碰同一文件）：
  T1  新表 entity/mapper        ← 纯新增文件
  T6  前端知识库页面            ← 另一个工作区(meiling-ui)，完全独立
  T7  kb 批量 ingest            ← 只动 kb/ 下 markdown
  T8  前端对接文档              ← 只动 meiling-ui/docs

第二波（依赖 T1 完成后再开）：
  T2  Query /kb/ask
  T3  浏览 API /kb/index、/kb/page
  T4  附件上传
  T5  图谱/体检落库重构

第三波（依赖 T1，且会碰多个 controller，建议单独做不并行）：
  T9  空间级 ACL
```

> 冲突红线：**T9（ACL）会改多个已有 Controller**，不要和 T2~T5 同时跑。
> T2~T5 各自新增文件为主，可并行；只在改 `KbDocumentController` 时注意 T3 与 T2 的交集（见各任务说明）。

---

## T1 · 五张新表的 entity + mapper ✅ 已完成（2026-06-22）

> 产出：`entity/{KbRelation,KbSpaceMember,KbLintIssue,KbSyncLog,KbQaLog}.java` + 对应 5 个 Mapper；`mvn compile` 通过。
> kb_relation/kb_space_member 继承 BaseEntity（全审计）；lint_issue/sync_log/qa_log 用普通 Serializable（无 create_id/update_id）。

- **目标**：为 `kb_relation` / `kb_lint_issue` / `kb_sync_log` / `kb_space_member` / `kb_qa_log` 建 MyBatis-Plus 实体与 Mapper，给后续任务打地基。
- **涉及文件**（全部新增，零冲突）：
  - `moli-knowledge-server/src/main/java/com/moli/knowledge/server/entity/Kb{Relation,LintIssue,SyncLog,SpaceMember,QaLog}.java`
  - `.../mapper/Kb*Mapper.java`（接口 + `@Mapper`）
- **依赖**：无（表 SQL 已就绪）。
- **验收**：5 entity 字段与 `03_knowledge_schema.sql` 一一对应；继承风格参考现有 `KbDocument`/`KbTag`；`mvn compile` 通过。
- **开工提示词**：
  > 读 `docs/sql/03_knowledge_schema.sql` 里 `kb_relation`/`kb_lint_issue`/`kb_sync_log`/`kb_space_member`/`kb_qa_log` 五张表，参照 `moli-knowledge/moli-knowledge-server` 现有 `entity/KbDocument.java` 与 `mapper/KbDocumentMapper.java` 的风格，为这五张表新建 entity + Mapper。注意 BaseEntity 已含 id/审计字段，不要重复；`kb_qa_log.citations` 是 JSON、`kb_document_version` 风格的无 BaseEntity 表（sync_log/qa_log/lint_issue 无 update 审计）按 SQL 字段如实建。

## T2 · Query 问答 API `POST /kb/ask` ✅ 已完成（2026-06-22）

> 产出：`controller/KbAskController`、`service/KbAskService(+Impl)`、`dto/AskRequest`/`AskResponse`、`config/KbLlmProperties`、`application-dev.yml` 增 `kb.llm.*`；`mvn compile` 通过。
> 作用域识别/分词/打分/片段对齐 serve.py；无 key 自动降级检索式；每次问答写 `kb_qa_log`。LLM 用 HttpURLConnection 调 OpenAI 兼容接口 + fastjson 解析。

- **目标**：检索选页 → 拼上下文 → 调 LLM → 返回**带引用的答案 + 来源列表**，并落 `kb_qa_log`。
- **涉及文件**（以新增为主）：
  - 新增 `controller/KbAskController.java`、`service/KbAskService(+Impl).java`、`dto/AskRequest.java`/`AskResponse.java`
  - LLM 配置：`application-dev.yml` 增 `kb.llm.*`（provider/base-url/key/model）
  - 复用 T1 的 `KbQaLogMapper`
- **依赖**：T1（要写 `kb_qa_log`）。
- **验收**：`POST /kb/ask {question, spaceId?}` 返回 `{answer, citations:[{docId,title,slug,snippet}], scope}`；无 key 时降级为「检索式」答案（参考 `kb/tools/serve.py` 的 Query 行为）；记一条 `kb_qa_log`。
- **开工提示词**：
  > 读 `kb/AGENTS.md §5 Query`、`kb/tools/serve.py` 里 Query/LLM 调用部分、`docs/sql/KNOWLEDGE_SCHEMA.md` 的 `kb_qa_log`。在 `moli-knowledge-server` 新增 `POST /kb/ask`：按关键词/作用域从 `kb_document`(status=1) 选 ≤15 页，拼上下文调 OpenAI 兼容 LLM（DeepSeek/Qwen/GLM，配置走 application-dev.yml，无 key 则降级检索式），返回带引用答案并写 `kb_qa_log`。统一返回 `MoliResult`。

## T3 · 浏览 API `/kb/index`、`/kb/page` ✅ 已完成（2026-06-22）

> 产出：`controller/KbBrowseController`、`service/KbBrowseService(+Impl)`、`dto/IndexTreeVo`/`PageDetailVo`；`mvn compile` 通过。
> `/kb/index` 按 kb_type 分组（同步文档 category_id 为空，故按类型组织）；`/kb/page?slug=` 用查询参数（slug 含斜杠）；出/入链读 `kb_relation`。

- **目标**：给前端提供目录树和按 slug 取单页（含正文渲染所需数据 + 出/入链）。
- **涉及文件**：
  - 新增 `controller/KbBrowseController.java`、`dto/IndexTreeVo.java`、`PageDetailVo.java`，service 复用现有 `KbDocumentService` + T1 `KbRelationMapper`（若已落库）
  - ⚠️ 若想挂在 `KbDocumentController` 下，需和 T2 协调；**建议单独 Controller 避免冲突**。
- **依赖**：T1（取关系/入链时用 `kb_relation`，没有可先用 `KbInsightService` 运行时算）。
- **验收**：`GET /kb/index?spaceId=` 返回分类/类型树；`GET /kb/page/{slug}?spaceId=` 返回单页 + backlinks。
- **开工提示词**：
  > 读 `moli-knowledge-server` 的 `KbDocumentController`/`KbCategoryController` 和 `docs/sql/KNOWLEDGE_SCHEMA.md`。新增 `KbBrowseController`：`/kb/index`（按 kb_type/分类组织目录树）、`/kb/page/{slug}`（按 `(spaceId,slug)` 取单页，附出链/入链）。不要改动现有 Controller，单独建类。

## T4 · 附件上传（MinIO）

- **目标**：补全 `KbAttachment` 的 Service/Controller，接 MinIO 上传/下载/删除。
- **涉及文件**（全新增）：
  - `service/KbAttachmentService(+Impl).java`、`controller/KbAttachmentController.java`、`config/MinioConfig.java`（若无）
  - 复用现有 `entity/KbAttachment` + mapper
- **依赖**：无。
- **验收**：`POST /kb/attachment/upload`（multipart）→ 存 MinIO + 写 `kb_attachment`；`GET /kb/attachment/{id}` 下载；删除置 `is_delete`。
- **开工提示词**：
  > 读 `moli-knowledge-server` 的 `application-dev.yml`(MinIO 配置)、`entity/KbAttachment.java`、`pom.xml`(确认 minio 依赖)。补全附件上传：新增 MinioConfig（如缺）、KbAttachmentService、KbAttachmentController，支持 upload/download/delete，元数据写 `kb_attachment`。统一 `MoliResult`。

## T5 · 图谱/体检落库重构

- **目标**：把现在运行时计算的 `/kb/graph`、`/kb/lint` 改为读 `kb_relation` / `kb_lint_issue`（同步时写入），大库更快、可跟踪「忽略/修复」。
- **涉及文件**：
  - 改 `service/impl/KbInsightServiceImpl.java`（读边表）
  - 新增 lint 扫描落库逻辑（写 `kb_lint_issue`）
  - 同步侧：`kb/tools/sync_to_db.py` 已写 `kb_relation`，本任务让 Java 读它
- **依赖**：T1。
- **验收**：`/kb/graph` 从 `kb_relation` 取边；`/kb/lint` 可触发扫描写 `kb_lint_issue` 并支持状态更新。保持返回结构兼容现有 `GraphVo`/`LintVo`。
- **开工提示词**：
  > 读 `moli-knowledge-server` 的 `service/impl/KbInsightServiceImpl.java`、`dto/GraphVo.java`/`LintVo.java`、`docs/sql/KNOWLEDGE_SCHEMA.md` 的 `kb_relation`/`kb_lint_issue`。把图谱/体检从运行时正则解析改为读边表（`kb_relation`）；新增一个 lint 扫描方法把问题落 `kb_lint_issue` 并支持 status 更新。返回结构与现有 VO 兼容。

## T6 · 前端知识库页面（meiling-ui）

- **目标**：在 `D:\work\moli_project\meiling-ui`（独立 Vue3+Vite 工作区）新增知识库模块：文档列表/详情、Query 问答、图谱、体检。
- **涉及文件**：meiling-ui 自己的 `src/views/knowledge/*`、路由、菜单、api 封装 —— **与后端仓库零冲突**。
- **依赖**：后端 API（可先用 mock，等 T2/T3 ready 再联调）；强烈建议先做 T8。
- **验收**：能调网关 `/KnowledgeServer/kb/...` 展示文档树/详情、提交问答、渲染图谱与体检。
- **开工提示词**（在 meiling-ui 工作区开对话）：
  > 读本仓库 `docs/KNOWLEDGE_API.md`（若未生成，先读 `moli-knowledge/moli-knowledge-server/README.md` 的 REST API 章节）。参照本项目现有 `src/views/system/UserManageView.vue` 的风格与 api 封装方式，新增「知识库」菜单与页面：文档列表+详情、Query 问答框、关系图谱、体检报告。接口前缀走网关 `/KnowledgeServer`。

## T7 · kb 批量 Ingest（充实知识库）

- **目标**：把 `kb/raw/` 大批语料按主题去重提炼成 wiki 页（控量省 token），充实知识库。
- **涉及文件**：只在 `kb/wiki/**` 新增/编辑 markdown + `index.md`/`log.md`/`edges.jsonl` —— **与所有代码任务零冲突**。
- **依赖**：无。
- **验收**：每批产出若干互链 wiki 页，更新 index/log/edges；遵守 `AGENTS.md` 契约。
- **开工提示词**：
  > 读 `kb/AGENTS.md`（全文，尤其 §2 格式、§4 Ingest）。从 `kb/raw/` 挑一个主题（如 MySQL 索引、JVM、Spring Boot 自动配置）做 ingest：去重提炼成 concept/article/interview 页，建 `[[]]` 与 edges，更新 index/log。每批控制在 5~10 页内、汇报 token 量。

## T8 · 前端对接文档 `docs/KNOWLEDGE_API.md`

- **目标**：把知识库所有 REST 接口（路径、网关前缀、鉴权头、请求/响应 JSON 示例）整理成前端可直接照着写的文档。
- **涉及文件**（全新增）：`docs/KNOWLEDGE_API.md`。
- **依赖**：无（按现有 + 规划接口写，T2/T3 定稿后补充）。
- **验收**：覆盖 文档/分类/标签/图谱/体检 现有接口 + Query/浏览 规划接口；含网关前缀 `/KnowledgeServer`、`Authorization` 头说明、字段示例。
- **开工提示词**：
  > 读 `moli-knowledge/moli-knowledge-server/README.md`、`controller/*.java`、`docs/sql/KNOWLEDGE_SCHEMA.md`。生成 `docs/KNOWLEDGE_API.md`：逐个接口给出 方法/路径/网关前缀/鉴权头/请求体/响应体 JSON 示例，供 meiling-ui 前端照着对接。

## T9 · 空间级 ACL（建议单独做，勿与 T2~T5 并行）

- **目标**：基于 `kb_space_member`（用户/角色 + viewer/editor/admin）做空间级可见性与编辑权限，复用 Shiro/Dubbo。
- **涉及文件**：会**改多个现有 Controller/Service**（空间、文档、分类的查询过滤）→ 冲突面大。
- **依赖**：T1。
- **验收**：非成员看不到私有空间内容；editor 以上才能改；过滤在 service 层统一做。
- **开工提示词**：
  > 读 `moli-knowledge-server` 的 `util/ShiroUtils.java`、各 Controller、`docs/sql/KNOWLEDGE_SCHEMA.md` 的 `kb_space_member`。实现空间级 ACL：新增成员服务 + 一个统一的空间可见性过滤，应用到空间/分类/文档查询。注意这会改多个现有类，独占式开发，避免与其他任务并行。

---

## 推荐推进顺序

1. **先开 T1**（地基，半天内能完成）。
2. T1 完成后，**并行开 T2 / T3 / T4 / T5**（各自新增文件为主）。
3. **全程可并行**：T6（前端）、T7（ingest）、T8（API 文档）——它们和后端代码不抢文件。
4. **最后单独做 T9**（ACL，改动面大）。

> 多对话协作小贴士：同一时间不要让两个对话改同一个 `.java` 文件；每个任务跑完各自 `mvn -q -pl moli-knowledge/moli-knowledge-server compile` 自测；合并前 `git status` 看清改了哪些文件。
