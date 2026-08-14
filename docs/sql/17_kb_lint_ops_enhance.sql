-- =============================================================
-- KBOPS-8 / KBOPS-9 · 体检工单增强 + 运维 Dashboard 权限
-- 运行顺序：在 03_knowledge_schema.sql、16_kb_import_entry_menu.sql 之后
-- =============================================================
SET NAMES utf8mb4;

-- 体检工单：处理人 + 优先级（0普通 1高 2紧急）
ALTER TABLE `kb_lint_issue`
  ADD COLUMN `assignee_id` bigint DEFAULT NULL COMMENT '处理人用户ID' AFTER `status`,
  ADD COLUMN `priority` tinyint DEFAULT 0 COMMENT '0普通 1高 2紧急' AFTER `assignee_id`;

ALTER TABLE `kb_lint_issue`
  ADD KEY `idx_kb_lint_assignee` (`assignee_id`),
  ADD KEY `idx_kb_lint_priority_status` (`priority`, `status`);

-- 运维 Dashboard 菜单动作（挂健康体检菜单 904 或独立运维页，此处挂 904）
INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('kb:ops:dashboard', 'kb', 'opsDashboard', '知识库运维看板', 904, 10, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(2, 'kb:ops:dashboard'),
(3, 'kb:ops:dashboard')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);
