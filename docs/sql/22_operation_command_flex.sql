-- =============================================================
-- 运维管理 · 灵活自定义（SVR-18 ~ SVR-20）
-- 运行顺序：在 21_operation_ssh_deploy.sql 之后执行
-- =============================================================
SET NAMES utf8mb4;

ALTER TABLE `operation_server_info`
  ADD COLUMN `upload_allowed_roots` text NULL COMMENT '该服务器允许上传的路径前缀，逗号或换行分隔' AFTER `conn_pref`;

INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('operation:command:exec', 'operation', 'commandExec', '远程执行命令', 405, 2, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(1, 'operation:command:exec'),
(2, 'operation:command:exec')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);
