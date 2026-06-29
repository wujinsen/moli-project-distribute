# 订单服务 · 秒杀 API

> 模块：`moli-order-server` · 基础路径：`/seckill`  
> 网关：`POST http://{gateway}:21000/OrderServer/seckill/...`  
> 直连：`http://localhost:8087/seckill/...`  
> 设计：[order-seckill-design.md](../design/order-seckill-design.md)

统一响应：`MoliResult<T>`（`code=200` 成功）。

---

## 1. 接口一览

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/seckill/ping` | 无 | 连通性探测 |
| GET | `/seckill/activity/{activityId}` | 无 | 查活动库存（Redis） |
| POST | `/seckill/order` | 无* | 秒杀下单（Lua 扣库存） |
| GET | `/seckill/metrics` | 无 | 压测计数 |
| POST | `/seckill/admin/init` | 无** | 初始化活动（仅 loadtest） |

\* v1 压测场景不强制登录；生产应加网关鉴权。  
\** `moli.seckill.load-test-mode=true` 时可用，否则 `403`。

---

## 2. GET `/seckill/ping`

**响应示例**

```json
{
  "code": 200,
  "data": { "pong": true, "ts": 1719567890123 }
}
```

---

## 3. GET `/seckill/activity/{activityId}`

**响应 `data`（SeckillActivityVo）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `activityId` | Long | 活动 ID |
| `name` | String | 活动名 |
| `stock` | Long | 当前 Redis 库存 |
| `sold` | Long | 已售（DB 异步汇总） |
| `status` | Integer | 1=active |

活动不存在 → `code=404`，`msg=activity not found`。

---

## 4. POST `/seckill/order`

**请求体**

```json
{
  "activityId": 1,
  "userId": "user-10001",
  "requestId": "optional-idempotency-key"
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `activityId` | 是 | 活动 ID |
| `userId` | 是 | 用户标识（压测可用虚拟 ID） |
| `requestId` | 否 | 幂等键；空则服务端生成 |

**成功 `data`（SeckillOrderResult）**

```json
{
  "status": "SUCCESS",
  "orderId": "1234567890123456789",
  "activityId": 1,
  "remainStock": 999999
}
```

**业务错误**

| HTTP 业务 code | status | 含义 |
|----------------|--------|------|
| 409 | SOLD_OUT | 库存不足 |
| 429 | DUPLICATE | 同一用户重复抢购 |
| 400 | INVALID_PARAM | 参数非法 |
| 404 | ACTIVITY_NOT_FOUND | 活动不存在 |

`status` 枚举：`SUCCESS` | `SOLD_OUT` | `DUPLICATE` | `ACTIVITY_NOT_FOUND` | `INVALID_PARAM`

---

## 5. GET `/seckill/metrics`

**响应 `data`**：Redis Hash 计数（如 `success`、`sold_out`、`duplicate` 等），具体键见 `SeckillService.metrics()`。

---

## 6. POST `/seckill/admin/init`

**Query 参数**

| 参数 | 必填 | 说明 |
|------|------|------|
| `activityId` | 是 | 活动 ID |
| `stock` | 否 | 库存，默认配置值 |
| `name` | 否 | 活动名称 |

仅 **loadtest** profile；否则 `403 load-test mode disabled`。

---

## 7. curl 示例（经网关）

```bash
# 探测
curl "http://127.0.0.1:21000/OrderServer/seckill/ping"

# 查活动
curl "http://127.0.0.1:21000/OrderServer/seckill/activity/1"

# 下单
curl -X POST "http://127.0.0.1:21000/OrderServer/seckill/order" \
  -H "Content-Type: application/json" \
  -d '{"activityId":1,"userId":"u1","requestId":"req-001"}'
```

---

## 8. Swagger

- 直连：`http://localhost:8087/swagger-ui.html`
- 网关：`http://localhost:21000/OrderServer/swagger-ui.html`

---

## 9. 相关

- DDL：[02_seckill_schema.sql](../sql/02_seckill_schema.sql)
- 手测：[order-seckill.md](../test/order-seckill.md)
- 压测：[load-test/README.md](../../load-test/README.md)
