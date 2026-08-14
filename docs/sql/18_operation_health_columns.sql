-- =============================================================
-- 运维管理 · 健康探测字段（SVR-4）
-- 运行顺序：在 17_operation_secret_view.sql 之后执行
-- =============================================================
SET NAMES utf8mb4;

ALTER TABLE `operation_server_info`
  ADD COLUMN `status` tinyint NULL DEFAULT 0 COMMENT '健康 0未知 1可达 2不可达 3跳过' AFTER `remark`,
  ADD COLUMN `last_check_time` datetime NULL DEFAULT NULL COMMENT '最近探测时间' AFTER `status`;

ALTER TABLE `operation_component_deploy_info`
  ADD COLUMN `status` tinyint NULL DEFAULT 0 COMMENT '健康 0未知 1可达 2不可达 3跳过' AFTER `remark`,
  ADD COLUMN `last_check_time` datetime NULL DEFAULT NULL COMMENT '最近探测时间' AFTER `status`;
