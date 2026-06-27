# 知识库 · Wiki 空间 Lint（T16a）测试说明

> 模块：`moli-knowledge-server` · API：`POST /kb/wiki/lint-space` · 契约：[`KNOWLEDGE_API.md` §4.6](../api/KNOWLEDGE_API.md#46-文件级空间-lintt16a--文件真值)

## 1. 测试范围

| 层级 | 类 | 说明 |
|------|-----|------|
| Service 单元 / 集成 | `KbWikiLintServiceImplTest` | 参数校验、ACL、脚本路径、`lint.py` 进程与 JSON 解析 |
| Controller 契约 | `KbWikiControllerLintApiTest` | `POST /kb/wiki/lint-space` 返回 `MoliResult<WikiSpaceLintVo>` |

测试目录：

`moli-knowledge/moli-knowledge-server/src/test/java/com/moli/knowledge/server/`

## 2. 用例清单

### 2.1 `KbWikiLintServiceImplTest`（8 用例）

| # | 方法 | 类型 | 预期 |
|---|------|------|------|
| 1 | `lintSpace_rejectsNullRequest` | Mock | `BaseException` |
| 2 | `lintSpace_rejectsMissingSpaceById` | Mock | 空间 ID 不存在 → `BaseException` |
| 3 | `lintSpace_rejectsMissingSpaceByCode` | Mock | `spaceCode` 不存在 → `BaseException` |
| 4 | `lintSpace_rejectsUnmappedWikiDir` | Mock | `kb.wiki.space-dirs` 未映射 → `BaseException` |
| 5 | `lintSpace_rejectsMissingScript` | Mock | `lint-script-path` 文件不存在 → `BaseException` |
| 6 | `lintSpace_propagatesAclDenied` | Mock | `assertCanEdit` 拒绝 → `BaseException` |
| 7 | `lintSpace_assertsEditorBeforeRunningScript` | 集成 | 先校验 editor；真实跑 `lint.py` 于 `enterprise-kb` wiki；返回 `stats`/`issues`/`exitCode` |
| 8 | `lintSpace_fixtureWiki_detectsBrokenLink` | 集成 | 临时 wiki 含 `[[nonexistent-slug-t16a-fixture]]` → 检出 `broken_link` error，`exitCode ≠ 0` |

集成用例（7、8）在仓库内找不到 `moli-knowledge/kb/tools/lint.py` 或本机无 `python` 时通过 `Assume.assumeNotNull` **跳过**，不影响 CI 中 Mock 用例通过。

### 2.2 `KbWikiControllerLintApiTest`（1 用例）

| # | 方法 | 类型 | 预期 |
|---|------|------|------|
| 1 | `POST_kb_wiki_lint_space` | Mock | `code=200`；`data.spaceCode`/`wikiDir`/`exitCode` 与 stub 一致；委托 `KbWikiLintService.lintSpace` |

## 3. 最近一次测试报告

| 项 | 值 |
|----|-----|
| 执行时间 | 2026-06-27 |
| 环境 | Windows 11 · JDK 11.0.31 (Temurin) · Maven 3.9.16 · Python 3.13（集成用例） |
| 命令 | 见 §4 |
| **合计** | **9** 用例 · **9** 通过 · **0** 失败 · **0** 错误 · **0** 跳过 |

| 测试类 | 用例数 | 通过 | 失败 | 耗时 |
|--------|--------|------|------|------|
| `KbWikiLintServiceImplTest` | 8 | 8 | 0 | 0.731 s |
| `KbWikiControllerLintApiTest` | 1 | 1 | 0 | 1.359 s |

Surefire 报告路径（本地执行后生成）：

`moli-knowledge/moli-knowledge-server/target/surefire-reports/`

## 4. 运行方式

```bash
cd moli-knowledge/moli-knowledge-server
mvn test "-Dtest=KbWikiLintServiceImplTest,KbWikiControllerLintApiTest"
```

PowerShell 下 `-Dtest` 含逗号时必须加引号，否则会被解析为数组导致 Maven 报错。

单类：

```bash
mvn test -Dtest=KbWikiLintServiceImplTest
mvn test -Dtest=KbWikiControllerLintApiTest
```

全模块回归：

```bash
cd moli-knowledge/moli-knowledge-server && mvn test
```

### 依赖说明

| 依赖 | Mock 用例 | 集成用例（7、8） |
|------|-----------|------------------|
| MySQL / Redis | 不需要 | 不需要 |
| Spring 容器 | 不需要（Mockito） | 不需要 |
| `python` + `lint.py` | 不需要 | 需要；脚本默认相对 monorepo 根 `moli-knowledge/kb/tools/lint.py` |
| 配置项 | Mock 注入 | `kb.wiki.lint-script-path`、`kb.wiki.space-dirs`、`kb.sync.python` |

## 5. 手工联调（可选）

服务启动后（`:8090` 或经网关 `/KnowledgeServer`）：

```http
POST /kb/wiki/lint-space
Authorization: <token>
Content-Type: application/json

{
  "spaceId": 900000000000000001,
  "strict": false
}
```

需对目标空间具备 **editor** 权限。`exitCode ≠ 0` 表示 lint 发现问题，HTTP 仍为 200（业务数据在 `data.issues`）。

## 6. 回归策略

1. 改 `KbWikiLintServiceImpl` / DTO / Controller 路由 → 跑 §4 两条测试类
2. 改 `kb/tools/lint.py` 输出 JSON 形态 → 必跑集成用例 7、8
3. T16b 前端治理页落地后 → 补充 E2E；后端仍以本目录 ApiTest 为门禁

## 7. 相关

- API 契约：[`KNOWLEDGE_API.md` §4.6、§8.6–8.7](../api/KNOWLEDGE_API.md)
- 产品方案：`moli-knowledge/kb/wiki/guides/Wiki治理工作台产品方案.md`
- Lint 脚本：`moli-knowledge/kb/tools/lint.py`
- 任务跟踪：`moli-knowledge/TASKS.md` · T16a
