# 茉莉系统操作手册（index）

> 独立 wiki 目录，同步至 `kb_space.space_code=moli-ops-manual`（`space_id=900000000000000003`）。  
> 面向部署、登录鉴权、权限配置、联调与日常运维；与技术文章/面试题所在的 `enterprise-kb` 分离。

## 快速入口

| 场景 | 文档 |
|------|------|
| 第一次本地跑起来 | [[本地启动指南]] → [[数据库初始化指南]] |
| 登录与调 API | [[登录与鉴权指南]] → [[swagger接口调试指南]] |
| 给员工开权限 | [[权限管理操作指南]] |
| 前端联调 | [[前端开发与联调指南]] |
| 出问题 | [[故障排查指南]] |
| 知识库模块 | [[知识库使用指南]] → [[wiki同步指南]] |

## guides（操作指导）

- [[本地启动指南]] — Nacos/MySQL/Redis + 各服务启动顺序
- [[数据库初始化指南]] — `scripts/moli.sql`、演示账号、知识库/秒杀表
- [[登录与鉴权指南]] — token、Authorization 头、常见返回码
- [[权限管理操作指南]] — 用户/角色/菜单/部门开通流程
- [[故障排查指南]] — 登录/Redis/Dubbo/Nacos/DB 决策树
- [[swagger接口调试指南]] — 各服务 Swagger 与鉴权头
- [[前端开发与联调指南]] — meiling-ui 本地与网关联调
- [[docker部署指南]] — 容器化部署要点
- [[nginx反向代理与前端部署指南]] — 静态资源与 API 反代
- [[minio-附件存储指南]] — 知识库附件 MinIO
- [[知识库使用指南]] — 浏览/问答/图谱/空间 ACL
- [[wiki同步指南]] — markdown → MySQL 同步
- [[查询与体检指南]] — Query 与 Lint 治理

## services（微服务）

- [[用户中心]] — 权限中枢（8888）
- [[网关]] — 统一入口（21000）
- [[订单服务]] — 订单与秒杀（8087）
- [[bi服务]] — BI 骨架（1128）
- [[知识库服务]] — 知识库 REST（8090）

## concepts（操作相关概念）

- [[认证与会话机制]] — Shiro Session + 共享 Redis
- [[rbac-权限模型]] — 用户→角色→菜单/动作

## 同步命令

```bash
# 预览
python moli-knowledge/kb/tools/sync_to_db.py --wiki-dir wiki-ops --space moli-ops-manual --dry-run

# 写库（需先执行 docs/sql/07_kb_space_ops_manual.sql）
python moli-knowledge/kb/tools/sync_to_db.py --wiki-dir wiki-ops --space moli-ops-manual
```
