# wujinsen defer 高价值 ingest 规划 · 执行结果

> 2026-07-05 · 批次 **defer-reopen#1**

## 结论

**无需手工补 cite。** 145 条 `defer-closed` 的 raw 早已写在各 hub 页 `sources` 中；根因是 `audit_wujinsen_images.py` 的 `RAW_SRC` 正则：

1. 旧版 `[^\s\]]+` 在路径**空格**处截断（如 `《深入理解 Java 内存模型》`）
2. 在路径 **`]`** 处截断（如 `[复制链接]`、`[Abp vNext…]`）

已改为 `raw/wujinsen_markdown/([^\n]+\.md)`。

## 执行步骤

| 步骤 | 命令 | 结果 |
|------|------|------|
| 合并审计 | `merge_wujinsen_audit.py --apply` | 145 defer → **pending**，252 done 保留 |
| 回迁 A/C-or-A | `remediate_wujinsen_images.py --apply --strategy A` | **121/121** annex |
| 回迁 B | `remediate_wujinsen_images.py --apply --strategy B` | **24/24** hub 插图节 |
| Lint | `lint.py --strict` | 474 页通过 |
| 修 slug | `annex-Hadoop大数据面试-Hadoop篇-复制链接` | 去掉 `[`/`]` 避免 `[[]]` 断链 |

## Manifest 终态

```
done: 397（原 252 + 本批 145）
pending: 0
defer-closed: 0
```

## 后续

- 新 raw 进 defer 时：先确认 hub `sources` 是否已 cite，再跑 `merge_wujinsen_audit.py --apply`
- 大图 annex slug 若含 `[`/`]`，remediate 应 sanitize（见 YUM / Hadoop 面试 annex 先例）
- 可选：`verify_wujinsen_images.py --report` + Wiki Sync

## R3 + Sync（2026-07-05 续）

| 步骤 | 结果 |
|------|------|
| R3 | **3544** refs · **0** broken · 295 annex · `WUJINSEN_R3_REPORT.md` PASS |
| 修 assets | `annex-Hadoop大数据面试-Hadoop篇-复制链接.assets`（rename 对齐 slug） |
| Sync | `sync_to_db.py` enterprise-kb · **insert=118 update=96** skip=260 |
