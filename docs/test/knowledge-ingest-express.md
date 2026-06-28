# Ingest Express（T18）手测

> 前置：知识库服务已启动；账号对目标空间有 **editor**；LLM 可选（Express Plan 不依赖 LLM）。

## 1. 一键预览

1. 打开 **知识库 → Ingest 工作台**
2. 选择空间 `wiki-jp-exam`（或含 `fe` 分类的空间）
3. 勾选 `raw/fe/fe_kamoku_b_set_sample_qs.md`
4. 填写主题，点击 **一键预览**
5. 期望：跳转到批次详情，`?express=1`；Plan create 含 `categoryId`（FE）、`slug=fe_kamoku_b_set_sample_qs`；至少 1 页草稿

## 2. 确认入库

1. 在 Express 横幅点击 **确认入库**，确认路径列表
2. 期望：`committed=true`；磁盘 `kb/wiki-jp-exam/fe/fe_kamoku_b_set_sample_qs.md`；批次状态 `committed`
3. 文档管理可浏览该页，`category_id` 对应 FE

## 3. Lint 阻塞（负例）

1. 手工改草稿引入断链或 frontmatter 错误
2. 再次 **确认入库**
3. 期望：`committed=false`；页面展示 lint ERROR；wiki 文件未覆盖

## 4. API 直调（可选）

**鉴权**：`POST http://127.0.0.1:8888/login`（或经网关 `21000/UserCenter/login`）拿 `data.token`，后续请求头 `Authorization: <token>`。

**rawPaths**：与 raw 树节点一致，**不含** `raw/` 前缀，例 `fe/fe_kamoku_b_set_sample_qs.md`。

**服务地址**：开发机 knowledge-server 直连 `8090`；经网关为 `21000/KnowledgeServer`。

```bash
# 登录
curl -X POST "http://127.0.0.1:8888/login" \
  -H "Content-Type: application/json" \
  -d '{"userName":"admin","password":"123456"}'

# 一键预览
curl -X POST "http://127.0.0.1:8090/kb/ingest/jobs/express?useLlmPlan=false" \
  -H "Content-Type: application/json" \
  -H "Authorization: <token>" \
  -d '{"spaceId":"900000000000000002","topic":"FE 样题","rawPaths":["fe/fe_kamoku_b_set_sample_qs.md"]}'

# 确认入库
curl -X POST "http://127.0.0.1:8090/kb/ingest/jobs/<JOB_ID>/publish?sync=true&approveAll=true" \
  -H "Authorization: <token>"
```

> **分类推断**：空间需存在 `dir_slug=fe` 的分类（文档管理新建），Plan 才会带 `categoryId` 并落盘 `wiki-jp-exam/fe/...`；否则走 legacy `type=article` → `articles/`。

契约详见 [`docs/api/KNOWLEDGE_API.md`](../api/KNOWLEDGE_API.md) §9.6.6。
