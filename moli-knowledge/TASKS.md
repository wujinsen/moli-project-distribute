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
| 同步 | ✅ sync API + git hook + 定时任务 + **GitHub Actions CI(T12)** | — |
| Java API | ✅ CRUD、Query(+**历史/反馈 T11**)、Browse、Graph/Lint、ACL、附件(+**列表 T11**)、**MySQL ngram 全文检索（M4：browse+ask 走 MATCH AGAINST，ask 全文召回 top-N+内存精排）** | Meilisearch/向量（召回/量级信号触发再上） |
| 文档 | ✅ **`docs/KNOWLEDGE_API.md`(T8)** 含附件 API §5.6 + **菜单 getRouters(T13)** | — |
| kb 知识 | ✅ **1398 页 wiki** + 关系边；**Agent 治理自动化 [`kb/tools/lint.py`](kb/tools/lint.py)**（分级体检+CI report-only） | 治理 lint 报告（66 断链/988 孤儿）；CI 升级 lint-strict 门禁 |
| 前端 meiling-ui | ✅ **T6 已完成**（2026-06-22） | 空间 CRUD 管理页（可选二期） |

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
> - **同步管理**：`GET /kb/sync/logs`、`GET /kb/sync/status`、`POST /kb/sync/trigger`（调 `sync_to_db.py`，需空间 admin 或 `kb:admin`）。

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

## T5 · 图谱/体检落库重构 ✅ 已完成（2026-06-22）

> 产出：`KbInsightServiceImpl` 重构（graph 优先读 `kb_relation`，空表回退运行时）；新增 `scan`/`issues`/`updateIssueStatus`；`KbInsightController` 加 `POST /kb/lint/scan`、`GET /kb/lint/issues`、`PUT /kb/lint/issue/{id}`。返回结构与 GraphVo/LintVo 兼容；`mvn compile` 通过。

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
  - 全局管理员：Shiro 权限串 `kb:admin`（或通配 `*`）一票通过。
- **统一接口** `KbAclService`：`canRead/canEdit/canAdmin` + `assertCanRead/assertCanEdit/assertCanAdmin` + `accessibleSpaceIds()`。
- **接入点**（service 层统一过滤，非散落在 controller）：
  - `KbSpaceServiceImpl`：列表只回可读空间；`getById` 断言可读；`update/delete` 断言可管理。
  - `KbDocumentServiceImpl`：`search` 按可读空间集合过滤 / 指定空间断言可读；`detail`/`versions` 断言可读；`save/publish/archive/delete` 断言可编辑。
  - `KbBrowseServiceImpl`：`index` 过滤可读空间；`page` 断言可读。
  - `KbAskServiceImpl`：候选页限定到可读空间；无可读空间直接回退。
- **成员管理 API**（`/kb/space/member`，均需空间管理权限）：`GET /list`、`POST`（单条）、`POST /batch`（批量添加）、`PUT`、`DELETE /{id}`（单条）、`POST /batch/remove`（批量移除）。
- **已知限制**：Dubbo 契约目前只透出权限串、不透出角色ID，**角色型成员(member_type=1)** 仅支持存储/管理，运行时不解析（用户型成员完整生效）。待 `UserCenterServer` 暴露角色后，在 `KbAclServiceImpl#memberRole` 处补一行即可。
- **验收**：非成员看不到私有空间内容；editor 以上才能改；过滤在 service 层统一做。✅

---

## 推荐推进顺序

1. **先开 T1**（地基，半天内能完成）。
2. T1 完成后，**并行开 T2 / T3 / T4 / T5**（各自新增文件为主）。
3. **全程可并行**：T6（前端）、T7（ingest）、T8（API 文档）——它们和后端代码不抢文件。
4. **最后单独做 T9**（ACL，改动面大）。

> 多对话协作小贴士：同一时间不要让两个对话改同一个 `.java` 文件；每个任务跑完各自 `mvn -q -pl moli-knowledge/moli-knowledge-server compile` 自测；合并前 `git status` 看清改了哪些文件。
