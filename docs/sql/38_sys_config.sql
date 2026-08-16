-- =============================================================
-- 系统管理 · 参数设置（sys_config）
-- 运行顺序：在 37_kb_research_menu.sql 之后执行
-- 设计：docs/design/sys-config-notice.md
-- ER：docs/diagrams/moli-sys-config-notice-er.drawio
--
-- 说明（务必先读设计 §2，否则容易误用本表）：
--   1. 本表**只存运行期覆盖值**，不存参数定义。参数的名称/类型/默认值/校验规则/分组
--      全部声明在代码 ConfigKey 注册表（moli-user-center-server .../config/param/ConfigKey.java）。
--   2. 因此**故意不建种子行**：任何初始行都意味着「一上线就覆盖了默认值」。
--      表为空 = 全部参数取 yaml 或声明默认值，这是期望的初始状态。
--   3. 删除一行 = 该参数重置为默认值，不是「删除参数」。
--   4. 无 system:config:add 权限：参数不能由 UI 创建，新增参数是代码变更。
--
-- 菜单：sys_menu id=8「参数设置」已在基线存在（parent_id=1，perms=system:config:list），
--       且 system_id 已由基线 L709 回填为 moli-admin，本脚本不改菜单本身。
-- 执行后相关用户需重新登录（Shiro 授权缓存）。
-- =============================================================
SET NAMES utf8mb4;

-- ---------- 参数覆盖值表 ----------
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id`           bigint        NOT NULL COMMENT '主键（应用侧雪花 ID）',
  `config_key`   varchar(128)  NOT NULL COMMENT '参数键名，必须是 ConfigKey 注册表中已声明的 key',
  `config_value` varchar(2048) NOT NULL COMMENT '覆盖值，按声明的 valueType 解析；无覆盖时应删除该行而非留空',
  `create_id`    bigint        DEFAULT NULL COMMENT '创建人',
  `create_time`  datetime      DEFAULT NULL COMMENT '创建时间',
  `update_id`    bigint        DEFAULT NULL COMMENT '修改人',
  `update_time`  datetime      DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数运行期覆盖值';

-- ---------- 动作权限 ----------
-- 与 system:post:* / system:dict:* 一致：list 由 sys_menu.perms 承载，此处只登记写操作。
-- remove 的业务语义是「重置为默认」，name 如实描述，避免运维以为会删掉参数本身。
INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('system:config:edit',   'config', 'edit',   '修改参数',       8, 1, 1),
('system:config:remove', 'config', 'remove', '重置参数为默认', 8, 2, 1)
ON DUPLICATE KEY UPDATE
  `resource` = VALUES(`resource`), `action` = VALUES(`action`), `name` = VALUES(`name`),
  `menu_id` = VALUES(`menu_id`), `order_num` = VALUES(`order_num`), `status` = VALUES(`status`);

-- ---------- 角色-动作授权 ----------
-- 1 超级管理员（虽有 fullPermission 兜底，仍按既有迁移惯例显式授权）
-- 2 系统管理员（参数设置的实际使用者）
-- 720354230530998272 test（基线已授权菜单 8，补齐其动作权限）
INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(1, 'system:config:edit'),
(1, 'system:config:remove'),
(2, 'system:config:edit'),
(2, 'system:config:remove'),
(720354230530998272, 'system:config:edit'),
(720354230530998272, 'system:config:remove')
ON DUPLICATE KEY UPDATE `perm_code` = VALUES(`perm_code`);

-- ---------- 角色-菜单授权（关键：不补则管理员看不到入口）----------
-- 基线中菜单 8 仅 test 角色（722982329659686913）有授权，角色 1 / 2 一行都没有。
-- 注意 sys_role_menu 只有 id 主键、**无 (role_id, menu_id) 唯一约束**，
-- 因此不能重复插入 test 角色，否则会产生同一 (role, menu) 的重复行。
-- id 规则：910 + 菜单号(2位) + 角色号(2位)，与 910400406 一类的语义化 ID 风格一致。
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(9100801, 1, 8),
(9100802, 2, 8)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`), `menu_id` = VALUES(`menu_id`);
