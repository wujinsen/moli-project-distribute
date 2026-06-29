---
title: 事故复盘（Postmortem）
slug: 事故复盘-postmortem
type: guide
status: active
tags: [运维, 质量, 协作]
sources:
  - raw/wujinsen_markdown/
related: [项目文档总览, 代码审查-checklist, 技术债-管理]
created: 2026-06-21
updated: 2026-06-21
---

# 事故复盘（Postmortem）

> 排查 [[项目文档总览]]；CR [[代码审查-checklist]]；技术债 [[技术债-管理]]。

## 1. 模板

1. **摘要**：影响范围、时长、严重级别
2. **时间线**：发现→定位→缓解→恢复
3. **根因**：5 Whys，区分触发 vs 系统性
4. **行动项**：Owner + 截止日期（非 blame）

## 2. 示例场景

- 秒杀超卖 / 库存不一致 → [[秒杀-库存对账校正]]
- 全站登录失败 → [[茉莉登录与鉴权故障根因汇总]]
- 连接池耗尽 → [[druid-连接池泄漏排查]]

## 3. 文化

- Blameless：改进系统而非追责
- ADR 记录重大决策 [[架构决策-adr]]

## 相关

[[混沌工程入门]] · [[prometheus-告警规则设计]]
