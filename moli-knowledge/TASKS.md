# 企业知识库 · 待办任务清单（可并行开工）

> 更新：2026-06-28
> 用途：每个任务**自包含、文件边界清晰**，可在不同对话框/工作区并行开工，尽量不互相冲突。
> 范式与分工见 [`kb/ROADMAP.md`](kb/ROADMAP.md)；表结构见 [`../docs/sql/KNOWLEDGE_SCHEMA.md`](../docs/sql/KNOWLEDGE_SCHEMA.md)。
>
> 开一个新对话时：**先贴该任务的「开工提示词」**，它会让那个对话先读必要文档再动手。

---

## 现状速览

| 模块 | 已完成 | 未完成 |
|------|--------|--------|
| 表结构 | ✅ 14 张表 + entity/mapper（T1） | — |
| 同步 | ✅ sync API + CI 多空间门禁（T12） | — |
| Java API | ✅ CRUD/Ask/Browse/Graph/Lint/ACL/附件/全文检索/Ingest/Wiki 治理 API | Meilisearch/向量（量级触发再上） |
| 文档 | ✅ `docs/KNOWLEDGE_API.md` + 前端对接三件套 + ops 操作手册 | — |
| kb 知识 | ✅ wiki + wiki-ops + wiki-jp-exam；`lint-strict` CI | 持续 ingest 语料 |
| 后端工作台 | ✅ T14 单页编辑 · T15 Ingest · T16a/e/g 治理 · T17 分类落盘 · T18 Express · T19 LLM 平台 | — |
| 前端 meiling-ui | ✅ T6 浏览/问答 · T15 Ingest（部分）· T14 编辑 | **T16f** 治理全链路 · **T19d** LLM 设置 UI · 空间 CRUD（二期） |

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
  T3  浏览 API /kb/index（meta）+ items/search/locate + /kb/page
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

## T3 · 浏览 API `/kb/index`、`/kb/page` ✅ 已完成（2026-06-22；**2026-06-23 大库优化**）

> 产出：`controller/KbBrowseController`、`service/KbBrowseService(+Impl)`、`dto/IndexTreeVo`/`IndexItemsPageVo`/`IndexLocateVo`/`PageDetailVo`；`mvn compile` 通过。
> **2026-06-23**：3300+ 篇场景下 `/kb/index` 改为 **meta 模式**（`groups[].count`，不含 items）；展开分组走 `/kb/index/items`（分页，轻量无 summary）；侧栏搜索 `/kb/index/search`；深链 `/kb/index/locate`。详见 `docs/KNOWLEDGE_API.md` §2。
> `/kb/page?slug=` 用查询参数（slug 含斜杠）；出/入链读 `kb_relation`。

- **目标**：给前端提供目录树和按 slug 取单页（含正文渲染所需数据 + 出/入链）；大文档量下首屏轻量、分组懒加载。
- **涉及文件**：
  - `controller/KbBrowseController.java`、`service/KbBrowseService(+Impl).java`
  - `dto/IndexTreeVo.java`、`IndexItemsPageVo.java`、`IndexLocateVo.java`、`PageDetailVo.java`
  - 前端 `meiling-ui`：`KnowledgeBrowseView.vue` + `api/knowledge.ts`
- **依赖**：T1（取关系/入链时用 `kb_relation`）。
- **验收**：
  - `GET /kb/index?spaceId=` → `{ total, groups:[{ type, label, count, items:[] }] }`
  - `GET /kb/index/items?spaceId=&type=&pageNum=&pageSize=` → 分组条目分页
  - `GET /kb/index/search?spaceId=&q=` → 侧栏搜索（服务端过滤）
  - `GET /kb/index/locate?spaceId=&slug=` → 深链所属分组
  - `GET /kb/page?slug=&spaceId=` → 单页 + outLinks/backLinks
- **开工提示词**（历史）：
  > 读 `docs/KNOWLEDGE_API.md` §2 与 `KbBrowseController`。浏览侧栏：**meta 首屏 + items 懒加载 + search**；勿再让 `/kb/index` 一次返回全库 items。

## T4 · 附件上传（MinIO） ✅ 已完成

> 产出：`MinioConfig` + `MinioProperties` + `KbAttachmentService`(+Impl) + `KbAttachmentController`；
> `POST /kb/attachment/upload`（multipart）→ MinIO + `kb_attachment`；`GET /{id}` 下载；`DELETE /{id}` 软删。
> 单测 10 条通过（Controller 3 + Service 7）。

- **目标**：补全 `KbAttachment` 的 Service/Controller，接 MinIO 上传/下载/删除。
- **涉及文件**（全新增）：
  - `service/KbAttachmentService(+Impl).java`、`controller/KbAttachmentController.java`、`config/MinioConfig.java`（若无）
  - 复用现有 `entity/KbAttachment` + mapper
- **依赖**：无。
- **验收**：`POST /kb/attachment/upload`（multipart）→ 存 MinIO + 写 `kb_attachment`；`GET /kb/attachment/{id}` 下载；删除置 `is_delete`。✅
- **已知小缺口**：~~附件接口尚未接入 T9 空间 ACL~~ → **已补全**（2026-06-22）。

## T10 · 全文检索 + ACL 补全 + 同步管理 API ✅ 已完成（2026-06-22）

> 产出：
> - **全文检索**：`KbDocumentMapper.searchFullText` + ngram `MATCH AGAINST`；`kb.search.mode=fulltext|like`（失败自动降级 LIKE）。
> - **ACL 补全**：`assertCanReadDocument/assertCanEditDocument`；接入 分类/标签/评论/收藏/附件/图谱体检。
> - **同步管理**：`GET /kb/sync/logs`、`GET /kb/sync/status`、`POST /kb/sync/trigger`（调 `sync_to_db.py`，需空间 admin 或平台超管）。

- **验收**：文档搜索走全文索引；私有空间附件/评论/分类等越权拦截；管理员可 API 触发同步并查日志。✅

## T11 · 问答历史 + 附件列表 + 自动同步 ✅ 已完成（2026-06-22）

> 产出：
> - **问答闭环**：`GET /kb/ask/history`、`PUT /kb/ask/feedback/{id}?useful=`；`POST /kb/ask` 响应增加 `qaLogId`。
> - **附件列表**：`GET /kb/attachment/list?documentId=`。
> - **文档详情增强**：`DocumentDetailVo` 补 `slug/kbType/domain/source`。
> - **自动同步**：`KbSyncScheduler`（`kb.sync.schedule-enabled`）；git hook `kb/tools/install_git_hook.{sh,ps1}`。

- **验收**：前端可展示问答历史并点赞/点踩；文档详情页可列附件；wiki commit 后可自动 sync（装 hook 后）。✅

## T12 · CI pipeline 挂 sync ✅ 已完成（2026-06-22）

> 产出：`.github/workflows/kb-sync.yml` + `kb/tools/ci/run_sync.sh` + `requirements-sync.txt`。

| Job | 触发 | 行为 |
|-----|------|------|
| `dry-run` | PR / push（`kb/wiki` 等路径变更） | 解析 wiki，不连库 |
| `sync-mysql` | push `main`/`master` 或手动选 `ci` | MySQL 8 服务容器 → 导入 schema → 真实 sync → 校验 `kb_document` 行数 |
| `sync-remote` | 手动 workflow_dispatch 选 `remote` | 连仓库 Secrets 配置的远程库 |

**远程 Secrets**（Settings → Secrets → Actions）：
`KB_SYNC_DB_HOST`、`KB_SYNC_DB_USER`、`KB_SYNC_DB_PASSWORD`（必填）；
`KB_SYNC_DB_PORT`、`KB_SYNC_DB_NAME`、`KB_SYNC_SPACE`（可选）。

本地等价命令：
```bash
bash moli-knowledge/kb/tools/ci/run_sync.sh dry-run
```

## T13 · 知识库菜单种子（sys_menu） ✅ 已完成（2026-06-22）

> 产出：`docs/sql/04_knowledge_menu.sql`；`init-db.ps1` 自动导入；`docs/KNOWLEDGE_API.md` §1.5 说明。

- **目标**：知识库左侧树由 **`GET /UserCenter/menu/getRouters`** 下发，不在 meiling-ui 写死。
- **菜单 ID 段**：900~908（目录 + 4 页面 + 4 按钮权限）。
- **角色绑定**：role 2/3/4/6/7；超管 role 1 走全菜单无需绑定。
- **已有库补数据**：`mysql -u root -p moli < docs/sql/04_knowledge_menu.sql`，然后重新登录。

## T5 · 图谱/体检落库重构 ✅ 已完成（2026-06-22；**2026-06-24 大库优化**）

> 产出：`KbInsightServiceImpl` 重构（graph 优先读 `kb_relation`，空表回退运行时）；新增 `scan`/`issues`/`updateIssueStatus`；`KbInsightController` 加 `POST /kb/lint/scan`、`GET /kb/lint/issues`、`PUT /kb/lint/issue/{id}`。返回结构与 GraphVo/LintVo 兼容；`mvn compile` 通过。
> **2026-06-24（图谱卡死优化）**：3300+ 篇下 `/kb/graph` 不再每次扫正文——边只读 `kb_relation`、节点只查 `id/title/kb_type/status`（**不含 content/summary**），按**度数降序裁剪**到 `maxNodes`（full=300/summary=50，上限 2000），新增 `minDeg` 过滤；`GraphVo` 加 `meta{totalNodes,totalLinks,returnedNodes,returnedLinks,truncated,source,mode}`。新增 `GET /kb/graph/ego?docId=&depth=`（BFS 邻域子图，逐层查 relation，不加载全图）。节点 `type` 改用 `kb_type`（与浏览分组一致）。详见 `docs/KNOWLEDGE_API.md` §4.1。
> ⚠️ 前端待办：默认 `mode=summary` 或 `minDeg`，核心节点先画，点击节点用 `ego` 展开；大图禁用 force 持续布局。

- **目标**：把现在运行时计算的 `/kb/graph`、`/kb/lint` 改为读 `kb_relation` / `kb_lint_issue`（同步时写入），大库更快、可跟踪「忽略/修复」。
- **涉及文件**：
  - 改 `service/impl/KbInsightServiceImpl.java`（读边表）
  - 新增 lint 扫描落库逻辑（写 `kb_lint_issue`）
  - 同步侧：`kb/tools/sync_to_db.py` 已写 `kb_relation`，本任务让 Java 读它
- **依赖**：T1。
- **验收**：`/kb/graph` 从 `kb_relation` 取边；`/kb/lint` 可触发扫描写 `kb_lint_issue` 并支持状态更新。保持返回结构兼容现有 `GraphVo`/`LintVo`。
- **开工提示词**：
  > 读 `moli-knowledge-server` 的 `service/impl/KbInsightServiceImpl.java`、`dto/GraphVo.java`/`LintVo.java`、`docs/sql/KNOWLEDGE_SCHEMA.md` 的 `kb_relation`/`kb_lint_issue`。把图谱/体检从运行时正则解析改为读边表（`kb_relation`）；新增一个 lint 扫描方法把问题落 `kb_lint_issue` 并支持 status 更新。返回结构与现有 VO 兼容。

## T6 · 前端知识库页面（meiling-ui） ✅ 已完成（2026-06-22）

> 产出：`KnowledgeBrowseView` / `KnowledgeAskView` / `KnowledgeGraphView` / `KnowledgeLintView`；
> `KbSpaceSelector`、`KbDocPreviewModal`、`KbAttachmentsPanel`、`KbSyncPanel`；
> API 封装覆盖 browse/ask/history/feedback/graph/lint/sync/attachment；markdown 表格+代码块渲染。

- **目标**：在 `D:\work\moli_project\meiling-ui`（独立 Vue3+Vite 工作区）新增知识库模块：文档列表/详情、Query 问答、图谱、体检。
- **涉及文件**：meiling-ui 自己的 `src/views/knowledge/*`、路由、api 封装 —— **菜单由后端 `sys_menu` + getRouters 下发（T13）**，与后端仓库零冲突。
- **依赖**：后端 API（可先用 mock，等 T2/T3 ready 再联调）；强烈建议先做 T8。
- **验收**：能调网关 `/KnowledgeServer/kb/...` 展示文档树/详情、提交问答、渲染图谱与体检。✅

## T7 · kb 批量 Ingest（充实知识库） ✅ 已完成（P0 批次）

> 产出：wiki 从 17 页扩至 **54 内容页**（+ index/log）；`edges.jsonl` 69 条边；
> log 记录批次 #6~#11：Redis 缓存、JVM、Spring Boot、Dubbo+Nacos、故障排查、MySQL 事务锁等 P0 主题簇。
> 每批遵守 `AGENTS.md`：concept 枢纽 + articles + interview 互链，更新 index/log/edges。

- **目标**：把 `kb/raw/` 大批语料按主题去重提炼成 wiki 页（控量省 token），充实知识库。
- **涉及文件**：只在 `kb/wiki/**` 新增/编辑 markdown + `index.md`/`log.md`/`edges.jsonl` —— **与所有代码任务零冲突**。
- **依赖**：无。
- **验收**：每批产出若干互链 wiki 页，更新 index/log/edges；遵守 `AGENTS.md` 契约。✅（P0 核心技术栈已覆盖；`kb/raw/` 仍有大量语料可持续 ingest）

## T8 · 前端对接文档 `docs/KNOWLEDGE_API.md` ✅ 已完成（2026-06-22）

> 产出：[`docs/KNOWLEDGE_API.md`](../docs/KNOWLEDGE_API.md)，覆盖 浏览/Query/图谱/体检 + 文档/分类/标签/评论/收藏/空间 全部接口，含网关前缀、Authorization 头、MoliResult 结构、JSON 示例、前端页面建议。

- **目标**：把知识库所有 REST 接口（路径、网关前缀、鉴权头、请求/响应 JSON 示例）整理成前端可直接照着写的文档。
- **涉及文件**（全新增）：`docs/KNOWLEDGE_API.md`。
- **依赖**：无（按现有 + 规划接口写，T2/T3 定稿后补充）。
- **验收**：覆盖 文档/分类/标签/图谱/体检 现有接口 + Query/浏览 规划接口；含网关前缀 `/KnowledgeServer`、`Authorization` 头说明、字段示例。
- **开工提示词**：
  > 读 `moli-knowledge/moli-knowledge-server/README.md`、`controller/*.java`、`docs/sql/KNOWLEDGE_SCHEMA.md`。生成 `docs/KNOWLEDGE_API.md`：逐个接口给出 方法/路径/网关前缀/鉴权头/请求体/响应体 JSON 示例，供 meiling-ui 前端照着对接。

## T9 · 空间级 ACL ✅ 已完成（2026-06-22）

> 产出：统一 ACL 服务 `KbAclService`(+impl) + 空间成员管理 `KbSpaceMemberService`(+impl) + `KbSpaceMemberController`；
> 过滤/断言下沉到 service 层并接入 空间 / 文档 / 浏览 / 问答 四处。`mvn compile` 通过。

- **目标**：基于 `kb_space_member`（用户/角色 + viewer/editor/admin）做空间级可见性与编辑权限，复用 Shiro/Dubbo。
- **权限模型**：
  - 空间可见性 `visibility`：2 公开（人人可读）/ 1 内部（登录即可读）/ 0 私有（仅成员、负责人）。
  - 成员角色 `role`：`viewer`(只读) / `editor`(可改) / `admin`(可管理成员)；`owner_id` 等同 admin。
  - 平台超管：`superadmin`/`admin` 或 Shiro `*:*:*` 一票通过。
- **统一接口** `KbAclService`：`canRead/canEdit/canAdmin` + `assertCanRead/assertCanEdit/assertCanAdmin` + `accessibleSpaceIds()`。
- **接入点**（service 层统一过滤，非散落在 controller）：
  - `KbSpaceServiceImpl`：列表只回可读空间；`getById` 断言可读；`update/delete` 断言可管理。
  - `KbDocumentServiceImpl`：`search` 按可读空间集合过滤 / 指定空间断言可读；`detail`/`versions` 断言可读；`save/publish/archive/delete` 断言可编辑。
  - `KbBrowseServiceImpl`：`index` 过滤可读空间；`page` 断言可读。
  - `KbAskServiceImpl`：候选页限定到可读空间；无可读空间直接回退。
- **成员管理 API**（`/kb/space/member`，均需空间管理权限）：`GET /list`、`POST`（单条）、`POST /batch`（批量添加）、`PUT`、`DELETE /{id}`（单条）、`POST /batch/remove`（批量移除）。
- **已知限制**：Dubbo 契约目前只透出权限串、不透出角色ID，**角色型成员(member_type=1)** 仅支持存储/管理，运行时不解析（用户型成员完整生效）。待 `UserCenterServer` 暴露角色后，在 `KbAclServiceImpl#memberRole` 处补一行即可。
- **验收**：非成员看不到私有空间内容；editor 以上才能改；过滤在 service 层统一做。✅

## T14 · Web Wiki 在线编辑 + AI 协助改稿 ✅ 已完成（T14a–f）

> 产品方案：[[Wiki在线编辑与AI协助改稿]]（`kb/wiki/guides/`）。规划里程碑 **M5**。

**目标**：在 Web 界面打开 wiki 页，调用已配置 LLM 协助改稿；展示修改前/后 diff；支持人工继续改；**Enrich 追加章节**；确认后保存回 `kb/wiki/*.md`，再 Sync 进库。

| 子任务 | 范围 | 验收 |
|--------|------|------|
| **T14a ✅** | 后端 `GET/PUT /kb/wiki/page`（`kb.wiki.*` 三空间根映射 + 乐观锁 + 防穿越）；meiling-ui `KnowledgeWikiEditView` 编辑/预览/行级 diff + 浏览页「编辑 wiki」入口 | editor 可改 wiki 文件；保存后 Sync 可见 |
| **T14b ✅** | `POST /kb/wiki/ai-revise`（`KbLlmClient` + 场景 B prompt）；编辑页 AI 面板 + 应用建议 + diff | 配好 llm 后可 AI 改稿并保存 |
| **T14c ✅** | 体检「修复」→ 编辑页（issue 上下文）；保存后可选标记已修复 | lint 列表 → 编辑 → 保存 → status=2 |
| **T14d ✅** | 「保存并 Sync」；`POST /kb/wiki/page/lint-preview` 保存前摘要 | 少点 Tab；预检断链/frontmatter |
| **T14f ✅** | `POST /kb/wiki/enrich` + `kb/tools/enrich.py`；编辑页 **Enrich 治理** 侧栏（preview/apply、log/index/edges）；Ingest PageWriter `related` 收敛（0–5 强相关） | 单页 enrich 与 CLI/Ingest 语义一致 |

- **涉及文件**：
  - server：✅ T14a–f：`KbWikiController`、`KbWikiFileService`、`KbWikiAiReviseService`、**`KbWikiEnrichService`**、`KbLlmClient`、DTO 全套；Ingest `related` 约束见 `KbIngestServiceImpl`
  - meiling-ui：✅ `KnowledgeWikiEditView`（AI/lint/sync/**enrich**）、`KnowledgeLintView` 修复入口、`KnowledgeBrowseView` 编辑 wiki
  - 文档：✅ `docs/api/KNOWLEDGE_API.md` §8（含 §8.4 Enrich）；🔜 `docs/sql` 菜单种子（`kb:wiki:edit`）
- **依赖**：T9 ACL（editor）、T2 LLM 配置、T3 `/kb/page`（读库展示可复用 slug）
- **铁律**：保存目标 = **wiki 文件**；`POST /kb/document` **已停用**（2026-06-24）

---

## T15 · Web Ingest 工作台（批次厚 Ingest） ✅ 已完成（T15a–e，2026-06-25）

> 产品方案：[[Ingest工作台产品方案]]（`kb/wiki/guides/`）。规划里程碑 **M6**。  
> 与 **T14** 并列：T14 = 单篇修稿；T15 = raw → 多页 ingest。

**目标**：Web 完成 AGENTS §4 等价流程：选 raw → Plan（去重）→ 多页 LLM 草稿 → 逐页 diff → lint → 原子写 wiki（含 index/log/edges）→ Sync。

| 子任务 | 范围 | 验收 |
|--------|------|------|
| **T15a** ✅ | `GET /kb/ingest/raw-tree` + job CRUD + **Plan 生成/编辑** + export-agent-prompt | 规划可对标 Agent ingest 第一步 |
| **T15b** ✅ | `POST .../generate` + 多页 diff UI | 5 页簇可审、不落盘 |
| **T15c** ✅ | lint 预检 + **原子 commit**（wiki/log/index/edges） | 交付物 = AGENTS §4 checklist |
| **T15d** ✅ | commit 后一键 Sync + 批次报告 | 线上可问答 |
| **T15e** ✅ | enrich patch、断点续跑、批次模板 | 大批量可恢复 |

### T15b–d 已完成（2026-06-25）

> 范围：按 plan 生成多页草稿（PageWriter/EnrichWriter）→ 逐页 baseline↔draft diff + 审批 → lint 预检（含批次内 slug）→ 原子 commit（写 wiki + append log/edges + 追加 index 批次段）→ 可选一键 Sync。

- **API**（`/kb/ingest`，详见 `docs/api/KNOWLEDGE_API.md` §9.5–9.7）：
  - `POST /jobs/{id}/generate`、`GET /jobs/{id}/drafts`、`GET|PUT /jobs/{id}/draft?slug=`、`POST /jobs/{id}/draft/regenerate?slug=`、`PUT /jobs/{id}/draft/approval?slug=&approval=`
  - `POST /jobs/{id}/lint`、`POST /jobs/{id}/commit?sync=`
- **commit 红线（后端强制）**：lint ERROR 阻塞、无 approved 阻塞、有 draft 未审阅阻塞；edges 仅当一端为本批次新页才追加。
- **生成同步状态机**：created → planned →（generate）reviewing →（commit）committed。
- **涉及文件**：
  - server（新增）：`entity/KbIngestDraft`+`KbIngestCommit` + Mapper、`dto/IngestDraftVo`+`IngestDraftUpdateRequest`+`IngestLintVo`+`IngestCommitResultVo`；`KbIngestService`(+Impl) 扩展 generate/draft CRUD/approve/lint/commit；复用 `KbWikiFileService`、`KbLlmClient`、`KbSyncService`
  - server（改）：`KbIngestController` 增 T15b–d 端点
  - meiling-ui（新增）：`views/knowledge/KnowledgeIngestWorkbenchView.vue`（列表/新建 + Plan/草稿 diff/lint/commit&Sync）；`viewRegistry`、`menuLabel` 注册 `knowledge/ingest/index`；`types`/`api` 增草稿/lint/commit；`i18n` zh/en/ja `knowledge.ingest.*`
- **已知简化（后续可选）**：generate 为同步调用（无 SSE 进度）；index 更新为追加批次段而非按类型分区插入。

### T15e 已完成（2026-06-25）

> 范围：enrich 草稿分离 `patch` 列 + 断点续跑 generate + 批次模板 CRUD / 从模板建 job / 另存为模板。

- **API**（`/kb/ingest`，详见 `docs/api/KNOWLEDGE_API.md` §9.8）：
  - `POST /jobs/{id}/generate?resume=` — 返回 `IngestGenerateResultVo`（total/generated/skipped/drafts）
  - `PUT /jobs/{id}/draft?slug=` — body 支持 `{content}` 或 `{patch}`（enrich）
  - `GET|POST /templates`、`DELETE /templates/{id}`、`POST /jobs/from-template/{templateId}`、`POST /jobs/{id}/save-as-template`
- **DDL**：`docs/sql/09_kb_ingest_t15e.sql`（`kb_ingest_draft.patch` + `kb_ingest_template`）
- **meiling-ui**：续跑/全量生成按钮、enrich Patch Tab、模板列表与从模板创建、另存为模板；i18n zh/en/ja

### T15a 已完成（2026-06-25）

> 范围：raw 只读树 + 批次 job CRUD + Plan 生成/编辑 + 导出 Agent 提示词。**只做「选源→规划」，不写 wiki / 不写 kb_document**（落盘在 T15b/c）。

- **API**（`/kb/ingest`，详见 `docs/api/KNOWLEDGE_API.md` §9）：
  - `GET /raw-tree?prefix=` 只读树（viewer，防目录穿越）
  - `POST /jobs`、`GET /jobs`（分页，按可读空间过滤）、`GET /jobs/{id}`（含最新 plan）
  - `POST /jobs/{id}/plan` LLM 生成（只输出 JSON；无 LLM 给可编辑骨架）、`PUT /jobs/{id}/plan` 人工改
  - `GET /jobs/{id}/export-agent-prompt`
- **Plan**：版本化（`kb_ingest_plan` 每次 append 一版）；Planner system prompt 强约束 enrich 优先、JSON-only。
- **后端鉴权**：raw 树/查询=空间 viewer；创建/规划=空间 **editor**（`KbAclService.assertCanEdit`）。

- **涉及文件**：
  - server（新增）：`config/KbIngestProperties`、`entity/KbIngestJob`+`KbIngestPlan`、`mapper/KbIngestJobMapper`+`KbIngestPlanMapper`、`dto/RawTreeNodeVo`+`IngestJobCreateRequest`+`IngestJobVo`+`IngestPlanUpdateRequest`、`service/KbIngestService`(+Impl)、`controller/KbIngestController`；复用 `KbWikiProperties`、`KbLlmClient`、`KbAclService`、`KbDocumentMapper`
  - server（改）：`application-dev.yml` 加 `kb.ingest.*`
  - meiling-ui（新增 api/types）：`types/knowledge.ts`（`KbRawTreeNode`/`KbIngestJob`/`KbIngestPlan*`）、`api/knowledge.ts`（`getKbIngestRawTreeApi`/`create|get|getsKbIngestJob*`/`generate|updateKbIngestPlanApi`/`exportKbIngestAgentPromptApi`）
  - 文档/SQL：`docs/api/KNOWLEDGE_API.md` §9；`docs/sql/08_kb_ingest_workbench.sql`（表 DDL + 菜单 906 + `kb:ingest:*` 权限）
- **待续（T15b 起）**：`KnowledgeIngestWorkbenchView.vue`（raw 树 + Plan 表 + 多页 diff）、`POST .../generate`、`kb_ingest_draft/commit` 落表。
- **依赖**：✅ Phase 0；**T14a**（wiki 读写）；T9 ACL（editor）；T2 LLM
- **铁律**：禁止 raw→DB、禁止无 plan 生成、禁止无 diff commit；见方案 §5

**开工提示词**：

> 读 `kb/wiki/guides/Ingest工作台产品方案.md`、`kb/AGENTS.md` §4、T14 的 `KbWikiFileService` 设计。实现 T15a：`/kb/ingest/raw-tree` + job/plan API + 前端 Plan 表；Plan LLM 只输出 JSON；不写 wiki 直到 commit。

---

## T16 · Web Wiki 治理工作台（Lint→修复→复检→Sync 链路） ✅ 后端已交付（T16a/e/g）；🔵 T16f 前端

> 产品方案：[[Wiki治理工作台产品方案]]（`kb/wiki/guides/`）。需求总览：`docs/product/knowledge-workbench-requirements.md`。  
> 与 T14（单篇编辑）/ T15（raw 批次 ingest）并列：**T16 = 空间级批量治理**（文件真值 lint → **script-fix / ai-batch-fix / auto-fix**）。

**锁定决策（2026-06-27）**：① Lint 用**文件真值**（`lint.py --wiki-dir`）；② 批量修复 = **script-fix**（metadata，无 LLM）+ **ai-batch-fix** + **auto-fix**；③ dup 用 **merge-hint** + 手改；④ **治理页不做批量 enrich**（enrich 仅 T14 单页 / Ingest Plan）。

**目标**：选空间 → lint-space → 勾选 issues → script/AI/一键修复 → 复检 → 可选 Sync。

| 子任务 | 范围 | 验收 | 状态 |
|--------|------|------|------|
| **T16a** | `POST /kb/wiki/lint-space` + 前端 lint 真值展示 | 选空间扫文件真值 | ✅ |
| **T16e** | `script-fix` / `ai-batch-fix` / `auto-fix` | metadata + 断链/孤儿批量修 | ✅ |
| **T16g** | `merge-hint` + `manualOnlyKinds` | dup_slug 复制 Cursor 指令 | ✅ |
| **T16f** | meiling-ui 治理页 | 见 `docs/api/wiki-govern-frontend.md` | 🔵 |
| ~~T16b/c enrich 批量~~ | — | **已废弃** | ❌ |

- **数据贯通**：`lint.py` 的 `issue.page` = slug，直接传入 govern API 的 `issues[]`。
- **涉及文件**：
  - server：`KbWikiGovernServiceImpl`、`KbWikiFrontmatterFixUtil`、`WikiGovernKindUtil`、`KbWikiMergeHintUtil`
  - meiling-ui：`KnowledgeWikiGovernView.vue`（待接）
  - 文档：`docs/api/wiki-govern-frontend.md`、`docs/test/knowledge-script-vs-llm-matrix.md`
- **依赖**：✅ T14a、T9 ACL、T2 LLM、`lint.py`

**开工提示词（T16f 前端）**：

> 读 `docs/api/wiki-govern-frontend.md` 与 `kb/wiki/guides/Wiki治理工作台产品方案.md`。接 lint-space + script-fix + ai-batch-fix + auto-fix + merge-hint；**不要**接批量 enrich。

---

## T17 · Ingest 落盘对齐文档分类（categoryId + 自定义 slug）✅

> **背景**：文档管理「分类」= `kb_category.dir_slug` = wiki 一级目录（Sync 回填 `category_id`）；Ingest Commit 仍用硬编码 `type → guides/articles/...`，与分类体系脱节，导致落盘路径不可选（如 `fe/`）、文件名被 LLM 英文 slug 覆盖 raw 原名。  
> **目标**：Plan/Commit 与 [[文档管理]]、[[wiki同步指南]] 单一真相一致：`{dir_slug}/{slug}.md`，UI 可选分类 + 可改文件名。

**用户故事**

| 角色 | 场景 |
|------|------|
| editor | 勾选 `raw/school/fe/fe_kamoku_b_set_sample_qs.md` → 规划页 **分类选「FE 题库/fe」**、**slug 默认 `fe_kamoku_b_set_sample_qs` 可改** → 生成草稿 → 批准 → Commit → 落盘 `wiki-jp-exam/fe/fe_kamoku_b_set_sample_qs.md` |
| editor | 仅改分类不重生成：Plan 表改 `categoryId` → 保存 Plan → 重新生成或 Commit 前校验路径预览 |

**Plan JSON 契约（v2，向后兼容 v1）**

`create[]` 每项：

| 字段 | 必填 | 说明 |
|------|------|------|
| `categoryId` | **create 推荐必填** | 目标空间 `kb_category.id`；落盘目录 = 该分类 `dir_slug` |
| `slug` | 是 | **裸文件名**（无 `.md`、无 `/`），如 `fe_kamoku_b_set_sample_qs` |
| `title` | 否 | 页标题；LLM 写 frontmatter |
| `sources` | 是 | raw 路径数组 |
| `type` | 否 | **deprecated 兜底**：无 `categoryId` 时用 `typeDir(type)`；有 `categoryId` 时默认取 `category.defaultType` 写 frontmatter |
| `reason` | 否 | 规划说明 |

落盘相对路径（权威）：

```text
relPath = {category.dir_slug}/{slug}.md
fullSlug = {dir_slug}/{slug}          // 写入 KbIngestDraft.slug、commit、DB slug
```

`enrich[]` **不变**（仍按已有 wiki 页路径增补）；可选后续 T17b 支持 enrich 改分类（= 移动分类）。

**向后兼容**

| 旧 Plan | 行为 |
|---------|------|
| 仅有 `type` + `slug` | 继续 `typeDir(type)/slug`（现状） |
| `slug` 含 `/`（如 `articles/foo`） | **不再**叠 `typeDir`；整段作 relPath（修正 API 文档与实现不一致） |

---

### 子任务拆分

| 子任务 | 范围 | 验收 |
|--------|------|------|
| **T17a** | **后端路径解析** + Plan 校验 + 单元测试 | `resolveCreateRelPath` 支持 `categoryId`；非法 slug/跨空间分类拒绝；旧 Plan 仍可通过 |
| **T17b** | **Planner / skeleton 预填** + PageWriter prompt | 骨架 Plan 从 raw 路径取 stem 填 `slug`；LLM Plan 注入空间分类列表（id/dir_slug/defaultType）；生成草稿后 `IngestDraftVo` 增 `categoryId/dirSlug/categoryName` |
| **T17c** | **前端 Plan 可视化表**（分类下拉 + slug 输入） | 批次详情 ① 区：create 行级编辑；调 `GET /kb/category/tree`；保存 Plan 写回 JSON；JSON 高级模式保留 |
| **T17d** | **落盘预览 + 文档** | Commit 前展示 `wiki-jp-exam/fe/xxx.md`；`docs/api/KNOWLEDGE_API.md` §9.3、`Ingest工作台产品方案` 更新；i18n zh/en/ja | ✅ |

---

### T17a · 后端（详细）

**涉及文件**

- `KbIngestServiceImpl.java`：`resolveCreateRelPath`、`genCreateDraft`、`skeletonPlan`、`parsePlan` 校验、`findPlanItem`
- 新增：`IngestPlanItemValidator` 或私有方法 `validateCreateItem`、`resolveCategoryForPlan`
- 复用：`KbCategoryMapper`、`KbCategoryServiceImpl`（校验 dir_slug 非空、空间一致）
- 复用：`KbDocumentServiceImpl.move` 同款规则 `{dirSlug}/{stem}`

**核心逻辑**

```java
// 伪代码
if (item.categoryId != null) {
  KbCategory cat = loadCategory(item.categoryId, job.spaceId);
  String stem = sanitizeBareSlug(item.slug); // 禁止 / . ..
  return cat.getDirSlug() + "/" + stem;
}
// legacy
return typeDir(item.type) + "/" + sanitizeBareSlug(item.slug);
```

**slug 校验**（与 `KbCategoryServiceImpl` dir_slug 规则对齐或略宽以支持中文 stem）：

- 允许：中文、英文、数字、`-`、`_`
- 禁止：`/`、`\`、`..`、首尾空白、空串

**DTO**

- `IngestDraftVo` 增加：`categoryId`、`dirSlug`、`categoryName`（只读展示）
- 可选：`IngestPlanCreateItemVo` 供未来结构化 API（T17c 仍用 planJson 字符串亦可）

**测试**（`KbIngestServiceImplPlanPathTest`）

1. `categoryId=fe分类` + `slug=fe_kamoku_b_set_sample_qs` → `fe/fe_kamoku_b_set_sample_qs`
2. 仅 `type=article` + `slug=foo` → `articles/foo`（兼容）
3. `slug=articles/foo` 无 category → relPath `articles/foo`（不双前缀）
4. 跨空间 `categoryId` → BaseException
5. commit 集成测（可选）：mock writePage 断言 slug

---

### T17b · Planner / 生成（详细）

**skeletonPlan**（LLM 未配置时）

- 每个 raw：`slug = Path(stem).md` 去后缀（`fe_kamoku_b_set_sample_qs`）
- `categoryId`：若空间仅一个分类则默认；否则 null + UI 必选
- `type`：来自 `job.expectTypes` 或分类 `defaultType`

**LLM Planner prompt**

- 注入：`GET categories for space` 列表 `[{id, categoryName, dirSlug, defaultType}]`
- 规则：create 必须输出 `categoryId` + 裸 `slug`；slug 优先 raw 文件名 stem；禁止输出 `articles/xxx` 全路径

**PageWriter**

- userPrompt 增加：`落盘目录：{dirSlug}/`；`frontmatter type：{defaultType}`

---

### T17c · 前端（详细）

**涉及文件**

- `KnowledgeIngestWorkbenchView.vue`：① Plan 区新增 `IngestPlanCreateTable.vue`（或内联）
- `api/knowledge.ts`：已有 `getKbCategoryTreeApi`
- `types/knowledge.ts`：`IngestPlanCreateRow`、`KbIngestDraft` 扩展字段
- i18n：`knowledge.ingest.planCategory`、`planSlug`、`planSlugHint`、`planPathPreview`

**UI 行为**

1. 解析 `planObj.create[]` → 表格列：**分类（树形下拉）| slug（input）| title | sources | 删除**
2. 新建批次 / 生成 skeleton 后：slug 默认 `rawPaths` 末段 stem
3. 选分类后显示预览：`{spaceWikiRoot}/{dirSlug}/{slug}.md`（只读）
4. 「保存 Plan」：`PUT /kb/ingest/jobs/{id}/plan` 序列化回 JSON
5. **高级**：保留现有 Plan JSON textarea（折叠），双向同步或「仅 JSON 模式」开关
6. ② 草稿列表：`displaySlug` 仍示 stem；tooltip 显示完整 `slug`

**权限**：无分类时提示「请先在文档管理创建分类（绑定目录）」链到文档管理。

---

### T17d · 文档与验收清单

**文档**

- `docs/api/KNOWLEDGE_API.md` §9.3 Plan JSON 表 + 示例（jp-fe-ap-exam + `fe` 分类）
- `moli-knowledge/kb/wiki/guides/Ingest工作台产品方案.md` §Plan 形态增补 T17
- 手测：[`docs/test/knowledge-ingest-acceptance.md`](../docs/test/knowledge-ingest-acceptance.md) §3

**E2E 验收**（jp-fe-ap-exam）

1. 文档管理新建分类：名称 FE 题库，`dir_slug=fe`，`default_type=interview`
2. Ingest 勾选 `fe/fe_kamoku_b_set_sample_qs.md`，分类选 fe，slug 保持默认
3. 生成草稿 → 批准 → Lint → Commit 并 Sync
4. 磁盘：`kb/wiki-jp-exam/fe/fe_kamoku_b_set_sample_qs.md`
5. 文档管理：该文档 `category_id` 对应 fe，浏览/索引分组正确

**非目标（本迭代不做）**

- enrich 改分类（移动分类）
- 多级分类子目录（仅一级 `dir_slug`）
- raw 自动 ingest 到多个 create 行（仍靠 Plan）

---

**开工提示词**

> 实现 T17a：读 `KbIngestServiceImpl.resolveCreateRelPath`、`KbDocumentServiceImpl.move`、`CategoryTreeVo`。Plan create 支持 `categoryId` + 裸 `slug`，落盘 `{dir_slug}/{slug}.md`；兼容旧 `type`；补单测。完成后 T17c 前端分类下拉复用 `GET /kb/category/tree`。

---

## T18 · Ingest 一键入库（Express）✅

> **目标**：Web 与 Agent ingest 同结果、更少步骤——「选 raw → 一键预览 → 确认入库」。

| 子项 | 内容 | 状态 |
|------|------|------|
| **T18a** | 后端 `expressStart` / `prepare` / `publish`；Express Plan（骨架 + `inferCategoryFromRawSource`） | ✅ |
| **T18b** | 前端列表「一键预览」、详情 Express 横幅 +「确认入库」；API types + i18n | ✅ |
| **T18c** | `docs/api/KNOWLEDGE_API.md` §9.6.6、产品方案、手测说明 | ✅ |

**API**（`/kb/ingest`，详见 `docs/api/KNOWLEDGE_API.md` §9.6.6）：

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/kb/ingest/jobs/express?useLlmPlan=false` | 创建 + Express Plan + 生成草稿 |
| POST | `/kb/ingest/jobs/{id}/prepare?useLlmPlan=false` | 已有批次 prepare |
| POST | `/kb/ingest/jobs/{id}/publish?sync=true&approveAll=true` | 全批准 + lint + commit + Sync |

**验收（jp-fe-ap-exam）**：

1. 列表勾选 `raw/school/fe/fe_kamoku_b_set_sample_qs.md` →「一键预览」
2. 详情 Express 模式：Plan create 行 `categoryId`→FE、`slug`→`fe_kamoku_b_set_sample_qs`
3.「确认入库」→ `wiki-jp-exam/fe/fe_kamoku_b_set_sample_qs.md` + Sync 成功

**非目标**：Express 不替代 Expert 逐步审阅；enrich 多页复杂批次仍建议 LLM Plan + 逐页 diff。

---

## 推荐推进顺序

1. ✅ Phase 0 治理（lint-strict + 空间去重）— 已完成。
2. **T14a → T14b**（M5 wiki 读写 + 单篇 AI 改稿底座）。
3. ✅ **T15a → T15b → T15c → T15d → T15e**（M6 Ingest 工作台闭环）。
4. T14c–d 可并行增强。
5. 🔵 **T16f**（M7 Wiki 治理前端：lint-space → script/AI/auto-fix → merge-hint → 复检 → Sync）。后端 T16a/e/g ✅。
6. ✅ **T17a → T17b → T17c → T17d**（M6+ Ingest 落盘对齐文档分类 + 自定义 slug）。
7. ✅ **T18**（M6+ Ingest 一键入库 Express 流）。
8. 🔵 **T19**（M8 平台 LLM 系统设置：DB 存 Key + 系统管理 UI）。后端 ✅；前端 T19d 📋。设计 [`docs/design/kb-llm-platform-settings.md`](../docs/design/kb-llm-platform-settings.md)

---

## T19 · 平台 LLM 系统设置（设计稿 2026-06-28）

**目标**：`kb.llm.*` 从 yaml 迁到 **Web 平台系统设置**（系统管理 → 知识库 LLM）；MySQL 加密存 api-key；Ask/Ingest/Wiki 治理共用。

| 子任务 | 内容 | 状态 |
|--------|------|------|
| **T19a** | DDL `11_kb_platform_llm_config.sql` + `KbLlmRuntime` + 加密 | ✅ |
| **T19b** | `GET/PUT/POST test` `/kb/platform/llm-config` + `kb:platform:llm` | ✅ |
| **T19c** | `KbLlmClient` 切 Runtime；回归 Ask/Ingest/Wiki | ✅ |
| **T19d** | `system/kb-llm/index` 设置页 + `12_kb_platform_llm_menu.sql` | 📋（前端） |
| **T19e** | API 契约 §3.5、前端对接文档、运维说明 | ✅ |

**非目标（一期）**：moli-ai 独立服务、按空间多套 Key、用户自带 Key。

**前端开工**：读 [`docs/api/kb-llm-platform-frontend.md`](../docs/api/kb-llm-platform-frontend.md)，按 §15 增 view + api + types + i18n；菜单由 `12_kb_platform_llm_menu.sql` + 动态路由加载。

**开工提示词（可复制给前端对话）**：

```
实现 T19d 平台 LLM 设置页（meiling-ui）：
- 先读 docs/api/kb-llm-platform-frontend.md（权威）
- 菜单 component: system/kb-llm/index，权限 kb:platform:llm
- API: GET/PUT /KnowledgeServer/kb/platform/llm-config，POST .../test
- api-key 留空=不改；测试用表单值、保存才写库
- 参考 src/views/system/DictManageView.vue 表单模式
- 不改 KbLlmToggle / Ask 页
```

> 设计 [`docs/design/kb-llm-platform-settings.md`](../docs/design/kb-llm-platform-settings.md)

---

> 多对话协作小贴士：同一时间不要让两个对话改同一个 `.java` 文件；每个任务跑完各自 `mvn -q -pl moli-knowledge/moli-knowledge-server compile` 自测；合并前 `git status` 看清改了哪些文件。
