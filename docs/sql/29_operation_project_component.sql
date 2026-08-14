-- =============================================================
-- 运营管理 · 项目↔组件依赖（SVR-26a）
-- 运行顺序：在 27_operation_server_tags.sql 之后执行
-- 设计：docs/design/operation-relations-navigation.md §5.1
-- =============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `operation_project_component` (
  `id` bigint NOT NULL COMMENT '主键',
  `project_id` bigint NOT NULL COMMENT '项目 ID',
  `component_id` bigint NOT NULL COMMENT '组件 ID',
  `remark` varchar(256) NULL DEFAULT NULL COMMENT '依赖说明，如 业务库/会话缓存',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_operation_project_component`(`project_id`, `component_id`),
  INDEX `idx_opc_component`(`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目→组件依赖（手工维护）';
