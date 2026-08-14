-- =============================================================
-- 运维管理 · SSH 远程部署 / 文件发布（SVR-13 ~ SVR-16）
-- 运行顺序：在 20_operation_project_deploy_columns.sql 之后执行
-- 设计：docs/design/server-ops-module-roadmap.md §6
-- 执行后相关用户重新登录
-- =============================================================
SET NAMES utf8mb4;

-- ---------- operation_server_info：SSH 凭据字段 ----------
-- 注：MySQL 8.0.29 以下不支持 ADD COLUMN IF NOT EXISTS；若列已存在会报 Duplicate column，可忽略该 ALTER 后继续执行下方语句
ALTER TABLE `operation_server_info`
  ADD COLUMN `ssh_port` int NULL DEFAULT 22 COMMENT 'SSH 端口' AFTER `last_check_time`,
  ADD COLUMN `ssh_user` varchar(64) NULL DEFAULT 'ubuntu' COMMENT 'SSH 登录用户' AFTER `ssh_port`,
  ADD COLUMN `ssh_auth_type` tinyint NULL DEFAULT 1 COMMENT '1私钥 2密码' AFTER `ssh_user`,
  ADD COLUMN `ssh_private_key` text NULL COMMENT 'SSH 私钥 AES-GCM 密文' AFTER `ssh_auth_type`,
  ADD COLUMN `ssh_passphrase` varchar(512) NULL COMMENT '私钥口令/登录密码 AES-GCM 密文' AFTER `ssh_private_key`,
  ADD COLUMN `conn_pref` varchar(16) NULL DEFAULT 'auto' COMMENT '连接偏好 auto/inner/public' AFTER `ssh_passphrase`;

-- ---------- operation_task：异步任务 ----------
CREATE TABLE IF NOT EXISTS `operation_task` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `task_type` varchar(32) NULL DEFAULT NULL COMMENT 'deploy/upload/command',
  `server_id` bigint NULL DEFAULT NULL COMMENT '目标服务器',
  `service_key` varchar(64) NULL DEFAULT NULL COMMENT 'user-center/gateway/knowledge',
  `action` varchar(64) NULL DEFAULT NULL COMMENT 'start/stop/restart/upload 等',
  `target_name` varchar(512) NULL DEFAULT NULL COMMENT '目标描述',
  `status` varchar(16) NULL DEFAULT 'pending' COMMENT 'pending/running/success/failed',
  `progress` int NULL DEFAULT 0 COMMENT '0-100',
  `task_log` mediumtext NULL COMMENT '执行日志',
  `message` varchar(512) NULL DEFAULT NULL COMMENT '结果摘要',
  `finish_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_operation_task_server` (`server_id`),
  KEY `idx_operation_task_type_status` (`task_type`, `status`),
  KEY `idx_operation_task_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='运维异步任务';

-- ---------- 权限 ----------
INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('operation:ssh:manage', 'operation', 'sshManage', '配置服务器SSH', 402, 7, 1),
('operation:file:upload', 'operation', 'fileUpload', '上传文件发布', 405, 1, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(1, 'operation:ssh:manage'),
(1, 'operation:file:upload'),
(2, 'operation:ssh:manage'),
(2, 'operation:file:upload')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);

-- ---------- 菜单：部署中心 ----------
-- 字段顺序：id, create_id, create_time, update_id, update_time,
--   menu_name, menu_name_en, menu_name_ja, parent_id, path, component, route_name,
--   menu_type, perms, status, icon, order_num

INSERT INTO `sys_menu` VALUES
(405, 1, NOW(), 1, NOW(), '部署中心', 'Deploy Center', 'デプロイセンター', 400,
 'deploy', 'operation/deploy/index', 'OperationDeployCenter', 'C', 'operation:server:list', 1, 'upload', 5)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), menu_name_en = VALUES(menu_name_en), menu_name_ja = VALUES(menu_name_ja),
  parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
  menu_type = VALUES(menu_type), perms = VALUES(perms), status = VALUES(status), icon = VALUES(icon),
  order_num = VALUES(order_num), update_time = NOW();

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(910400405, 1, 405),
(910402405, 2, 405)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), menu_id = VALUES(menu_id);
