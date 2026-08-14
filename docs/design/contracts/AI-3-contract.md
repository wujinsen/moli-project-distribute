# AI-3 Eval 回归看板 + CI 门禁 · 施工契约（Opus 产出，Composer 施工）

> **角色**：Opus（架构与门禁阈值策略负责人）产出，Composer 施工的**唯一契约**。
> **任务**：AI-3 评测结果落库 + `KbOpsService`「检索质量趋势」卡片 + CI 回归门禁，第 1 波 W4。
> **主导**：🟢 **Composer**（表+mapper+落库、看板卡片、CI yaml 为样板量最大三块）；**Opus 仅拍板「门禁基线阈值策略」（§1）**，其余给出契约由 Composer 铺量。
> **状态**：**done** · 2026-07-19 · Opus §6 验收通过（六项全绿，红线未破）
> **上游**：[`../kb-hybrid-retrieval.md`](../kb-hybrid-retrieval.md) §4 · [`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4「AI-3」·「§4.1/4.2/4.3」 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §2 W4 · §9.1 AI-3 · [`AI-1-contract.md`](AI-1-contract.md)（评测字段/report JSON 口径）· [`AI-2-contract.md`](AI-2-contract.md)（三档基线数据）
> **现有落地（复用，勿重造）**：`eval_ask.py`（已有 `--strategy` / `--gate-at-k` / `--min-hit` 与 report JSON）· `KbOpsController` `/kb/ops/dashboard` + `KbOpsDashboardVo`（卡片模式）· `kb_llm_call_log`（审计表 DDL 参照）· `.github/workflows/kb-sync.yml`（CI 模式）· 权限 `kb:ops:dashboard`

---

## 0. 契约边界（读我）

**本契约定义**：① 门禁基线阈值策略（gate 指标/容差/更新协议——**Opus 拍板，Composer 不得擅改**）；② `kb_eval_run` 落库表结构与写入路径；③ `/kb/ops/*` 看板只读 API 契约与 VO 字段；④ CI 门禁工作流形态与触发；⑤ 施工清单 + 验收 + 禁改范围。

**不在本契约内（Composer 按现有风格落地）**：MyBatis-Plus entity/mapper/xml、`@ApiModel` VO 样板、`KbOpsServiceImpl` 聚合查询、`eval_ask.py --emit-db` 的 pymysql 写库样板、CI yaml 步骤脚本、DDL 迁移文件落盘（走 `@sql-migration-baseline`）、菜单/权限 SQL 复用现有 `kb:ops:dashboard`。

**红线（Composer 不得擅改，见 §7）**：§1 门禁阈值策略与基线更新协议、`kb_eval_run` 为**追加只读**（Java 不写）、看板 API 不改 `/kb/ask` 与 `/kb/ops/dashboard` 现有字段语义、CI 门禁默认档位（ngram 阻断 / hybrid 非阻断）。发现歧义回 Opus 窗口改契约。

---

## §1 门禁基线阈值策略（Opus 拍板 · 核心）

> 目标：**人为制造的检索回退必被 CI 拦截**，同时**不因 59 题小样本的单题抖动误杀**（1 题 ≈ 1.7pp）。

### 1.1 基线来源（单一真相 · committed）

- 新增**受版本管理**的基线文件 `moli-knowledge/kb/eval/baselines.json`（**不 gitignore**；report JSON 仍 gitignore）。
- 基线值来自 **AI-2 已签核的三档复测**（2026-07-19，全库 6961 段）：

```jsonc
{
  "updated": "2026-07-19",
  "git_sha": "<签核提交>",
  "golden_total": 59,
  "gate_metric": "hit3",          // 门禁主指标 = 全集 answerable hit@3
  "strategies": {
    "ngram":         { "hit3": 0.7917, "dirty_hit3": 0.80, "tolerance": 0.00 },
    "hybrid":        { "hit3": 0.8958, "dirty_hit3": 0.90, "tolerance": 0.05 },
    "hybrid-rerank": { "hit3": 0.8333, "dirty_hit3": 0.90, "tolerance": 0.05 }
  }
}
```

### 1.2 门禁判定（冻结规则）

对某 `strategy` 的一次评测报告，**任一条件不满足即 gate 失败（退出码非 0）**：

1. **主指标**：`report.hit_at["3"] ≥ baseline.hit3 − tolerance`。
2. **零错误**：`report.errors == 0`（catch 首轮 hybrid-rerank 21 报错类问题——降级失效必须拦）。
3. **dirty 底线**：`report.by_difficulty.dirty.hit_at["3"] ≥ baseline.dirty_hit3 − tolerance`（语义档的价值区，防「整体没掉但脏 query 崩」）。

> **容差取值理由**：
> - `ngram` **tolerance=0**：ngram 确定性召回，任何回退都是真回退，零容忍（AI-2 已验证逐题可复现）。
> - `hybrid` / `hybrid-rerank` **tolerance=0.05（5pp）**：向量召回有轻微非确定性 + 小样本抖动；5pp ≈ 3 题，能吸收噪声但仍能拦住首轮那种 −6.25pp/−26.7pp 的真回退。

### 1.3 基线更新协议（防「棘轮下滑」）

- **CI 绝不自动改基线**。`baselines.json` 只能由**人/Opus 显式提交**更新，且必须：① 附新 report 路径与 `git_sha`；② 在 `kb/eval/README.md` 记一行（日期 + 原因 + 前后值）。
- **只准向上或平移，不准无理由下调**：若某次改动确实牺牲 hit@3 换取别的收益，须在 PR 描述与 README 写明权衡并经人审，否则视为回退。
- golden 集扩容/改题（AI-1 后续迭代）→ 必须重跑三档、同批更新 `baselines.json`，否则门禁失真。

### 1.4 CI 跑哪些档（务实决策）

- **`ngram` = 每 PR 阻断门禁**：确定性、无需 sidecar，是常驻回归护栏。
- **`hybrid` = 非阻断（nightly / workflow_dispatch，需 sidecar+索引环境）**：CI runner 装不下 bge-m3/Chroma+索引；在有完整栈的环境（self-hosted 或定时对可达 staging）跑，结果落库 + 告警，但**不阻断 PR 合并**（避免因环境不可用误杀）。
- `hybrid-rerank` 同 hybrid，非阻断，仅趋势观测（P95 高，见 AI-2 签核建议）。

---

## §2 结果落库契约（`kb_eval_run`）

### 2.1 表结构（DDL 由 Composer 走 `docs/sql/31_kb_eval_run.sql`，`@sql-migration-baseline`）

字段契约（列名/语义固定；类型给建议，Composer 可微调对齐现有风格）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK **AUTO_INCREMENT** | 本表**仅 Python 写**、Java 只读，无跨服务 id 共享，用自增即可（区别于雪花 id 的业务表） |
| `run_at` | datetime | 报告 `time` |
| `strategy` | varchar(16) | `ngram`/`hybrid`/`hybrid-rerank`（可空=未指定档） |
| `use_llm` | tinyint(1) | 检索式 0 / 生成式 1 |
| `golden_total` | int | 报告 `total` |
| `answerable_total` / `negative_total` | int | 分母隔离（对齐 AI-1 口径） |
| `errors` | int | 报告 `errors` |
| `hit1` `hit3` `hit5` `hit8` | decimal(5,4) | 报告 `hit_at`（缺档存 NULL） |
| `mrr` `coverage` | decimal(5,4) | |
| `refusal_accuracy` | decimal(5,4) | 可空 |
| `p95_ms` | int | 可空 |
| `by_difficulty_json` | json / text | 报告 `by_difficulty` 原样 |
| `report_path` | varchar(255) | `kb/eval/reports/*.json` 相对路径 |
| `git_sha` | varchar(64) | 关联提交（CI 传 `GITHUB_SHA`；本地可空） |
| `gate_pass` | tinyint(1) | 该次是否过门禁（§1.2 判定结果，可空=未判） |
| `create_time` | datetime | 落库时间 |

索引：`idx_kb_eval_run_at(run_at)`、`idx_kb_eval_strategy_run(strategy, run_at)`。字符集 utf8mb4，`ENGINE=InnoDB`，`COMMENT='知识库评测回归记录'`。

### 2.2 写入路径（选定：Python `--emit-db`，Java 只读）

**决策：`eval_ask.py` 增 `--emit-db` 直接落库（复用 pymysql，同 `index_chunks.py`/`sync_to_db.py` 连库方式），不新增 Java 写接口。**

理由：评测是 DB 侧派生数据，Python 端已持有完整报告对象；新增 Java 写接口会平白扩大写面与鉴权成本。Java `KbOpsService` 对本表**只读**（§3）。

`--emit-db` 契约：
- 默认关闭；开启时在写完 report JSON 后 insert 一行 `kb_eval_run`（`report_path` 指向刚落盘文件）。
- 连库参数复用环境变量（`MYSQL_HOST/PORT/USER/PASSWORD/DB` 或 `KB_SYNC_*`，与现有工具一致）；缺库参数时 `--emit-db` 只告警不报错（不影响评测主流程与退出码）。
- `gate_pass` 由脚本按 §1.2 就地判定写入（无 `baselines.json` 时写 NULL）。
- **不侵入现有 CLI 默认行为**：不带 `--emit-db` 时行为与现状逐字节一致。

---

## §3 看板 API 契约（`/kb/ops/*` 只读扩展）

> 复用权限 `kb:ops:dashboard`（已在 `17_kb_lint_ops_enhance.sql`）；**不新增菜单/权限**。响应包 `MoliResult<T>`。

### 3.1 Dashboard 卡片（扩展 `KbOpsDashboardVo`，不改现有字段）

新增字段 `retrievalQuality`（`KbOpsEvalSummaryVo`）——「检索质量」卡片摘要：

| 字段 | 说明 |
|------|------|
| `strategies[]` | 每档一项：`strategy`、`latestRunAt`、`hit1/hit3/hit5`、`mrr`、`p95Ms`、`errors`、`baselineHit3`、`deltaHit3`（latest − baseline）、`gatePass` |
| `golden_total` | 最近一次的题量 |

> 现有 `syncTrend`/`lintSummary`/`unresolvedRelationCount`/`llm`/`driftSummary` **字段与语义不动**（红线）。

### 3.2 趋势明细端点（新增，供曲线图 + 明细跳转）

| 方法/路径 | 入参 | 出参 |
|-----------|------|------|
| `GET /kb/ops/eval-trend` | `strategy`（可空=全档）、`days`（默认 14，最大 90） | `List<KbOpsEvalTrendPointVo>`：`date`、`strategy`、`hit3`、`mrr`（按日取当日最后一次；样式仿 `KbOpsSyncTrendPointVo`） |
| `GET /kb/ops/eval-runs` | `strategy`（可空）、`limit`（默认 20，最大 100） | `List<KbOpsEvalRunVo>`：`kb_eval_run` 单次明细（含 `report_path`、`git_sha`、`gate_pass`、`by_difficulty_json`），供「链到单次报告明细」 |

- 排序 `run_at DESC`；`by_difficulty_json` 原样透传（前端解析），Java 不重算。
- **只读**：这三个读接口不得写 `kb_eval_run`。

---

## §4 CI 门禁契约（工作流）

### 4.1 新增 `.github/workflows/kb-eval.yml`（仿 `kb-sync.yml` 结构）

- **触发**：`workflow_dispatch`（手动，选 strategy）+ `schedule`（nightly，如 `cron: '0 18 * * *'` UTC）+ 可选 `push`（仅当有可达栈时；默认注释）。
- **job A · ngram-gate（阻断）**：在能连到栈的环境跑
  ```bash
  python moli-knowledge/kb/tools/eval_ask.py --strategy ngram \
      --gate-at-k 3 --min-hit <baselines.ngram.hit3 − 0> --emit-db
  ```
  退出码非 0 → job 失败（阻断）。**门禁阈值从 `baselines.json` 读取**，不硬编码在 yaml（Composer 写个读 JSON 传参的小步骤或 `eval_gate.py` 薄封装）。
- **job B · hybrid-observe（非阻断）**：`continue-on-error: true`；跑 `--strategy hybrid ... --emit-db`；仅落库 + 汇总告警，不阻断。
- 所有 job 用 secrets 提供 `MOLI_LOGIN_BASE` / `MOLI_KB_BASE` / `MOLI_EVAL_USER` / `MOLI_EVAL_PASS` / DB 连接；缺失则 job 跳过并说明（不 fail 死）。

### 4.2 门禁判定归一（避免 yaml 与脚本各写一套）

- §1.2 判定逻辑**只实现一处**：优先在 `eval_ask.py`（`--min-hit` 已有；补 `errors>0` 与 dirty 底线判定），或新增 `kb/tools/eval_gate.py` 读 report+baselines 统一判。**二选一，不重复**。
- baseline 容差、dirty 底线、errors 判定的**数值来源只认 `baselines.json`**。

---

## §5 施工清单（Phase W4 · 全 Composer）

| 交付 | 内容 |
|------|------|
| `kb/eval/baselines.json` | §1.1 committed 基线（初值 = AI-2 签核三档） |
| `docs/sql/31_kb_eval_run.sql` | §2.1 建表（`@sql-migration-baseline`，合并进 `scripts/moli.sql` 基线 + 更新 schema 文档） |
| `eval_ask.py --emit-db` | §2.2 落库 + `gate_pass` 判定；补 §1.2 的 errors/dirty 门禁（或 `eval_gate.py`） |
| entity/mapper/xml | `KbEvalRun` + MyBatis-Plus（**只读查询**用） |
| VO | `KbOpsEvalSummaryVo` / `KbOpsEvalTrendPointVo` / `KbOpsEvalRunVo` |
| `KbOpsService` | `dashboard()` 增 `retrievalQuality`；新增 `evalTrend()` / `evalRuns()` |
| `KbOpsController` | `GET /kb/ops/eval-trend`、`GET /kb/ops/eval-runs`（`@ApiOperation`，权限 `kb:ops:dashboard`） |
| `.github/workflows/kb-eval.yml` | §4 门禁工作流 |
| 文档 | `kb-hybrid-retrieval.md §4` 回填「已落地」；`kb/eval/README.md` 记基线与门禁用法；`docs/api/` KB 运维接口增量；`wiki-moli/develop/` enrich（走 kb sync） |

> 单一相位（对齐排期 W4）。出口见 §6。

---

## §6 验收标准

- [x] **落库**：`emit_eval_run` 字段逐一对应 report JSON→`kb_eval_run`（DDL §2.1 一致，`by_difficulty_json` = `json` 列）；冒烟 id=1 `hit3=0.7917 gate_pass=1`。✅
- [x] **看板可见**：`buildRetrievalQuality()` 按 `STRATEGY_ORDER` 三档给 hit1/3/5/mrr/p95/errors + `baselineHit3`/`deltaHit3`/`gatePass`；`evalTrend()` 按日取最后一次；`evalRuns()` 带 `report_path`。✅
- [x] **门禁拦得住**：`eval_gate.evaluate_gate` 实现 §1.2 三判定（hit3/errors=0/dirty 底线）；冒烟人为 0.7917→0.5 → exit 1。✅
- [x] **不误杀**：AI-2 签核三档报告过门禁全 PASS。✅
- [x] **零侵入**：新旗标 `--emit-db`/`--gate-from-baselines` 均为 opt-in；默认路径与旧 `--min-hit` 保留、report 结构不变；`/kb/ops/dashboard` 原字段未动。✅
- [x] **基线协议**：`baselines.json` committed（非 gitignore）；CI 仅 `--gate-from-baselines` 读、`KbEvalBaselinesProvider` Java 只读，均不写基线。✅

---

## §7 Composer 禁改范围（Do-Not-Touch）

- ❌ 改 §1 门禁**阈值策略/容差/基线更新协议**（ngram tol=0、hybrid tol=0.05、errors=0、dirty 底线、CI 不自动改基线）——需调整回 Opus 窗口。
- ❌ 让 Java 写 `kb_eval_run`（落库只走 Python `--emit-db`；Java 只读）。
- ❌ 改 `/kb/ask` 或 `/kb/ops/dashboard` **现有**字段语义；新增只加字段/端点。
- ❌ 把 `hybrid` 门禁设为 PR 阻断（CI runner 无 sidecar，会误杀）；hybrid 恒为非阻断观测档。
- ❌ 门禁阈值**硬编码进 yaml/代码**（只认 `baselines.json`）。
- ❌ 新增菜单/权限（复用 `kb:ops:dashboard`）。
- ❌ 把 report JSON 纳入 git（保持 gitignore；只 commit `baselines.json`）。

如需调整以上任一项 → 回 Opus 窗口改契约，不在 Composer 窗口拍板。

---

## §8 相关

- 技术方案：[`../kb-hybrid-retrieval.md`](../kb-hybrid-retrieval.md) §4
- 路线 / 排期 / 分工：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §2 W4 · §9.1
- 评测口径（report JSON / 字段）：[`AI-1-contract.md`](AI-1-contract.md) · 现有 `kb/tools/eval_ask.py`
- 三档基线数据来源：[`AI-2-contract.md`](AI-2-contract.md) §Review W3 签核 · `kb-hybrid-retrieval.md §6.1`
- 现有看板：`KbOpsController` `/kb/ops/dashboard` · `KbOpsDashboardVo` · `kb_llm_call_log`（DDL 参照）
- CI 参照：`.github/workflows/kb-sync.yml`

---

## §Review 意见（Composer 回填 / Opus 复核）

- **未决（需 Opus 定夺）**：无（§1 阈值/容差/基线协议未改）。
- **实现清单**（§5 全交付）：

| # | 交付物 | 路径 |
|---|--------|------|
| 1 | 三档基线 committed | `moli-knowledge/kb/eval/baselines.json` |
| 2 | DDL 增量 + 知识库基线合并 | `docs/sql/31_kb_eval_run.sql` · `docs/sql/03_knowledge_schema.sql` §15 · `docs/ops/sql-migration-order.md` |
| 3 | 门禁单点 + 落库 | `kb/tools/eval_gate.py` · `eval_ask.py --gate-from-baselines` / `--emit-db` |
| 4 | Java 只读 | `KbEvalRun` · `KbEvalRunMapper` · `mapper/KbEvalRunMapper.xml` · `KbEvalBaselinesProvider` |
| 5 | VO | `KbOpsEvalSummaryVo` · `KbOpsEvalStrategySummaryVo` · `KbOpsEvalTrendPointVo` · `KbOpsEvalRunVo` |
| 6 | Service | `KbOpsServiceImpl.buildRetrievalQuality()` · `evalTrend()` · `evalRuns()` |
| 7 | Controller | `GET /kb/ops/eval-trend` · `GET /kb/ops/eval-runs`（权限 `kb:ops:dashboard`） |
| 8 | CI | `.github/workflows/kb-eval.yml`（ngram 阻断 · hybrid `continue-on-error`） |
| 9 | 文档 | `kb/eval/README.md` · `kb-hybrid-retrieval.md §4` · `docs/api/knowledge-ops-frontend.md` |

- **冒烟结果**（2026-07-19 本地）：

| 项 | 结果 |
|----|------|
| `eval_gate.py` 三档 AI-2 签核 report | **PASS**（ngram / hybrid / hybrid-rerank） |
| 人为降 hit@3（0.7917→0.5） | **FAIL**（exit 1，`passed=False`） |
| `--emit-db` 落库 | **OK** — `kb_eval_run` id=1，`strategy=ngram`，`hit3=0.7917`，`gate_pass=1` |
| `KbOpsServiceImplTest` | **2/2 pass** |
| `/kb/ops/dashboard` → `retrievalQuality` | 代码已合入；需 KnowledgeServer + `31` 表 + 落库数据后 HTTP 验 |
| `/kb/ops/eval-trend` · `/eval-runs` | 同上（只读端点已注册） |

> **基线合并说明**：知识库表权威在 `03_knowledge_schema.sql`（`init-db.ps1` 导入链），非 `scripts/moli.sql`（该文件不含 kb_* 表）；`31_kb_eval_run.sql` 供已有库增量 + CI `init-schema`。

---

### Opus §6 验收签核（2026-07-19 · status → done）

逐项核对源码，六项全绿，§7 红线未破，判 **AI-3 W4 通过**。

| # | 验收项 | 结论 | 证据 |
|---|--------|------|------|
| 1 | 落库字段 vs report JSON | ✅ | `eval_gate.emit_eval_run` 逐字段映射 report→行；`31_kb_eval_run.sql` 列/类型/索引与 §2.1 一致，`by_difficulty_json` 为 `json` 列 |
| 2 | `retrievalQuality` 三档摘要 | ✅ | `KbOpsServiceImpl.buildRetrievalQuality()` 按 `STRATEGY_ORDER` 三档取最新 run，给 hit1/3/5/mrr/p95/errors + `baselineHit3`/`deltaHit3`/`gatePass` |
| 3 | eval-trend/eval-runs 只读 + 权限 | ✅ | 两端点均 `kbAclService.assertCanOpsDashboard(null)`（`kb:ops:dashboard`）+ 仅 `selectList`；`buildRetrievalQuality` 仅 `selectOne`；全库无 `kbEvalRunMapper.insert/update` |
| 4 | 门禁拦回退 / 正常全 pass | ✅ | `eval_gate.evaluate_gate` = §1.2 三判定（hit3≥base−tol、errors=0、dirty≥base−tol）；冒烟 0.7917→0.5 FAIL exit 1、AI-2 三档签核值全 PASS |
| 5 | 不带 `--emit-db` 零侵入 | ✅ | `--emit-db`/`--gate-from-baselines` 均 opt-in；旧 `--min-hit` 分支保留；report 结构与 `/kb/ops/dashboard` 原字段未动 |
| 6 | baselines committed / CI 不改基线 | ✅ | `baselines.json` 已 commit（非 gitignore，golden_total=59 对齐）；`kb-eval.yml` 仅 `--gate-from-baselines` 读、`KbEvalBaselinesProvider` Java 只读；ngram 阻断 / hybrid `continue-on-error` |

**非阻断观察（不影响转 done，仅记录）**：
- `ngram-gate` 在 `pull_request` 触发但依赖 `MOLI_LOGIN_BASE`/`MOLI_KB_BASE` secrets，缺失时 `::notice` skip（非 fail）——即「配置了 live stack secrets 才真正阻断」，符合 §1.4「CI runner 无法自托管全栈」的既定设计，非缺陷。
- `baselines.json` 的 `git_sha: "6d9e4c74"` 为占位短 sha；后续基线更新按 §1.3 记真实签核 commit 即可。

**§1 阈值策略 / 容差 / 基线协议**：Composer 未改，`status: done` 生效。
