-- =============================================================
-- 企业知识库 · Ingest 工作台 T15e 增量
-- 运行顺序：在 08_kb_ingest_workbench.sql 之后执行
-- 内容：enrich patch 列 + 批次模板表
-- =============================================================

-- enrich 增量段落（patch）；draft 仍为合并预览/落盘全文（重复执行前请确认列未存在）
ALTER TABLE `kb_ingest_draft`
  ADD COLUMN `patch` mediumtext COMMENT 'enrich 追加段落（EnrichWriter patch）' AFTER `baseline`;

-- 批次模板（可复用 raw 范围 / 期望类型 / 可选 plan）
CREATE TABLE IF NOT EXISTS `kb_ingest_template` (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint NOT NULL COMMENT '目标空间ID',
  `name` varchar(128) NOT NULL COMMENT '模板名称',
  `topic` varchar(255) NOT NULL COMMENT '默认主题',
  `expect_types` varchar(255) DEFAULT NULL COMMENT '期望产出类型，逗号分隔',
  `raw_paths` text COMMENT 'raw 路径 JSON 数组',
  `plan_json` mediumtext COMMENT '可选 Plan JSON 快照',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `is_delete` int DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_ingest_tpl_space` (`space_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Ingest 批次模板';
