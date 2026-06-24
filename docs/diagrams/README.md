# 茉莉项目 · draw.io 架构图

> 源文件在本目录（`.drawio`），用 [diagrams.net](https://app.diagrams.net/) 或 VS Code **Draw.io Integration** 插件打开编辑。  
> 改图后导出 PNG/SVG 供 README / 架构文档引用。

## 文件清单

| 文件 | 说明 |
|------|------|
| [`moli-container-architecture.drawio`](moli-container-architecture.drawio) | **全项目 C4 容器图**：网关 → 各微服务 → 基础设施 |
| [`moli-auth-flow.drawio`](moli-auth-flow.drawio) | **鉴权流程**：登录 Session、业务请求校验 |
| [`moli-knowledge-sync.drawio`](moli-knowledge-sync.drawio) | **知识库双轨概览**（简版） |
| [`moli-kb-architecture.drawio`](moli-kb-architecture.drawio) | **知识库系统架构**：写入轨 kb/ + 读取轨 knowledge-server |
| [`moli-kb-er.drawio`](moli-kb-er.drawio) | **ER 图**：14 张 kb_* 表及逻辑外键 |
| [`moli-kb-raw-pipeline.drawio`](moli-kb-raw-pipeline.drawio) | **RAW → 落地全链路**（L0–L6 分层 · 五条加工轨） |
| [`moli-kb-functional-flows.drawio`](moli-kb-functional-flows.drawio) | **功能流程**：Browse / Ask / Sync / Graph·Lint / Ingest |

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

安装 [draw.io Desktop](https://github.com/jgraph/drawio-desktop/releases) 后：

```powershell
& "C:\Program Files\draw.io\draw.io.exe" --export --format png --scale 2 --border 10 `
  -o "D:\work\moli_project\moli-project-distribute\docs\diagrams\png\moli-container-architecture.png" `
  "D:\work\moli_project\moli-project-distribute\docs\diagrams\moli-container-architecture.drawio"
```

批量导出见 [`export-diagrams.ps1`](export-diagrams.ps1)。

## 在文档中引用

导出后建议固定路径：

```
docs/diagrams/png/moli-container-architecture.png
docs/diagrams/png/moli-kb-architecture.png
docs/diagrams/png/moli-kb-er.png
docs/diagrams/png/moli-kb-raw-pipeline.png
docs/diagrams/png/moli-kb-functional-flows.png
docs/diagrams/png/moli-auth-flow.png
docs/diagrams/png/moli-knowledge-sync.png
```

在 [`docs/zh-CN/ARCHITECTURE.md`](../zh-CN/ARCHITECTURE.md) 可替换 Mermaid 为 PNG（按需）。

## 绘图约定（与项目一致）

- **HTTP 对外**：仅经 `moli-gateway :21000`，路径前缀 `/UserCenter` `/OrderServer` `/BiServer` `/KnowledgeServer`
- **鉴权**：`Authorization` = Shiro SessionId，共享 Redis db=2
- **服务间**：Dubbo → `user-center-server`（非 OpenFeign）
- **知识库**：markdown 唯一写入源 `kb/wiki*`，Java 只读 MySQL
