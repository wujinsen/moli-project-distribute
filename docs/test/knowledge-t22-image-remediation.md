# T22 · wujinsen 插图回迁 · 验收与自动化测试

> PRD：[wujinsen-wiki-image-remediation-prd.md](../product/wujinsen-wiki-image-remediation-prd.md) · 操作：`kb/tools/` · 前端：[kb-markdown-image-frontend.md](../api/kb-markdown-image-frontend.md)

## 1. 范围

| 项 | 说明 |
|----|------|
| **R0** | `GET /kb/raw/asset`、`GET /kb/wiki/asset` |
| **F2** | `POST /kb/wiki/asset` 编辑页上传 |
| **R2** | annex（策略 A）+ hub `## 原文插图`（策略 B） |
| **manifest** | `kb/tools/WUJINSEN_IMAGE_REMEDIATION.json` |

## 2. 自动化测试

### 2.1 Python（kb/tools）

```bash
cd moli-knowledge
python -m unittest discover -s kb/tools/tests -p "test_*.py" -v
```

覆盖：`audit_wujinsen_images.RAW_SRC`（空格、`[]` 路径）· `load_wiki_citations()`。

### 2.2 Java（knowledge-server）

```bash
cd moli-knowledge/moli-knowledge-server
mvn test "-Dtest=KbAssetServiceImplTest,KbAssetControllerApiTest"
```

覆盖：raw/wiki 读图 · F2 上传 · 空文件/超限/非法类型/SVG 默认拒绝。

### 2.3 前端（meiling-ui 独立仓）

```bash
cd D:/work/moli_project/meiling-ui
npm test
```

覆盖：`kbAssetUrl.ts` · `useKbMarkdownRender` 挂载 `KbMarkdownImage`。

## 3. 内容验收（CLI）

| 步骤 | 命令 | 期望 |
|------|------|------|
| R3 断链 | `python kb/tools/verify_wujinsen_images.py --report` | 0 broken · 报告 `WUJINSEN_R3_REPORT.md` |
| wiki lint | `python kb/tools/lint.py --strict` | 无 ERROR |
| defer 重开 | `python kb/tools/merge_wujinsen_audit.py --dry-run` | cited defer → pending |
| Web 抽检 | `python kb/tools/spotcheck_wujinsen_web.py` | 5 页 PASS · `WUJINSEN_WEB_SPOTCHECK.md` |

## 4. 手工 Web（5 页）

| slug | 检查 |
|------|------|
| `bigdata/hadoop-生态入门` | hub 插图节 + annex 链接可进 |
| `java/jvm-内存与gc` | raw.asset 图可见 |
| `middleware/netty-reactor与线程模型` | 同上 |
| `bigdata/annex-Hadoop应用开发技术详解》迷你书` | `.assets/` 内嵌图 |
| `middleware/annex-Netty-In-Action` | 大图 annex |

**PASS**：非 `alt=image N` 占位；登录后 blob 非 JSON。

## 5. 发布前

1. `sync_to_db.py --space enterprise-kb`（有 wiki 变更时）
2. 网关 + knowledge-server + meiling-ui 部署含 F1/F2
3. 更新 `WUJINSEN_*` 报告日期（可选）

## 6. 相关报告（`kb/tools/`）

| 文件 | 用途 |
|------|------|
| `WUJINSEN_R3_REPORT.md` | 全库插图引用验收 |
| `WUJINSEN_DEFER_INGEST_PLAN.md` | defer 重开记录 |
| `WUJINSEN_ATTACH_RESOLVED.md` | `.note.attach` 7 条 |
| `WUJINSEN_IMAGE_REMEDIATION.json` | manifest 唯一真相 |
