---
title: ES match 与 bool 查询
slug: es-match与bool查询
type: article
status: active
tags: [elasticsearch, DSL, match, bool]
sources:
- raw/wujinsen_markdown/BigData/ElasticSearch/ES深度分页查询.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/ES源码解析与优化实战/Elasticsearch选主流程.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/ES源码解析与优化实战/Search流程.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/ES源码解析与优化实战/elasticsearch 选主流程.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/ElasticSearch同步MySql.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/ElasticSearch源码解析与优化实战/search流程.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/Elasticsearch 默认配置 IK 及 Java AnalyzeRequestBuilder 使用.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/Elasticsearch之elasticsearch5.x 新特性.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/Elasticsearch和mysql数据增量同步.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/spring-boot  elasticsearch版本匹配问题.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/同步mysql数据到ElasticSearch的最佳实践.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/安装/Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/安装/Mac安装elasticsearch-- head插件.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/安装/Windows环境搭建ElasticSearch 5.6并配置head.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/教程/ES7语法/Elasticsearch 7  关于 Index、Type、Document.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/教程/ES7语法/index document.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/教程/ES7语法/查询语句.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/教程/Elasticsearch6.x注意事项.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/教程/[Elasticsearch] 全文搜索 (三) - match查询和bool查询的关系，提升查询子句.note.md
- raw/wujinsen_markdown/BigData/ElasticSearch/教程/elasticsearch java api 使用ik 分词器.note.md
related: [elasticsearch-搜索, es-搜索与分片路由]
created: 2026-06-22
updated: 2026-07-05
---

# ES match 与 bool 查询

> 搜索流程 [[search/es-搜索与分片路由]]；枢纽 [[search/elasticsearch-搜索]]。

## 1. match 本质是 bool

**match** 对文本分词后，默认 **OR** 连接各 term：

```json
{ "match": { "title": "brown fox" } }
```

等价于：

```json
{
 "bool": {
 "should": [
 { "term": { "title": "brown" } },
 { "term": { "title": "fox" } }
 ]
 }
}
```

`operator: "and"` → 各 term 进 **must**。

## 2. minimum_should_match

```json
{
 "match": {
 "title": {
 "query": "quick brown fox",
 "minimum_should_match": "75%"
 }
 }
}
```

3 个 term、75% → 至少 2 个 should 匹配。也可直接写 bool 的 `minimum_should_match: 2`。

## 3. bool 组合

| 子句 | 语义 |
|------|------|
| **must** | 必须匹配，计入 score |
| **should** | 应匹配，加分 |
| **must_not** | 必须不匹配 |
| **filter** | 必须匹配，**不计分**（可 cache） |

bool 可嵌套 bool，用于复杂业务检索。

## 4. boost 调权

```json
{
 "bool": {
 "must": [
 { "match": { "content": "full text search" } }
 ],
 "should": [
 { "match": { "content": { "query": "Elasticsearch", "boost": 2 } } },
 { "match": { "content": { "query": "Lucene", "boost": 3 } } }
 ]
 }
}
```

`boost > 1` 提高子句权重；should 命中越多 `_score` 越高。

**match 无法**单独表达「某词权重更高」→ 用 bool + boost。

## 5. term vs match

| 查询 | 用途 |
|------|------|
| **term** | 精确值（keyword、未分词字段） |
| **match** | 全文分词 |
| **match_phrase** | 短语顺序匹配 |

MySQL `=` 类似 term；`LIKE` 无分词相关性。

## 6. 过滤与聚合

- 价格区间、状态枚举 → **filter**（不影响相关性）
- 统计 → **aggregations**（cardinality 大数据量用 HLL，见面试篇）

## 7. 知识库检索映射

若 接 ES，典型 mapping：

| 字段 | 类型 | 查询 |
|------|------|------|
| title | text + keyword | match + 精确 filter |
| content | text | match / bool |
| tags | keyword | term filter |
| space_id | keyword | filter |

当前 MySQL `LIKE '%question%'` 无 `_score` 排序，可迁 ES 后 `/kb/ask` 检索式分支质量提升。
