-- =============================================================
-- 企业知识库 · 左侧菜单（sys_menu + sys_role_menu）
-- 运行顺序：在 scripts/moli.sql 与 03_knowledge_schema.sql 之后执行
-- 前端 meiling-ui 通过 GET /UserCenter/menu/getRouters 拉取，component 对齐 viewRegistry.ts
-- =============================================================

-- -------------------------------------------------------------
-- 1. 菜单（ID 段 900~908 预留给知识库）
-- 字段顺序：id, create_id, create_time, update_id, update_time,
--   menu_name, menu_name_en, menu_name_ja, parent_id, path, component, route_name,
--   menu_type, perms, status, icon, order_num
-- -------------------------------------------------------------

-- 一级目录（M：component 为空时后端也会默认 Layout，此处显式写入与菜单管理一致）
INSERT INTO `sys_menu` VALUES
(900, 1, NOW(), 1, NOW(), '企业知识库', 'Enterprise Knowledge Base', '企業ナレッジ', 0,
 'knowledge', 'Layout', 'Knowledge', 'M', NULL, 1, 'knowledge', 8)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), menu_name_en = VALUES(menu_name_en), menu_name_ja = VALUES(menu_name_ja),
  parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
  menu_type = VALUES(menu_type), perms = VALUES(perms), status = VALUES(status), icon = VALUES(icon),
  order_num = VALUES(order_num), update_time = NOW();

-- 子页面（C=菜单，出现在左侧树）
INSERT INTO `sys_menu` VALUES
(901, 1, NOW(), 1, NOW(), '文档浏览', 'Browse', 'ドキュメント閲覧', 900,
 'browse', 'knowledge/browse/index', 'KnowledgeBrowse', 'C', 'kb:browse:list', 1, 'documentation', 1),
(902, 1, NOW(), 1, NOW(), '智能问答', 'Ask', '質問', 900,
 'ask', 'knowledge/ask/index', 'KnowledgeAsk', 'C', 'kb:ask:list', 1, 'query', 2),
(903, 1, NOW(), 1, NOW(), '关系图谱', 'Graph', 'グラフ', 900,
 'graph', 'knowledge/graph/index', 'KnowledgeGraph', 'C', 'kb:graph:list', 1, 'graph', 3),
(904, 1, NOW(), 1, NOW(), '健康体检', 'Health Check', 'ヘルスチェック', 900,
 'lint', 'knowledge/lint/index', 'KnowledgeLint', 'C', 'kb:lint:list', 1, 'health', 4)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), menu_name_en = VALUES(menu_name_en), menu_name_ja = VALUES(menu_name_ja),
  parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
  menu_type = VALUES(menu_type), perms = VALUES(perms), status = VALUES(status), icon = VALUES(icon),
  order_num = VALUES(order_num), update_time = NOW();

-- 按钮（F=权限，不出现在侧栏，供 Shiro / 前端 v-hasPermi）
INSERT INTO `sys_menu` VALUES
(905, 1, NOW(), 1, NOW(), '触发同步', 'Sync Trigger', '同期', 900,
 '', NULL, NULL, 'F', 'kb:sync:trigger', 1, NULL, 1),
(906, 1, NOW(), 1, NOW(), '知识库管理员', 'KB Admin', 'KB管理', 900,
 '', NULL, NULL, 'F', 'kb:admin', 1, NULL, 2),
(907, 1, NOW(), 1, NOW(), '体检扫描', 'Lint Scan', 'スキャン', 904,
 '', NULL, NULL, 'F', 'kb:lint:scan', 1, NULL, 1),
(908, 1, NOW(), 1, NOW(), '空间成员管理', 'Space Members', 'メンバー', 900,
 '', NULL, NULL, 'F', 'kb:space:admin', 1, NULL, 3)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), perms = VALUES(perms), status = VALUES(status), update_time = NOW();

-- -------------------------------------------------------------
-- 2. 角色授权（按需调整；admin 超管走 getMenuTreeAll 无需绑定）
-- role 2=系统管理员 3=研发 4=产品 6=数据分析 7=普通员工
-- -------------------------------------------------------------

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
-- 系统管理员：全部页面 + 管理按钮
(910900002, 2, 900), (910900902, 2, 901), (910900903, 2, 902), (910900904, 2, 903), (910900905, 2, 904),
(910900906, 2, 905), (910900907, 2, 906), (910900908, 2, 907), (910900909, 2, 908),
-- 研发工程师
(910903900, 3, 900), (910903901, 3, 901), (910903902, 3, 902), (910903903, 3, 903), (910903904, 3, 904),
(910903907, 3, 907),
-- 产品经理
(910904900, 4, 900), (910904901, 4, 901), (910904902, 4, 902), (910904903, 4, 903), (910904904, 4, 904),
-- 数据分析师
(910906900, 6, 900), (910906901, 6, 901), (910906902, 6, 902), (910906903, 6, 903), (910906904, 6, 904),
-- 普通员工：浏览 + 问答
(910907900, 7, 900), (910907901, 7, 901), (910907902, 7, 902)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), menu_id = VALUES(menu_id);
