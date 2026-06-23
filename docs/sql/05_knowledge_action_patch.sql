-- =============================================================
-- 知识库 sys_action 修正（已有环境在 04_knowledge_menu.sql 之后执行一次）
-- 空间管理 Tab：CRUD + 批量授权；健康体检 Tab：体检扫描 + Wiki 同步
-- =============================================================

INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('kb:space:add',    'kb', 'spaceAdd',    '新增空间',           909, 1, 1),
('kb:space:edit',   'kb', 'spaceEdit',   '修改空间',           909, 2, 1),
('kb:space:remove', 'kb', 'spaceRemove', '删除空间',           909, 3, 1),
('kb:space:member', 'kb', 'spaceMember', '批量授权',           909, 4, 1),
('kb:lint:scan',    'kb', 'lintScan',    '知识库体检扫描',     904, 1, 1),
('kb:sync:trigger', 'kb', 'syncTrigger', '触发Wiki同步',       904, 2, 1),
('kb:admin',        'kb', 'admin',       '知识库管理员（全局）', 901, 9, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(2, 'kb:space:add'), (2, 'kb:space:edit'), (2, 'kb:space:remove'), (2, 'kb:space:member')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);
