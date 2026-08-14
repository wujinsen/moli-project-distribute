# 知识库问答评测（golden set + eval_ask.py）

> 目的：给 `/kb/ask` 检索管线一把「尺子」。任何检索改动（chunk 化、Meilisearch、
> rerank、图谱扩展）前后各跑一遍，用指标说话。切段规则见 wiki [[知识库-chunk切段规范]]（`wiki-moli/develop/知识库-chunk切段规范.md`）。
> 背景见 `kb/ROADMAP.md` §六「评测」。

## 文件

| 文件 | 说明 |
|------|------|
| `golden.jsonl` | 标准问答集（一行一题，可持续追加） |
| `baselines.json` | **committed** 门禁基线（hit@3 / dirty / tolerance；CI 只读，不自动改） |
| `reports/` | 每次评测输出的 JSON 报告（gitignore 建议保留最近几份即可） |
| `../tools/eval_ask.py` | 评测脚本：登录网关 → 逐题调 `/kb/ask` → 出指标 |

## golden.jsonl 字段

```json
{"id": "M01", "space": "moli-ops-manual", "question": "本地怎么启动整套茉莉微服务？",
 "expect_slugs": ["guides/本地启动指南"], "expect_keywords": ["nacos"],
 "difficulty": "easy", "expect_answerable": true, "note": "操作类 → guide"}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `id` | 是 | 唯一编号；`M*`=moli-ops-manual、`E*`=enterprise-kb、`J*`=jp-fe-ap-exam |
| `space` | 是 | `kb_space.space_code`，运行时解析成 spaceId |
| `question` | 是 | 用户视角提问，尽量口语化、换说法（考召回，别照抄页标题） |
| `expect_slugs` | 条件 | **`expect_answerable=true` 时必填非空**；`negative` 题必须为 `[]` |
| `expect_keywords` | 否 | 生成式答案里应出现的关键词（小写比较）；仅 `--use-llm` 时检查；negative 禁填 |
| `note` | 否 | 出题理由 / 考察点 |
| **`difficulty`** | **是** | `easy` \| `paraphrase` \| `dirty` \| `multi-hop` \| `negative` |
| **`expect_answerable`** | **是** | `true`=库内有据应命中；`false`=应拒答（negative） |
| **`expect_all`** | 否 | 仅 `multi-hop`；`true` 表示 expect_slugs **全部**被引用才算完全命中 |

### 分层配额（AI-1 扩容后 ≥55 题）

| difficulty | 数量 | 说明 |
|------------|------|------|
| `easy` | 15 | 词面较贴近页标题 |
| `paraphrase` | 15 | 换说法/近义提问 |
| `dirty` | 10 | 口语化/错别字/中英混 |
| `multi-hop` | 8 | 需综合 2+ 页 |
| `negative` | 7 | 库内无据，期望拒答 |

空间分布：moli-ops-manual ≥60%、enterprise-kb ≥20%、jp-fe-ap-exam ≥10%。

## 运行

```bash
# 前置：user-center(8888) + knowledge-server(8090 或网关 21000) 已启动，wiki 已 Sync 进库
python kb/tools/eval_ask.py                       # 检索式（默认，不耗 LLM）
python kb/tools/eval_ask.py --use-llm             # 生成式（同时检查 expect_keywords）
python kb/tools/fill_eval_metrics.py --run --use-llm   # 跑评测 + 自动回填 README/PORTFOLIO
python kb/tools/eval_ask.py --only M03            # 只跑单题
python kb/tools/eval_ask.py --space moli-ops-manual
python kb/tools/eval_ask.py --min-hit 0.9 --gate-at-k 3   # hit@3 低于 0.9 时退出码 1（legacy 单阈值）
python kb/tools/eval_ask.py --strategy ngram --gate-from-baselines   # AI-3：读 baselines.json §1.2 三条件门禁
python kb/tools/eval_ask.py --strategy hybrid --gate-from-baselines --emit-db   # 评测 + 落库 kb_eval_run
python kb/tools/eval_gate.py eval/reports/xxx.json --strategy ngram   # 对已有 report 单独判 gate
python kb/tools/eval_ask.py --use-llm --llm-context-top-k 3
python kb/tools/eval_ask.py --difficulty dirty,paraphrase  # 只跑指定难度
python kb/tools/eval_ask.py --no-negative                  # 排除 negative 题
python kb/tools/eval_ask.py --baseline                     # 基线报告 baseline-ngram-*.json

# AI-9 Guardrails 开/关对比（需 --use-llm；off 轮默认 kb.guardrails.enabled=false）
python kb/tools/eval_ask.py --use-llm --guardrails-baseline
# 开启 kb.guardrails.enabled=true 并重启后：
python kb/tools/eval_ask.py --use-llm --compare-guardrails \
  --guardrails-off-report eval/reports/ai9-guardrails-off-YYYYMMDD-HHMMSS.json
```

登录默认 `admin/123456`，走 **user-center 直连** `http://127.0.0.1:8888/login`。
KnowledgeServer 自动尝试 `21000/KnowledgeServer` → `8090` 直连。
可用 `--login-base` / `--kb-base` 或环境变量 `MOLI_LOGIN_BASE` / `MOLI_KB_BASE` 覆盖。

## 指标

| 指标 | 含义 |
|------|------|
| `hit@k` | expect_slugs 任一出现在 citations 前 k 条里的题占比 |
| `hit_at` | 报告 JSON 中多档命中率（默认派生 hit@3 / hit@5 / hit@8，需 `--top-k` ≥ k） |
| `mrr` | 首个命中 slug 的排名倒数均值（越靠前越好） |
| `coverage` | expect_slugs 被引用的比例均值（多候选题用；**仅 answerable 子集**） |
| `kw_pass` | 生成式答案包含全部 expect_keywords 的题占比（仅 `--use-llm`） |
| `refusal_accuracy` | negative 题正确拒答占比（检索式：citations 为空；生成式：拒答短语 + citations 为空） |
| `by_difficulty` | 报告 JSON 中按难度分层的 hit@k / MRR / coverage / refusal_accuracy |
| `guard_metrics` | AI-9：`grounding_coverage_mean` · `hallucination_proxy_mean`（1-coverage）· `grounding_low_rate` · `hallucination_samples` |

**分母隔离**：`hit@k` / `mrr` / `coverage` / `kw_pass` 只对 `expect_answerable=true` 子集计算；`refusal_accuracy` 只对 negative 子集计算。

**Ask 生产默认**（`kb.ask.*`）：citations 最多 8 页，LLM prompt 最多 3 页（`llmContextTopK`）；评测可用 `--llm-context-top-k` 覆盖。

## 门禁基线（AI-3 · `baselines.json`）

| 策略 | hit@3 基线 | dirty hit@3 | tolerance | CI 档位 |
|------|------------|-------------|-----------|---------|
| `ngram` | 0.7917 | 0.80 | **0**（零容忍） | PR **阻断** |
| `hybrid` | 0.8958 | 0.90 | 0.05 | nightly **非阻断** |
| `hybrid-rerank` | 0.8333 | 0.90 | 0.05 | nightly **非阻断** |

**三条件**（`--gate-from-baselines` / `eval_gate.py`，任一失败 exit 1）：

1. 全集 `hit@3 ≥ baseline.hit3 − tolerance`
2. `errors == 0`
3. `by_difficulty.dirty.hit@3 ≥ baseline.dirty_hit3 − tolerance`

**基线更新协议**（防棘轮下滑）：

- CI **绝不**自动改 `baselines.json`；只能人审 PR + 本 README 记一行（日期、原因、前后值）。
- golden 扩容/改题后须同批重跑三档并更新基线。
- 初值来源：AI-2 W3 签核复测（2026-07-19，6961 段索引）。

## 基线记录

| 日期 | 模式 | 题数 | hit@3 | hit@8 | MRR | refusal_acc | 备注 |
|------|------|------|-------|-------|-----|-------------|------|
| 2026-07-13 | 检索式 | 12 | — | 66.67% | 0.456 | — | 首次基线（ngram 全文 + bigram 精排）。未命中共性：**问题换说法与页标题词面不重合** |
| 2026-07-13 | 检索式 | 12 | — | **75.00%** | **0.498** | — | **chunk v1**（`kb_document_chunk` + ask 按段召回 + 整页分数合并；修复「三操作」误触发 guide 作用域）。M05 命中 |
| 2026-07-13 | 检索式 | 12 | — | **100.00%** | **0.833** | — | **作用域 + 精排**：「怎么设计/怎么工作」→ concept+article；操作类收窄 guide 触发词；正文词频封顶 8 + `/annex-` 降权 |
| 2026-07-17 | 检索式 | 59 | 79.17% | 91.67% | 0.742 | 0.0% | **AI-1 修订基线**（§9.2a ingest+jp sync；`baseline-ngram-20260717-022510.json`） |
| 2026-07-17 | 生成式 | 59 | 79.17% | 91.67% | 0.742 | 0.0% | **AI-1 修订基线**（`baseline-ngram-20260717-023735.json`；kw_pass=85%） |

## 维护约定

1. **只增不删**：题目答不好先查检索/内容，别删题降指标；确认页已归档才移除。
2. **从 qa_log 沉淀**：Web「问答历史」里用户标 👍 的问题优先补进来（改写成换说法）。
3. **改检索必跑**：前后各一次，报告存 `reports/`，diff 写进 PR 描述。
4. 新空间加题时在本 README 登记 id 前缀。
