-- =============================================================
-- 运维管理 · AIOps 故障诊断菜单与权限
-- 挂载：运营管理（parent_id=400）
-- 执行后相关用户重新登录
-- =============================================================
SET NAMES utf8mb4;

INSERT INTO `sys_menu`
  (`id`, `create_id`, `create_time`, `update_id`, `update_time`, `menu_name`, `menu_name_en`, `menu_name_ja`,
   `parent_id`, `path`, `component`, `route_name`, `menu_type`, `perms`, `status`, `icon`, `order_num`)
VALUES
  (408, 1, NOW(), 1, NOW(), '故障诊断', 'Fault Diagnosis', '障害診断',
   400, 'aiops', 'operation/aiops/index', 'OperationAiopsDiagnosis', 'C', 'operation:aiops:list', 1, 'stethoscope', 8),
  (409, 1, NOW(), 1, NOW(), '诊断历史', 'Diagnosis History', '診断履歴',
   400, 'aiops-runs', 'operation/aiops-runs/index', 'OperationAiopsRuns', 'C', 'operation:aiops:list', 1, 'history', 9)
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

INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('operation:aiops:list', 'operation', 'aiopsList', '查看诊断历史', 408, 1, 1),
('operation:aiops:diagnose', 'operation', 'aiopsDiagnose', '发起故障诊断', 408, 2, 1),
('operation:aiops:approve', 'operation', 'aiopsApprove', '审批处置预案', 408, 3, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(910400408, 2, 408),
(910400409, 2, 409)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), menu_id = VALUES(menu_id);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(1, 'operation:aiops:list'),
(1, 'operation:aiops:diagnose'),
(1, 'operation:aiops:approve'),
(2, 'operation:aiops:list'),
(2, 'operation:aiops:diagnose'),
(2, 'operation:aiops:approve')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);
