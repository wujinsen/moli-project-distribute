-- =============================================================
-- jp-fe-ap-exam：Certify（サーティファイ）分类
-- 在 04_kb_space_jp_exam.sql 之后执行；可重复执行。
-- 导入：mysql --default-character-set=utf8mb4 moli < docs/sql/16_kb_category_jp_certify.sql
-- =============================================================

SET NAMES utf8mb4;

INSERT INTO `kb_category`
  (`id`,`create_id`,`create_time`,`update_id`,`update_time`,`space_id`,`parent_id`,`category_name`,`icon`,`dir_slug`,`sort`,`is_delete`)
VALUES
  (900000000000000125, 1, NOW(), 1, NOW(), 900000000000000002, 0, 'Certify サーティファイ', NULL, 'certify', 5, 0)
ON DUPLICATE KEY UPDATE
  `category_name` = VALUES(`category_name`),
  `dir_slug`      = VALUES(`dir_slug`),
  `sort`          = VALUES(`sort`),
  `is_delete`     = 0,
  `update_time`   = NOW();
