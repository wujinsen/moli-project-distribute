# 企业知识库 · 前端对接 API 文档

> 更新：2026-06-23 · 后端：`moli-knowledge-server`（:8090）
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
> 例：浏览目录 meta = `GET {VITE_API_BASE_URL}/KnowledgeServer/kb/index`；展开分组 = `/kb/index/items`。

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
> **系统操作手册**独立空间 `spaceId = 900000000000000003`（`space_code=moli-ops-manual`，内部可见），wiki 源 `kb/wiki-ops/`，种子见 [`sql/07_kb_space_ops_manual.sql`](sql/07_kb_space_ops_manual.sql)。  
> 多数浏览/检索接口 `spaceId` 省略表示**当前用户可读的全部空间**（非字面「全库」）。

### 1.4 空间级权限（ACL）

所有浏览/检索/问答/编辑接口都做了空间级过滤，前端无需额外判断，越权会收到业务错误（`无权访问/编辑/管理该知识空间`）。规则：

| 维度 | 说明 |
|------|------|
| 空间可见性 `visibility` | 仅作元数据展示（`2` 公开 / `1` 内部 / `0` 私有），**不**自动授予读权限 |
| 内容侧访问 | 只有被分配到该空间的成员、空间 `owner_id`、平台超管可见；未分配空间的用户看不到该空间及其数据，只能看自己被分配的空间 |
| 成员角色 `role` | `viewer` 只读 / `editor` 可读可改内容（仅这两种）；空间 `owner_id` 默认可读可改 |
| 管理能力（建/改/删空间、成员授权） | 不由成员角色授予，而由系统动作权限 `kb:space:add/edit/remove/member` + 平台超管控制 |
| 平台超管 | `superadmin` / `admin` 账号（或 Shiro `*:*:*`）对所有空间可读可写可管理 |

- **省略 `spaceId` / `spaceIds`** 的列表/检索/问答接口：后端自动收敛到「当前用户可读的空间集合」，无可读空间时返回空结果。
- **指定 `spaceId`** 时：不可读会直接报错（`无权访问该知识空间`）。
- **指定 `spaceIds[]`**（问答、文档搜索）：对每个 ID 校验读权限，仅在所列空间内检索；与 `spaceId` 同时传时 **`spaceIds` 优先**。
- 写操作（文档保存/发布/归档/删除、分类/标签/评论/附件、空间更新/删除、成员管理）按上表校验编辑/管理权限。
- ⚠️ 当前仅 **用户型成员**（`memberType=0`）在运行时生效；角色型成员（`memberType=1`）可录入但暂不参与运行时鉴权。

### 1.5 左侧菜单（getRouters）

知识库**不在前端写死路由**，由用户中心维护 `sys_menu`，登录后前端调用 **`GET /UserCenter/menu/getRouters`** 拉取整棵菜单树。

种子数据：

| 脚本 | 说明 |
|------|------|
| [`docs/sql/04_knowledge_menu.sql`](sql/04_knowledge_menu.sql) | 侧栏菜单 + 角色菜单绑定（`init-db.ps1` 在 `03_knowledge_schema.sql` 后自动执行） |
| [`docs/sql/05_knowledge_action_patch.sql`](sql/05_knowledge_action_patch.sql) | **动作权限** `sys_action`（空间 CRUD/批量授权、体检扫描、Wiki 同步）；**已有库需手动补一次** |

| menu_id | 类型 | 名称 | path | component（对齐 meiling-ui viewRegistry） |
|---------|------|------|------|-------------------------------------------|
| 900 | M 目录 | 知识库 | `/knowledge` | `Layout` |
| 901 | C 菜单 | 文档浏览 | `browse` | `knowledge/browse/index` |
| 902 | C 菜单 | 智能问答 | `ask` | `knowledge/ask/index` |
| 903 | C 菜单 | 关系图谱 | `graph` | `knowledge/graph/index` |
| 904 | C 菜单 | 健康体检 | `lint` | `knowledge/lint/index` |
| 909 | C 菜单 | 空间管理 | `spaces` | `knowledge/spaces/index` |

按钮权限（`sys_action`，在「分配权限」右侧按页面分组；F 菜单 perms 不进 Shiro）：

| 页面 menu_id | 动作 perm_code |
|--------------|----------------|
| 909 空间管理 | `kb:space:add`、`kb:space:edit`、`kb:space:remove`、`kb:space:member` |
| 904 健康体检 | `kb:lint:scan`、`kb:sync:trigger` |

侧栏 C 菜单 perms：`kb:browse:list`、`kb:ask:list`、`kb:graph:list`、`kb:lint:list`、`kb:space:admin`。

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
mysql -u root -p moli < docs/sql/05_knowledge_action_patch.sql   # 动作权限（空间管理/体检按钮）
mysql -u root -p moli < docs/sql/04_kb_space_jp_exam.sql         # 可选：日本語試験私有空间
mysql -u root -p moli < docs/sql/07_kb_space_ops_manual.sql    # 可选：系统操作手册空间
```

执行后**重新登录**，前端会重新拉取 `getRouters`。

---

## 2. 浏览（T3）—— 前端知识库主页面用

### 2.1 目录 meta `GET /kb/index`

按知识类型分组的**计数**（已发布文档；**不含 items**，轻量首屏）。

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `spaceId` | query | 否 | 省略=可读的全部空间 |

响应 `data.groups[]` 含 `type/label/count`；`items` 为空数组。展开分组时调 **2.2**。

```json
{
  "total": 3308,
  "groups": [
    { "type": "article", "label": "技术文章", "count": 1155, "items": [] }
  ]
}
```

### 2.2 分组条目 `GET /kb/index/items`

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `spaceId` | query | 否 | 同 index |
| `type` | query | 是 | guide/service/concept/article/interview/output/other |
| `pageNum` | query | 否 | 默认 1 |
| `pageSize` | query | 否 | 默认 50，最大 200 |

响应轻量条目（`id/slug/title/spaceId`，无 `summary`）。

### 2.3 目录搜索 `GET /kb/index/search`

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `spaceId` | query | 否 | 同 index |
| `q` | query | 是 | 关键词（title/slug/summary LIKE） |
| `limit` | query | 否 | 默认 200，最大 500 |

### 2.4 slug 定位 `GET /kb/index/locate`

深链展开：根据 slug 找到所属 kb_type 分组，供侧栏自动展开对应分组并高亮条目。

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `slug` | query | 是 | 如 `interview/spring-事务` |
| `spaceId` | query | 否 | 多空间或 slug 冲突时建议必传 |

响应 `data`（`IndexLocateVo`）：

```json
{
  "type": "interview",
  "label": "面试题",
  "item": {
    "id": 90020,
    "slug": "interview/spring-事务",
    "title": "Spring 事务（面试题系列）",
    "summary": null,
    "spaceId": 900000000000000001
  }
}
```

> 找不到 slug 时返回业务错误；`item.summary` 在 locate 场景通常为空（轻量）。

### 2.5 单页详情 `GET /kb/page`

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

> **LLM 开关**：后端 `kb.llm.*` 配 provider/api-key 后 **`available=true`**；每次提问是否调 LLM 由请求体 **`useLlm`** 控制（默认 `false` → 检索式）。Nacos 托管模板见 [`docs/nacos/`](nacos/)（暂未启用）。

### `GET /kb/ask/llm-config`

探测后端是否已配置 LLM（**不含 api-key**），供前端决定是否可勾选 `useLlm`。

```json
{
  "available": true,
  "configEnabled": true,
  "apiKeyConfigured": true,
  "provider": "glm",
  "model": "glm-4-flash"
}
```

| 字段 | 说明 |
|------|------|
| `available` | `kb.llm.enabled && api-key` 均已配置 |

### `POST /kb/ask`

请求体（JSON）：

```json
{ "question": "Spring 事务在什么情况下会失效？", "spaceId": 900000000000000001, "topK": 8, "useLlm": false }
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
| `useLlm` | 否 | 是否启用 LLM 生成式，默认 **false**；须后端 `available=true` 才生效 |

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
| `mode` | `generative`（`useLlm=true` 且后端 LLM 可用）/ `retrieval`（默认或未启用 LLM） |
| `scope` / `scopeReason` | 自动识别的作用域及理由（如"面试题"→ interview） |
| `citations` | 引用来源；前端可渲染为可点链接，跳到 `/kb/page?slug=`。`answer` 里的 `[[slug]]` 也对应这些页 |
| `qaLogId` | 本次问答日志 ID，用于提交反馈 |

### `GET /kb/ask/history`

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `spaceId` | query | 否 | 省略=可读空间内全部 |
| `pageNum` / `pageSize` | query | 否 | 分页 |

响应 `data`：`Page<QaHistoryVo>`。

| 字段 | 说明 |
|------|------|
| `id` | 问答日志 ID（提交反馈时用） |
| `spaceId` | 所属空间（省略 spaceId 查历史时可能有多个空间的结果） |
| `question` / `answer` | 问题与答案 |
| `mode` | `generative` / `retrieval` |
| `scope` | 作用域标签 |
| `provider` / `model` | LLM 提供方与模型（检索式时可能为空） |
| `citations` | 引用列表，结构同 `POST /kb/ask` 的 `citations` |
| `useful` | `1` 有用 / `0` 无用 / `null` 未评 |
| `createTime` | 提问时间 |

```json
{
  "records": [
    {
      "id": 901234567890123456,
      "spaceId": 900000000000000001,
      "question": "Spring 事务在什么情况下会失效？",
      "answer": "结论... [[interview/spring-事务]]",
      "mode": "retrieval",
      "scope": "[interview]",
      "provider": null,
      "model": null,
      "citations": [
        { "docId": 90020, "spaceId": 900000000000000001, "slug": "interview/spring-事务", "title": "Spring 事务", "kbType": "interview", "snippet": "..." }
      ],
      "useful": 1,
      "createTime": "2026-06-22 16:00:00"
    }
  ],
  "total": 1,
  "current": 1,
  "size": 10
}
```

### `PUT /kb/ask/feedback/{id}?useful=`

| 参数 | 说明 |
|------|------|
| `id` | 问答日志 ID（`qaLogId`） |
| `useful` | `1` 有用 / `0` 无用 |

> 后端是否走生成式取决于 `application-dev.yml` 的 `kb.llm.enabled + api-key`。前端无需关心，按 `mode` 展示即可（generative 显示富文本答案；retrieval 提示"检索式"并列出引用）。

---

## 4. 图谱与体检（T5）—— 可视化/治理页面用

### 4.1 关系图谱 `GET /kb/graph`

> **大库优化（2026-06-24）**：边只读 `kb_relation`（**不再扫正文**），节点只查 `id/title/kb_type/status`（**不含 content/summary**）。默认按**度数降序**裁剪到 `maxNodes`，并返回 `meta` 统计。前端**不要**默认渲染全库，应先画核心子图，点击节点再用 §4.1.1 `ego` 展开。

| 参数 | 位置 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `spaceId` | query | 否 | 全部可读空间 | |
| `mode` | query | 否 | `full` | `full`=裁剪后子图；`summary`=只回 Top 枢纽 + 它们之间的边 |
| `maxNodes` | query | 否 | `full`=300 / `summary`=50 | 最多返回节点数（按度数降序保留），上限 2000 |
| `minDeg` | query | 否 | 0 | 仅保留度数 ≥ minDeg 的节点（过滤弱连接，减边） |

响应 `data`：

```json
{
  "nodes": [ { "id": "90010", "title": "用户中心", "type": "service", "deg": 6 } ],
  "links": [ { "source": "90010", "target": "90011", "type": "related" } ],
  "meta": {
    "totalNodes": 3308,
    "totalLinks": 12044,
    "returnedNodes": 300,
    "returnedLinks": 1820,
    "truncated": true,
    "source": "relation",
    "mode": "full"
  }
}
```

> - `id` 为文档 ID 字符串；节点 `type` = **`kb_type`**（guide/service/concept/article/interview/output，与浏览分组一致；缺省回退分类名/状态）；连线 `type` = `links_to`/`same_tag`/`related`/`depends_on` 等。
> - `deg` 可映射节点大小；`meta.truncated=true` 表示还有更多节点未返回（可提示用户用搜索或 ego 展开）。
> - `meta.source`：`relation`=读已落库边；`runtime`=relation 表为空时回退运行时解析（仅小库/未同步时出现）。
> - 节点数 > 几百时，前端建议默认 `mode=summary` 或带 `minDeg=2`，再配合 ego 探索。

#### 4.1.1 邻域子图 `GET /kb/graph/ego`

以某文档为中心做 BFS（探索式，点击节点再拉），逐层查 `kb_relation`，**不加载全图**。

| 参数 | 位置 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `spaceId` | query | 否 | 全部可读空间 | |
| `docId` | query | **是** | — | 中心文档 ID |
| `depth` | query | 否 | 1 | 跳数，1~3 |
| `maxNodes` | query | 否 | 200 | 子图节点上限（上限 2000） |

响应结构同 `/kb/graph`（含 `nodes/links/meta`，`meta.mode=ego`）。

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
| GET | `/kb/document/search` | 管理侧文档搜索，返回 `Page<KbDocument>` |
| GET | `/kb/document/{id}` | 详情 `DocumentDetailVo`（会自增浏览数） |
| POST | `/kb/document` | 保存（`DocumentSaveRequest`） |
| PUT | `/kb/document/{id}/publish` | 发布 |
| PUT | `/kb/document/{id}/archive` | 归档 |
| DELETE | `/kb/document/{id}` | 删除（逻辑） |
| GET | `/kb/document/{id}/versions?pageNum=&pageSize=` | 版本历史 `Page<KbDocumentVersion>` |

> ⚠️ 文档接口已接入空间 ACL：搜索自动过滤不可读空间；详情/版本需读权限；保存/发布/归档/删除需编辑权限。

#### `GET /kb/document/search` 参数（`DocumentSearchRequest`）

| 参数 | 位置 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `spaceId` | query | 否 | — | 单空间；与 `spaceIds` 同时传时 **`spaceIds` 优先** |
| `spaceIds` | query | 否 | — | 多空间数组，如 `spaceIds=1&spaceIds=2` |
| `categoryId` | query | 否 | — | 分类 ID |
| `keyword` | query | 否 | — | 关键词 |
| `status` | query | 否 | — | `0` 草稿 / `1` 已发布 / `2` 已归档 |
| `tagId` | query | 否 | — | 按标签过滤 |
| `pageNum` | query | 否 | `1` | 页码 |
| `pageSize` | query | 否 | `10` | 每页条数 |

**检索模式**（`application-dev.yml` → `kb.search.mode`）：

| 值 | 行为 |
|----|------|
| `fulltext`（默认） | MySQL ngram 全文索引 `MATCH AGAINST`；索引异常时**自动降级**三字段 `LIKE` |
| `like` | 始终用 `title`/`summary`/`content` 的 `LIKE` |

> 前端「知识库浏览」侧栏用 `/kb/index` + `/kb/index/items|search|locate` + `/kb/page`；管理后台文档搜索用本接口。

#### `GET /kb/document/{id}` 响应示例（`DocumentDetailVo`）

```json
{
  "id": 90010,
  "spaceId": 900000000000000001,
  "categoryId": 900000000000000103,
  "slug": "services/用户中心",
  "kbType": "service",
  "domain": "AP",
  "source": "kb",
  "title": "用户中心",
  "summary": "用户中心服务说明",
  "content": "# 用户中心\n...(markdown)...",
  "docType": "markdown",
  "status": 1,
  "viewCount": 42,
  "likeCount": 0,
  "versionNo": 3,
  "publishTime": "2026-06-10 12:00:00",
  "createId": 1,
  "createTime": "2026-06-09 10:00:00",
  "tagIds": [900001, 900002],
  "favorited": false
}
```

| 字段 | 说明 |
|------|------|
| `slug` | 空间内唯一路径，wiki 同步页有值 |
| `kbType` | 知识类型：guide/service/concept/article/interview/output |
| `domain` | 领域标签（如 AP/FE） |
| `source` | `kb`（wiki 同步）/ `manual`（手工创建） |
| `favorited` | 当前登录用户是否已收藏 |

#### `POST /kb/document` 请求体（`DocumentSaveRequest`）

```json
{
  "id": null,
  "spaceId": 900000000000000001,
  "categoryId": 900000000000000103,
  "title": "新文档",
  "summary": "摘要",
  "content": "# Hello",
  "docType": "markdown",
  "status": 0,
  "tagIds": [900001],
  "changeLog": "初稿"
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `id` | 否 | 有值=更新，无值=新建 |
| `spaceId` | 是 | 目标空间（需编辑权限） |
| `status` | 否 | 默认草稿 `0` |
| `tagIds` | 否 | 标签 ID 列表 |
| `changeLog` | 否 | 版本变更说明 |

### 5.2 分类 `/kb/category`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/category/tree?spaceId=` | 分类树 `CategoryTreeVo[]` |
| POST / PUT | `/kb/category` | 创建 / 更新（body `KbCategory`） |
| DELETE | `/kb/category/{id}` | 删除 |

> ⚠️ 已接入空间 ACL：`tree` 需空间读权限；增删改需空间编辑权限。`spaceId` 不可读时直接报错。

### 5.3 标签 `/kb/tag`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/tag/list?spaceId=` | 空间标签列表 `KbTag[]` |
| POST / PUT | `/kb/tag` | 创建 / 更新（body `KbTag`：`spaceId/tagName/color`） |
| DELETE | `/kb/tag/{id}` | 删除 |

> ⚠️ 已接入空间 ACL：列表需读权限；增删改需编辑权限。

### 5.4 评论 `/kb/comment`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/comment/page?documentId=&pageNum=&pageSize=` | 评论分页 |
| POST | `/kb/comment` | 发表（body `KbComment`：`documentId/parentId?/content`） |
| DELETE | `/kb/comment/{id}` | 删除 |

> ⚠️ 已接入空间 ACL：按文档所属空间校验——分页/发表需读权限；删除需**编辑**权限。

### 5.5 收藏 `/kb/favorite`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/kb/favorite/{documentId}` | 收藏 |
| DELETE | `/kb/favorite/{documentId}` | 取消 |
| GET | `/kb/favorite/my?pageNum=&pageSize=` | 我的收藏 `Page<KbDocument>` |

> ⚠️ 已接入空间 ACL：收藏/取消需文档读权限；「我的收藏」仅返回当前用户可读空间内的文档。

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
| GET | `/kb/space/mine` | **推荐** 当前用户可读空间列表 `KbAccessibleSpaceVo[]`（`canEdit`=内容编辑能力，供浏览/问答空间选择器） |
| GET | `/kb/space/manage` | 空间管理页列表（菜单 `kb:space:admin` 或任一 `kb:space:*` 动作=**全部空间**；`canEdit`/`canAdmin` 反映动作权限，供按钮显隐） |
| GET | `/kb/space/page?pageNum=&pageSize=` | 分页（仅可读空间；query 可带 `KbSpace` 字段过滤） |
| GET | `/kb/space/{id}` | 详情 |
| POST | `/kb/space` | 创建（动作 `kb:space:add`） |
| PUT | `/kb/space` | 更新（动作 `kb:space:edit`） |
| DELETE | `/kb/space/{id}` | 删除（动作 `kb:space:remove`） |

> **权限分层**：菜单 `kb:space:admin` 决定能否进入管理页并看到空间数据；动作 `kb:space:add/edit/remove/member` 控制具体按钮与对应写接口；空间成员角色（仅 viewer/editor）只作用于**内容侧**（浏览/问答/文档编辑），不参与管理页操作鉴权。平台超管（`superadmin`/`admin`/`*:*:*`）全通过。

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

> 均需 **`kb:space:member` 动作权限**（或平台超管）；与空间成员角色无关——成员管理是管理页能力，由动作权限控制。
> 雪花 ID 在 JSON 中建议用**字符串**传递（与全局 Long 序列化策略一致）。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/space/member/list?spaceId=` | 成员列表 `KbSpaceMember[]` |
| POST | `/kb/space/member` | 添加**单个**成员，返回成员行 `id` |
| POST | `/kb/space/member/batch` | **批量添加**成员，返回 `KbSpaceMemberBatchResult` |
| PUT | `/kb/space/member` | 更新成员角色（body 带 `id` + `role`） |
| DELETE | `/kb/space/member/{id}` | 移除**单个**成员 |
| POST | `/kb/space/member/batch/remove` | **批量移除**成员，返回 `KbSpaceMemberBatchResult` |

添加成员（单条）请求体：

```json
{ "spaceId": "900000000000000001", "memberType": 0, "memberId": "719712653013942272", "role": "editor" }
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `spaceId` | 是 | 空间ID |
| `memberType` | 否 | `0` 用户（默认，运行时生效）/ `1` 角色（暂仅存储） |
| `memberId` | 是 | 用户ID或角色ID |
| `role` | 否 | `viewer`(默认) / `editor`（仅这两种；管理能力走系统权限，不在此授予） |

批量添加请求体：

```json
{
  "spaceId": "900000000000000001",
  "memberType": 0,
  "memberIds": ["719712653013942272", "720351341083361280"],
  "role": "viewer"
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `spaceId` | 是 | 空间ID |
| `memberIds` | 是 | 用户ID或角色ID列表（非空） |
| `memberType` | 否 | 同单条添加，默认 `0` |
| `role` | 否 | 同单条添加，默认 `viewer` |

批量移除请求体：

```json
{ "ids": ["900000000000000501", "900000000000000502"] }
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `ids` | 是 | `kb_space_member` 表主键 ID 列表（非空） |

批量操作响应 `KbSpaceMemberBatchResult`：

```json
{
  "successCount": 2,
  "skipCount": 0,
  "failCount": 0,
  "memberRowIds": ["900000000000000501", "900000000000000502"]
}
```

| 字段 | 说明 |
|------|------|
| `successCount` | 成功数（添加：新增或恢复软删成员；移除：成功软删） |
| `skipCount` | 跳过数（添加：已是有效成员；移除：不存在或已删） |
| `failCount` | 失败数 |
| `memberRowIds` | 本次成功涉及的成员行 ID |

> 软删成员再次添加时，单条/批量接口均会**恢复**原记录并更新 `role`，避免唯一键冲突。

---

## 6. kb→DB 同步管理（T10）

> 需**空间管理权限**（owner / 空间 admin）或平台超管。用于把 `kb/wiki/`（或独立目录）markdown 同步进 MySQL。

CLI 多空间同步：

```bash
python moli-knowledge/kb/tools/sync_to_db.py --dry-run
python moli-knowledge/kb/tools/sync_to_db.py --wiki-dir wiki-jp-exam --space jp-fe-ap-exam
python moli-knowledge/kb/tools/sync_to_db.py --wiki-dir wiki-ops --space moli-ops-manual
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

### 6.1 同步配置 `kb.sync.*`

| 配置项 | 默认（dev） | 说明 |
|--------|-------------|------|
| `kb.sync.enabled` | `true` | 总开关；`false` 时 API trigger 与定时任务均不执行 |
| `kb.sync.python` | `python` | Python 解释器 |
| `kb.sync.script-path` | `moli-knowledge/kb/tools/sync_to_db.py` | 同步脚本路径（相对进程 cwd） |
| `kb.sync.space-code` | `enterprise-kb` | 默认同步目标空间编码 |
| `kb.sync.timeout-seconds` | `300` | 脚本超时 |
| `kb.sync.schedule-enabled` | **`false`** | 是否启用定时同步 |
| `kb.sync.schedule-cron` | `0 0 2 * * ?` | 定时 cron（默认每天 02:00） |

### 6.2 定时同步 `KbSyncScheduler`（T11）

Spring `@Scheduled` 任务，**默认关闭**。与 Git hook、手动 `POST /kb/sync/trigger` 互不排斥，可并存：

| 方式 | 触发时机 | 权限 |
|------|----------|------|
| **定时任务** | `schedule-enabled=true` 且 `kb.sync.enabled=true` 时按 cron 跑 | 无需登录（服务端后台） |
| **Git hook** | commit 变更 `kb/wiki/` 后 post-commit | 本地开发机 |
| **API trigger** | 前端/运维手动调 | 空间 admin 或平台超管 |
| **GitHub Actions** | CI push main / 手动 workflow | 见 §6.3 |

定时任务调用与 API 相同的 `sync_to_db.py`，使用 `kb.sync.space-code` 指定空间；日志写入 `kb_sync_log`，可通过 `/kb/sync/logs` 查询。

### 6.3 Git hook（commit 后自动 sync）

```bash
# Linux / Git Bash
bash moli-knowledge/kb/tools/install_git_hook.sh

# Windows PowerShell
powershell -ExecutionPolicy Bypass -File moli-knowledge/kb/tools/install_git_hook.ps1
```

仅当 commit 变更 `moli-knowledge/kb/wiki/` 下文件时触发 `sync_to_db.py`。

### 6.4 AI 自我进化（wiki MD 审校 → Lint → Sync）

Web **不提供**「AI 改 MD」能力；推荐在 **Cursor Agent** 中改 `kb/wiki/*.md`，用 **`lint.py` 门禁**通过后再 Sync。

**完整手册**（含 Ingest/Lint/Sync 分工、Crystallize、Web 体检与 Sync 区别、`kb_lint_issue` 后续操作）：

`moli-knowledge/kb/wiki/guides/AI自我进化与MD审校流程.md`

**范式**：LLM-Wiki — 知识在 wiki **编译一次、持续保鲜**；自我进化 = Ingest + Query/crystallize + Lint + Sync 闭环（非无人值守乱改库）。

**推荐顺序**（不要颠倒）：

1. AI/人工修改 `kb/wiki/**/*.md`（Ingest / crystallize / 单篇审校；只改 wiki，不改 raw）
2. `python moli-knowledge/kb/tools/lint.py --strict`（wiki 文件级门禁，先于 Sync）
3. `git diff` 人工确认
4. `sync_to_db.py --dry-run` → 正式 sync，或 Web **健康体检 → Wiki 同步 → 触发同步**
5. （可选）Web **扫描并落库**（写 `kb_lint_issue`）+ **智能问答** 验证；问答缺口 → crystallize 回到步骤 1

**易混淆**：

| Web 按钮 | 实际作用 |
|----------|----------|
| **扫描并落库**（质量体检 Tab） | DB 体检问题 → `kb_lint_issue`；**不是** Sync |
| **触发同步**（Wiki 同步 Tab） | `kb/wiki/` → `kb_document` |
| 同步一直提示成功 | 正常；hash 未变时全 `skip`，看 insert/update 数量 |

契约层说明见 `moli-knowledge/kb/AGENTS.md` §8。

### 6.5 GitHub Actions CI（T12）

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
| 文档浏览 | `knowledge/browse/index` | `/kb/space/mine` + `/kb/index`（meta）+ `/kb/index/items|search|locate` + `/kb/page` | 顶部**空间选择器**；分组懒加载；`spaceId` 随 API 传递 |
| 智能问答 | `knowledge/ask/index` | `/kb/ask` + history/feedback | 空间选择器 + **跨空间多选**（`spaceIds[]`）；引用含 `spaceId` |
| 关系图谱 | `knowledge/graph/index` | `/kb/graph` | 按所选空间过滤 |
| 健康体检 | `knowledge/lint/index` | `/kb/lint*` + 同步 Tab | 体检与 `/kb/sync/*` 同页 |
| **Wiki 编辑** | `knowledge/edit/index` 🔜 | `/kb/wiki/page` + `/kb/wiki/ai-revise` | 浏览/体检「编辑/修复」入口；diff + AI 改稿 + 保存 wiki（T14） |
| 空间管理 | `knowledge/spaces/index` | `/kb/space/*` + `/kb/space/member/*` | 需菜单权限 `kb:space:admin` 或空间 `canAdmin` |

前端实现：`meiling-ui/src/composables/useKbSpace.ts`（共享空间上下文）、`src/components/knowledge/KbSpaceSelector.vue`。

> 参考实现：本地零依赖 viewer `kb/tools/serve.py`（`python kb/tools/serve.py` → `http://127.0.0.1:8765`）。

---

## 8. Wiki 在线编辑 + AI 协助改稿（T14 · 规划中）

> 产品方案：[`kb/wiki/guides/Wiki在线编辑与AI协助改稿.md`](../moli-knowledge/kb/wiki/guides/Wiki在线编辑与AI协助改稿.md)。  
> **保存铁律**：写回服务器 `kb/wiki/*.md`，再 Sync；不默认只写 `kb_document`。

### 8.1 读/写 wiki 文件

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/wiki/page?slug=&spaceId=` | 返回 wiki 全文（frontmatter + body）；需空间 **editor** |
| PUT | `/kb/wiki/page` | 写 wiki 文件；body 见下 |

**PUT 请求体（草案）**

```json
{
  "slug": "guides/本地启动指南",
  "spaceId": "900000000000000001",
  "content": "---\ntitle: ...\n---\n\n# ...",
  "changeLog": "修复断链",
  "contentHash": "可选；乐观锁，冲突 409"
}
```

**响应**：`{ "slug", "savedAt", "contentHash" }`

配置：`kb.wiki.root` 指向部署机 wiki 目录（与 `sync_to_db.py` 同源）。

### 8.2 AI 协助改稿

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/kb/wiki/ai-revise` | 调 `kb.llm.*`；**不写盘**，仅返回建议全文 |

**请求体（草案）**

```json
{
  "slug": "guides/本地启动指南",
  "spaceId": "900000000000000001",
  "instruction": "修复 detail 中的断链",
  "baselineContent": "可选；不传则服务端读 wiki",
  "issueContext": {
    "issueType": "broken_link",
    "detail": "..."
  }
}
```

**响应**：`{ "suggestedContent", "provider", "model", "notes?" }`

### 8.3 权限

| 操作 | ACL |
|------|-----|
| GET/PUT wiki | 空间 **editor**（或 owner / 平台超管） |
| ai-revise | 同上 + `kb.llm.usable()` |

菜单动作（种子 SQL 待补）：`kb:wiki:edit`。
