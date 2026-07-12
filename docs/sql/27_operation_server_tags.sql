-- SVR-24 · 服务器标签（tags JSON 数组）
-- 可重复执行：列已存在则跳过
-- 执行顺序：见 docs/ops/sql-migration-order.md（在 26 之后）

SET NAMES utf8mb4;

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'operation_server_info'
    AND COLUMN_NAME = 'tags'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `operation_server_info` ADD COLUMN `tags` varchar(512) NULL DEFAULT NULL COMMENT ''标签 JSON 数组，如 ["gz","knowledge"]'' AFTER `server_role`',
  'SELECT ''skip: tags column exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 种子回填（仅空值，按名称推断示例标签）
UPDATE `operation_server_info` SET `tags` = '["local","dev"]' WHERE `tags` IS NULL AND `server_name` LIKE '%-dev';
UPDATE `operation_server_info` SET `tags` = '["test"]' WHERE `tags` IS NULL AND `environment` = 2;
UPDATE `operation_server_info` SET `tags` = '["pre"]' WHERE `tags` IS NULL AND `environment` = 3;
UPDATE `operation_server_info` SET `tags` = '["pro"]' WHERE `tags` IS NULL AND `environment` = 4;
