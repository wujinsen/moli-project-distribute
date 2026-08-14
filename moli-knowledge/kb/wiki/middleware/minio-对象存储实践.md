---
title: MinIO 对象存储实践
slug: minio-对象存储实践
type: guide
status: active
tags: [MinIO, 对象存储, S3, 附件]
sources:
- raw/wujinsen_markdown/架构/文件存储/minio/Minio介绍.note.md
- raw/wujinsen_markdown/架构/文件存储/minio/minio安装.note.md
- raw/wujinsen_markdown/架构/文件存储/minio/minio数据迁移.note.md
- raw/wujinsen_markdown/架构/文件存储/minio/minio设置永久访问链接.note.md
- raw/wujinsen_markdown/架构/文件存储/minio/上传文件到minio文件大小限制.note.md
related: [消息队列, 容器与-docker, ops/linux-运维基础, api-接口安全设计]
created: 2026-07-05
updated: 2026-07-05
---

# MinIO 对象存储实践

> MinIO 是兼容 **S3 API** 的开源对象存储，适合附件、导出文件、静态资源。部署见 [[ops/容器与-docker]]。

## 1. 定位

| 对比 | MinIO | MySQL |
|------|-------|-------|
| 模型 | Bucket + Object（大文件、非结构化） | 行记录 |
| 访问 | HTTP/S3 SDK | SQL |
| 场景 | 图片/报表/备份 | 业务数据 |

## 2. Linux 单机安装（raw 笔记摘要）

```bash
wget http://dl.minio.org.cn/server/minio/release/linux-amd64/minio
chmod +x minio
export MINIO_ROOT_USER=minioadmin
export MINIO_ROOT_PASSWORD='强密码'
./minio server /opt/minio/data --address "0.0.0.0:9000" --console-address "0.0.0.0:9001"
```

| 端口 | 用途 |
|------|------|
| **9000** | S3 API |
| **9001** | Web Console |

后台运行示例：`nohup ./minio server /opt/minio/data --address 0.0.0.0:9000 --console-address 0.0.0.0:9001 &`

## 3. 客户端 mc

```bash
mc alias set myminio http://127.0.0.1:9000 minioadmin '强密码'
mc mb myminio/attachments
mc cp local.pdf myminio/attachments/
```

## 4. 常见问题（raw）

| 问题 | 方向 |
|------|------|
| 上传大小限制 | 调整 Nginx `client_max_body_size` 或应用 multipart 阈值 |
| 永久访问链接 | 预签名 URL / 桶策略（公开读慎用） |
| 集群迁移 | **rclone** `sync` 源桶 → 目标桶（S3 provider=Minio） |

## 5. 与 Java 服务

- SDK：MinIO Java Client 或 AWS S3 SDK（endpoint 指向 MinIO）。
- 凭证勿写进代码；生产用环境变量或配置中心。
- 外网暴露需 HTTPS + 鉴权，见 [[security/api-接口安全设计]]。

## 相关

[[ops/linux-运维基础]] · [[middleware/消息队列]]（异步导出大文件时可配合 MQ）

原文插图 annex：[[middleware/annex-上传文件到minio文件大小限制]]
