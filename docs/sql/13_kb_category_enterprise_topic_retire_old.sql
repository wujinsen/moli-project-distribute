-- =============================================================
-- enterprise-kb：Sync 之后物理删除旧分类 concepts / articles / interview
-- 在 13_kb_category_enterprise_topic.sql §1 + sync_to_db 之后执行。
-- 空间：enterprise-kb ONLY  space_id = 900000000000000001
--
-- 注意：Sync 后文档 category_id 应已指向新主题分类；下面 UPDATE 仅兜底。
-- =============================================================

SET NAMES utf8mb4;

UPDATE `kb_document` d
INNER JOIN `kb_category` c ON d.`category_id` = c.`id`
SET d.`category_id` = NULL, d.`update_time` = NOW()
WHERE c.`space_id` = 900000000000000001
  AND c.`dir_slug` IN ('concepts', 'articles', 'interview');

DELETE FROM `kb_category`
WHERE `space_id` = 900000000000000001
  AND `dir_slug` IN ('concepts', 'articles', 'interview');

SELECT `id`, `category_name`, `dir_slug`, `sort`
FROM `kb_category`
WHERE `space_id` = 900000000000000001
ORDER BY `sort`, `id`;
