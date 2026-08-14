-- =============================================================
-- 运维任务 · 关联项目（Phase R3 · P1）
-- 可重复执行：列/索引已存在则跳过
-- 运行顺序：见 docs/ops/sql-migration-order.md（在 24 之后）
-- 设计：docs/design/operation-module-refactor-plan.md §3.3
-- =============================================================
SET NAMES utf8mb4;

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'operation_task'
    AND COLUMN_NAME = 'project_id'
);

SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `operation_task` ADD COLUMN `project_id` bigint NULL '
  'COMMENT ''关联项目台账 ID（deploy 从项目页跳转）'' AFTER `server_id`',
  'SELECT ''skip: project_id column exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'operation_task'
    AND INDEX_NAME = 'idx_operation_task_project'
);

SET @idx_ddl := IF(@idx_exists = 0,
  'ALTER TABLE `operation_task` ADD INDEX `idx_operation_task_project` (`project_id`)',
  'SELECT ''skip: idx_operation_task_project exists''');
PREPARE stmt FROM @idx_ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
