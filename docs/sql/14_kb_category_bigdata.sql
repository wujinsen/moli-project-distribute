-- =============================================================
-- enterprise-kb：补充分类 bigdata（Phase 2 wujinsen ingest）
--
-- 适用：已执行过旧版 13_kb_category_enterprise_topic.sql（仅 141–150）
--       但未包含 bigdata 的环境。
-- 全量新环境：直接跑 13_kb_category_enterprise_topic.sql（已含 151）即可。
-- =============================================================

SET NAMES utf8mb4;

INSERT INTO `kb_category`
  (`id`,`create_id`,`create_time`,`update_id`,`update_time`,`space_id`,`parent_id`,`category_name`,`icon`,`dir_slug`,`sort`,`is_delete`)
VALUES
  (900000000000000151, 1, NOW(), 1, NOW(), 900000000000000001, 0, '大数据', NULL, 'bigdata', 11, 0)
ON DUPLICATE KEY UPDATE
  `category_name` = VALUES(`category_name`),
  `dir_slug`      = VALUES(`dir_slug`),
  `sort`          = VALUES(`sort`),
  `is_delete`     = 0,
  `update_time`   = NOW();

SELECT `id`, `category_name`, `dir_slug`, `sort`
FROM `kb_category`
WHERE `space_id` = 900000000000000001 AND `dir_slug` = 'bigdata';
