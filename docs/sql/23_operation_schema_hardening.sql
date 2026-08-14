-- =============================================================
-- 运营管理 · Schema 加固（Phase R1）
-- 运行顺序：在 22_operation_command_flex.sql 之后执行
-- 设计：docs/design/operation-module-refactor-plan.md §Phase 1
-- =============================================================
SET NAMES utf8mb4;

-- ---------- 组件表补 server_id ----------
ALTER TABLE `operation_component_deploy_info`
  ADD COLUMN `server_id` bigint NULL COMMENT '服务器ID' AFTER `component_name`,
  ADD INDEX `idx_operation_component_server_id` (`server_id`);

UPDATE `operation_component_deploy_info` c
JOIN `operation_server_info` s
  ON c.`server_ip` = s.`ip` OR c.`server_ip` = s.`inner_ip`
SET c.`server_id` = s.`id`
WHERE c.`server_id` IS NULL;

-- ---------- N:N 唯一约束 ----------
ALTER TABLE `operation_server_project`
  ADD UNIQUE KEY `uk_server_project` (`server_id`, `project_id`);

ALTER TABLE `operation_server_component`
  ADD UNIQUE KEY `uk_server_component` (`server_id`, `component_id`);

-- ---------- 查询索引 ----------
ALTER TABLE `operation_server_info`
  ADD INDEX `idx_operation_server_env` (`environment`),
  ADD INDEX `idx_operation_server_ip` (`ip`);

ALTER TABLE `operation_project_deploy_info`
  ADD INDEX `idx_operation_project_env` (`environment`),
  ADD INDEX `idx_operation_project_name` (`project_name`(64));

ALTER TABLE `operation_component_deploy_info`
  ADD INDEX `idx_operation_component_env` (`environment`),
  ADD INDEX `idx_operation_component_name` (`component_name`(64));
