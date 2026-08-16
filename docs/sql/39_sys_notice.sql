-- =============================================================
-- 系统管理 · 通知公告（sys_notice + sys_notice_read_cursor）
-- 运行顺序：在 38_sys_config.sql 之后执行
-- 设计：docs/design/sys-config-notice.md
-- ER：docs/diagrams/moli-sys-config-notice-er.drawio
--
-- 说明：
--   1. notice_type 复用基线已有字典 sys_notice_type（sys_dict_type.id=9，
--      数据 901/902/903 → 通知/公告/维护），不新增枚举表、不硬编码。
--   2. notice_content 存 **Markdown 源文**，与 kb_document.content 形态统一；
--      渲染端控制允许语法，无需维护 HTML 标签白名单。
--   3. status 三态含「已撤回」：发错信息必须能撤下，而物理删除会丢失
--      「对外发布过什么」的痕迹。
--   4. 未读追踪用**水位表** sys_notice_read_cursor（一用户一行），
--      而非「每用户每公告一行」的关联表 —— 规模从 O(用户×公告) 降到 O(用户)。
--      代价：只能表达「X 时刻前都已读」，无法乱序标记单条已读（通知栏场景可接受）。
--
-- 菜单：sys_menu id=9「通知公告」已在基线存在（parent_id=1，perms=system:notice:list），
--       system_id 已由基线 L709 回填为 moli-admin，本脚本不改菜单本身。
-- 执行后相关用户需重新登录（Shiro 授权缓存）。
-- =============================================================
SET NAMES utf8mb4;

-- ---------- 公告主表 ----------
CREATE TABLE IF NOT EXISTS `sys_notice` (
  `id`             bigint       NOT NULL COMMENT '主键（应用侧雪花 ID）',
  `notice_title`   varchar(255) NOT NULL COMMENT '公告标题',
  `notice_type`    int          NOT NULL COMMENT '类型，对应字典 sys_notice_type：1通知 2公告 3维护',
  `notice_content` mediumtext   COMMENT '公告正文（Markdown 源文）',
  `status`         int          NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布 2已撤回',
  `top_flag`       int          DEFAULT 0 COMMENT '1置顶 0普通',
  `publish_time`   datetime     DEFAULT NULL COMMENT '发布时间，由发布动作写入',
  `expire_time`    datetime     DEFAULT NULL COMMENT '过期时间，NULL=长期有效',
  `create_id`      bigint       DEFAULT NULL COMMENT '创建人',
  `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
  `update_id`      bigint       DEFAULT NULL COMMENT '修改人',
  `update_time`    datetime     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_notice_publish` (`status`, `publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告';

-- ---------- 未读水位表 ----------
-- user_id 直接做主键：一个用户恒定一行，行数不随公告数量增长。
-- 未读数 = SELECT count(*) FROM sys_notice
--          WHERE status = 1 AND publish_time > last_read_time
--            AND (expire_time IS NULL OR expire_time > NOW());
-- 走 idx_sys_notice_publish 的索引范围扫描。
CREATE TABLE IF NOT EXISTS `sys_notice_read_cursor` (
  `user_id`        bigint   NOT NULL COMMENT '用户 ID（sys_user.id），一用户一行',
  `last_read_time` datetime NOT NULL COMMENT '最后一次读公告列表的时间水位',
  `update_time`    datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户公告已读水位';

-- ---------- 动作权限 ----------
-- 与 system:post:* / system:dict:* 一致：list 由 sys_menu.perms 承载。
-- publish / revoke 复用 edit 权限，不单独开权限码：能改公告的人就能决定它是否对外，
-- 拆开只会让角色授权页多两个几乎总是与 edit 同时勾选的项。
INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('system:notice:add',    'notice', 'add',    '新增公告', 9, 1, 1),
('system:notice:edit',   'notice', 'edit',   '修改公告（含发布/撤回）', 9, 2, 1),
('system:notice:remove', 'notice', 'remove', '删除公告', 9, 3, 1)
ON DUPLICATE KEY UPDATE
  `resource` = VALUES(`resource`), `action` = VALUES(`action`), `name` = VALUES(`name`),
  `menu_id` = VALUES(`menu_id`), `order_num` = VALUES(`order_num`), `status` = VALUES(`status`);

-- ---------- 角色-动作授权 ----------
INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(1, 'system:notice:add'),
(1, 'system:notice:edit'),
(1, 'system:notice:remove'),
(2, 'system:notice:add'),
(2, 'system:notice:edit'),
(2, 'system:notice:remove'),
(720354230530998272, 'system:notice:add'),
(720354230530998272, 'system:notice:edit'),
(720354230530998272, 'system:notice:remove')
ON DUPLICATE KEY UPDATE `perm_code` = VALUES(`perm_code`);

-- ---------- 角色-菜单授权 ----------
-- 同 38_sys_config.sql：基线中菜单 9 仅 test 角色有授权；
-- sys_role_menu 无 (role_id, menu_id) 唯一约束，故不重复插入 test。
-- id 规则：910 + 菜单号(2位) + 角色号(2位)。
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(9100901, 1, 9),
(9100902, 2, 9)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`), `menu_id` = VALUES(`menu_id`);

-- ---------- 阅读侧说明（无需 SQL）----------
-- GET /notice/feed、/notice/feed/{id}、PUT /notice/feed/read 仅要求登录，
-- 不挂 perms / sys_action：公告的意义是全员可见，用路径前缀把阅读侧与
-- 后台管理侧分开，各自的可见性规则保持单一。
