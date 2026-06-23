# Nacos 配置模板（预留）

> 当前 LLM 开关由 **前端请求 `useLlm`** 控制；后端仍读本地 `application-*.yml` 的 `kb.llm.*`。  
> 以下模板供后续启用 Nacos 托管时使用（`kb.llm.nacos.enabled=true` + bootstrap extension-configs）。

| 文件 | DataId | 说明 |
|------|--------|------|
| [`knowledge-server-kb-llm-dev.yaml`](knowledge-server-kb-llm-dev.yaml) | `knowledge-server-kb-llm-dev.yaml` | 知识库 LLM（`kb.llm.*`） |
