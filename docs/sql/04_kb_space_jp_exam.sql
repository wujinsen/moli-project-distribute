-- =============================================================
-- 日本語試験（基本情報 FE / 応用情報 AP）知识空间 — 增量种子
-- 在 docs/sql/03_knowledge_schema.sql 之后执行；可重复执行。
-- 导入：mysql --default-character-set=utf8mb4 moli < 本文件
-- =============================================================

SET NAMES utf8mb4;
-- 私有空间：仅 owner / kb_space_member 可读；与 enterprise-kb 同库隔离（space_id）
INSERT INTO `kb_space`
  (`id`, `create_id`, `create_time`, `update_id`, `update_time`,
   `space_code`, `space_name`, `description`, `icon`,
   `visibility`, `owner_id`, `status`, `sort`, `is_delete`)
VALUES
  (900000000000000002, 1, NOW(), 1, NOW(),
   'jp-fe-ap-exam', '日本語試験（FE/AP）',
   '基本情報技術者・応用情報技術者备考知识（raw/school/ap、raw/school/fe），私有空间',
   'globe', 0, 1, 1, 2, 0)
ON DUPLICATE KEY UPDATE
  `space_name` = VALUES(`space_name`),
  `description` = VALUES(`description`),
  `visibility` = VALUES(`visibility`),
  `owner_id` = VALUES(`owner_id`),
  `update_time` = NOW();

-- 分类=目录（单一真相源）：绑定 kb/wiki-jp-exam/ 子目录
INSERT INTO `kb_category`
  (`id`,`create_id`,`create_time`,`update_id`,`update_time`,`space_id`,`parent_id`,`category_name`,`icon`,`dir_slug`,`sort`,`is_delete`)
VALUES
  (900000000000000121, 1, NOW(), 1, NOW(), 900000000000000002, 0, '操作指导', NULL, 'guides',    1, 0),
  (900000000000000123, 1, NOW(), 1, NOW(), 900000000000000002, 0, '技术文章', NULL, 'articles',  2, 0),
  (900000000000000124, 1, NOW(), 1, NOW(), 900000000000000002, 0, 'FE 科目',  NULL, 'fe',        3, 0),
  (900000000000000125, 1, NOW(), 1, NOW(), 900000000000000002, 0, 'Certify サーティファイ', NULL, 'certify', 5, 0),
  (900000000000000122, 1, NOW(), 1, NOW(), 900000000000000002, 0, '面试题',   NULL, 'interview', 4, 0)
ON DUPLICATE KEY UPDATE
  `category_name` = VALUES(`category_name`),
  `sort`          = VALUES(`sort`),
  `is_delete`     = 0;

-- 示例成员（user-center 演示用户，见 scripts/moli.sql sys_user）
-- 719712653013942272 = admin；720351341083361280 = 普通演示用户
INSERT INTO `kb_space_member`
  (`id`, `create_id`, `create_time`, `update_id`, `update_time`,
   `space_id`, `member_type`, `member_id`, `role`, `is_delete`)
VALUES
  (900000000000000401, 1, NOW(), 1, NOW(),
   900000000000000002, 0, 719712653013942272, 'admin', 0),
  (900000000000000402, 1, NOW(), 1, NOW(),
   900000000000000002, 0, 720351341083361280, 'viewer', 0)
ON DUPLICATE KEY UPDATE
  `role` = VALUES(`role`),
  `update_time` = NOW(),
  `is_delete` = 0;
