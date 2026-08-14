-- =============================================================
-- T20 · Ingest 工作台 · Raw 投喂动作权限
-- 运行顺序：在 08_kb_ingest_workbench.sql 之后执行
-- 设计：docs/design/kb-import-entry-design.md §6
-- 前端：docs/api/kb-import-entry-frontend.md
-- Tab3 成品导入复用 kb:wiki:edit（空间 ACL）+ kb:sync:trigger（904 F 菜单，见 04_knowledge_menu.sql）
-- 执行后相关用户重新登录；平台超管 getMenuTreeAll 无需绑定
-- =============================================================
SET NAMES utf8mb4;

-- 动作权限（906 Ingest 工作台）：Raw 浏览器上传 Tab1
INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('kb:ingest:rawUpload', 'kb', 'ingestRawUpload', 'Raw投喂上传', 906, 3, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

-- 角色授权：2=系统管理员 3=研发（与 kb:ingest:job 同级；可按环境追加 editor 角色）
INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(2, 'kb:ingest:rawUpload'),
(3, 'kb:ingest:rawUpload')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);
