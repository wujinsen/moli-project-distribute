# SQL 迁移 · 模板参考

## 新表（operation N:N 范例）

```sql
CREATE TABLE IF NOT EXISTS `operation_project_component` (
  `id` bigint NOT NULL COMMENT '主键',
  `project_id` bigint NOT NULL COMMENT '项目 ID',
  `component_id` bigint NOT NULL COMMENT '组件 ID',
  `remark` varchar(256) NULL DEFAULT NULL COMMENT '依赖说明',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_operation_project_component`(`project_id`, `component_id`),
  INDEX `idx_opc_component`(`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目→组件依赖（手工维护）';
```

## moli.sql 表块格式

```sql
-- ----------------------------
-- Table structure for operation_xxx
-- ----------------------------
DROP TABLE IF EXISTS `operation_xxx`;
CREATE TABLE `operation_xxx`  (
  ...
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '...' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_xxx
-- ----------------------------
INSERT INTO `operation_xxx` VALUES (...);
```

## 菜单（parent_id=400 运营）

```sql
INSERT INTO `sys_menu` VALUES (407, 1, NOW(), 1, NOW(), '拓扑图', 'Topology', 'トポロジ図',
  400, 'topology', 'operation/topology/index', 'OperationTopology', 'C', 'operation:server:list', 1, 'git-branch', 7);
```

moli.sql 内 `sys_role_menu`（role 1 + 演示角色）：

```sql
INSERT INTO `sys_role_menu` VALUES (910400407, 1, 407);
INSERT INTO `sys_role_menu` VALUES (910720407, 720354230530998272, 407);
```

增量脚本可额外：

```sql
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES (910400407, 2, 407)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), menu_id = VALUES(menu_id);
```

## 编号约定

- 用户中心运维：`17_`～`29_operation_*.sql`（见 `sql-migration-order.md`）
- 知识库：`03_`、`08_`～`14_kb_*.sql`
- 下一个运维脚本：取目录内最大 `NN` + 1
