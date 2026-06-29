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
updated: 2026-06-22
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

## 5. 与 wiki 双轨

- **markdown 正文**：`kb/wiki/` → sync → `kb_document`
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
