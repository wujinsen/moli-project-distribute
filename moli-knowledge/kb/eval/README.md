# 知识库问答评测（golden set + eval_ask.py）

> 目的：给 `/kb/ask` 检索管线一把「尺子」。任何检索改动（chunk 化、Meilisearch、
> rerank、图谱扩展）前后各跑一遍，用指标说话。切段规则见 wiki [[知识库-chunk切段规范]]（`wiki-moli/develop/知识库-chunk切段规范.md`）。
> 背景见 `kb/ROADMAP.md` §六「评测」。

## 文件

| 文件 | 说明 |
|------|------|
| `golden.jsonl` | 标准问答集（一行一题，可持续追加） |
| `reports/` | 每次评测输出的 JSON 报告（gitignore 建议保留最近几份即可） |
| `../tools/eval_ask.py` | 评测脚本：登录网关 → 逐题调 `/kb/ask` → 出指标 |

## golden.jsonl 字段

```json
{"id": "M01", "space": "moli-ops-manual", "question": "本地怎么启动整套茉莉微服务？",
 "expect_slugs": ["guides/本地启动指南"], "expect_keywords": ["nacos"], "note": "操作类 → guide"}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `id` | 是 | 唯一编号；`M*`=moli-ops-manual、`E*`=enterprise-kb、`J*`=jp-fe-ap-exam |
| `space` | 是 | `kb_space.space_code`，运行时解析成 spaceId |
| `question` | 是 | 用户视角提问，尽量口语化、换说法（考召回，别照抄页标题） |
| `expect_slugs` | 是 | 期望被引用的页（全路径 slug，与 DB `kb_document.slug` 一致）；**任一命中算 hit** |
| `expect_keywords` | 否 | 生成式答案里应出现的关键词（小写比较）；仅 `--use-llm` 时检查 |
| `note` | 否 | 出题理由 / 考察点 |

## 运行

```bash
# 前置：网关 8888 + KnowledgeServer 已启动，wiki 已 Sync 进库
python kb/tools/eval_ask.py                       # 检索式（默认，不耗 LLM）
python kb/tools/eval_ask.py --use-llm             # 生成式（同时检查 expect_keywords）
python kb/tools/eval_ask.py --only M03            # 只跑单题
python kb/tools/eval_ask.py --space moli-ops-manual
python kb/tools/eval_ask.py --min-hit 0.8         # 命中率低于 0.8 时退出码 1（CI 门禁）
```

登录默认 `admin/123456`，可用 `--username/--password` 或环境变量
`MOLI_EVAL_USER` / `MOLI_EVAL_PASS` 覆盖；网关地址 `--gateway`。

## 指标

| 指标 | 含义 |
|------|------|
| `hit@k` | expect_slugs 任一出现在 citations 里的题占比（k=topK，默认 8） |
| `mrr` | 首个命中 slug 的排名倒数均值（越靠前越好） |
| `coverage` | expect_slugs 被引用的比例均值（多候选题用） |
| `kw_pass` | 生成式答案包含全部 expect_keywords 的题占比（仅 `--use-llm`） |

## 基线记录

| 日期 | 模式 | hit@8 | MRR | coverage | 未命中 | 备注 |
|------|------|-------|-----|----------|--------|------|
| 2026-07-13 | 检索式 | 66.67% | 0.456 | 62.5% | M03 M05 M06 E01 | 首次基线（ngram 全文 + bigram 精排）。未命中共性：**问题换说法与页标题词面不重合** |
| 2026-07-13 | 检索式 | **75.00%** | **0.498** | **75.0%** | M03 M06 E01 | **chunk v1**（`kb_document_chunk` + ask 按段召回 + 整页分数合并；修复「三操作」误触发 guide 作用域）。M05 命中 |
| 2026-07-13 | 检索式 | **100.00%** | **0.833** | **100.0%** | — | **作用域 + 精排**：「怎么设计/怎么工作」→ concept+article；操作类收窄 guide 触发词；正文词频封顶 8 + `/annex-` 降权 |

## 维护约定

1. **只增不删**：题目答不好先查检索/内容，别删题降指标；确认页已归档才移除。
2. **从 qa_log 沉淀**：Web「问答历史」里用户标 👍 的问题优先补进来（改写成换说法）。
3. **改检索必跑**：前后各一次，报告存 `reports/`，diff 写进 PR 描述。
4. 新空间加题时在本 README 登记 id 前缀。
