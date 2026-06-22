-- =============================================================
-- 日本語試験（基本情報 FE / 応用情報 AP）知识空间 — 增量种子
-- 在 docs/sql/03_knowledge_schema.sql 之后执行；可重复执行。
-- =============================================================

-- 私有空间：仅 owner / kb_space_member 可读；与 enterprise-kb 同库隔离（space_id）
INSERT INTO `kb_space`
  (`id`, `create_id`, `create_time`, `update_id`, `update_time`,
   `space_code`, `space_name`, `description`, `icon`,
   `visibility`, `owner_id`, `status`, `sort`, `is_delete`)
VALUES
  (900000000000000002, 1, NOW(), 1, NOW(),
   'jp-fe-ap-exam', '日本語試験（FE/AP）',
   '基本情報技術者・応用情報技術者备考知识（raw/ap、raw/fe），私有空间',
   'globe', 0, 1, 1, 2, 0)
ON DUPLICATE KEY UPDATE
  `space_name` = VALUES(`space_name`),
  `description` = VALUES(`description`),
  `visibility` = VALUES(`visibility`),
  `owner_id` = VALUES(`owner_id`),
  `update_time` = NOW();

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
