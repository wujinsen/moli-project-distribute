-- =============================================================
-- 运维管理 · 部署脚本执行权限（SVR-8）
-- 运行顺序：在 18_operation_health_columns.sql 之后执行
-- 设计：docs/design/server-ops-module-roadmap.md §5 P2
-- 说明：GET status 仅需 operation:server:list；POST start/stop/restart 需本权限
-- 执行后相关用户重新登录
-- =============================================================
SET NAMES utf8mb4;

INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('operation:deploy:exec', 'operation', 'deployExec', '执行部署脚本', 400, 6, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(1, 'operation:deploy:exec'),
(2, 'operation:deploy:exec')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);
