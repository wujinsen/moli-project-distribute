-- Fix kb_space moli-ops-manual garbled text (PowerShell pipe without utf8mb4)
-- Usage: mysql --default-character-set=utf8mb4 -u root -p moli < docs/sql/07_kb_space_ops_manual_fix_charset.sql

SET NAMES utf8mb4;

UPDATE `kb_space`
SET
  `space_name` = '茉莉系统手册',
  `description` = 'moli-project-distribute 用户指导手册：产品、技术、测试、运维与操作指南（wiki 源 kb/wiki-moli/）',
  `update_time` = NOW()
WHERE `space_code` = 'moli-ops-manual'
  AND `is_delete` = 0;

SELECT id, space_code, space_name, description FROM kb_space WHERE space_code = 'moli-ops-manual';
