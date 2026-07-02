-- =============================================================
-- kb_category 分类=目录：dir_slug（已有库增量，可重复执行）
-- 在 03_knowledge_schema.sql 及 04/07 空间种子之后执行。
-- 若库中仍有 default_type 列，请再跑 15_kb_category_drop_default_type.sql
-- =============================================================

SET NAMES utf8mb4;

SET @db = DATABASE();

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'kb_category' AND COLUMN_NAME = 'dir_slug');
SET @sql = IF(@col = 0,
  'ALTER TABLE `kb_category` ADD COLUMN `dir_slug` varchar(64) DEFAULT NULL COMMENT ''绑定的 wiki 子目录名（分类=目录）'' AFTER `icon`',
  'SELECT ''dir_slug exists'' AS _skip');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'kb_category' AND INDEX_NAME = 'uk_kb_category_dir');
SET @sql = IF(@idx = 0,
  'ALTER TABLE `kb_category` ADD UNIQUE KEY `uk_kb_category_dir` (`space_id`, `dir_slug`)',
  'SELECT ''uk_kb_category_dir exists'' AS _skip');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `kb_category` SET `is_delete` = 1, `update_time` = NOW()
WHERE `id` IN (900000000000000101, 900000000000000102, 900000000000000103)
  AND `is_delete` = 0;

INSERT INTO `kb_category`
  (`id`,`create_id`,`create_time`,`update_id`,`update_time`,`space_id`,`parent_id`,`category_name`,`icon`,`dir_slug`,`sort`,`is_delete`)
VALUES
  (900000000000000111, 1, NOW(), 1, NOW(), 900000000000000001, 0, '操作指导', NULL, 'guides',    1, 0),
  (900000000000000112, 1, NOW(), 1, NOW(), 900000000000000001, 0, '微服务',   NULL, 'services',  2, 0),
  (900000000000000113, 1, NOW(), 1, NOW(), 900000000000000001, 0, '概念',     NULL, 'concepts',  3, 0),
  (900000000000000114, 1, NOW(), 1, NOW(), 900000000000000001, 0, '技术文章', NULL, 'articles',  4, 0),
  (900000000000000115, 1, NOW(), 1, NOW(), 900000000000000001, 0, '面试题',   NULL, 'interview', 5, 0),
  (900000000000000116, 1, NOW(), 1, NOW(), 900000000000000001, 0, '综合',     NULL, 'outputs',   6, 0)
ON DUPLICATE KEY UPDATE
  `category_name` = VALUES(`category_name`),
  `dir_slug`      = VALUES(`dir_slug`),
  `sort`          = VALUES(`sort`),
  `is_delete`     = 0,
  `update_time`   = NOW();

INSERT INTO `kb_category`
  (`id`,`create_id`,`create_time`,`update_id`,`update_time`,`space_id`,`parent_id`,`category_name`,`icon`,`dir_slug`,`sort`,`is_delete`)
VALUES
  (900000000000000121, 1, NOW(), 1, NOW(), 900000000000000002, 0, '操作指导', NULL, 'guides',    1, 0),
  (900000000000000122, 1, NOW(), 1, NOW(), 900000000000000002, 0, '面试题',   NULL, 'interview', 2, 0)
ON DUPLICATE KEY UPDATE
  `category_name` = VALUES(`category_name`),
  `dir_slug`      = VALUES(`dir_slug`),
  `sort`          = VALUES(`sort`),
  `is_delete`     = 0,
  `update_time`   = NOW();

UPDATE `kb_category` SET `is_delete` = 1, `update_time` = NOW()
WHERE `space_id` = 900000000000000003 AND `dir_slug` IN ('services', 'concepts') AND `is_delete` = 0;

INSERT INTO `kb_category`
  (`id`,`create_id`,`create_time`,`update_id`,`update_time`,`space_id`,`parent_id`,`category_name`,`icon`,`dir_slug`,`sort`,`is_delete`)
VALUES
  (900000000000000131, 1, NOW(), 1, NOW(), 900000000000000003, 0, '操作指导', NULL, 'guides',  1, 0),
  (900000000000000134, 1, NOW(), 1, NOW(), 900000000000000003, 0, '产品',     NULL, 'product', 2, 0),
  (900000000000000135, 1, NOW(), 1, NOW(), 900000000000000003, 0, '技术',     NULL, 'develop', 3, 0),
  (900000000000000136, 1, NOW(), 1, NOW(), 900000000000000003, 0, '运维',     NULL, 'ops',     4, 0),
  (900000000000000137, 1, NOW(), 1, NOW(), 900000000000000003, 0, '测试',     NULL, 'test',    5, 0)
ON DUPLICATE KEY UPDATE
  `category_name` = VALUES(`category_name`),
  `dir_slug`      = VALUES(`dir_slug`),
  `sort`          = VALUES(`sort`),
  `is_delete`     = 0,
  `update_time`   = NOW();

UPDATE `kb_document` SET `category_id` = 900000000000000111, `update_time` = NOW()
WHERE `id` = 900000000000000301 AND `category_id` = 900000000000000103;
