-- =============================================================
-- T19 · 企业知识库 → LLM 配置菜单
-- 挂载：企业知识库（parent_id=900，与 Ingest / Wiki 治理同级）
-- 设计：docs/api/kb-llm-platform-frontend.md
-- 执行后重新登录；保存 Key 需运维配置 KB_LLM_CONFIG_SECRET
-- =============================================================
SET NAMES utf8mb4;

-- 字段顺序：id, create_id, create_time, update_id, update_time,
--   menu_name, menu_name_en, menu_name_ja, parent_id, path, component, route_name,
--   menu_type, perms, status, icon, order_num

INSERT INTO `sys_menu` VALUES
(920, 1, NOW(), 1, NOW(), 'LLM 配置', 'LLM Settings', 'LLM 設定', 900,
 'kb-llm', 'knowledge/kb-llm/index', 'KbPlatformLlmSettings', 'C', 'kb:platform:llm', 1, 'cpu', 7)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), menu_name_en = VALUES(menu_name_en), menu_name_ja = VALUES(menu_name_ja),
  parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
  menu_type = VALUES(menu_type), perms = VALUES(perms), status = VALUES(status), icon = VALUES(icon),
  order_num = VALUES(order_num), update_time = NOW();

INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('kb:platform:llm', 'kb', 'platformLlm', '知识库LLM配置', 920, 1, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

-- 角色 2=系统管理员（可按环境追加 role_id）
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(910900920, 2, 920)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), menu_id = VALUES(menu_id);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(2, 'kb:platform:llm')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);
