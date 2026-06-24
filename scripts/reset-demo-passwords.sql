-- 将所有演示用户（除 superadmin）密码统一为 123456
-- 算法：SHA-256 + salt=moli + 15 轮（与 SHA256Util / Shiro 一致）
-- 用法：mysql --default-character-set=utf8mb4 -u root -p moli < scripts/reset-demo-passwords.sql

UPDATE sys_user
SET password = 'a7917efb0e543c470f9a78a12e73f7d7802e589f4133cc83e29b83d54efef169',
    salt     = 'moli'
WHERE is_delete = 0
  AND user_name <> 'superadmin';

-- 校验：应与 zhangsan 完全一致
-- SELECT user_name, password, salt, status FROM sys_user WHERE user_name IN ('zhangsan','lisi','wangwu');
