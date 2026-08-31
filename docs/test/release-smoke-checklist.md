# v1.0 上线冒烟清单

> **范围**：[moli-v1-release-scope.md](../product/moli-v1-release-scope.md)  
> **环境**：dev / 预发；网关 `28100`  
> **执行人**：测试 / 运维 · 更新：2026-06-28

勾选全部 **P0** 后方可标记 v1 可发布。

---

## 0. 前置

- [ ] MySQL `moli` 已导入 [`scripts/moli.sql`](../../scripts/moli.sql)
- [ ] 秒杀表：[`02_seckill_schema.sql`](../sql/02_seckill_schema.sql)
- [ ] 知识库表与菜单：`03`–`12` 增量（或 `init-db.ps1` 未 Skip）
- [ ] Redis `16379` db=1 可连（本地 Windows dev；生产环境为 `6379`）
- [ ] Nacos `28548` namespace `dev`
- [ ] 服务启动顺序：user-center → order / bi / knowledge → **gateway 最后**

---

## 1. 网关与连通（P0）

| # | 步骤 | 期望 | ✓ |
|---|------|------|---|
| G1 | `GET /OrderServer/seckill/ping` | `pong: true` | |
| G2 | `POST /UserCenter/login` admin/123456 | `code=200`，含 `token` | |
| G3 | 带 `Authorization` 访问 `/UserCenter/user/list` | 有数据或空列表，非 token 失效 | |
| G4 | `GET /AiServer/demo/test` | `test success` | |
| G5 | `GET /KnowledgeServer/kb/index?spaceId=900000000000000001` | 目录 meta 返回 | |

详见 [gateway-routes.md](../api/gateway-routes.md)。

---

## 2. 用户中心（P0）

| # | 步骤 | 期望 | ✓ |
|---|------|------|---|
| U1 | 登录 → 获取菜单 `GET /menu/getRouters` | 含知识库等菜单 | |
| U2 | 无权限接口（非 admin 账号） | `code=10009` | |
| U3 | `GET /auth/capabilities` | 返回 permissions | |
| U4 | 登出后再访问受保护接口 | token 失效 | |

自动化：`cd moli-user-center-server && mvn test`（见 [user-center.md](user-center.md)）

---

## 3. 秒杀（P0）

| # | 步骤 | 期望 | ✓ |
|---|------|------|---|
| S1 | `GET /OrderServer/seckill/activity/1` | stock > 0 | |
| S2 | `POST /OrderServer/seckill/order` 合法 body | `status=SUCCESS` | |
| S3 | 同一 userId 再次下单 | `429` DUPLICATE | |
| S4 | 等待 ~1s 查 MySQL `seckill_order` | 有对应记录 | |

详见 [order-seckill.md](order-seckill.md)。

---

## 4. 知识库（P0）

| # | 步骤 | 期望 | ✓ |
|---|------|------|---|
| K1 | Web 浏览：选空间 `enterprise-kb` | 目录树可展开 | |
| K2 | 打开任意文档页 | 正文渲染 | |
| K3 | `POST /KnowledgeServer/kb/ask` 简单问题 | 返回答案或检索式降级 | |
| K4 | Ingest：Express 预览一条 raw | 批次创建、草稿生成 | |
| K5 | Wiki 编辑：打开 slug 保存 | 磁盘文件更新 | |
| K6 | `POST /kb/wiki-moli/lint-space` | 返回 issues 列表 | |

Ingest 深度验收：[knowledge-ingest-acceptance.md](knowledge-ingest-acceptance.md)  
知识库大改后：[knowledge-e2e-regression.md](knowledge-e2e-regression.md)

---

## 5. 知识库 Sync（P0）

| # | 步骤 | 期望 | ✓ |
|---|------|------|---|
| Y1 | `run_sync.sh dry-run-all` | 两空间无 fatal | |
| Y2 | `run_sync.sh sync-all` 或 Web 触发 Sync | `kb_sync_log` 成功 | |
| Y3 | Web 健康体检扫描 | 无新增 blocker | |
| Y4 | `GET /kb/lint/issue-types` + 工单批量 API | KBOPS-8/10 字段齐全 | |

---

## 6. 可选（P1 · 不阻塞 v1）

- [ ] **DeepResearch（AI-10）**：主题调研页 + Ingest `?jobId=` 深链 — [knowledge-deep-research-smoke.md §4](knowledge-deep-research-smoke.md#4-验收勾选总表发版--功能签收)
- [ ] Wiki 治理：script-fix / auto-fix UI（T16f 未全量）
- [ ] 平台 LLM 设置页保存 Key（T19d）
- [ ] k6 压测 1k RPS 5 分钟无错误尖峰
- [ ] MinIO 附件上传

---

## 7. 失败处理

| 现象 | 查 |
|------|-----|
| token 失效 | Redis db 是否一致；user-center 是否先启动 |
| 503 / 504 | Nacos 服务是否注册；gateway 路由 |
| 知识库 403 | 空间 ACL；账号是否 editor |
| 秒杀 404 | 是否执行 `02_seckill_schema.sql`；Redis 是否初始化库存 |

排障：[wiki-moli/故障排查指南](../../moli-knowledge/kb/wiki-moli/ops/故障排查指南.md)

---

## 8. 签核

| 角色 | 姓名 | 日期 | 结果 |
|------|------|------|------|
| 测试 | | | |
| 研发 | | | |
| 运维 | | | |
