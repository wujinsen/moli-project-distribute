# 茉莉项目 · draw.io 架构图

> 源文件在本目录（`.drawio`），用 [diagrams.net](https://app.diagrams.net/) 或 VS Code **Draw.io Integration** 插件打开编辑。  
> 改图后导出 PNG/SVG 供 README / 架构文档引用。  
> **Agent 绘图规范**：根 [`AGENTS.md`](../../AGENTS.md) §3 + [`.cursor/skills/drawio-diagrams/SKILL.md`](../../.cursor/skills/drawio-diagrams/SKILL.md)（对话可说 `@drawio-diagrams`）。  
> **禁止**在 `docs/`、`kb/wiki*`、模块 README 中用 ASCII 箭头图替代本目录的 draw.io 主图。

## 文件清单

| 文件 | 说明 |
|------|------|
| [`moli-container-architecture.drawio`](moli-container-architecture.drawio) | **全项目 C4 容器图**：网关 → 各微服务 → 基础设施 |
| [`moli-auth-flow.drawio`](moli-auth-flow.drawio) | **鉴权流程**：登录 Session、业务请求校验 |
| [`moli-auth-layers.drawio`](moli-auth-layers.drawio) | **鉴权分层**：网关 → 会话 → 权限 → Dubbo 五层 |
| [`moli-knowledge-sync.drawio`](moli-knowledge-sync.drawio) | **知识库双轨概览**（简版） |
| [`moli-kb-architecture.drawio`](moli-kb-architecture.drawio) | **知识库系统架构**：写入轨 kb/ + 读取轨 knowledge-server |
| [`moli-kb-er.drawio`](moli-kb-er.drawio) | **ER 图**：14 张 kb_* 表及逻辑外键 |
| [`moli-kb-raw-pipeline.drawio`](moli-kb-raw-pipeline.drawio) | **RAW → 落地全链路**（L0–L6 · 四条加工轨 · L1/L2 已废弃） |
| [`moli-kb-functional-flows.drawio`](moli-kb-functional-flows.drawio) | **功能流程**：Browse / Ask / Sync / Graph·Lint / CLI Ingest / **M6 工作台** |
| [`moli-kb-category-flow.drawio`](moli-kb-category-flow.drawio) | **分类管理流程**：分类=目录 · 创建/删除/移动/Sync 回填 · groupBy=category |
| [`moli-kb-ingest-workbench.drawio`](moli-kb-ingest-workbench.drawio) | **M6 Ingest 工作台**（T15 六步状态机 + 架构页） |
| [`moli-kb-import-entry.drawio`](moli-kb-import-entry.drawio) | **T20 双入口导入**：Editor 浏览器 Tab1/3 · Tab2 Ingest · Sync · 运维兜底虚线 |
| [`moli-kb-import-entry-api.drawio`](moli-kb-import-entry-api.drawio) | **T20 API 时序**：HTTPS multipart · Linux 磁盘写盘 · 无 Editor SSH |
| [`moli-kb-wujinsen-image-remediation.drawio`](moli-kb-wujinsen-image-remediation.drawio) | **T22 wujinsen 图片回迁**：Asset API + annex/插图节 · raw `.note_images` → Web 可见 |
| [`moli-kb-wiki-govern.drawio`](moli-kb-wiki-govern.drawio) | **M7 Wiki 治理工作台**（T16：Lint→script-fix/ai-batch-fix/auto-fix→merge-hint→复检→Sync） |
| [`moli-kb-llm-settings-flow.drawio`](moli-kb-llm-settings-flow.drawio) | **T19 平台 LLM 设置**：系统管理 UI → knowledge-server → DB/yaml → 厂商 API |
| [`moli-kb-meilisearch.drawio`](moli-kb-meilisearch.drawio) | **Meilisearch 接入规划**：索引轨（sync→reindex）+ 查询轨（ACL filter + 体裁/分类 facet） |
| [`moli-gateway-routes.drawio`](moli-gateway-routes.drawio) | **网关路由一览**：四路由 + StripPrefix + 端口 |
| [`moli-rbac-model.drawio`](moli-rbac-model.drawio) | **RBAC 模型**：用户→角色→菜单/动作 + Shiro 运行时 |
| [`moli-rbac-menu-query.drawio`](moli-rbac-menu-query.drawio) | **RBAC 菜单授权查询**：sys_user_role → MenuVo 树 · admin bypass |
| [`moli-user-center-position.drawio`](moli-user-center-position.drawio) | **用户中心定位**：网关 HTTP + Dubbo + 共享 Redis |
| [`moli-seckill-flow.drawio`](moli-seckill-flow.drawio) | **秒杀全链路**：Gateway → Lua → 队列 → MySQL |
| [`moli-deploy-topology.drawio`](moli-deploy-topology.drawio) | **本地部署拓扑**：基础设施 + 启动顺序 |

**三空间 Sync 映射**（权威操作说明：`moli-knowledge/kb/wiki-moli/ops/wiki同步指南.md` §1）：

| wiki 目录 | space_code |
|-----------|------------|
| `kb/wiki/` | `enterprise-kb` |
| `kb/wiki-moli/` | `moli-ops-manual` |
| `kb/wiki-jp-exam/` | `jp-fe-ap-exam` |

日常：`bash moli-knowledge/kb/tools/ci/run_sync.sh sync-all`

## 打开方式

### 1. 在线（零安装）

1. 打开 https://app.diagrams.net/
2. **Open Existing Diagram** → 选本仓库 `docs/diagrams/*.drawio`
3. 编辑完成后 **File → Export as → PNG**（建议缩放 200%、边框 10px）

### 2. VS Code / Cursor

扩展 ID：**`hediet.vscode-drawio`**（Marketplace 名称 *Draw.io Integration*，作者 Henning Dieterichs）。

**Cursor 里搜不到时**，用命令行安装（已在本机验证可用）：

```powershell
cursor --install-extension hediet.vscode-drawio
```

或在扩展面板搜索 **`hediet`** / **`diagrams.net`**（不要只搜 `Draw.io`，有时搜不到）。

安装后：

1. 打开 `docs/diagrams/*.drawio`
2. 若空白或报错：右键 → **Open With…** → 先选 **Text Editor** 打开一次，再 **Reopen Editor With…** → **Draw.io**
3. 导出：命令面板 `Draw.io: Export to PNG` 或在线/桌面版导出

### 3. 命令行导出 PNG（可选）

**方式 A — draw.io Desktop**（推荐批量、高分辨率）：

安装 [draw.io Desktop](https://github.com/jgraph/drawio-desktop/releases) 后运行 [`export-diagrams.ps1`](export-diagrams.ps1)，或单文件：

```powershell
& "C:\Program Files\draw.io\draw.io.exe" --export --format png --scale 2 --border 10 `
  -o "D:\work\moli_project\moli-project-distribute\docs\diagrams\png\moli-container-architecture.png" `
  "D:\work\moli_project\moli-project-distribute\docs\diagrams\moli-container-architecture.drawio"
```

**方式 B — 无桌面版时**（本机已验证）：

```powershell
npx --yes draw.io-export docs/diagrams/moli-gateway-routes.drawio -o docs/diagrams/png/moli-gateway-routes.png
```

对 `docs/diagrams/*.drawio` 逐个执行，或在 CI 中批量导出。

## 在文档中引用

导出后建议固定路径：

```
docs/diagrams/png/moli-container-architecture.png
docs/diagrams/png/moli-kb-architecture.png
docs/diagrams/png/moli-kb-er.png
docs/diagrams/png/moli-kb-raw-pipeline.png
docs/diagrams/png/moli-kb-functional-flows.png
docs/diagrams/png/moli-kb-ingest-workbench.png
docs/diagrams/png/moli-kb-wiki-govern.png
docs/diagrams/png/moli-kb-meilisearch.png
docs/diagrams/png/moli-gateway-routes.png
docs/diagrams/png/moli-rbac-model.png
docs/diagrams/png/moli-rbac-menu-query.png
docs/diagrams/png/moli-user-center-position.png
docs/diagrams/png/moli-seckill-flow.png
docs/diagrams/png/moli-deploy-topology.png
docs/diagrams/png/moli-auth-flow.png
docs/diagrams/png/moli-auth-layers.png
docs/diagrams/png/moli-knowledge-sync.png
```

在 [`docs/zh-CN/ARCHITECTURE.md`](../zh-CN/ARCHITECTURE.md) 可替换 Mermaid 为 PNG（按需）。

## 绘图约定（与项目一致）

- **HTTP 对外**：仅经 `moli-gateway :21000`，路径前缀 `/UserCenter` `/OrderServer` `/BiServer` `/KnowledgeServer`
- **鉴权**：`Authorization` = Shiro SessionId，共享 Redis db=2
- **服务间**：Dubbo → `user-center-server`（非 OpenFeign）
- **知识库**：markdown 唯一写入源 `kb/wiki*`，Java 只读 MySQL
