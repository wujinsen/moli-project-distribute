# 茉莉 Monorepo · Agent 规则索引

> **全仓库唯一入口**（user-center / order / bi / gateway / knowledge 等所有模块共用）。  
> 各微服务**不需要**再各写一份 `AGENTS.md`；模块差异写在 `moli-xxx/README.md` 与 `docs/` 下。

---

## 1. 规则分层（读哪个）

| 层级 | 路径 | 作用域 |
|------|------|--------|
| **L0** | [`.cursor/rules/`](.cursor/rules/) | Cursor 自动注入；改对应文件时生效 |
| **L1** | **本文件** `AGENTS.md` | 全 monorepo：文档落点、draw.io、微服务 README |
| **L2** | [`moli-knowledge/kb/AGENTS.md`](moli-knowledge/kb/AGENTS.md) | **仅** `kb/` 下 Ingest / Query / Lint / sync |
| **L3** | [`.cursor/skills/`](.cursor/skills/) | 专项能力（如 [`drawio-diagrams`](.cursor/skills/drawio-diagrams/SKILL.md)） |

**决策**

```
任务涉及 kb/raw、wiki ingest、sync？     → 先读 L2（kb/AGENTS.md）
任务涉及任意模块文档 / 架构图 / API 说明？ → 读 L1（本文件）+ docs/README.md
需要画架构 / ER / 流程图？               → 读 L3 drawio skill，禁止只写 ASCII 主图
```

---

## 2. 文档放哪里（全项目）

权威地图：[`docs/README.md`](docs/README.md)（五类文档 + 微服务归属 §1.2 摘要在 kb/AGENTS §1.2）。

| 类型 | 工程契约 | 浏览/成品 |
|------|----------|-----------|
| PRD | `docs/product/` 索引 | `kb/wiki/guides/` |
| 技术方案 | `docs/design/`、`docs/zh-CN/` | `kb/wiki/concepts/`、`articles/` |
| API | **`docs/api/`** | `kb/wiki/services/` 摘要 + 链接 |
| 测试 | `docs/test/`、`load-test/` | `kb/wiki/guides/` |
| 运维 | `docs/ops/` 索引 | **`kb/wiki-ops/guides/`** |
| 单服务 | **`moli-xxx/README.md`** | `kb/wiki/services/{服务名}.md` |

---

## 3. draw.io 绘图（全项目文档强制）

**凡文档里需要「架构 / 部署 / 调用链 / ER / 业务流程 / 状态机」类图，必须用 draw.io，并调用 skill，不得用 ASCII / Mermaid 作为主展示。**

### 3.1 Agent 必做

1. **先读** [`.cursor/skills/drawio-diagrams/SKILL.md`](.cursor/skills/drawio-diagrams/SKILL.md)（对话可说 `@drawio-diagrams`）。
2. **写源文件** → `docs/diagrams/moli-{主题}.drawio`。
3. **导出 PNG** → `docs/diagrams/png/`（`export-diagrams.ps1` 或 `npx draw.io-export`）。
4. **Markdown 嵌入** PNG + 链到 `.drawio`；更新 [`docs/diagrams/README.md`](docs/diagrams/README.md)。
5. ASCII 仅允许放在 `<details>` 作备查，**不能**替代主图。

### 3.2 适用路径（所有模块）

- `docs/**/*.md`（含 `design/`、`zh-CN/`、`sql/`）
- `moli-knowledge/kb/wiki/**/*.md`、`wiki-ops/**/*.md`
- `moli-*/README.md`（user-center、order、bi、gateway、knowledge…）
- 根目录 `README*.md`

### 3.3 不必 draw.io 的情况

- 纯文字 API 字段表、配置键值表
- 单行命令、目录树（``` 树形 ```）
- 已有 draw.io 可链式引用、无需重画

### 3.4 反例 → 正例

| ❌ 不要 | ✅ 要 |
|--------|------|
| §1 定位用 ASCII 箭头链作为主图 | `moli-user-center-position.drawio` + PNG 嵌入 |
| 新建 ER 只用 Mermaid 块 | `moli-xxx-er.drawio`，MD 引 PNG |
| 在 wiki 里画 ASCII 网关拓扑 | 复用 `moli-gateway-routes.drawio` 或新建后嵌入 |

---

## 4. 各微服务如何被约束（无需 per-service AGENTS.md）

1. **Cursor L0 规则** — 编辑 `docs/`、`kb/wiki*`、模块 `README` 时自动带上 draw.io 规则。
2. **本文件 L1** — 任何 Agent 任务先认 monorepo 入口。
3. **模块 README** — 各服务第一稿 + 文档索引（见 [`moli-user-center/README.md`](moli-user-center/README.md) 范例）。
4. **kb L2** — Ingest 时 enrich `wiki/services/`，不复制 `docs/api/` 全文。
5. **Skill L3** — 画图时读 skill，保证端口/表名与代码一致。

未来若拆多 Git 仓：各仓复制 **L0 + L1 摘要 + drawio skill**；**L2 仍只在知识库仓**。

---

## 5. 相关

- 文档总览：[`docs/README.md`](docs/README.md)
- 图清单：[`docs/diagrams/README.md`](docs/diagrams/README.md)
- 知识库 Ingest：[`moli-knowledge/kb/AGENTS.md`](moli-knowledge/kb/AGENTS.md)
