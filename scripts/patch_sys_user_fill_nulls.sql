-- Patch NULL fields in sys_user (demo/dev)
-- Only fills NULL/empty values; does not overwrite existing data
-- Usage: mysql -u root -p moli < scripts/patch_sys_user_fill_nulls.sql

SET NAMES utf8mb4;

START TRANSACTION;

-- 1. Audit fields
UPDATE sys_user
SET create_id   = 1,
    create_time = COALESCE(create_time, update_time, NOW())
WHERE is_delete = 0
  AND (create_id IS NULL OR create_time IS NULL);

UPDATE sys_user
SET update_id   = COALESCE(update_id, create_id, 1),
    update_time = COALESCE(update_time, create_time, NOW())
WHERE is_delete = 0
  AND (update_id IS NULL OR update_time IS NULL);

-- 2. Status (status 1=enabled, 0=disabled; is_job 0=active)
UPDATE sys_user SET status    = 1 WHERE is_delete = 0 AND status    IS NULL;
UPDATE sys_user SET is_job    = 0 WHERE is_delete = 0 AND is_job    IS NULL;
UPDATE sys_user SET error_num = 0 WHERE is_delete = 0 AND error_num IS NULL;
UPDATE sys_user SET salt      = 'moli'  WHERE is_delete = 0 AND (salt IS NULL OR salt = '');
UPDATE sys_user SET language  = 'zh-CN' WHERE is_delete = 0 AND (language IS NULL OR language = '');

-- 3. Display name / work number
UPDATE sys_user
SET nick_name = '超级管理员'
WHERE is_delete = 0 AND user_name = 'superadmin' AND (nick_name IS NULL OR nick_name = '');

UPDATE sys_user
SET nick_name = '系统管理员'
WHERE is_delete = 0 AND user_name = 'admin' AND (nick_name IS NULL OR nick_name = '');

UPDATE sys_user
SET nick_name = user_name
WHERE is_delete = 0
  AND user_name NOT IN ('superadmin', 'admin')
  AND (nick_name IS NULL OR nick_name = '');

UPDATE sys_user
SET work_no = CONCAT('W', id)
WHERE is_delete = 0 AND (work_no IS NULL OR work_no = '');

-- 4. Department (default: R&D dept 100)
UPDATE sys_user
SET dept_id = 100
WHERE is_delete = 0 AND dept_id IS NULL;

-- 5. Contact (unique indexes: only patch NULL)
UPDATE sys_user SET telephone = '13900000001'
WHERE is_delete = 0 AND user_name = 'superadmin' AND (telephone IS NULL OR telephone = '');

UPDATE sys_user SET telephone = '13900000002'
WHERE is_delete = 0 AND user_name = 'admin' AND (telephone IS NULL OR telephone = '');

UPDATE sys_user SET telephone = '13900000099'
WHERE is_delete = 0 AND user_name = 'test' AND (telephone IS NULL OR telephone = '');

UPDATE sys_user
SET telephone = CONCAT('138', RIGHT(LPAD(CAST(id AS CHAR), 15, '0'), 8))
WHERE is_delete = 0
  AND user_name NOT IN ('superadmin', 'admin', 'test')
  AND (telephone IS NULL OR telephone = '');

UPDATE sys_user
SET email = CONCAT(user_name, '@demo.local')
WHERE is_delete = 0 AND (email IS NULL OR email = '');

-- 6. Profile
UPDATE sys_user SET sex = 2 WHERE is_delete = 0 AND sex IS NULL;

UPDATE sys_user
SET address = '上海市浦东新区'
WHERE is_delete = 0 AND (address IS NULL OR address = '');

UPDATE sys_user
SET work_time = COALESCE(DATE(create_time), '2026-01-01 09:00:00')
WHERE is_delete = 0 AND work_time IS NULL;

UPDATE sys_user
SET identity_card = CONCAT(
    '1101011990',
    LPAD(MOD(id, 100), 2, '0'),
    '01',
    LPAD(MOD(id, 10000), 4, '0')
)
WHERE is_delete = 0 AND (identity_card IS NULL OR identity_card = '');

UPDATE sys_user
SET avatar = '/avatar/default.png'
WHERE is_delete = 0 AND (avatar IS NULL OR avatar = '');

COMMIT;

-- Verify: should return 0 rows
SELECT 'still_null' AS check_type, user_name, nick_name, dept_id, telephone, email, status, is_job, salt
FROM sys_user
WHERE is_delete = 0
  AND (
        nick_name IS NULL OR nick_name = ''
     OR dept_id IS NULL
     OR telephone IS NULL OR telephone = ''
     OR email IS NULL OR email = ''
     OR status IS NULL
     OR is_job IS NULL
     OR salt IS NULL OR salt = ''
  );

SELECT id, user_name, nick_name, work_no, dept_id, telephone, email, sex, status, is_job, language, salt
FROM sys_user
WHERE is_delete = 0
ORDER BY id
LIMIT 10;
