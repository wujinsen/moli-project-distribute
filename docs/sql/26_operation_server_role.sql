-- SVR-23 · 服务器角色分类（server_role）
-- 可重复执行：列/索引已存在则跳过
-- 执行顺序：见 docs/ops/sql-migration-order.md（在 25 之后）

SET NAMES utf8mb4;

-- 1) 列
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'operation_server_info'
    AND COLUMN_NAME = 'server_role'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `operation_server_info` ADD COLUMN `server_role` varchar(32) NULL DEFAULT NULL COMMENT ''角色 app/db/cache/mq/gateway/bastion/middleware/other'' AFTER `environment`',
  'SELECT ''skip: server_role column exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 索引
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'operation_server_info'
    AND INDEX_NAME = 'idx_operation_server_role'
);
SET @ddl := IF(@idx_exists = 0,
  'ALTER TABLE `operation_server_info` ADD INDEX `idx_operation_server_role` (`server_role`)',
  'SELECT ''skip: idx_operation_server_role exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 种子回填（仅空值）
UPDATE `operation_server_info` SET `server_role` = 'app' WHERE `server_role` IS NULL AND (`server_name` LIKE '%backend%' OR `server_name` LIKE '%app%');
UPDATE `operation_server_info` SET `server_role` = 'db' WHERE `server_role` IS NULL AND (`server_name` LIKE '%mysql%' OR `server_name` LIKE '%postgres%' OR `server_name` LIKE '%mariadb%');
UPDATE `operation_server_info` SET `server_role` = 'cache' WHERE `server_role` IS NULL AND (`server_name` LIKE '%redis%' OR `server_name` LIKE '%memcache%');
UPDATE `operation_server_info` SET `server_role` = 'gateway' WHERE `server_role` IS NULL AND (`server_name` LIKE '%gateway%' OR `server_name` LIKE '%nginx%' OR `server_name` LIKE '%lb%');
UPDATE `operation_server_info` SET `server_role` = 'middleware' WHERE `server_role` IS NULL AND (`server_name` LIKE '%nacos%' OR `server_name` LIKE '%zk%' OR `server_name` LIKE '%kafka%');
UPDATE `operation_server_info` SET `server_role` = 'app' WHERE `server_role` IS NULL;
