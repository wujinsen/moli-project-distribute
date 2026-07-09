-- =============================================================
-- 运维管理 · 项目部署状态字段（架构债：定时同步 deploy status）
-- 运行顺序：在 19_operation_deploy_exec.sql 之后执行
-- =============================================================
SET NAMES utf8mb4;

ALTER TABLE `operation_project_deploy_info`
  ADD COLUMN `deploy_running` tinyint(1) NULL DEFAULT NULL COMMENT '部署进程是否运行（定时同步）' AFTER `remark`,
  ADD COLUMN `last_deploy_check_time` datetime NULL DEFAULT NULL COMMENT '最近部署状态同步时间' AFTER `deploy_running`;
