-- =============================================================
-- enterprise-kb 分类精简：仅保留与 wiki/ 一级目录一致的 3 类
-- concepts · articles · interview（guides/services/outputs 软删）
-- 在 10_kb_category_dir_slug.sql 之后执行；可重复执行。
-- =============================================================

SET NAMES utf8mb4;

UPDATE `kb_category` SET `is_delete` = 1, `update_time` = NOW()
WHERE `space_id` = 900000000000000001
  AND `dir_slug` IN ('guides', 'services', 'outputs')
  AND `is_delete` = 0;

-- 确保 3 类处于启用状态
UPDATE `kb_category` SET `is_delete` = 0, `update_time` = NOW()
WHERE `space_id` = 900000000000000001
  AND `dir_slug` IN ('concepts', 'articles', 'interview');
