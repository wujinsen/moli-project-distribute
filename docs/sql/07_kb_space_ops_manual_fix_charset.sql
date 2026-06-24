-- Fix kb_space moli-ops-manual garbled text (PowerShell pipe without utf8mb4)
-- Usage: mysql --default-character-set=utf8mb4 -u root -p moli < docs/sql/07_kb_space_ops_manual_fix_charset.sql

SET NAMES utf8mb4;

UPDATE `kb_space`
SET
  `space_name` = '茉莉系统操作手册',
  `description` = '茉莉微服务全家桶：本地启动、数据库、登录鉴权、权限、联调、部署与故障排查（与 enterprise-kb 技术文库分离）',
  `update_time` = NOW()
WHERE `space_code` = 'moli-ops-manual'
  AND `is_delete` = 0;

SELECT id, space_code, space_name, description FROM kb_space WHERE space_code = 'moli-ops-manual';
