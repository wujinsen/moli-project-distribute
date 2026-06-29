-- =============================================================
-- 茉莉系统操作手册 · 独立知识空间
-- 在 docs/sql/03_knowledge_schema.sql 之后执行；可重复执行。
-- wiki 源：moli-knowledge/kb/wiki-ops/
-- 同步：python kb/tools/sync_to_db.py --wiki-dir wiki-ops --space moli-ops-manual
-- 导入务必带字符集：mysql --default-character-set=utf8mb4 moli < 本文件
-- =============================================================

SET NAMES utf8mb4;
INSERT INTO `kb_space`
  (`id`, `create_id`, `create_time`, `update_id`, `update_time`,
   `space_code`, `space_name`, `description`, `icon`,
   `visibility`, `owner_id`, `status`, `sort`, `is_delete`)
VALUES
  (900000000000000003, 1, NOW(), 1, NOW(),
   'moli-ops-manual', '茉莉系统操作手册',
   '茉莉微服务全家桶：本地启动、数据库、登录鉴权、权限、联调、部署与故障排查（与 enterprise-kb 技术文库分离）',
   'guide', 1, 1, 1, 3, 0)
ON DUPLICATE KEY UPDATE
  `space_name` = VALUES(`space_name`),
  `description` = VALUES(`description`),
  `visibility` = VALUES(`visibility`),
  `icon` = VALUES(`icon`),
  `sort` = VALUES(`sort`),
  `update_time` = NOW();

-- 分类=目录（单一真相源）：绑定 kb/wiki-ops/ 子目录，default_type 用于移动时对齐 frontmatter type
INSERT INTO `kb_category`
  (`id`,`create_id`,`create_time`,`update_id`,`update_time`,`space_id`,`parent_id`,`category_name`,`icon`,`dir_slug`,`default_type`,`sort`,`is_delete`)
VALUES
  (900000000000000131, 1, NOW(), 1, NOW(), 900000000000000003, 0, '操作指导', NULL, 'guides',   'guide',   1, 0),
  (900000000000000132, 1, NOW(), 1, NOW(), 900000000000000003, 0, '微服务',   NULL, 'services', 'service', 2, 0),
  (900000000000000133, 1, NOW(), 1, NOW(), 900000000000000003, 0, '概念',     NULL, 'concepts', 'concept', 3, 0)
ON DUPLICATE KEY UPDATE
  `category_name` = VALUES(`category_name`),
  `default_type`  = VALUES(`default_type`),
  `sort`          = VALUES(`sort`),
  `is_delete`     = 0;

-- 演示成员：admin 可管理；普通演示用户只读
INSERT INTO `kb_space_member`
  (`id`, `create_id`, `create_time`, `update_id`, `update_time`,
   `space_id`, `member_type`, `member_id`, `role`, `is_delete`)
VALUES
  (900000000000000501, 1, NOW(), 1, NOW(),
   900000000000000003, 0, 719712653013942272, 'admin', 0),
  (900000000000000502, 1, NOW(), 1, NOW(),
   900000000000000003, 0, 720351341083361280, 'viewer', 0)
ON DUPLICATE KEY UPDATE
  `role` = VALUES(`role`),
  `update_time` = NOW(),
  `is_delete` = 0;
