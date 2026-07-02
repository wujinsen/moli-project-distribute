-- =============================================================
-- 删除 kb_category.default_type（体裁仅由 wiki frontmatter type: 维护）
-- 在 10_kb_category_dir_slug.sql 之后执行；可重复执行。
-- 导入：mysql --default-character-set=utf8mb4 moli < 本文件
-- =============================================================

SET NAMES utf8mb4;

SET @db = DATABASE();
SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'kb_category' AND COLUMN_NAME = 'default_type');
SET @sql = IF(@col > 0,
  'ALTER TABLE `kb_category` DROP COLUMN `default_type`',
  'SELECT ''default_type already dropped'' AS _skip');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SHOW COLUMNS FROM `kb_category` LIKE 'default_type';
