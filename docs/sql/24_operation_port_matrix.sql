-- =============================================================
-- 运维管理 · 端口矩阵可配置化（SVR-21）
-- 运行顺序：在 23_operation_schema_hardening.sql 之后执行
-- 设计：docs/design/operation-port-matrix-config.md
-- API：docs/api/operation-port-matrix-api.md
-- 说明：种子数据 = 原 OperationPortMatrix.java 硬编码；执行后相关用户重新登录
-- =============================================================
SET NAMES utf8mb4;

-- ---------- 主表 ----------
CREATE TABLE IF NOT EXISTS `operation_port_matrix` (
  `id` bigint NOT NULL COMMENT '主键',
  `matrix_key` varchar(64) NOT NULL COMMENT '矩阵主键，如 user-center',
  `display_name` varchar(128) DEFAULT NULL COMMENT '展示名',
  `expected_port` varchar(16) NOT NULL COMMENT '期望端口',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `source` varchar(256) DEFAULT 'ops-console' COMMENT '来源说明',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_matrix_key` (`matrix_key`),
  KEY `idx_port_matrix_enabled` (`enabled`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维端口矩阵';

-- ---------- 别名表 ----------
CREATE TABLE IF NOT EXISTS `operation_port_matrix_alias` (
  `id` bigint NOT NULL COMMENT '主键',
  `matrix_id` bigint NOT NULL COMMENT 'operation_port_matrix.id',
  `alias` varchar(128) NOT NULL COMMENT '别名，全局唯一',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alias` (`alias`),
  KEY `idx_matrix_alias_matrix_id` (`matrix_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维端口矩阵别名';

-- ---------- 种子：与 OperationPortMatrix.java / production-checklist §2 对齐 ----------
-- 主键 ID 段 501–508，别名 601–624（演示环境固定 ID，与 operation 其它种子风格一致）

INSERT INTO `operation_port_matrix`
  (`id`, `matrix_key`, `display_name`, `expected_port`, `sort_order`, `enabled`, `source`, `remark`, `create_id`, `create_time`, `update_id`, `update_time`)
VALUES
  (501, 'gateway',      '网关',       '21000', 10, 1, 'migration:java-default', NULL, 1, NOW(), 1, NOW()),
  (502, 'user-center',  '用户中心',   '8888',  20, 1, 'migration:java-default', NULL, 1, NOW(), 1, NOW()),
  (503, 'order',        '订单服务',   '8087',  30, 1, 'migration:java-default', NULL, 1, NOW(), 1, NOW()),
  (504, 'knowledge',    '知识库',     '8090',  40, 1, 'migration:java-default', NULL, 1, NOW(), 1, NOW()),
  (505, 'ai',           'AI 服务',    '1128',  50, 1, 'migration:java-default', NULL, 1, NOW(), 1, NOW()),
  (506, 'nacos',        'Nacos',      '8848',  60, 1, 'migration:java-default', NULL, 1, NOW(), 1, NOW()),
  (507, 'mysql',        'MySQL',      '3306',  70, 1, 'migration:java-default', NULL, 1, NOW(), 1, NOW()),
  (508, 'redis',        'Redis',      '6379',  80, 1, 'migration:java-default', NULL, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE
  `display_name` = VALUES(`display_name`),
  `expected_port` = VALUES(`expected_port`),
  `sort_order` = VALUES(`sort_order`),
  `enabled` = VALUES(`enabled`),
  `source` = VALUES(`source`),
  `update_id` = VALUES(`update_id`),
  `update_time` = VALUES(`update_time`);

INSERT INTO `operation_port_matrix_alias` (`id`, `matrix_id`, `alias`, `create_time`) VALUES
  (601, 501, 'gateway',       NOW()),
  (602, 501, 'moli-gateway',  NOW()),
  (603, 502, 'user-center',         NOW()),
  (604, 502, 'moli-user-center',    NOW()),
  (605, 502, 'user-center-server',  NOW()),
  (606, 502, 'moli-server',         NOW()),
  (607, 503, 'order',       NOW()),
  (608, 503, 'moli-order',  NOW()),
  (609, 504, 'knowledge',         NOW()),
  (610, 504, 'moli-knowledge',    NOW()),
  (611, 504, 'knowledge-server',  NOW()),
  (612, 505, 'ai',         NOW()),
  (613, 505, 'moli-ai',    NOW()),
  (617, 505, 'ai-server', NOW()),
  (614, 506, 'nacos',    NOW()),
  (615, 507, 'mysql',    NOW()),
  (616, 508, 'redis',    NOW())
ON DUPLICATE KEY UPDATE
  `matrix_id` = VALUES(`matrix_id`);

-- ---------- 菜单 ----------
INSERT INTO `sys_menu`
  (`id`, `create_id`, `create_time`, `update_id`, `update_time`, `menu_name`, `menu_name_en`, `menu_name_ja`,
   `parent_id`, `path`, `component`, `route_name`, `menu_type`, `perms`, `status`, `icon`, `order_num`)
VALUES
  (406, 1, NOW(), 1, NOW(), '端口矩阵', 'Port Matrix', 'ポートマトリクス',
   400, 'port-matrix', 'operation/port-matrix/index', 'OperationPortMatrix', 'C', 'operation:port-matrix:list', 1, 'table', 6)
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `menu_name_en` = VALUES(`menu_name_en`),
  `menu_name_ja` = VALUES(`menu_name_ja`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `route_name` = VALUES(`route_name`),
  `perms` = VALUES(`perms`),
  `status` = VALUES(`status`),
  `icon` = VALUES(`icon`),
  `order_num` = VALUES(`order_num`),
  `update_time` = NOW();

-- ---------- 动作权限 ----------
INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('operation:port-matrix:list',   'operation', 'portMatrixList',   '端口矩阵列表', 406, 1, 1),
('operation:port-matrix:add',    'operation', 'portMatrixAdd',    '新增端口矩阵', 406, 2, 1),
('operation:port-matrix:edit',   'operation', 'portMatrixEdit',   '编辑端口矩阵', 406, 3, 1),
('operation:port-matrix:remove', 'operation', 'portMatrixRemove', '删除端口矩阵', 406, 4, 1)
ON DUPLICATE KEY UPDATE
  `resource` = VALUES(`resource`), `action` = VALUES(`action`), `name` = VALUES(`name`),
  `menu_id` = VALUES(`menu_id`), `order_num` = VALUES(`order_num`), `status` = VALUES(`status`);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(1, 'operation:port-matrix:list'),
(1, 'operation:port-matrix:add'),
(1, 'operation:port-matrix:edit'),
(1, 'operation:port-matrix:remove'),
(2, 'operation:port-matrix:list'),
(2, 'operation:port-matrix:add'),
(2, 'operation:port-matrix:edit'),
(2, 'operation:port-matrix:remove'),
(720354230530998272, 'operation:port-matrix:list'),
(720354230530998272, 'operation:port-matrix:add'),
(720354230530998272, 'operation:port-matrix:edit'),
(720354230530998272, 'operation:port-matrix:remove')
ON DUPLICATE KEY UPDATE `perm_code` = VALUES(`perm_code`);

-- 超级管理员 / 运维角色菜单可见（id 规则同 21_operation_ssh_deploy.sql 菜单 405）
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(910400406, 1, 406),
(910402406, 2, 406),
(910720406, 720354230530998272, 406)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`), `menu_id` = VALUES(`menu_id`);
