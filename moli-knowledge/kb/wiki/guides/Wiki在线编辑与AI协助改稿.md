---
title: Wiki 在线编辑与 AI 协助改稿（产品方案）
slug: Wiki在线编辑与AI协助改稿
type: guide
status: draft
tags: [知识库, wiki, AI, 编辑器, P1]
sources:
  - moli-knowledge/kb/AGENTS.md
  - moli-knowledge/kb/ROADMAP.md
  - moli-knowledge/kb/wiki/guides/AI自我进化与MD审校流程.md
  - moli-knowledge/moli-knowledge-server/src/main/java/com/moli/knowledge/server/service/impl/KbAskServiceImpl.java
related: [AI自我进化与MD审校流程, 查询与体检指南, wiki同步指南, 知识库使用指南]
created: 2026-06-24
updated: 2026-06-24
---

# Wiki 在线编辑与 AI 协助改稿（产品方案）

> **状态：draft / 待开发（T14）**  
> 目标：在 **Web 界面** 打开 wiki 页，**调用已配置的 LLM** 协助改稿；展示 **修改前/后对比**；支持 **人工继续改**；确认后 **保存回 `kb/wiki/*.md`**，再 Sync 进库。

### 与 [[AI自我进化与MD审校流程]] 的关系

| | [[AI自我进化与MD审校流程]] | **本文（T14）** |
|---|---------------------------|-----------------|
| 范围 | 全闭环：Ingest / Query / Crystallize / Lint / Sync | **仅单篇修稿**的 Web 产品化 |
| AI 改 MD | §4 **场景 B** 审校规则 + §6 Cursor 模板 | 同一审校规则 → `POST /kb/wiki/ai-revise` 的 system prompt |
| 人工确认 | `git diff` + commit | 界面 **baseline ↔ 编辑区 diff** + 保存 |
| 门禁 | `lint.py --strict`（CLI） | 保存前 diff 必看；T14d 可选服务端 lint 摘要 |
| 上线 | sync_to_db / Web Wiki 同步 | 同左；T14d「保存并 Sync」 |

**不重复写审校细则**：frontmatter、`[[slug]]`、sources、只改 wiki 等约束以 [[AI自我进化与MD审校流程]] §4 场景 B 为准；本文只写 **界面、API、权限、分阶段交付**。

Agent/Cursor 仍是 **批量 Ingest / crystallize** 主力；本功能面向 **单篇修润、体检问题修复、editor 在浏览器改 wiki**。

---

## 1. 用户故事

| 角色 | 场景 |
|------|------|
| editor | 浏览页发现内容过时 → 点「编辑」→ 手改或 AI 润色 → 看 diff → 保存 |
| 运维 | 健康体检 `broken_link` → 点「修复」→ 打开编辑页（已填 issue 上下文）→ AI 修链 → 保存 → Sync |
| 平台管理员 | 同上 + 可触发 Sync |

**不做**：在 Web 里直接改 `raw/`；不默认只写 MySQL 而不回 wiki（见 §4 铁律）。

---

## 2. 界面交互（建议）

入口：

- **文档浏览** 详情页：editor 可见 **「编辑 wiki」**
- **健康体检 · 问题列表**：每行 **「修复」**（带 `documentId` / slug / issueType / detail）

编辑页布局（三栏或 Tab）：

```
┌─────────────────────────────────────────────────────────────┐
│  slug: guides/xxx   空间: enterprise-kb    [AI 协助] [保存]   │
├──────────────┬──────────────────────────────────────────────┤
│  修改前(只读)  │  编辑区（可人工改，Markdown 源码）              │
│  (baseline)  │  + 右侧/下方：预览（渲染后）                    │
├──────────────┴──────────────────────────────────────────────┤
│  Diff 对比（保存前必看）：并排或 unified diff，高亮增删          │
└─────────────────────────────────────────────────────────────┘
```

### 2.1 AI 协助面板

- **指令输入**：如「修复断链 [[不存在的页]]」「补 summary」「根据 issue 精简段落」
- **可选上下文**（自动带入）：
  - 当前全文（frontmatter + body）
  - 来自体检：`issueType`、`detail`
  - 相邻页 slug 列表（index 同组，防乱链）
- **生成**：调用后端 `POST /kb/wiki/ai-revise` → 返回 **建议全文**（含 frontmatter）
- **应用方式**：
  - 「应用到编辑区」→ 编辑区替换为 AI 稿；**baseline 仍为打开时的原文**（diff 始终对「打开时 vs 当前编辑区」）
  - 可 **多次** AI 生成；每次应用前可预览 diff
- **降级**：LLM 未配置时按钮禁用，提示配置 `kb.llm.*`（与问答同一套）

### 2.2 保存前校验（对应 [[AI自我进化与MD审校流程]] §步骤 2–4）

1. 前端展示 **baseline ↔ 当前编辑区** diff（Web 侧等价于 §步骤 3 的 `git diff`）
2. 用户确认 → `PUT /kb/wiki/page` 写文件
3. 可选：**保存并 Sync**（§步骤 4）
4. 可选：保存后提示跑 **lint.py**（§步骤 2 门禁；T14d 可服务端摘要）

### 2.3 与「标记修复」的关系

- 保存成功后，若从 `kb_lint_issue` 进入，可弹窗：**「标记该问题为已修复？」** → `PUT /kb/lint/issue/{id}?status=2`
- **仅改状态不会修内容**；内容修复必须走本编辑页保存 wiki

---

## 3. API 设计（待实现 · T14）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/wiki/page?slug=&spaceId=` | 读 wiki 文件全文（frontmatter+body）；需空间 **editor** |
| PUT | `/kb/wiki/page` | 写 wiki 文件；body：`slug, spaceId, content, changeLog?`；需 editor |
| POST | `/kb/wiki/ai-revise` | AI 改稿建议；见下表 |
| GET | `/kb/wiki/page/history?slug=` | 🔜 P2：git 历史或版本表（可选） |

### 3.1 `POST /kb/wiki/ai-revise`

请求：

```json
{
  "slug": "guides/本地启动指南",
  "spaceId": "900000000000000001",
  "instruction": "修复 detail 中的断链，并补一段 summary",
  "baselineContent": "可选；不传则服务端读 wiki 文件",
  "issueContext": {
    "issueType": "broken_link",
    "detail": "本地启动指南 -> [[不存在的页]]"
  }
}
```

响应：

```json
{
  "suggestedContent": "---\ntitle: ...\n---\n\n# ...",
  "provider": "deepseek",
  "model": "deepseek-chat",
  "notes": "可选：AI 修改说明"
}
```

实现要点：

- 复用 `KbLlmProperties` + `KbAskServiceImpl` 中 OpenAI 兼容 HTTP 调用
- **System prompt**：与 [[AI自我进化与MD审校流程]] **§4 场景 B** 一致（`AGENTS.md` §2：frontmatter、[[slug]]、sources；只输出完整 markdown，不要解释）
- **不写盘**；仅返回建议，由用户应用后再 PUT

### 3.2 权限

| 操作 | ACL |
|------|-----|
| 读/写 wiki 文件 | 空间 **editor**（或 owner/平台超管） |
| AI 改稿 | 同上 + `kb.llm.usable()` |
| Sync | 现有 `kb:sync:trigger` 或空间 editor（与现网一致） |

动作权限（菜单）建议新增：`kb:wiki:edit`（编辑 wiki）、`kb:wiki:ai`（AI 协助，可合并到 edit）。

---

## 4. 保存写哪里（铁律）

```
Web 编辑保存
    → 写服务器 kb/wiki/{type}/{slug}.md  （权威源）
    → （用户或勾选）Wiki 同步
    → kb_document 更新
```

**不采用**「只 POST /kb/document 写 MySQL、不回 wiki」作为默认路径（与 ROADMAP 双写铁律冲突）。  
若未来需要「草稿仅 DB、确认后导出 wiki」，作为 **P2 可选**，不在 T14 范围。

wiki 路径解析：与 `sync_to_db.py` 一致，`slug` = 相对 wiki 根、去扩展名；按 `type` 目录 + 文件名定位（需与现有 1398 页 slug 规则一致，服务端维护 slug→path 映射表或扫描 index）。

---

## 5. 技术栈建议

| 层 | 选型 |
|----|------|
| 前端路由 | `knowledge/edit/index` 或浏览页抽屉/fullscreen |
| Markdown 编辑 | CodeMirror 6 或 Vditor（项目无现成依赖，T14 引入其一） |
| Diff | `diff` + `diff2html` 或 Monaco diff editor |
| 后端写文件 | `KbWikiFileService`：Java NIO 写 `kb.sync` 同级 wiki 根路径配置 `kb.wiki.root` |
| LLM | 抽取 `KbAskServiceImpl` 调用为 `KbLlmClient`，Ask 与 ai-revise 共用 |

---

## 6. 分阶段交付（T14）

| 阶段 | 范围 | 验收 |
|------|------|------|
| **T14a** | GET/PUT wiki 页 + 浏览页「编辑」+ 源码编辑 + diff + 保存 | editor 可改 wiki 文件；保存后 sync 可见 |
| **T14b** | `ai-revise` + AI 面板 + 应用建议 + diff | 配好 llm 后可 AI 改稿并保存 |
| **T14c** | 体检问题「修复」入口 + 保存后标记已修复 | 从 lint 列表跳进编辑页 |
| **T14d** | 保存并 Sync 一键；保存前服务端 lint 摘要（可选调 lint.py） | 闭环少点两次 |

---

## 7. 风险与约束

| 风险 | 缓解 |
|------|------|
| 多人同时改同一 wiki | P1 乐观锁：保存带 `contentHash` / `updated` frontmatter 比对，冲突 409 |
| AI 乱改/断链 | 保存前 diff + 可选 lint；prompt 强调 [[slug]] 约束 |
| 服务器无 git wiki 目录 | 配置 `kb.wiki.root` 指向部署机 `moli-knowledge/kb/wiki` |
| 与 Cursor Agent 并行改 | 以 **git 为准**；Web 保存 = 一次 commit 前操作 |

---

## 8. 相关文档

- 开发任务：`moli-knowledge/TASKS.md` **T14**
- 规划：`moli-knowledge/kb/ROADMAP.md` **M5**
- 前端 API 契约（实现后）：`docs/KNOWLEDGE_API.md` §Wiki 编辑

---

## 相关

[[AI自我进化与MD审校流程]] · [[wiki同步指南]] · [[查询与体检指南]] · [[知识库三操作]]
