# 知识库 · T15 generate SSE 技术方案

> **状态**：design · 2026-07-12  
> **产品背景**：[`Ingest工作台产品方案.md`](../../moli-knowledge/kb/wiki-moli/develop/Ingest工作台产品方案.md) §4.2（初版 PRD：generate 异步 + SSE；当前为同步 HTTP）  
> **任务跟踪**：[`moli-knowledge/TASKS.md`](../../moli-knowledge/TASKS.md) **T15** · 后续子项建议 **T15f**  
> **时序图**：[`docs/diagrams/moli-kb-ingest-generate-sse.drawio`](../diagrams/moli-kb-ingest-generate-sse.drawio)  
> **API 契约（实现时补章）**：[`docs/api/KNOWLEDGE_API.md`](../api/KNOWLEDGE_API.md) §9.4.1

---

## 1. 背景与问题

### 1.1 现状（T15b 已实现）

| 项 | 现状 |
|----|------|
| 接口 | `POST /kb/ingest/jobs/{id}/generate?resume=&useLlmGenerate=` |
| 行为 | **同步**：按 Plan 逐页调 LLM（或模板直贴），内存攒草稿，**事务末**一次性写 `kb_ingest_draft` |
| 前端 | `generateKbIngestDraftsApi`，`timeoutMs: 300_000`（5 分钟），全程 loading |
| 单批上限 | `kb.ingest.max-pages-per-batch` 默认 **15** |
| job 状态 | 完成后 `reviewing`；实体注释含 `generating`，**代码未使用** |

### 1.2 痛点

1. **UX**：15 页 LLM 批次可能 2–5 分钟无进度，用户不知道卡在哪一页。
2. **超时**：网关 / 浏览器 / 反向代理可能早于 300s 断连，导致「前端失败、后端仍在跑」。
3. **Express 一键**：`prepare → generate` 长链路更难感知中间状态。
4. **与 PRD 差距**：产品方案 §4.2 已记录「同步 HTTP + 续跑弥补」为已知简化。

### 1.3 目标

| 目标 | 说明 |
|------|------|
| **G1** | 生成过程**逐页推送进度**（当前页 slug、成功/跳过/失败、累计计数） |
| **G2** | HTTP 连接断开时，**生成任务可继续**（异步），支持**重连订阅** |
| **G3** | **兼容**现有同步 `POST .../generate`（小批次 / 脚本 / 旧前端） |
| **G4** | 不破坏 T15e **断点续跑**语义（`resume=true` 跳过已有 content 的草稿） |

### 1.4 非目标（本期不做）

- 单页 `draft/regenerate` 的 SSE（仍保持同步，单页耗时短）
- Plan 生成的 SSE（Plan 通常 <30s）
- 跨实例分布式任务调度（首版单节点内存任务表即可；多副本见 §6.3）
- WebSocket 双向通道（SSE 单向足够）

---

## 2. 方案选型

### 2.1 候选

| 方案 | 描述 | 优点 | 缺点 |
|------|------|------|------|
| **A. 同步改流式 POST** | `POST .../generate` 直接 `produces=text/event-stream` | 改动小 | 断线即丢订阅；难重连 |
| **B. 启动 + SSE 订阅** ✅ | `POST .../generate/start` 立即返回 `taskId`；`GET .../generate/stream` 推事件 | 异步、可重连、job 可标 `generating` | 多一个 task 生命周期 |
| **C. 轮询** | `GET .../generate/status` 每 2s 拉进度 | 实现简单 | 非 PRD 方向；LLM 场景延迟差 |

**推荐 B**，与初版 PRD「异步 + SSE」一致，并复用 `KbIngestJob.status=generating`（已预留未用）。

### 2.2 总体架构

```
meiling-ui  Ingest 工作台
    │  POST /generate/start  →  { taskId, total }
    │  GET  /generate/stream?taskId=  (SSE / fetch stream)
    ▼
moli-gateway  (/KnowledgeServer/**)
    │  SSE：禁用缓冲、延长 read timeout（见 §5）
    ▼
moli-knowledge-server
    KbIngestController
    KbIngestGenerateTaskService   ← 新增：任务注册、SSE 广播
    KbIngestServiceImpl           ← 重构：generate 核心循环抽 generatePages(ProgressSink)
    @Async ingestGenerateExecutor
    ▼
kb_ingest_draft（P0 仍批量落库；P1 可逐页 upsert）
```

主时序见 [`moli-kb-ingest-generate-sse.drawio`](../diagrams/moli-kb-ingest-generate-sse.drawio)。

---

## 3. API 设计

### 3.1 新增：启动异步生成

**`POST /kb/ingest/jobs/{id}/generate/start`**

| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| `resume` | boolean | `false` | 同现有 generate |
| `useLlmGenerate` | boolean | `true` | 同现有 generate |

**前置校验**（与同步版相同）：

- 空间 **editor**
- 已有 Plan 且 `create`+`enrich` 非空
- `total ≤ max-pages-per-batch`
- 同一 `jobId` **同时仅允许 1 个 running task**（否则 409 + 返回已有 `taskId`）

**响应** `MoliResult<IngestGenerateStartVo>`：

```json
{
  "code": 200,
  "data": {
    "taskId": "7f3c2a1b-9e4d-4c8a-b012-3456789abcde",
    "jobId": 900000000000000100,
    "total": 8,
    "resume": false,
    "templateMode": false,
    "status": "running"
  }
}
```

**副作用**：

- `kb_ingest_job.status` → `generating`
- 内存注册 `IngestGenerateTask`（见 §4.1）

### 3.2 新增：SSE 进度流

**`GET /kb/ingest/jobs/{id}/generate/stream`**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `taskId` | string | **是** | start 返回的 UUID |
| `accessToken` | string | 否* | 鉴权令牌（EventSource 无法带 Header 时的兜底） |

\* 推荐前端用 **fetch + ReadableStream** 解析 SSE，继续走 `Authorization` Header（与现有 `http.ts` 一致）。`accessToken` query 仅作 EventSource 降级路径，需评估安全策略（短效 token / 仅内网）。

**响应**：`Content-Type: text/event-stream`，`Cache-Control: no-cache`，`Connection: keep-alive`

**SSE 事件**（`event` 名 + `data` JSON）：

| event | 时机 | data 字段 |
|-------|------|-----------|
| `started` | 任务开始 | `taskId`, `jobId`, `total`, `resume`, `templateMode`, `llmFallback?` |
| `page_start` | 开始处理一页 | `index`（0-based）, `slug`, `action`（`create`/`enrich`） |
| `page_done` | 单页结束 | `slug`, `outcome`（`generated`/`skipped`/`failed`）, `message?` |
| `progress` | 计数变化 | `generated`, `skipped`, `failed`, `done`, `total` |
| `complete` | 全部结束 | 同 `IngestGenerateResultVo`（含 `drafts` 摘要或仅统计 + 提示拉 `/drafts`） |
| `error` | 不可恢复失败 | `code`, `message` |
| `cancelled` | 用户取消 | `message` |

**`complete` 示例**：

```json
{
  "total": 8,
  "generated": 6,
  "skipped": 1,
  "failed": 1,
  "resume": false,
  "templateMode": false,
  "llmFallback": false
}
```

> **载荷控制**：`complete` 默认**不附带**全量 `drafts[]`（避免 SSE 最后一帧过大）；前端收到后调已有 `GET /drafts` 刷新列表。若需兼容可设 `includeDrafts=true` query（默认 false）。

### 3.3 新增：查询任务 / 取消（可选 P0.5）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/jobs/{id}/generate/tasks/{taskId}` | 断线后拉快照：`status` + 最近 `progress` |
| POST | `/jobs/{id}/generate/tasks/{taskId}/cancel` | 协作式取消（见 §4.3） |

### 3.4 保留：同步 generate（兼容）

**`POST /kb/ingest/jobs/{id}/generate`** — **行为不变**，内部可复用 `generatePages()` 无 sink。

标记 `@Deprecated` 于 Swagger 描述即可，**不删**至前端全量切流式。

---

## 4. 后端实现要点

### 4.1 任务注册表（单节点 P0）

```java
// 伪代码
class IngestGenerateTask {
    String taskId;
    Long jobId;
    volatile TaskStatus status; // RUNNING | COMPLETED | FAILED | CANCELLED
    IngestGenerateProgress progress;
    SseEmitter emitter; // 或 CopyOnWriteArrayList<SseEmitter> 支持多订阅者
    Future<?> future;
    volatile boolean cancelRequested;
}
```

- 容器：`ConcurrentHashMap<String, IngestGenerateTask>`
- TTL：任务终态后保留 **30min** 供重连查询，然后移除
- 限制：每 `jobId` 最多 1 个 `RUNNING`

### 4.2 核心循环重构

将 `KbIngestServiceImpl.generate()` 拆为：

```text
generate(jobId, resume, useLlm)           // 同步入口，sink=null
generateAsync(jobId, resume, useLlm, task) // @Async，带 ProgressSink
generatePages(ctx, sink)                  // create/enrich 双循环
```

每页前后调用 sink：

```java
sink.onPageStart(index, slug, action);
// genCreateDraft / genEnrichDraft
sink.onPageDone(slug, outcome, message);
sink.onProgress(generated, skipped, failed, total);
```

**P0 落库策略**：与现网一致 — 全部页处理完后 **事务内**批量替换 `kb_ingest_draft`，再 `job.status=reviewing`。  
SSE 仅改善**可观测性**，不改变数据一致性模型。

**P1 增强（可选）**：每 `page_done(generated)` 即 **upsert** 单条 draft，断线后 `resume=true` 可真正从中间页继续，无需重跑已成功页。

### 4.3 协作式取消

- `cancelRequested=true` 后，循环在**下一页开始前**退出
- 已生成页：P0 丢弃（未 commit 到 DB）；P1 保留已 upsert 的草稿
- 发 `cancelled` 事件；`job.status` 回 `planned` 或保持 `reviewing`（若已有历史草稿）

### 4.4 线程池

```yaml
kb:
  ingest:
    generate:
      async-enabled: true
      core-pool-size: 2
      max-pool-size: 4
      queue-capacity: 8
      sse-timeout-ms: 600000   # SseEmitter 10min
```

`@EnableAsync` + 命名线程池 `ingestGenerateExecutor`，避免占满 Tomcat 请求线程。

### 4.5 错误与并发

| 场景 | 行为 |
|------|------|
| LLM 单页失败 | `page_done failed`，继续下一页（与现网一致） |
| 全量模式全部失败 | `error` 事件 + 保留旧草稿 + `job` 回 `planned` |
| 重复 start | 409，`data.existingTaskId` |
| 空间权限变更 | 任务启动时已校验 editor；运行中不重复校验 |

---

## 5. 网关与反向代理

SSE 经过 `moli-gateway` 时需避免缓冲与过早超时：

| 层 | 配置 |
|----|------|
| **Spring Cloud Gateway** | 对 `/KnowledgeServer/kb/ingest/**/generate/stream` 路由：`metadata.response-timeout` 调至 ≥600s；如用 Netty，确认不聚合 SSE 帧 |
| **Nginx**（若有） | `proxy_buffering off;`、`proxy_read_timeout 600s;`、`chunked_transfer_encoding on;`（见 `kb/wiki/middleware/nginx-限流与缓冲调优.md`） |

开发环境直连 `8090` 可绕过网关先验收。

---

## 6. 前端改造（meiling-ui）

### 6.1 API 层

新增 `kbIngest.ts`：

```typescript
/** 启动异步生成 */
export async function startKbIngestGenerateApi(jobId, opts)

/** SSE 订阅（推荐 fetch stream + Authorization） */
export function subscribeKbIngestGenerateStream(
  jobId: string | number,
  taskId: string,
  handlers: {
    onEvent: (event: string, data: unknown) => void
    onError?: (err: Error) => void
    signal?: AbortSignal
  },
)
```

实现要点：

- 使用 `fetch(url, { headers: { Authorization } })` + 读 `response.body` 按 `\n\n` 解析 SSE 帧（**不用**原生 `EventSource`，以便带 Token）
- 网关前缀：`${VITE_API_BASE_URL}/KnowledgeServer/kb/ingest/...`

### 6.2 UI：`KnowledgeIngestWorkbenchView.vue`

| 区域 | 改动 |
|------|------|
| 生成按钮 | `generateDrafts()` → `start` + `subscribe` |
| 进度条 | `progress.done / progress.total`，当前 slug 副标题 |
| 失败页 | 列表展示 `page_done failed` 的 slug，保留「续跑生成」 |
| Express | `setExpressProgressStage('generate')` 期间消费 `progress` 事件 |
| 降级 | `subscribe` 失败或 404 → fallback 同步 `generateKbIngestDraftsApi` + Toast 提示 |

### 6.3 断线重连

1. 用户刷新页面 → `GET /jobs/{id}` 若 `status=generating`，读 `tasks/{taskId}` 或 localStorage 存 `taskId`
2. 重新 `subscribe`：服务端补发当前 `progress` + 后续事件

---

## 7. 分阶段交付

| 阶段 | 范围 | 验收 |
|------|------|------|
| **P0** | start + stream + 循环抽 sink；**批量落库**不变；前端进度条 | 8 页 LLM 批次可见逐页进度；断线后任务仍完成；`complete` 后 drafts 列表正确 |
| **P0.5** | task 查询 + cancel + 重连 | 刷新页面可恢复进度条 |
| **P1** | 逐页 upsert draft + 真·中间续跑 | 第 5 页失败后续跑只从第 6 页开始 |
| **P2** | Redis 任务状态（多副本 knowledge-server） | K8s 双 Pod 下 stream 挂任意实例可订阅 |

建议任务编号：**T15f · generate SSE**。

---

## 8. 测试计划

### 8.1 后端单测

| 类 | 用例 |
|----|------|
| `KbIngestGenerateTaskServiceTest` | 注册/重复 start 409/TTL 清理 |
| `KbIngestServiceImplGenerateSinkTest` | sink 回调顺序；skipped/failed 计数 |
| `KbIngestControllerGenerateSseApiTest` | stream 首帧 `started`、末帧 `complete` |

### 8.2 集成 / 手工

| # | 步骤 | 预期 |
|---|------|------|
| 1 | Plan 8 页 → start → stream | 收到 8 次 `page_start`/`page_done`，`complete.generated≥1` |
| 2 | `resume=true` 二次 start | `skipped` 增加，不重复调 LLM |
| 3 | 生成中关浏览器 | 后端跑完；`job.status=reviewing`；草稿齐全 |
| 4 | 经 Gateway stream | 无缓冲，事件实时到达 |
| 5 | 同步 `POST .../generate` | 行为与升级前一致 |

### 8.3 文档

- `KNOWLEDGE_API.md` §9.4.1 增补 start/stream
- `ingest-workbench-frontend.md` 前端对接说明
- `TASKS.md` 增 T15f 行
- `Ingest工作台产品方案.md` §4.2 差异表更新为「已实现 SSE」

---

## 9. 风险与对策

| 风险 | 对策 |
|------|------|
| Gateway 缓冲导致 SSE 成块到达 | 专项路由 + `proxy_buffering off` |
| 内存任务表随重启丢失 | P0 接受；P2 Redis；重启时 `generating` job 标 `planned` + 运维说明 |
| `complete` 带全量 drafts 过大 | 默认不含，前端拉 `/drafts` |
| 与 Express prepare 嵌套超时 | prepare 的 generate 子步改用 start+stream |

---

## 10. 工作量粗估

| 模块 | 人日 |
|------|------|
| 后端 P0（task + sink + SSE） | 2–3 |
| 前端 P0（stream 解析 + 进度 UI） | 1–2 |
| 网关 / 联调 | 0.5–1 |
| 测试 + 文档 | 1 |
| **合计 P0** | **约 5–7 人日** |

P1 逐页落库另计 2 人日。

---

## 11. 相关文件（实现时）

| 类型 | 路径 |
|------|------|
| Controller | `KbIngestController.java` |
| Service | `KbIngestServiceImpl.java`、`KbIngestGenerateTaskService.java`（新） |
| DTO | `IngestGenerateStartVo.java`、`IngestGenerateProgressVo.java`（新） |
| 配置 | `KbIngestProperties.java`、`application-dev.yml` |
| 前端 | `meiling-ui/src/api/knowledge/kbIngest.ts`、`KnowledgeIngestWorkbenchView.vue` |
| 产品 | `kb/wiki-moli/develop/Ingest工作台产品方案.md` |
