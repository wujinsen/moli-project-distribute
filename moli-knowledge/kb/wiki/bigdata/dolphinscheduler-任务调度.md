---
title: DolphinScheduler 任务调度
slug: dolphinscheduler-任务调度
type: guide
status: active
tags: [DolphinScheduler, 调度, 大数据]
sources:
- raw/wujinsen_markdown/BigData/DolphinScheduler/分享 springboot项目集成dolphinscheduler调度器 实现datax数据同步任务.note.md
- raw/wujinsen_markdown/BigData/DolphinScheduler/单机部署(Standalone).note.md
- raw/wujinsen_markdown/BigData/DolphinScheduler/安装部署/docker 配置文件common.properties.note.md
- raw/wujinsen_markdown/BigData/DolphinScheduler/安装部署/docker 配置文件install_config.conf.note.md
- raw/wujinsen_markdown/BigData/DolphinScheduler/安装部署/dolphinscheduler 启动停止命令.note.md
- raw/wujinsen_markdown/BigData/DolphinScheduler/安装部署/dolphinscheduler安装.note.md
- raw/wujinsen_markdown/BigData/DolphinScheduler/安装部署/mac 添加用户.note.md
- raw/wujinsen_markdown/BigData/DolphinScheduler/安装部署/采坑记录.note.md
- raw/wujinsen_markdown/BigData/架构设计/任务调度平台架构设计/任务调度系统框架.note.md
- raw/wujinsen_markdown/BigData/架构设计/任务调度平台架构设计/大数据调度系统为什么选型Apache DolphinScheduler.note.md
related: [数据采集与-etl-工具选型, jenkins-ci入门, hive-数仓与-sql]
created: 2026-07-05
updated: 2026-07-05
---

# DolphinScheduler 任务调度

## 1. 定位

分布式 **DAG 工作流**调度；可视化编排 Hive/Spark/Sql/Shell 等任务。

## 2. 核心概念

- **Project / Process**：项目与工作流
- **Task**：Shell、SQL、Spark、SubProcess 等
- **依赖**：上下游；失败策略、重试、告警

## 3. 与 Jenkins

Jenkins 偏 **CI/CD** 构建发布；DS 偏 **数据管道** 日批依赖。见 [[ops/jenkins-ci入门]]。

## 4. 选型（raw）

对比 Azkaban/Oozie/Airflow：DS 中文社区、多租户、资源中心较完善。

## 批次#1323 增补（wujinsen Phase2 P2 数仓调度）

本页 Phase 2 #1323 创建；sources **10** 篇。
