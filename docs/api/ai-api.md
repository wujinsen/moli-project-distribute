# AI 服务 · API（骨架）

> 模块：`moli-ai-server` · Nacos 名 `ai-server` · **v1 仅占位**  
> 网关：`GET http://{gateway}:21000/AiServer/demo/test`

---

## 1. 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/demo/test` | 健康/demo，返回字符串 |

### 经网关

```bash
curl http://127.0.0.1:21000/AiServer/demo/test
```

期望响应 body：`test success`

---

## 2. Swagger

- 直连：`http://localhost:1128/swagger-ui.html`
- 网关：`http://localhost:21000/AiServer/swagger-ui.html`

---

## 3. v1 验收

纳入 [release-smoke-checklist.md](../test/release-smoke-checklist.md) **G4**（连通即可，无业务断言）。

---

## 4. 相关

- 模块 README：[moli-ai/README.md](../../moli-ai/README.md)
- 发布范围：[moli-v1-release-scope.md](../product/moli-v1-release-scope.md) §3.5
