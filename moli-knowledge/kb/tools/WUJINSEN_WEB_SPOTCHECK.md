# T22 Web 抽检记录

> `spotcheck_wujinsen_web.py` · gateway `http://127.0.0.1:8888` · 2026-07-05

## 汇总

| 项 | 结果 |
|----|------|
| 抽检页数 | 5 |
| 磁盘/markdown | **5/5 PASS** |
| Gateway API | SKIP（admin/123456 登录失败，需本地账号） |
| 浏览器 | 建议手动打开 `:5173` 知识库浏览上述 slug |

## 明细

| 页 | slug | 插图节 | 引用 | 磁盘 assets |
|----|------|--------|------|-------------|
| hub B · hadoop 生态 | `bigdata/hadoop-生态入门` | ✅ `## 原文插图（wujinsen）` | 2 | 2/2（raw.asset URL） |
| hub B · JVM | `java/jvm-内存与gc` | ✅ | 3 | 3/3 |
| hub B · Netty | `middleware/netty-reactor与线程模型` | ✅ | 3 | 3/3 |
| annex A · Hadoop 迷你书 | `bigdata/annex-Hadoop应用开发技术详解》迷你书` | — | 3 | 3/3（`.assets/`） |
| annex A · Netty In Action | `middleware/annex-Netty-In-Action` | — | 3 | 3/3 |

## 手动浏览器清单

1. `bigdata/hadoop-生态入门` — 文末 wujinsen 插图节 + annex 链接
2. `java/jvm-内存与gc`
3. `middleware/netty-reactor与线程模型`
4. `bigdata/annex-Hadoop应用开发技术详解》迷你书`
5. `middleware/annex-Netty-In-Action`

**PASS 条件**：图片非 `alt=image N` 占位、无 JSON 当图片 blob。
