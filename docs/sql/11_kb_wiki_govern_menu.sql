-- =============================================================
-- 企业知识库 · Wiki 治理工作台（T16）菜单 + 角色绑定
-- 运行顺序：在 04_knowledge_menu.sql、08_kb_ingest_workbench.sql 之后执行
-- 前端 meiling-ui：component = knowledge/wiki-govern/index → KnowledgeWikiGovernView.vue
-- =============================================================

-- C 菜单（ID 910；907/908 为 F 按钮，906=Ingest，909=空间管理）
INSERT INTO `sys_menu` VALUES
(910, 1, NOW(), 1, NOW(), 'Wiki 治理', 'Wiki Governance', 'Wiki ガバナンス', 900,
 'wiki-govern', 'knowledge/wiki-govern/index', 'KnowledgeWikiGovern', 'C', 'kb:wiki:govern:list', 1, 'tool', 5)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), menu_name_en = VALUES(menu_name_en), menu_name_ja = VALUES(menu_name_ja),
  parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
  menu_type = VALUES(menu_type), perms = VALUES(perms), status = VALUES(status), icon = VALUES(icon),
  order_num = VALUES(order_num), update_time = NOW();

-- 角色授权：系统管理员(2)、研发(3) — 与 Ingest/体检扫描同级
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(910900910, 2, 910),
(910903910, 3, 910)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), menu_id = VALUES(menu_id);
