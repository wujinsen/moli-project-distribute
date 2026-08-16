-- =============================================================
-- 企业知识库 · DeepResearch 主题调研（AI-10）菜单 + 角色绑定
-- 运行顺序：在 04_knowledge_menu.sql、30_sso_menu_system_id.sql 之后执行
-- 前端 meiling-ui：component = knowledge/research/index → KnowledgeResearchView.vue
-- 权限：复用 kb:ask:list（与智能问答同级）
-- 字段：18 列（含 system_id=1 · moli-admin，与 900 段其它菜单一致）
-- =============================================================
SET NAMES utf8mb4;

INSERT INTO `sys_menu` VALUES
(911, 1, NOW(), 1, NOW(), '主题调研', 'Deep Research', 'テーマ調査', 900,
 'research', 'knowledge/research/index', 'KnowledgeResearch', 'C', 'kb:ask:list', 1, 'search', 8, 1)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), menu_name_en = VALUES(menu_name_en), menu_name_ja = VALUES(menu_name_ja),
  parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
  menu_type = VALUES(menu_type), perms = VALUES(perms), status = VALUES(status), icon = VALUES(icon),
  order_num = VALUES(order_num), system_id = VALUES(system_id), update_time = NOW();

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(910900911, 2, 911),
(910903911, 3, 911),
(910904911, 4, 911),
(910906911, 6, 911),
(910907911, 7, 911)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), menu_id = VALUES(menu_id);
