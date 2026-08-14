-- =============================================================
-- 运维管理 · 明文凭据查看权限（SVR-3）
-- 运行顺序：在 scripts/moli.sql 基线之后执行
-- 设计：docs/design/server-ops-module-roadmap.md §5 P0
-- 执行后相关用户重新登录
-- =============================================================
SET NAMES utf8mb4;

INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('operation:secret:view', 'operation', 'secretView', '查看运维凭据', 400, 5, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(1, 'operation:secret:view'),
(2, 'operation:secret:view')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);
