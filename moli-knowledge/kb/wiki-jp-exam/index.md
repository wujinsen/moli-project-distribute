# 日本語試験（FE/AP）知识库

> 独立 wiki 目录，同步至 `kb_space.space_code=jp-fe-ap-exam`（`space_id=900000000000000002`）。

| 领域 | raw 源 | 说明 |
|------|--------|------|
| 基本情報 FE | **`kb/raw/school/fe/`** | 基本情報技術者（真题/样题/答案） |
| 応用情報 AP | **`kb/raw/school/ap/`** | 応用情報技術者（暂无文件时可先建空目录） |
| **Certify サーティファイ** | **`kb/raw/school/certify/`** | IT 基础资格 Moodle 模拟题 + 中文解析 → **`certify/`** |

入口页：[[Certifyサーティファイ]] · Ingest：`python kb/tools/ingest_certify_wiki.py`

约定详见 [`raw/school/README.md`](../raw/school/README.md)。

同步：与 `enterprise-kb` / `moli-ops-manual` 一并执行 `run_sync.sh sync-all`；映射表见 ops 空间 `guides/wiki同步指南` §1。

```bash
bash moli-knowledge/kb/tools/ci/run_sync.sh dry-run-all
bash moli-knowledge/kb/tools/ci/run_sync.sh sync-all
```

## 批次 #WB-20260628055918（Web Ingest 2026-06-28） <!-- ingest-job:726300606100291584 -->

- [[basic-information-technician-examination-subject-b-sample-questions]] — create article

## 批次 #gov-202606281451（Web Enrich 2026-06-28） <!-- enrich-batch:gov-202606281451 -->

- [[基本情報技術者試験 科目 B サンプル問題]] — enrich

## 批次 #gov-202606281508（Web Enrich 2026-06-28） <!-- enrich-batch:gov-202606281508 -->

- [[基本情報技術者試験 科目 B サンプル問題]] — enrich

## 批次 #WB-20260628235829（Web Ingest 2026-06-28） <!-- ingest-job:726572191503761408 -->

- [[fe_kamoku_b_set_sample_qs3]] — create article

## certify 分类（批次 #certify-20260707）

- [[Certifyサーティファイ]] — create guide（Certify 入口）
- [[certify-katakana-vocab]] — create article（片假名词汇 395 条）
- 模擬問題 1/2/3/5/6/サンプル + 中文解析 — create article ×12
- ランダム問題 1/2/3/5/サンプル + 中文解析 — create article ×10
- 開発技術 / 技術要素（アルゴ）/ 技術要素(DB) / マネジメント_ストラテジ + 中文解析 — create article ×8

## certify 分类（批次 #certify-20260707c）

- [[certify-katakana-vocab]] — update（395→**400** 条；新增 63 译项 + 复合词/OCR 合并规则）
- [[模擬問題4]] / [[模擬問題4-中文解析]] — create（pic/OCR 50 问）
- enrich [[Certifyサーティファイ]] — 模擬問題4 索引 + 词汇条数

