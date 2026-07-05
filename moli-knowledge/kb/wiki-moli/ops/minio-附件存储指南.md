---
title: MinIO 附件存储指南
slug: minio-附件存储指南
type: guide
status: active
tags: [minio, 对象存储, 附件, P0]
sources:
  - moli-knowledge/moli-knowledge-server/src/main/resources/application-dev.yml
  - moli-knowledge/moli-knowledge-server/README.md
  - raw/wujinsen_markdown/架构/文件存储/minio/minio安装.note.md
related: [知识库服务, 知识库使用指南, 本地启动指南, 技术栈与版本]
created: 2026-06-22
updated: 2026-07-05
---

# MinIO 附件存储指南

> 服务实体 [[知识库服务]]；API [[知识库使用指南]]；技术栈 [[技术栈与版本]]（MinIO 7.0.2）。

面向「知识库附件上传/下载」的 MinIO 本地搭建与接口说明。

## 1. MinIO 是什么

**MinIO** 是 S3 兼容的**对象存储**（bucket + object key），适合图片、PDF、导出文件。茉莉 **knowledge-server** 用其存 `kb_attachment`，元数据在 MySQL。

## 2. 本地启动 MinIO

### Docker（推荐）

```bash
docker run -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  quay.io/minio/minio server /data --console-address ":9001"
```

### 二进制

```bash
chmod +x minio
./minio server /path/to/data --address 0.0.0.0:9000 --console-address 0.0.0.0:9001
```

| 端点 | 默认 |
|------|------|
| API | http://127.0.0.1:9000 |
| Console | http://127.0.0.1:9001 |
| 账号 | minioadmin / minioadmin |

## 3. knowledge-server 配置

`application-dev.yml`：

```yaml
minio:
  url: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucket: moli-knowledge
```

首次上传前需存在 bucket `moli-knowledge`（Console 创建或代码自动建）。

## 4. 附件 API

经网关 Base：`http://127.0.0.1:21000/KnowledgeServer`（需 [[登录与鉴权指南]] token）。

| 操作 | 方法 | 路径 |
|------|------|------|
| 上传 | POST | `/kb/attachment/upload`（multipart） |
| 下载 | GET | `/kb/attachment/{id}` |
| 删除 | DELETE | `/kb/attachment/{id}` |

流程：上传 → MinIO `putObject` + 写 `kb_attachment` 表；删除为**软删**记录，MinIO 对象默认保留（见 `KNOWLEDGE_API.md`）。

**MySQL 存什么（没有 URL 字段）**

| 字段 | 含义 |
|------|------|
| `id` | 附件主键；**下载 URL 用这个 id** |
| `document_id` | 挂在哪篇文档（`kb_document.id`） |
| `file_name` | 原始文件名 |
| **`object_key`** | MinIO 对象键，如 `kb/attachment/900/1001/demo.pdf` |
| `file_size` / `content_type` | 大小与 MIME |
| `is_delete` | 软删标记 |

**下载链接怎么来**

- 库内**不存** HTTP URL。
- 前端拼：`GET /KnowledgeServer/kb/attachment/{id}`（带 `Authorization`）。
- 服务端：`id` → 查 `object_key` → MinIO 读流返回。

**正文会不会自动带上附件链接？**

- **不会**。`kb_document.content` 是 markdown 正文；附件在独立表。
- 要在正文里可点，需 editor 手改 md（或未来 T21「插入附件链接」）；与 T22 inline 图（`.assets/` / Asset API）是另一条线。

**前端入口（2026-07-05）**

- **文档浏览**：附件列表 + 下载（只读）。
- **Wiki 编辑**：上传 / 删除 / 列表。详见 `docs/api/knowledge-workbench-frontend.md` §1.2。

## 5. 与 wiki 双轨

- **markdown 正文**：`kb/wiki-moli/` → sync → `kb_document`
- **二进制附件**：MinIO + `kb_attachment`

二者通过文档 id / slug 业务关联，勿把大文件塞进 markdown 仓库。

## 6. 常见问题

| 现象 | 处理 |
|------|------|
| Connection refused 9000 | 启动 MinIO |
| Access Denied | accessKey/secretKey 与配置一致 |
| Bucket 不存在 | Console 建 `moli-knowledge` |
| 上传大小限制 | Spring `multipart.max-file-size`（dev 10MB） |

## 7. 生产注意

- 独立密钥，禁用默认 minioadmin
- HTTPS、桶策略、生命周期清理孤儿 object
- 软删策略：定期任务清理 MinIO 与 DB 一致

MinIO **非**全站文件中心；其他模块若需对象存储可复用同一集群不同 bucket。
