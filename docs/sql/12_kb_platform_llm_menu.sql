-- =============================================================
-- T19 · 平台系统设置菜单：知识库 LLM
-- 挂载：系统管理（parent_id=1，与系统注册 system/system/index 同级目录）
-- 设计：docs/design/kb-llm-platform-settings.md
-- =============================================================
SET NAMES utf8mb4;

-- 菜单：系统管理 → 知识库 LLM（平台超管 / kb:platform:llm）
INSERT INTO `sys_menu` (`id`, `create_id`, `create_time`, `update_id`, `update_time`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `menu_type`, `perms`, `visible`, `icon`, `is_delete`)
SELECT 920, 1, NOW(), 1, NOW(), '知识库LLM', 1, 90, 'kb-llm', 'system/kb-llm/index', 'KbPlatformLlmSettings', 'C', 'kb:platform:llm', 1, 'cpu', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 920);

INSERT INTO `sys_action` (`action_code`, `module`, `action_key`, `name`, `menu_id`, `sort`, `status`)
SELECT 'kb:platform:llm', 'kb', 'platformLlm', '知识库LLM配置', 920, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_action` WHERE `action_code` = 'kb:platform:llm');

-- 角色 2=系统管理员 授权（可按环境调整）
INSERT IGNORE INTO `sys_role_action` (`role_id`, `action_code`) VALUES (2, 'kb:platform:llm');
