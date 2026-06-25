---
name: drawio-diagrams
description: >-
  Create and maintain draw.io (.drawio) architecture, ER, and flow diagrams
  for the Moli project. Outputs to docs/diagrams/ with moli-*.drawio naming,
  correct mxGraph XML formatting (no broken swimlane HTML), and project color
  conventions. Use when the user asks for draw.io/diagrams.net diagrams,
  architecture diagrams, ER diagrams, flowcharts, C4 containers, or says
  「用 draw.io 画」「画架构图」「画 ER 图」「画流程图」.
---

# Moli · draw.io 绘图 Skill

## 何时使用

用户要求用 **draw.io / diagrams.net** 可视化，或要画 **架构图 / ER 图 / 流程图 / 数据流图** 时，读本 skill 并直接写入 `.drawio` 源文件（Agent 写 XML，用户自行预览）。

## 输出位置与命名

| 项 | 约定 |
|----|------|
| 目录 | `docs/diagrams/` |
| 文件名 | `moli-{主题}.drawio`（小写、连字符，如 `moli-kb-er.drawio`） |
| PNG 导出 | `docs/diagrams/png/`（可选，需 draw.io Desktop 或用户手动导出） |
| 清单 | 新建/重命名后更新 `docs/diagrams/README.md` 表格 |
| 文档链接 | 必要时在相关 `ARCHITECTURE.md` / `KNOWLEDGE_SCHEMA.md` 加链接 |

## 工作流程

1. **读代码/文档** — 先查真实结构（SQL、Controller、README），禁止臆造表名/端口/路径。
2. **选参考模板** — 打开同类型已有图模仿布局（见下表）。
3. **写 `.drawio` XML** — 遵守「格式禁令」。
4. **更新 README** — 文件清单加一行。
5. **告知预览方式** — diagrams.net 在线打开，或 `hediet.vscode-drawio` 扩展。

## 参考模板（先读再画）

| 类型 | 参考文件 |
|------|----------|
| 全项目 C4 容器 | `docs/diagrams/moli-container-architecture.drawio` |
| 鉴权序列 | `docs/diagrams/moli-auth-flow.drawio` |
| ER 图（14 表） | `docs/diagrams/moli-kb-er.drawio` |
| 分层数据流 L0–L6 | `docs/diagrams/moli-kb-raw-pipeline.drawio` |
| 多功能流程（泳道分块） | `docs/diagrams/moli-kb-functional-flows.drawio` |
| 模块双轨架构 | `docs/diagrams/moli-kb-architecture.drawio` |

## 格式禁令（ER 乱码教训）

**禁止** 在 ER/实体框上使用 `swimlane` + `childLayout=stackLayout`，并把字段全写在父节点 `value` 里 —— draw.io 会溢出、HTML 标签原样显示。

**正确做法 — 实体/节点：**

```
style="rounded=1;whiteSpace=wrap;html=1;align=left;verticalAlign=top;spacingLeft=10;spacingTop=6;fontSize=11;fillColor=#dae8fc;strokeColor=#6c8ebf;fontStyle=1;"
```

- 换行用 **`&#xa;`**（XML 内），不要用 `<br>` / `<hr>` 堆在 swimlane 里
- 表名首行 + `─────────────` 分隔线 + 字段列表
- 框高足够容纳全部字段（宁大勿小）

**容器分组** 可用普通 `swimlane`（`startSize=28`），**子元素必须是独立 mxCell**，不要把多行 HTML 塞进 swimlane 的 value。

**文件头：**

```xml
<mxfile host="app.diagrams.net" agent="moli-docs" version="22.1.0" type="device">
  <diagram id="unique-id" name="Diagram Name">
    <mxGraphModel dx="1400" dy="900" grid="1" gridSize="10" pageWidth="2400" pageHeight="1600" ...>
```

## 配色（与现有图一致）

| 用途 | fillColor | strokeColor |
|------|-----------|-------------|
| 网关 / 警告 | `#fff2cc` | `#d6b656` |
| Java 服务 / 同步 | `#dae8fc` | `#6c8ebf` |
| 运行时 / 成功 | `#d5e8d4` | `#82b366` |
| Agent / UI | `#e1d5e7` | `#9673a6` |
| MySQL / 数据 | `#ffe6cc` | `#d79b00` |
| 基础设施 Nacos | `#f8cecc` | `#b85450` |
| 外部 / 虚线 | `#f5f5f5` | `#999999` dashed |
| ER 核心表 | `#fff2cc` | `#d6b656` |
| ER 空间/ACL | `#dae8fc` | `#6c8ebf` |

## 项目架构事实（画图必须对齐）

- 对外 HTTP 仅 **`moli-gateway :21000`**，前缀 `/UserCenter` `/OrderServer` `/BiServer` `/KnowledgeServer`（StripPrefix=1）
- 服务：`user-center :8888`，`order :8087`，`bi :1128`，`knowledge :8090`
- 鉴权：`Authorization` = Shiro SessionId，Redis **db=2**；服务间 **Dubbo**（非 OpenFeign）
- 知识库：markdown 唯一写入源 `kb/wiki*`，`sync_to_db.py` 单向同步 MySQL；三空间 `enterprise-kb` / `jp-fe-ap-exam` / `moli-ops-manual`

## 各图类型要点

### 架构图
- 写入轨 vs 读取轨分泳道；箭头标注协议（REST / Dubbo / JDBC）
- 基础设施放右侧：Nacos、Redis、MySQL、MinIO

### ER 图
- `kb_space` 居中上，`kb_document` 居中；虚线箭头 + `1:N` 标签
- 注明「逻辑外键、无物理 FK」；外部表 `sys_user` 用 dashed 框
- 字段来源：`docs/sql/03_knowledge_schema.sql`、`docs/sql/KNOWLEDGE_SCHEMA_ER.mmd`

### 数据流 / RAW 管道
- 用 **L0–L6 分层 swimlane**（见 `moli-kb-raw-pipeline.drawio`）
- 并行路径（A/B/C…）在同一层横向排列；直写 MySQL 用红色虚线 bypass wiki

### 功能流程
- 左→右步骤框 + 菱形判断（ACL）；API 路径写真实 endpoint
- 多条流程可同页上下分区，或 `mxfile` 多 `<diagram>` 分页

## 完成后自检

- [ ] 无裸露 HTML 标签、无文字溢出框外
- [ ] 端口/表名/路径与代码一致
- [ ] `docs/diagrams/README.md` 已更新
- [ ] 告知用户：在线 https://app.diagrams.net/ 或扩展 `hediet.vscode-drawio` 预览

## 用户怎么说（示例）

```
@drawio-diagrams 画订单模块 ER 图，表看 docs/sql/
```

```
用 draw.io 画网关路由流程，输出 docs/diagrams/moli-gateway-routes.drawio
```

```
按 drawio skill 优化 moli-kb-er.drawio 布局
```

## 可选：批量导出 PNG

```powershell
.\docs\diagrams\export-diagrams.ps1
```

需安装 [draw.io Desktop](https://github.com/jgraph/drawio-desktop/releases)。未安装时只交付 `.drawio`，不阻塞。
