-- =============================================================
-- 运维管理 · 拓扑图菜单（SVR-25c）
-- 挂载：运营管理（parent_id=400）
-- 设计：docs/design/server-topology-visualization.md §3.4
-- perms 复用 operation:server:list，无需新权限码
-- 执行后相关用户重新登录
-- =============================================================
SET NAMES utf8mb4;

INSERT INTO `sys_menu`
  (`id`, `create_id`, `create_time`, `update_id`, `update_time`, `menu_name`, `menu_name_en`, `menu_name_ja`,
   `parent_id`, `path`, `component`, `route_name`, `menu_type`, `perms`, `status`, `icon`, `order_num`)
VALUES
  (407, 1, NOW(), 1, NOW(), '拓扑图', 'Topology', 'トポロジ図',
   400, 'topology', 'operation/topology/index', 'OperationTopology', 'C', 'operation:server:list', 1, 'git-branch', 7)
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `menu_name_en` = VALUES(`menu_name_en`),
  `menu_name_ja` = VALUES(`menu_name_ja`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `route_name` = VALUES(`route_name`),
  `perms` = VALUES(`perms`),
  `status` = VALUES(`status`),
  `icon` = VALUES(`icon`),
  `order_num` = VALUES(`order_num`),
  `update_time` = NOW();

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(910400407, 2, 407)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), menu_id = VALUES(menu_id);
