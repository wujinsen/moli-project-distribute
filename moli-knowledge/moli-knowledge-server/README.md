# moli-knowledge-server · 知识库服务（Java REST 后端）

企业知识库的 **Java REST 后端**，茉莉微服务全家桶的一员。提供空间 / 分类 / 文档 / 标签 / 评论 / 版本 / 收藏 等能力，基于 Spring Boot + MyBatis-Plus + MySQL，复用用户中心的 Shiro 会话与 Dubbo 鉴权。

> **定位（双轨）**：知识的「编写 / 去重 / 提炼」发生在上层 `../kb/`（LLM-Wiki markdown 知识库）；本服务定位为其**下游只读门面 + 对外 Web API**。
> 当前本服务仍是全功能 CRUD，与「只读门面」尚未对齐，对齐计划见 [`../kb/ROADMAP.md`](../kb/ROADMAP.md) 与本文 [规划](#规划roadmap)。

---

## 快速开始

### 1. 前置依赖

| 依赖 | 说明 |
|------|------|
| JDK 8 | 与全家桶一致 |
| MySQL | `moli` 库，需先导入知识库表 |
| Redis | Shiro Session 共享，`database=2`，密码 `123456` |
| Nacos | 服务注册，`127.0.0.1:8848`，namespace `dev` |
| user-center-server | Dubbo 提供方（鉴权）+ 共享 Redis 会话 |
| MinIO（可选） | 附件存储，`127.0.0.1:9000` |

### 2. 初始化数据库

```powershell
# 在仓库根目录：init-db.ps1 已导入知识库表/菜单（utf8mb4 + source）
.\scripts\init-db.ps1 -SkipSeckill
```

### 3. 启动

```powershell
cd moli-knowledge\moli-knowledge-server
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 4. 访问

| 方式 | 地址 |
|------|------|
| 直连 | `http://127.0.0.1:8090/kb/space/page` |
| 网关 | `http://127.0.0.1:21000/KnowledgeServer/kb/space/page` |
| Swagger | `http://127.0.0.1:8090/swagger-ui.html` |

> 受保护接口需带登录态：先经 [用户中心](../../moli-user-center) 登录拿 token，请求头带 `Authorization: <token>`（机制见 [`../kb/wiki/concepts/认证与会话机制.md`](../kb/wiki/concepts/认证与会话机制.md)）。

---

## 配置一览

| 项 | 值 | 来源 |
|----|----|------|
| 服务名 | `knowledge-server` | bootstrap.yml |
| HTTP 端口 | `8090` | application.yml |
| Dubbo 端口 | `20884`（仅 Consumer，订阅 `user-center-server`） | bootstrap.yml |
| 网关路由 | `/KnowledgeServer/**` → StripPrefix=1 | moli-gateway |
| 数据源 | MySQL `moli`（Druid 连接池） | application-dev.yml |
| Redis | `localhost:6379` db=2 pwd=123456 | application-dev.yml |
| MinIO | `http://localhost:9000`（minioadmin/minioadmin） | application-dev.yml |
| Shiro | `moli.user-center.shiro.enabled=true`，会话 86400s | application-dev.yml |

---

## 工程结构

```
moli-knowledge-server/
  src/main/java/com/moli/knowledge/server/
    KnowledgeApplication.java     # 启动类（@MapperScan + @EnableDiscoveryClient）
    config/                       # Druid / MyBatis-Plus / 字段自动填充
    controller/                   # 6 个 REST 控制器（space/category/document/tag/comment/favorite）
    service/  service/impl/       # 业务逻辑
    mapper/                       # MyBatis-Plus Mapper
    entity/                       # 9 张表实体（Kb*）
    dto/                          # 请求/响应 VO（DocumentSaveRequest / DocumentDetailVo / CategoryTreeVo ...）
    enums/                        # DocumentStatus / SpaceVisibility
    util/                         # ShiroUtils（取当前登录用户）
    swagger/                      # Swagger2 配置
  src/main/resources/
    application.yml application-dev.yml bootstrap.yml logback-spring.xml
```

---

## 数据模型

共 **14 张表**，分四组。完整字段、设计理由与表关系图见 [`docs/sql/KNOWLEDGE_SCHEMA.md`](../../docs/sql/KNOWLEDGE_SCHEMA.md)。

**核心内容（9，已做 CRUD）**

| 表 | 说明 |
|----|------|
| `kb_space` | 知识空间（多租户，编码唯一，可见性） |
| `kb_category` | 分类树（parentId 自关联，+icon） |
| `kb_document` | 文档。除 title/summary/content/status/版本外，新增 `slug`(空间内唯一)、`source`(kb/manual)、`source_path`、`content_hash`、`kb_type`(知识类型)、`domain`(领域)，并加 ngram 全文索引 |
| `kb_tag` | 标签（空间内名称唯一） |
| `kb_document_tag` | 文档-标签关联 |
| `kb_comment` | 评论（parentId 楼中楼） |
| `kb_document_version` | 版本历史（+content_hash） |
| `kb_favorite` | 个人收藏 |
| `kb_attachment` | 附件（MinIO，`/kb/attachment` 上传/下载/删除） |

**图谱治理（2）** · **同步/权限/问答（3）** —— 为后续功能预留：

| 表 | 对应功能 |
|----|----------|
| `kb_relation` | 图谱边落库（links_to/same_tag/related/supersedes/references），`resolved=0` 即断链 |
| `kb_lint_issue` | 体检问题持久化 + 处理状态（待处理/已忽略/已修复） |
| `kb_sync_log` | kb→DB 单向增量同步审计（M2） |
| `kb_space_member` | 空间级 ACL（成员可为用户或角色，viewer/editor/admin） |
| `kb_qa_log` | Query `/kb/ask` 历史：问题/答案/引用(JSON)/provider/model/token/反馈 |

> `doc_type`=内容格式(markdown/rich)，`kb_type`=知识类型(guide/service/concept/article/interview/output)，两者不同。
> 向量库刻意不建，先用 MySQL ngram 全文索引，量大再外置（见 [ROADMAP §五](../kb/ROADMAP.md)）。

建表脚本：[`docs/sql/03_knowledge_schema.sql`](../../docs/sql/03_knowledge_schema.sql)。

---

## REST API

统一返回体 `MoliResult<T>`（`{ code, msg, data }`），分页用 MyBatis-Plus `Page`。

### 空间 `/kb/space`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page?pageNum=&pageSize=` | 分页查询 |
| GET | `/{id}` | 详情 |
| POST | `` | 创建 |
| PUT | `` | 更新 |
| DELETE | `/{id}` | 删除 |

### 分类 `/kb/category`
| GET `/tree?spaceId=` 树形查询 · 增删改 |

### 文档 `/kb/document`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/search` | 搜索（`DocumentSearchRequest`：spaceId/categoryId/keyword/status…）；**默认 MySQL ngram 全文索引**，`kb.search.mode=like` 可回退 LIKE |
| GET | `/{id}` | 详情（`DocumentDetailVo`） |
| POST | `` | 保存（`DocumentSaveRequest`） |
| PUT | `/{id}/publish` | 发布 |
| PUT | `/{id}/archive` | 归档 |
| DELETE | `/{id}` | 删除 |
| GET | `/{id}/versions` | 版本历史 |

### 标签 `/kb/tag` · 评论 `/kb/comment` · 收藏 `/kb/favorite`
列表 / 增删（评论支持 `parentId`；收藏为「添加 / 取消 / 我的」）。

### 图谱与体检 `/kb`（T5，移植自 `../kb/tools/serve.py`）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/graph?spaceId=&mode=&maxNodes=&minDeg=` | 关系图谱：节点=文档（轻量，**不查 content**），边读 `kb_relation`（空表回退运行时）。**大库按度数降序裁剪**到 `maxNodes`（full=300/summary=50，上限 2000），`minDeg` 过滤弱连接。返回 `GraphVo{nodes,links,meta}`，`meta` 含 `totalNodes/totalLinks/returnedNodes/returnedLinks/truncated/source/mode` |
| GET | `/graph/ego?spaceId=&docId=&depth=&maxNodes=` | 以 `docId` 为中心 BFS（depth 1~3，逐层查 `kb_relation`，不加载全图）。供点击节点展开邻居。返回同 `GraphVo` |
| GET | `/lint?spaceId=` | 体检（只算不落库）：断链 / 孤儿页 / 缺摘要，返回 `LintVo{broken,orphans,noSummary,counts}` |
| POST | `/lint/scan?spaceId=` | 体检并落库 `kb_lint_issue`（清旧待处理项后重建），返回同 `LintVo` |
| GET | `/lint/issues?spaceId=&status=` | 查询已落库体检问题（status：0待处理/1已忽略/2已修复），返回 `KbLintIssue[]` |
| PUT | `/lint/issue/{id}?status=` | 更新某条体检问题状态 |

### 问答 Query `/kb`（T2）
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ask` | 提问。返回 `AskResponse`（含 `qaLogId` 供反馈） |
| GET | `/ask/history?spaceId=&pageNum=&pageSize=` | 我的问答历史 `Page<QaHistoryVo>` |
| PUT | `/ask/feedback/{id}?useful=` | 问答反馈（1有用/0无用） |

### 浏览 `/kb`（T3）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/index?spaceId=` | **meta**：按 kb_type 分组计数，`groups[].items` 为空 |
| GET | `/index/items?spaceId=&type=&pageNum=&pageSize=` | 分组条目分页（轻量：id/slug/title/spaceId） |
| GET | `/index/search?spaceId=&q=&limit=` | 侧栏搜索（服务端 LIKE 过滤） |
| GET | `/index/locate?spaceId=&slug=` | 深链定位所属分组 |
| GET | `/page?slug=&spaceId=` | 按 slug 取单页，返回 `PageDetailVo{...content, tags, outLinks, backLinks}`。出/入链读 `kb_relation`（需先跑同步脚本） |

### 同步管理 `/kb/sync`（T10）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/sync/logs?spaceId=&batchNo=&pageNum=&pageSize=` | 同步日志分页（需空间 admin 或平台超管） |
| GET | `/sync/status?spaceId=` | 最近一批同步统计（insert/update/delete/skip 计数） |
| POST | `/sync/trigger?spaceId=&spaceCode=` | 触发 `kb/tools/sync_to_db.py` 写库 |
| — | 定时任务 | `kb.sync.schedule-enabled=true` 时按 cron 自动 sync |
| — | Git hook | `kb/tools/install_git_hook.sh` 安装 post-commit 自动 sync |
| — | **GitHub Actions** | [`.github/workflows/kb-sync.yml`](../../.github/workflows/kb-sync.yml)：PR dry-run；main 写库；可手动 sync 远程库 |

> 与 viewer 的差异：viewer 基于 `kb/` markdown 的 frontmatter（`tags`/`related`）+ `edges.jsonl` + `[[slug]]`；
> 本服务基于自身 DB——wikilink 按**文档标题**解析，`related/edges` 用 **同标签**近似，`sources` 用 **summary** 近似。
> `spaceId` 省略则全库。

### 请求示例
```bash
# 搜索文档（MySQL ngram 全文索引，失败自动降级 LIKE）
GET /kb/document/search?spaceId=900000000000000001&keyword=上手

# 保存文档
POST /kb/document
{ "spaceId": 900000000000000001, "categoryId": 900000000000000103,
  "title": "新文档", "content": "# Hello", "status": 0 }
```

---

## 鉴权与会话

- 复用 `moli-user-center-shiro-starter`：从共享 Redis（db=2）按 token 校验 Shiro Session。
- 通过 Dubbo 订阅 `user-center-server`，拿用户 / 权限。
- `ShiroUtils` 取当前登录用户。
- 已在 `sys_system` 注册 SSO 门户（`system_code=moli-knowledge`，`base_url=http://127.0.0.1:21000/KnowledgeServer`，`sso_mode=EXTERNAL`）。

### 空间级 ACL（已落地）

由 `KbAclService` 统一裁决，过滤/断言下沉到 service 层（空间 / 文档 / 浏览 / 问答）：

- 可见性 `visibility`：`2` 公开 / `1` 内部（登录可读）/ `0` 私有（仅成员、负责人）。
- 成员角色 `role`：`viewer` 只读 / `editor` 可改 / `admin` 可管成员；`owner_id` 等同 admin。
- 平台超管（`superadmin`/`admin` 或 `*:*:*`）一票通过全部空间 ACL。
- 列表/检索/问答省略 `spaceId` 时自动收敛到「可读空间集合」；指定空间不可读则报错；写操作校验编辑/管理权限。
- 成员管理：`/kb/space/member`（list / 单条 add·remove / **batch 批量 add·remove** / update，需空间管理权限）。
- 限制：Dubbo 契约暂只透出权限串，**角色型成员(member_type=1)** 运行时不解析，待 `UserCenterServer` 暴露角色后在 `KbAclServiceImpl#memberRole` 补全。

---

## 现状与已知限制（实事求是）

- ~~**检索是 MySQL `LIKE`**~~ → **默认 ngram 全文索引**（`kb.search.mode=fulltext`），索引缺失或异常时自动降级 LIKE。
- ~~`KbAttachment` 仅有 entity + mapper，无 Service / Controller~~ → **已落地** `/kb/attachment`。
- ~~Shiro 已开，但未见空间级 ACL 过滤~~ → **已落地**（见上「空间级 ACL」）。
- 仍是全功能 CRUD，**尚未对齐**「kb/ 为源、本服务只读门面」的目标。

---

## 规划（Roadmap）

按 [`../kb/ROADMAP.md`](../kb/ROADMAP.md)：

| 阶段 | 事项 |
|------|------|
| M2 | `kb`（markdown）→ `kb_document` **单向同步**；`kb_document` 加 `slug` 唯一键 |
| Query | 新增 `POST /kb/ask`：检索选页 → 拼上下文 → 调 LLM → **带引用答案 + 来源列表**（先中等模型，可随时换 `key+base-url+model`） |
| 浏览 | `/kb/index` meta + `/kb/index/items|search|locate`、`/kb/page`（`/kb/graph`、`/kb/lint` 已落地，见上 [REST API](#图谱与体检-kb移植自-kbtoolsservepy)） |
| 后期 | ~~空间级 ACL（复用 Shiro）~~ ✅、~~附件上传（MinIO）~~ ✅、检索升级（Meilisearch/向量，按量触发） |

> 想先在界面看 Query 效果，无需改 Java：用上层零依赖 viewer [`../kb/tools/serve.py`](../kb/tools/serve.py)（`python ../kb/tools/serve.py`）即可浏览 wiki、试检索式 Query。

---

## 相关文档

- 模块总览：[`../README.md`](../README.md)
- LLM-Wiki 知识库：[`../kb/README.md`](../kb/README.md) · 契约 [`../kb/AGENTS.md`](../kb/AGENTS.md) · 规划 [`../kb/ROADMAP.md`](../kb/ROADMAP.md)
- 服务实体页：[`../kb/wiki/services/知识库服务.md`](../kb/wiki/services/知识库服务.md)
