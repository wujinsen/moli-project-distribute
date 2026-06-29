# KB Sync / Ask 代码审查纪要（2026-06-25）

## 背景

本次审查聚焦近期改动中与知识库问答与同步链路相关的实现，目标是识别：

- 功能缺口（是否满足“多空间 Web Sync”等目标）
- 潜在缺陷（CI 可靠性、回归风险）
- 可优化点（校验覆盖率、验收口径）

## 审查范围

- `.github/workflows/kb-sync.yml`
- `moli-knowledge/kb/tools/ci/run_sync.sh`
- `moli-knowledge/moli-knowledge-server/src/main/java/com/moli/knowledge/server/service/impl/KbAskServiceImpl.java`
- `moli-knowledge/moli-knowledge-server/src/main/java/com/moli/knowledge/server/service/KbLlmClient.java`

## 主要发现（按严重级别）

### P1（高）PR 阶段未完整覆盖多空间校验

**现象**

- workflow 已监听 `wiki-ops` / `wiki-jp-exam` 目录变更；
- 但 `dry-run` 与 `lint-strict` 仍只跑默认 `wiki` 空间。

**影响**

- PR 可以通过，但主分支 `sync-all` 阶段才暴露 wiki-ops / wiki-jp-exam 问题；
- 增加“合并后失败”的概率，降低门禁有效性。

**建议**

- dry-run 任务改为 `run_sync.sh dry-run-all`；
- 新增 `run_sync.sh lint-all`（分别对 `wiki`、`wiki-ops`、`wiki-jp-exam` 执行 `lint.py --strict`）；
- workflow 中将 lint 步骤切到 `lint-all`。

### P2（中）remote 手动同步仍是单空间

**现象**

- `sync-mysql`（CI）已改为 `sync-all`；
- `sync-remote`（workflow_dispatch/remote）仍执行 `run_sync.sh sync`。

**影响**

- “多空间同步”在不同入口语义不一致；
- 人工触发 remote 时可能误以为已覆盖全部空间。

**建议**

- 若目标是统一行为：remote 也改为 `sync-all`；
- 若暂时只允许主空间：在 workflow 描述中明确“remote 仅 enterprise-kb”。

### P2（中）verify 口径过宽，无法证明多空间成功

**现象**

- `verify` 仅校验 `kb_document` 中 `source='kb'` 总量是否大于 0。

**影响**

- 即使某个空间同步失败，也可能被其他空间数据掩盖；
- 对“多空间成功”的验收证据不足。

**建议**

- 按 `space_code` 分别校验（至少各空间 `>=1`）；
- 或按本次变更涉及空间做最小针对性校验。

## 代码质量结论（Ask 链路）

- `KbAskServiceImpl` 中 LLM 调用迁移到 `KbLlmClient` 属于正向收敛，复用性与可维护性提升；
- 现有降级逻辑（LLM 失败回退 retrieval）仍保留，未见明显功能回归；
- 本轮主要风险集中在 CI 门禁覆盖与验收口径，而非 Ask 主逻辑正确性。

## 建议落地顺序

1. 先补 `run_sync.sh` 的 `lint-all`，并在 workflow 启用 `dry-run-all + lint-all`；
2. 再统一 `sync-remote` 与 `sync-mysql` 的多空间策略；
3. 最后增强 `verify` 为按空间校验，形成可追溯验收。

## 备注

- 本纪要为当前代码状态下的静态审查结论；
- 大量文档内容本身（业务正确性）不在本次逐条审校范围内。
