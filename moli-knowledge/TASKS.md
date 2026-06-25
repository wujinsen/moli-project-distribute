# 企业知识库 · 待办任务清单（可并行开工）

> 更新：2026-06-25
> 用途：每个任务**自包含、文件边界清晰**，可在不同对话框/工作区并行开工，尽量不互相冲突。
> 范式与分工见 [`kb/ROADMAP.md`](kb/ROADMAP.md)；表结构见 [`../docs/sql/KNOWLEDGE_SCHEMA.md`](../docs/sql/KNOWLEDGE_SCHEMA.md)。
>
> 开一个新对话时：**先贴该任务的「开工提示词」**，它会让那个对话先读必要文档再动手。

---

## 现状速览

| 模块 | 已完成 | 未完成 |
|------|--------|--------|
| 表结构 | ✅ 14 张表 SQL + 设计文档；**14 个 entity/mapper 全部就绪**（T1 完成） | — |
| 同步 | ✅ sync API + git hook + 定时任务 + **GitHub Actions CI(T12)** | — |
| Java API | ✅ CRUD、Query(+**历史/反馈 T11**)、Browse（**meta 目录 + 分组分页/搜索/locate**）、Graph/Lint、ACL、附件(+**列表 T11**)、**MySQL ngram 全文检索（M4）** | Meilisearch/向量（召回/量级信号触发再上） |
| 文档 | ✅ **`docs/KNOWLEDGE_API.md`(T8)** 含附件 API §5.6 + **菜单 getRouters(T13)** | — |
| kb 知识 | ✅ **375 页** wiki（Phase 0 治理后）；**`lint-strict` CI 门禁**；`wiki-ops` 运维空间独立 | M5 T14 单篇编辑 |
| 前端 meiling-ui | ✅ **T6 已完成**（2026-06-22）；**T15 Ingest 工作台 UI**（2026-06-25） | 空间 CRUD（可选二期） |

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

## T14 · Web Wiki 在线编辑 + AI 协助改稿 ✅ 已完成（T14a–d）

> 产品方案：[[Wiki在线编辑与AI协助改稿]]（`kb/wiki/guides/`）。规划里程碑 **M5**。

**目标**：在 Web 界面打开 wiki 页，调用已配置 LLM 协助改稿；展示修改前/后 diff；支持人工继续改；确认后保存回 `kb/wiki/*.md`，再 Sync 进库。

| 子任务 | 范围 | 验收 |
|--------|------|------|
| **T14a ✅** | 后端 `GET/PUT /kb/wiki/page`（`kb.wiki.*` 三空间根映射 + 乐观锁 + 防穿越）；meiling-ui `KnowledgeWikiEditView` 编辑/预览/行级 diff + 浏览页「编辑 wiki」入口 | editor 可改 wiki 文件；保存后 Sync 可见 |
| **T14b ✅** | `POST /kb/wiki/ai-revise`（`KbLlmClient` + 场景 B prompt）；编辑页 AI 面板 + 应用建议 + diff | 配好 llm 后可 AI 改稿并保存 |
| **T14c ✅** | 体检「修复」→ 编辑页（issue 上下文）；保存后可选标记已修复 | lint 列表 → 编辑 → 保存 → status=2 |
| **T14d ✅** | 「保存并 Sync」；`POST /kb/wiki/page/lint-preview` 保存前摘要 | 少点 Tab；预检断链/frontmatter |

- **涉及文件**：
  - server：✅ T14a–d：`KbWikiController`、`KbWikiFileService`、`KbWikiAiReviseService`、`KbLlmClient`、DTO 全套
  - meiling-ui：✅ `KnowledgeWikiEditView`（AI/lint/sync）、`KnowledgeLintView` 修复入口、`KnowledgeBrowseView` 编辑 wiki
  - 文档：✅ `docs/api/KNOWLEDGE_API.md` §8；🔜 `docs/sql` 菜单种子（`kb:wiki:edit`）
- **依赖**：T9 ACL（editor）、T2 LLM 配置、T3 `/kb/page`（读库展示可复用 slug）
- **铁律**：保存目标 = **wiki 文件**，不是默认 `POST /kb/document` 双写

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

## 推荐推进顺序

1. ✅ Phase 0 治理（lint-strict + 空间去重）— 已完成。
2. **T14a → T14b**（M5 wiki 读写 + 单篇 AI 改稿底座）。
3. ✅ **T15a → T15b → T15c → T15d → T15e**（M6 Ingest 工作台闭环）。
4. T14c–d 可并行增强。

> 多对话协作小贴士：同一时间不要让两个对话改同一个 `.java` 文件；每个任务跑完各自 `mvn -q -pl moli-knowledge/moli-knowledge-server compile` 自测；合并前 `git status` 看清改了哪些文件。
