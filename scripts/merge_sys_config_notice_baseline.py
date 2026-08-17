#!/usr/bin/env python3
"""Merge docs/sql/38_sys_config.sql + 39_sys_notice.sql into scripts/moli.sql baseline."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MOLI_SQL = ROOT / "scripts" / "moli.sql"


def main() -> None:
    text = MOLI_SQL.read_text(encoding="utf-8")

    if "CREATE TABLE `sys_config`" in text:
        print("sys_config already present in moli.sql — skip merge")
        return

    text = text.replace(
        ") ENGINE = InnoDB AUTO_INCREMENT = 390 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统动作目录（非导航）' ROW_FORMAT = DYNAMIC;",
        ") ENGINE = InnoDB AUTO_INCREMENT = 394 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统动作目录（非导航）' ROW_FORMAT = DYNAMIC;",
        1,
    )

    action_block = (
        "INSERT INTO `sys_action` VALUES (388, 'ai:chat:trace:all', 'ai', 'chatTraceAll', 'ChatBI 跨用户链路', 610, 3, 1);\n\n"
        "-- ----------------------------\n"
        "-- Table structure for sys_dept"
    )
    action_replacement = action_block.replace(
        "-- Table structure for sys_dept",
        """INSERT INTO `sys_action` VALUES (389, 'system:config:edit', 'config', 'edit', '修改参数', 8, 1, 1);
INSERT INTO `sys_action` VALUES (390, 'system:config:remove', 'config', 'remove', '重置参数为默认', 8, 2, 1);
INSERT INTO `sys_action` VALUES (391, 'system:notice:add', 'notice', 'add', '新增公告', 9, 1, 1);
INSERT INTO `sys_action` VALUES (392, 'system:notice:edit', 'notice', 'edit', '修改公告（含发布/撤回）', 9, 2, 1);
INSERT INTO `sys_action` VALUES (393, 'system:notice:remove', 'notice', 'remove', '删除公告', 9, 3, 1);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL COMMENT '主键（应用侧雪花 ID）',
  `config_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '参数键名，必须是 ConfigKey 注册表中已声明的 key',
  `config_value` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '覆盖值，按声明的 valueType 解析；无覆盖时应删除该行而非留空',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统参数运行期覆盖值' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint NOT NULL COMMENT '主键（应用侧雪花 ID）',
  `notice_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` int NOT NULL COMMENT '类型，对应字典 sys_notice_type：1通知 2公告 3维护',
  `notice_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公告正文（Markdown 源文）',
  `status` int NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布 2已撤回',
  `top_flag` int NULL DEFAULT 0 COMMENT '1置顶 0普通',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间，由发布动作写入',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间，NULL=长期有效',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_notice_publish`(`status` ASC, `publish_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------

-- ----------------------------
-- Table structure for sys_notice_read_cursor
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_read_cursor`;
CREATE TABLE `sys_notice_read_cursor`  (
  `user_id` bigint NOT NULL COMMENT '用户 ID（sys_user.id），一用户一行',
  `last_read_time` datetime NOT NULL COMMENT '最后一次读公告列表的时间水位',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户公告已读水位' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice_read_cursor
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dept""",
        1,
    )
    if action_block not in text:
        raise SystemExit("action anchor not found")
    text = text.replace(action_block, action_replacement, 1)

    role_action_block = (
        "INSERT INTO `sys_role_action` VALUES (720354230530998272, 'system:user:resetPwd');\n\n"
        "-- ----------------------------\n"
        "-- Table structure for sys_role_menu"
    )
    role_action_replacement = role_action_block.replace(
        "-- Table structure for sys_role_menu",
        """INSERT INTO `sys_role_action` VALUES (1, 'system:config:edit');
INSERT INTO `sys_role_action` VALUES (1, 'system:config:remove');
INSERT INTO `sys_role_action` VALUES (2, 'system:config:edit');
INSERT INTO `sys_role_action` VALUES (2, 'system:config:remove');
INSERT INTO `sys_role_action` VALUES (720354230530998272, 'system:config:edit');
INSERT INTO `sys_role_action` VALUES (720354230530998272, 'system:config:remove');
INSERT INTO `sys_role_action` VALUES (1, 'system:notice:add');
INSERT INTO `sys_role_action` VALUES (1, 'system:notice:edit');
INSERT INTO `sys_role_action` VALUES (1, 'system:notice:remove');
INSERT INTO `sys_role_action` VALUES (2, 'system:notice:add');
INSERT INTO `sys_role_action` VALUES (2, 'system:notice:edit');
INSERT INTO `sys_role_action` VALUES (2, 'system:notice:remove');
INSERT INTO `sys_role_action` VALUES (720354230530998272, 'system:notice:add');
INSERT INTO `sys_role_action` VALUES (720354230530998272, 'system:notice:edit');
INSERT INTO `sys_role_action` VALUES (720354230530998272, 'system:notice:remove');

-- ----------------------------
-- Table structure for sys_role_menu""",
        1,
    )
    if role_action_block not in text:
        raise SystemExit("role_action anchor not found")
    text = text.replace(role_action_block, role_action_replacement, 1)

    role_menu_anchor = (
        "INSERT INTO `sys_role_menu` VALUES (910400406, 1, 406);\n"
        "INSERT INTO `sys_role_menu` VALUES (910400407, 2, 407);"
    )
    role_menu_replacement = (
        "INSERT INTO `sys_role_menu` VALUES (910400406, 1, 406);\n"
        "INSERT INTO `sys_role_menu` VALUES (9100801, 1, 8);\n"
        "INSERT INTO `sys_role_menu` VALUES (9100802, 2, 8);\n"
        "INSERT INTO `sys_role_menu` VALUES (9100901, 1, 9);\n"
        "INSERT INTO `sys_role_menu` VALUES (9100902, 2, 9);\n"
        "INSERT INTO `sys_role_menu` VALUES (910400407, 2, 407);"
    )
    if role_menu_anchor not in text:
        raise SystemExit("role_menu anchor not found")
    text = text.replace(role_menu_anchor, role_menu_replacement, 1)

    MOLI_SQL.write_text(text, encoding="utf-8")
    print("moli.sql merged OK")


if __name__ == "__main__":
    main()
