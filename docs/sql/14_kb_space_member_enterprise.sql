-- =============================================================
-- enterprise-kb 空间成员种子（补全可读/可写）
--
-- 背景：enterprise-kb visibility=公开(2) 已允许任意登录用户「读」；
--       本脚本为 superadmin / 演示账号显式授予 editor，避免仅 viewer 时文档管理只读。
-- 空间：enterprise-kb ONLY  space_id = 900000000000000001
-- =============================================================

SET NAMES utf8mb4;

INSERT INTO `kb_space_member`
  (`id`, `create_id`, `create_time`, `update_id`, `update_time`,
   `space_id`, `member_type`, `member_id`, `role`, `is_delete`)
VALUES
  (900000000000000601, 1, NOW(), 1, NOW(),
   900000000000000001, 0, 1, 'admin', 0),
  (900000000000000602, 1, NOW(), 1, NOW(),
   900000000000000001, 0, 2, 'admin', 0),
  (900000000000000603, 1, NOW(), 1, NOW(),
   900000000000000001, 0, 719712653013942272, 'editor', 0)
ON DUPLICATE KEY UPDATE
  `role` = VALUES(`role`),
  `update_time` = NOW(),
  `is_delete` = 0;

SELECT `space_id`, `member_id`, `role`, `is_delete`
FROM `kb_space_member`
WHERE `space_id` = 900000000000000001 AND `is_delete` = 0
ORDER BY `member_id`;
