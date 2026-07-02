-- =============================================================
-- enterprise-kb 分类迁移（方案 B · 按技术主题分目录）
--
-- 空间：enterprise-kb  ONLY  space_id = 900000000000000001
-- wiki：kb/wiki/  新一级目录 database/cache/java/...
--
-- 执行顺序（重要）：
--   1) 本脚本 §1 — 插入 10 个新分类（体裁走 frontmatter type:）
--   2) wiki 已 git mv 后：python kb/tools/sync_to_db.py --wiki-dir wiki --space enterprise-kb
--   3) 本脚本 §2 或 13_kb_category_enterprise_topic_retire_old.sql — 物理删旧三类
--
-- 可重复执行（ON DUPLICATE KEY UPDATE / 条件 UPDATE）。
-- 勿在 jp-fe-ap-exam / moli-ops-manual 空间执行 §2。
-- =============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------- §1 新分类（主题域）
INSERT INTO `kb_category`
  (`id`,`create_id`,`create_time`,`update_id`,`update_time`,`space_id`,`parent_id`,`category_name`,`icon`,`dir_slug`,`sort`,`is_delete`)
-- ID 141–150：避开 116(outputs)、121–124(jp-fe-ap-exam)
VALUES
  (900000000000000141, 1, NOW(), 1, NOW(), 900000000000000001, 0, '数据库',       NULL, 'database',   1, 0),
  (900000000000000142, 1, NOW(), 1, NOW(), 900000000000000001, 0, '缓存与 Redis', NULL, 'cache',      2, 0),
  (900000000000000143, 1, NOW(), 1, NOW(), 900000000000000001, 0, 'Java 与 JVM',  NULL, 'java',       3, 0),
  (900000000000000144, 1, NOW(), 1, NOW(), 900000000000000001, 0, '微服务与中间件', NULL, 'middleware', 4, 0),
  (900000000000000145, 1, NOW(), 1, NOW(), 900000000000000001, 0, 'Spring 生态',  NULL, 'spring',     5, 0),
  (900000000000000146, 1, NOW(), 1, NOW(), 900000000000000001, 0, '搜索与 ES',    NULL, 'search',     6, 0),
  (900000000000000147, 1, NOW(), 1, NOW(), 900000000000000001, 0, '网络与安全',   NULL, 'security',   7, 0),
  (900000000000000148, 1, NOW(), 1, NOW(), 900000000000000001, 0, '运维与 Linux', NULL, 'ops',        8, 0),
  (900000000000000149, 1, NOW(), 1, NOW(), 900000000000000001, 0, '设计模式',     NULL, 'patterns',   9, 0),
  (900000000000000150, 1, NOW(), 1, NOW(), 900000000000000001, 0, '前端',         NULL, 'frontend',  10, 0)
ON DUPLICATE KEY UPDATE
  `category_name` = VALUES(`category_name`),
  `dir_slug`      = VALUES(`dir_slug`),
  `sort`          = VALUES(`sort`),
  `is_delete`     = 0,
  `update_time`   = NOW();

-- 校验 §1
SELECT `id`, `category_name`, `dir_slug`, `sort`, `is_delete`
FROM `kb_category`
WHERE `space_id` = 900000000000000001
  AND `dir_slug` IN ('database','cache','java','middleware','spring','search','security','ops','patterns','frontend')
ORDER BY `sort`;

-- ------------------------------------------------------------- §2 物理删旧分类（Sync 完成后再执行）
-- 推荐单独跑：docs/sql/13_kb_category_enterprise_topic_retire_old.sql

/*
UPDATE `kb_document` d
INNER JOIN `kb_category` c ON d.`category_id` = c.`id`
SET d.`category_id` = NULL, d.`update_time` = NOW()
WHERE c.`space_id` = 900000000000000001
  AND c.`dir_slug` IN ('concepts', 'articles', 'interview');

DELETE FROM `kb_category`
WHERE `space_id` = 900000000000000001
  AND `dir_slug` IN ('concepts', 'articles', 'interview');
*/