-- =============================================================
-- 移除 kb:admin（全局 KB 权限由 superadmin/admin / *:*:* 承担）
-- 在 04_knowledge_menu.sql 或 05_knowledge_action_patch.sql 之后执行
-- =============================================================

DELETE FROM `sys_role_action` WHERE `perm_code` = 'kb:admin';
DELETE FROM `sys_action` WHERE `perm_code` = 'kb:admin';
DELETE FROM `sys_role_menu` WHERE `menu_id` = 906;
DELETE FROM `sys_menu` WHERE `id` = 906;
