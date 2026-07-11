-- =============================================================
-- 运维任务 · 关联项目（Phase R3 · P1）
-- 运行顺序：在 24_operation_port_matrix.sql 之后执行
-- 设计：docs/design/operation-module-refactor-plan.md §3.3
-- =============================================================
SET NAMES utf8mb4;

ALTER TABLE `operation_task`
  ADD COLUMN `project_id` bigint NULL COMMENT '关联项目台账 ID（deploy 从项目页跳转）' AFTER `server_id`,
  ADD INDEX `idx_operation_task_project` (`project_id`);
