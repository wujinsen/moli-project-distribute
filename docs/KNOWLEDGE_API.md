# 企业知识库 · 前端对接 API 文档

> 更新：2026-06-21 · 后端：`moli-knowledge-server`（:8090）
> 供 `meiling-ui` 前端对接知识库模块（浏览 / Query / 图谱 / 体检 / 文档管理）。
> 表结构见 [`sql/KNOWLEDGE_SCHEMA.md`](sql/KNOWLEDGE_SCHEMA.md)；后端实现见 [`../moli-knowledge/moli-knowledge-server/README.md`](../moli-knowledge/moli-knowledge-server/README.md)。

---

## 1. 通用约定

### 1.1 访问地址 / 网关前缀

| 方式 | Base URL | 说明 |
|------|----------|------|
| 经网关（推荐，前端用这个） | `http://127.0.0.1:21000/KnowledgeServer` | 网关 `StripPrefix=1` 去掉 `/KnowledgeServer` 后转发到服务 |
| 直连服务（联调用） | `http://127.0.0.1:8090` | 绕过网关，直接打服务 |
| Swagger | `http://127.0.0.1:8090/swagger-ui.html` | 在线接口文档 |

> 前端 `meiling-ui` 通过 `VITE_API_BASE_URL` 配置 Base URL；本模块所有路径在下表均以**服务内路径**（不含 `/KnowledgeServer`）书写，经网关时自动加前缀。
> 例：浏览目录树 = `GET {VITE_API_BASE_URL}/KnowledgeServer/kb/index`。

### 1.2 鉴权

- 除登录/SSO 白名单外，接口需登录态：请求头带 **`Authorization: <token>`**（token 即 Shiro SessionId，登录后由用户中心签发）。
- `meiling-ui` 的 `src/api/http.ts` 已自动注入 `Authorization` 头，无需单独处理。

### 1.3 统一返回体 `MoliResult<T>`

```json
{ "code": 200, "msg": "成功", "data": { } }
```

| 字段 | 说明 |
|------|------|
| `code` | `200` 成功；`401/403` 鉴权失败（前端 http.ts 已统一处理）；其它为业务错误 |
| `msg`  | 提示信息 |
| `data` | 业务数据（泛型） |

分页统一用 MyBatis-Plus `Page<T>`：`data.records[]`、`data.total`、`data.current`、`data.size`。

> 默认演示空间 `spaceId = 900000000000000001`（`space_code=enterprise-kb`，公开）。  
> 日本語試験私有空间 `spaceId = 900000000000000002`（`space_code=jp-fe-ap-exam`），种子见 [`sql/04_kb_space_jp_exam.sql`](sql/04_kb_space_jp_exam.sql)。  
> 多数浏览/检索接口 `spaceId` 省略表示**当前用户可读的全部空间**（非字面「全库」）。

### 1.4 空间级权限（ACL）

所有浏览/检索/问答/编辑接口都做了空间级过滤，前端无需额外判断，越权会收到业务错误（`无权访问/编辑/管理该知识空间`）。规则：

| 维度 | 说明 |
|------|------|
| 空间可见性 `visibility` | `2` 公开（人人可读）/ `1` 内部（登录即可读）/ `0` 私有（仅成员、负责人） |
| 成员角色 `role` | `viewer` 只读 / `editor` 可编辑文档 / `admin` 可管理成员；空间 `owner_id` 等同 admin |
| 全局管理员 | 拥有 Shiro 权限 `kb:admin`（或通配 `*`）的用户对所有空间可读可写可管理 |

- **省略 `spaceId` / `spaceIds`** 的列表/检索/问答接口：后端自动收敛到「当前用户可读的空间集合」，无可读空间时返回空结果。
- **指定 `spaceId`** 时：不可读会直接报错（`无权访问该知识空间`）。
- **指定 `spaceIds[]`**（问答、文档搜索）：对每个 ID 校验读权限，仅在所列空间内检索；与 `spaceId` 同时传时 **`spaceIds` 优先**。
- 写操作（文档保存/发布/归档/删除、空间更新/删除、成员管理）按上表校验编辑/管理权限。
- ⚠️ 当前仅 **用户型成员**（`memberType=0`）在运行时生效；角色型成员（`memberType=1`）可录入但暂不参与运行时鉴权。

### 1.5 左侧菜单（getRouters）

知识库**不在前端写死路由**，由用户中心维护 `sys_menu`，登录后前端调用 **`GET /UserCenter/menu/getRouters`** 拉取整棵菜单树。

种子数据：[`docs/sql/04_knowledge_menu.sql`](sql/04_knowledge_menu.sql)（`scripts/init-db.ps1` 在导入 `03_knowledge_schema.sql` 后会自动执行）。

| menu_id | 类型 | 名称 | path | component（对齐 meiling-ui viewRegistry） |
|---------|------|------|------|-------------------------------------------|
| 900 | M 目录 | 知识库 | `/knowledge` | `Layout` |
| 901 | C 菜单 | 文档浏览 | `browse` | `knowledge/browse/index` |
| 902 | C 菜单 | 智能问答 | `ask` | `knowledge/ask/index` |
| 903 | C 菜单 | 关系图谱 | `graph` | `knowledge/graph/index` |
| 904 | C 菜单 | 健康体检 | `lint` | `knowledge/lint/index` |
| 909 | C 菜单 | 空间管理 | `spaces` | `knowledge/spaces/index` |

按钮权限（F，不出现在侧栏，供 Shiro / `v-hasPermi`）：`kb:browse:list`、`kb:ask:list`、`kb:graph:list`、`kb:lint:list`、`kb:sync:trigger`、`kb:admin`、`kb:lint:scan`、`kb:space:admin`。

`getRouters` 返回片段示例：

```json
{
  "code": 200,
  "data": [
    {
      "path": "/knowledge",
      "name": "Knowledge",
      "component": "Layout",
      "alwaysShow": true,
      "meta": { "title": "企业知识库", "icon": "knowledge" },
      "children": [
        {
          "path": "browse",
          "name": "KnowledgeBrowse",
          "component": "knowledge/browse/index",
          "meta": { "title": "文档浏览", "icon": "documentation" }
        },
        {
          "path": "ask",
          "name": "KnowledgeAsk",
          "component": "knowledge/ask/index",
          "meta": { "title": "智能问答", "icon": "query" }
        }
      ]
    }
  ]
}
```

已有库单独补菜单：

```bash
mysql -u root -p moli < docs/sql/04_knowledge_menu.sql
mysql -u root -p moli < docs/sql/04_kb_space_jp_exam.sql   # 可选：日本語試験私有空间
```

执行后**重新登录**，前端会重新拉取 `getRouters`。

---

## 2. 浏览（T3）—— 前端知识库主页面用

### 2.1 目录树 `GET /kb/index`

按知识类型分组的目录（已发布文档）。

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `spaceId` | query | 否 | 省略=可读的全部空间 |

响应 `data.groups[].items[]` 每项含 `id/slug/title/summary/spaceId`（合并多空间浏览时 **`spaceId` 必传** 给 `/kb/page`）。

```json
{
  "total": 17,
  "groups": [
    {
      "type": "guide", "label": "操作指导",
      "items": [
        { "id": 90001, "slug": "guides/本地启动指南", "title": "本地启动指南", "summary": "...", "spaceId": 900000000000000001 }
      ]
    }
  ]
}
```

> `type` 取值：`guide/service/concept/article/interview/output`（+ 兜底 `other`）。前端可据此渲染左侧分组导航。

### 2.2 单页详情 `GET /kb/page`

按 slug 取单页（slug 形如 `services/用户中心`，**含斜杠故用查询参数**，不要拼进路径）。

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `slug` | query | 是 | 如 `services/用户中心` |
| `spaceId` | query | 否 | 多空间合并浏览或 slug 冲突时**建议必传** |

响应 `data` 含 `spaceId`（所属空间），引用跳转时需一并带上。

```json
{
  "docId": 90010,
  "spaceId": 900000000000000001,
  "slug": "services/用户中心",
  "title": "用户中心",
  "summary": "...",
  "content": "# 用户中心\n...(markdown)...",
  "kbType": "service",
  "domain": "AP",
  "status": 1,
  "updateTime": "2026-06-22 14:00:00",
  "tags": ["微服务", "权限"],
  "outLinks": [ { "docId": 90011, "slug": "concepts/rbac-权限模型", "title": "RBAC 权限模型", "relationType": "related" } ],
  "backLinks": [ { "docId": 90012, "slug": "guides/本地启动指南", "title": "本地启动指南", "relationType": "links_to" } ]
}
```

> `content` 是 markdown，前端需 markdown 渲染；`[[slug]]` 形式的站内链接可点跳转到对应页。
> `outLinks/backLinks` 来自 `kb_relation`，需先跑同步脚本 `kb/tools/sync_to_db.py`，否则为空数组。

---

## 3. Query 问答（T2）—— 问答框页面用

### `POST /kb/ask`

请求体（JSON）：

```json
{ "question": "Spring 事务在什么情况下会失效？", "spaceId": 900000000000000001, "topK": 8 }
```

多空间（须对每个 space 有读权限）：

```json
{
  "question": "对比企业规范与日本语考试要点",
  "spaceIds": [900000000000000001, 900000000000000002],
  "topK": 8
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `question` | 是 | 问题 |
| `spaceId` | 否 | 单空间；省略=全部可读空间 |
| `spaceIds` | 否 | 多空间数组；非空时优先于 `spaceId` |
| `topK` | 否 | 候选页上限，默认 8 |

响应 `data`：

```json
{
  "answer": "结论...\n要点... [[interview/spring-事务]]",
  "mode": "generative",
  "scope": "[interview]",
  "scopeReason": "命中『面试题』意图 → 限 interview",
  "provider": "deepseek",
  "model": "deepseek-chat",
  "citations": [
    { "docId": 90020, "spaceId": 900000000000000001, "slug": "interview/spring-事务", "title": "Spring 事务（面试题系列）", "kbType": "interview", "snippet": "..." }
  ],
  "qaLogId": 901234567890123456
}
```

| 字段 | 说明 |
|------|------|
| `mode` | `generative`（已配 LLM，带引用作答）/ `retrieval`（未配 key，降级为检索式列出相关页） |
| `scope` / `scopeReason` | 自动识别的作用域及理由（如"面试题"→ interview） |
| `citations` | 引用来源；前端可渲染为可点链接，跳到 `/kb/page?slug=`。`answer` 里的 `[[slug]]` 也对应这些页 |
| `qaLogId` | 本次问答日志 ID，用于提交反馈 |

### `GET /kb/ask/history`

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `spaceId` | query | 否 | 省略=可读空间内全部 |
| `pageNum` / `pageSize` | query | 否 | 分页 |

响应 `data`：`Page<QaHistoryVo>`，含 `question/answer/mode/scope/citations/useful/createTime`。

### `PUT /kb/ask/feedback/{id}?useful=`

| 参数 | 说明 |
|------|------|
| `id` | 问答日志 ID（`qaLogId`） |
| `useful` | `1` 有用 / `0` 无用 |

> 后端是否走生成式取决于 `application-dev.yml` 的 `kb.llm.enabled + api-key`。前端无需关心，按 `mode` 展示即可（generative 显示富文本答案；retrieval 提示"检索式"并列出引用）。

---

## 4. 图谱与体检（T5）—— 可视化/治理页面用

### 4.1 关系图谱 `GET /kb/graph`

| 参数 | 位置 | 必填 |
|------|------|------|
| `spaceId` | query | 否 |

响应 `data`：

```json
{
  "nodes": [ { "id": "90010", "title": "用户中心", "type": "service", "deg": 6 } ],
  "links": [ { "source": "90010", "target": "90011", "type": "related" } ]
}
```

> `id` 为文档 ID 字符串；`type`（节点）= 分类名/状态；`type`（连线）= `links_to`/`same_tag`/`related`/`depends_on` 等。
> 前端可用力导向图（d3-force / echarts graph）渲染；`deg` 可映射节点大小。

### 4.2 体检（只算不落库）`GET /kb/lint`

| 参数 | 位置 | 必填 |
|------|------|------|
| `spaceId` | query | 否 |

响应 `data`：

```json
{
  "broken":   [ { "page": "90010", "title": "用户中心", "target": "不存在的页" } ],
  "orphans":  [ { "slug": "90030", "title": "孤儿页" } ],
  "noSummary":[ { "slug": "90031", "title": "缺摘要页" } ],
  "counts": { "pages": 17, "broken": 0, "orphans": 2, "noSummary": 1 }
}
```

### 4.3 体检并落库 `POST /kb/lint/scan`

参数同上（`spaceId` query 可选）。执行扫描并把问题写入 `kb_lint_issue`（清掉旧的「待处理」项后重建），返回结构同 `/kb/lint`。

### 4.4 体检问题列表 `GET /kb/lint/issues`

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `spaceId` | query | 否 | |
| `status` | query | 否 | `0`待处理 `1`已忽略 `2`已修复 |

响应 `data`：`KbLintIssue[]`，元素含 `id/spaceId/documentId/issueType/detail/status/scanTime`。

### 4.5 更新问题状态 `PUT /kb/lint/issue/{id}?status=`

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `id` | path | 是 | 问题 ID |
| `status` | query | 是 | `0`/`1`/`2` |

---

## 5. 文档 / 分类 / 标签 / 评论 / 收藏（已有 CRUD）

### 5.1 文档 `/kb/document`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/document/search` | 搜索（query：`spaceId` / `spaceIds[]` / `categoryId/keyword/status/tagId/pageNum/pageSize`），返回 `Page<KbDocument>` |
| GET | `/kb/document/{id}` | 详情 `DocumentDetailVo`（含 `tagIds`、`favorited`，会自增浏览数） |
| POST | `/kb/document` | 保存（`DocumentSaveRequest`：`id?/spaceId/categoryId/title/summary/content/docType/status/tagIds[]/changeLog`） |
| PUT | `/kb/document/{id}/publish` | 发布 |
| PUT | `/kb/document/{id}/archive` | 归档 |
| DELETE | `/kb/document/{id}` | 删除（逻辑） |
| GET | `/kb/document/{id}/versions?pageNum=&pageSize=` | 版本历史 `Page<KbDocumentVersion>` |

> 注意：`/kb/document/search` 默认使用 MySQL **ngram 全文索引**（`MATCH AGAINST`），配置 `kb.search.mode=like` 可回退旧行为。前端"知识库浏览"建议用 `/kb/index` + `/kb/page`；"管理后台搜索"用 `/kb/document/search`。

### 5.2 分类 `/kb/category`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/category/tree?spaceId=` | 分类树 `CategoryTreeVo[]` |
| POST / PUT | `/kb/category` | 创建 / 更新（body `KbCategory`） |
| DELETE | `/kb/category/{id}` | 删除 |

### 5.3 标签 `/kb/tag`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/tag/list?spaceId=` | 空间标签列表 `KbTag[]` |
| POST / PUT | `/kb/tag` | 创建 / 更新（body `KbTag`：`spaceId/tagName/color`） |
| DELETE | `/kb/tag/{id}` | 删除 |

### 5.4 评论 `/kb/comment`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/comment/page?documentId=&pageNum=&pageSize=` | 评论分页 |
| POST | `/kb/comment` | 发表（body `KbComment`：`documentId/parentId?/content`） |
| DELETE | `/kb/comment/{id}` | 删除 |

### 5.5 收藏 `/kb/favorite`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/kb/favorite/{documentId}` | 收藏 |
| DELETE | `/kb/favorite/{documentId}` | 取消 |
| GET | `/kb/favorite/my?pageNum=&pageSize=` | 我的收藏 `Page<KbDocument>` |

### 5.6 附件 `/kb/attachment`（T4）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/attachment/list?documentId=` | 文档附件列表 `KbAttachment[]` |
| POST | `/kb/attachment/upload` | 上传（`multipart/form-data`：`documentId` + `file`） |
| GET | `/kb/attachment/{id}` | 下载（直接写 response 流，非 JSON） |
| DELETE | `/kb/attachment/{id}` | 删除（软删 `kb_attachment`，MinIO 对象保留） |

上传成功响应 `data` 为 `KbAttachment`：

```json
{
  "id": 1001,
  "documentId": 900,
  "fileName": "demo.pdf",
  "objectKey": "kb/attachment/900/1001/demo.pdf",
  "fileSize": 12345,
  "contentType": "application/pdf"
}
```

> 前端上传示例：`FormData` 追加 `documentId` 与 `file` 字段；下载用 `window.open` 或带 `Authorization` 的 blob 请求。
> ⚠️ 附件接口已接入空间 ACL（读/写随文档空间权限）。

### 5.7 空间 `/kb/space`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/space/accessible` | **推荐** 当前用户可读空间列表 `KbAccessibleSpaceVo[]`（含 `canEdit`/`canAdmin`，供前端空间选择器） |
| GET | `/kb/space/page?pageNum=&pageSize=` | 分页（仅可读空间；query 可带 `KbSpace` 字段过滤） |
| GET | `/kb/space/{id}` | 详情 |
| POST / PUT | `/kb/space` | 创建 / 更新（更新需空间管理权限） |
| DELETE | `/kb/space/{id}` | 删除（需空间管理权限） |

`KbAccessibleSpaceVo` 示例：

```json
{
  "id": 900000000000000002,
  "spaceCode": "jp-fe-ap-exam",
  "spaceName": "日本語試験（FE/AP）",
  "description": "基本情報・応用情報备考",
  "visibility": 0,
  "canEdit": false,
  "canAdmin": true
}
```

### 5.8 空间成员 `/kb/space/member`（T9 · ACL）

> 均需**空间管理权限**（owner / 空间 admin / 全局 `kb:admin`）。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/space/member/list?spaceId=` | 成员列表 `KbSpaceMember[]` |
| POST | `/kb/space/member` | 添加成员 |
| PUT | `/kb/space/member` | 更新成员角色（body 带 `id` + `role`） |
| DELETE | `/kb/space/member/{id}` | 移除成员 |

添加成员请求体：

```json
{ "spaceId": 900000000000000001, "memberType": 0, "memberId": 1001, "role": "editor" }
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `spaceId` | 是 | 空间ID |
| `memberType` | 否 | `0` 用户（默认，运行时生效）/ `1` 角色（暂仅存储） |
| `memberId` | 是 | 用户ID或角色ID |
| `role` | 否 | `viewer`(默认) / `editor` / `admin` |

---

## 6. kb→DB 同步管理（T10）

> 需**空间管理权限**（owner / 空间 admin）或全局 `kb:admin`。用于把 `kb/wiki/`（或独立目录）markdown 同步进 MySQL。

CLI 多空间同步：

```bash
python moli-knowledge/kb/tools/sync_to_db.py --dry-run
python moli-knowledge/kb/tools/sync_to_db.py --wiki-dir wiki-jp-exam --space jp-fe-ap-exam
```

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/sync/logs?spaceId=&batchNo=&pageNum=&pageSize=` | 同步日志分页 `Page<KbSyncLog>` |
| GET | `/kb/sync/status?spaceId=` | 最近一批统计 `SyncStatusVo` |
| POST | `/kb/sync/trigger?spaceId=&spaceCode=` | 触发 `sync_to_db.py`，返回 `SyncTriggerVo` |

`SyncStatusVo` 示例：

```json
{
  "batchNo": "20260622153000",
  "spaceId": 900000000000000001,
  "lastSyncTime": "2026-06-22 15:30:00",
  "total": 54,
  "actionCounts": { "insert": 2, "update": 10, "skip": 42 },
  "failCount": 0
}
```

`SyncTriggerVo`：`success`、`exitCode`、`spaceCode`、`outputTail`（脚本输出末尾）。

配置见 `application-dev.yml` → `kb.sync.*`（`script-path` / `python` / `space-code` / `timeout-seconds` / `schedule-enabled` / `schedule-cron`）。

**Git hook（commit 后自动 sync）**：

```bash
# Linux / Git Bash
bash moli-knowledge/kb/tools/install_git_hook.sh

# Windows PowerShell
powershell -ExecutionPolicy Bypass -File moli-knowledge/kb/tools/install_git_hook.ps1
```

仅当 commit 变更 `moli-knowledge/kb/wiki/` 下文件时触发 `sync_to_db.py`。

### 6.1 GitHub Actions CI

工作流：[`.github/workflows/kb-sync.yml`](../.github/workflows/kb-sync.yml)

| 场景 | 行为 |
|------|------|
| PR / push（wiki 或 sync 脚本变更） | `dry-run`：解析 wiki，不连库 |
| merge 到 `main`/`master` | MySQL 容器内真实 sync + 校验文档数 |
| Actions → Run workflow → `remote` | 写远程库（需配置 Secrets） |

必填 Secrets：`KB_SYNC_DB_HOST`、`KB_SYNC_DB_USER`、`KB_SYNC_DB_PASSWORD`。

本地与 CI 同款脚本：

```bash
bash moli-knowledge/kb/tools/ci/run_sync.sh dry-run
```

---

## 7. 前端页面（meiling-ui）

| 页面 | 路由 component | 主要接口 | 备注 |
|------|----------------|----------|------|
| 文档浏览 | `knowledge/browse/index` | `/kb/space/accessible` + `/kb/index` + `/kb/page` | 顶部**空间选择器**；`spaceId` 随 API 传递；无权限时 `KbAccessDenied` |
| 智能问答 | `knowledge/ask/index` | `/kb/ask` + history/feedback | 空间选择器 + **跨空间多选**（`spaceIds[]`）；引用含 `spaceId` |
| 关系图谱 | `knowledge/graph/index` | `/kb/graph` | 按所选空间过滤 |
| 健康体检 | `knowledge/lint/index` | `/kb/lint*` + 同步 Tab | 体检与 `/kb/sync/*` 同页 |
| 空间管理 | `knowledge/spaces/index` | `/kb/space/*` + `/kb/space/member/*` | 需菜单权限 `kb:space:admin` 或空间 `canAdmin` |

前端实现：`meiling-ui/src/composables/useKbSpace.ts`（共享空间上下文）、`src/components/knowledge/KbSpaceSelector.vue`。

> 参考实现：本地零依赖 viewer `kb/tools/serve.py`（`python kb/tools/serve.py` → `http://127.0.0.1:8765`）。
