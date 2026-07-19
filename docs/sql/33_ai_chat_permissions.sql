-- =============================================================
-- AI-4 W7 · ChatBI 权限 ai:chat:* + 菜单
-- 运行顺序：在 32_ai_chat_trace.sql 之后
-- 设计：docs/design/bi-chatbi-nl2sql-contract.md §1.1
-- =============================================================
SET NAMES utf8mb4;

-- ChatBI 问数（挂烛龙 BI 段 600）
INSERT INTO `sys_menu` VALUES
(610, 1, NOW(), 1, NOW(), 'ChatBI 问数', 'ChatBI', 'ChatBI 問数', 600,
 'chatbi', 'CandlelightDragon/chatbi/index', 'ChatBiAsk', 'C', 'ai:chat:query', 1, 'message', 4, 6)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), menu_name_en = VALUES(menu_name_en), menu_name_ja = VALUES(menu_name_ja),
  parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
  menu_type = VALUES(menu_type), perms = VALUES(perms), status = VALUES(status), icon = VALUES(icon),
  order_num = VALUES(order_num), system_id = VALUES(system_id), update_time = NOW();

INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('ai:chat:query', 'ai', 'chatQuery', 'ChatBI 自然语言问数', 610, 1, 1),
('ai:chat:trace', 'ai', 'chatTrace', 'ChatBI 问答链路', 610, 2, 1),
('ai:chat:trace:all', 'ai', 'chatTraceAll', 'ChatBI 跨用户链路', 610, 3, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(910600610, 1, 610),
(910720610, 2, 610),
(910730610, 3, 610)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), menu_id = VALUES(menu_id);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(1, 'ai:chat:query'),
(1, 'ai:chat:trace'),
(1, 'ai:chat:trace:all'),
(2, 'ai:chat:query'),
(2, 'ai:chat:trace'),
(3, 'ai:chat:query'),
(3, 'ai:chat:trace')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);
