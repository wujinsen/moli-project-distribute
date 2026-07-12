-- SVR-24 · 服务器标签（tags JSON 数组）
-- 可重复执行：列已存在则跳过
-- 执行顺序：见 docs/ops/sql-migration-order.md（建议在 26 之后；未跑 26 也可单独执行）

SET NAMES utf8mb4;

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'operation_server_info'
    AND COLUMN_NAME = 'tags'
);

SET @role_col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'operation_server_info'
    AND COLUMN_NAME = 'server_role'
);

-- server_role 已存在则紧随其后；否则接在 environment 后（兼容未执行 26 的库）
SET @after_col := IF(@role_col_exists > 0, 'server_role', 'environment');

SET @ddl := IF(@col_exists = 0,
  CONCAT(
    'ALTER TABLE `operation_server_info` ADD COLUMN `tags` varchar(512) NULL DEFAULT NULL ',
    'COMMENT ''标签 JSON 数组，如 ["gz","knowledge"]'' AFTER `', @after_col, '`'
  ),
  'SELECT ''skip: tags column exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 种子回填（仅空值，按名称推断示例标签）
UPDATE `operation_server_info` SET `tags` = '["local","dev"]' WHERE `tags` IS NULL AND `server_name` LIKE '%-dev';
UPDATE `operation_server_info` SET `tags` = '["test"]' WHERE `tags` IS NULL AND `environment` = 2;
UPDATE `operation_server_info` SET `tags` = '["pre"]' WHERE `tags` IS NULL AND `environment` = 3;
UPDATE `operation_server_info` SET `tags` = '["pro"]' WHERE `tags` IS NULL AND `environment` = 4;
