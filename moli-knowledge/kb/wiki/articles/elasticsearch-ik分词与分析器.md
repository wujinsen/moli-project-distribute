---
title: Elasticsearch IK 分词与分析器
slug: elasticsearch-ik分词与分析器
type: article
status: active
tags: [Elasticsearch, 搜索, 知识库]
sources:
  - raw/wujinsen_markdown/
related: [elasticsearch-搜索, es-match与bool查询, 知识库-全文检索规划, es-索引与写入流程]
created: 2026-06-21
updated: 2026-06-21
---

# Elasticsearch IK 分词与分析器

> ES 总览 [[elasticsearch-搜索]]；查询 [[es-match与bool查询]]；茉莉规划 [[知识库-全文检索规划]]。

中文检索依赖 **analysis** 链：`character filter → tokenizer → token filter`。

## 1. IK 两种模式

| 模式 | 说明 | 场景 |
|------|------|------|
| `ik_max_word` | 最细粒度 | 索引、召回优先 |
| `ik_smart` | 粗粒度 | 搜索 query 分析 |

```json
{
  "settings": {
    "analysis": {
      "analyzer": {
        "ik_index": { "tokenizer": "ik_max_word" },
        "ik_search": { "tokenizer": "ik_smart" }
      }
    }
  }
}
```

## 2. 自定义词典

- **ext_dict**：行业词（「秒杀」「RBAC」）
- **stopwords**：停用词
- 热更新：挂载 dict 文件 + `reload` 或重建 index

## 3. 茉莉知识库注意

- `title` 用 ik_search；`body` 可 ik_index + 子字段 keyword 排序
- 与 MySQL `LIKE` 过渡方案对比 [[mysql-slow-log慢查询分析]]
- 同步源：wiki markdown [[kb-wiki到es同步流水线]]

## 4. 常见问题

| 现象 | 处理 |
|------|------|
| 搜不到专有名词 | 扩展词典 |
| 分词过碎噪声大 | query 侧 ik_smart |
| 高亮错位 | unified highlighter + 相同 analyzer |

## 相关

[[elasticsearch-面试题]] · [[知识库-混合检索规划]]
