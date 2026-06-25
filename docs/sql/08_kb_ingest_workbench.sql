-- =============================================================
-- 企业知识库 · Ingest 工作台（T15）表结构 + 菜单/权限
-- 运行顺序：在 03_knowledge_schema.sql、04_knowledge_menu.sql 之后执行
-- 设计：Ingest工作台产品方案 §4.1；T15a 只用到 kb_ingest_job / kb_ingest_plan，
--       kb_ingest_draft / kb_ingest_commit 待 T15b/c 实现（此处先建表占位）。
-- 通用约定：bigint 雪花主键；create_id/create_time/update_id/update_time 审计字段；
--           is_delete 逻辑删除（0未删/1已删）；utf8mb4。
-- =============================================================

-- -------------------------------------------------------------
-- 1. Ingest 批次任务（一行=一次 raw→多页 wiki 批次的生命周期）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_ingest_job` (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint NOT NULL COMMENT '目标空间ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号（与 log.md 一致）',
  `topic` varchar(255) NOT NULL COMMENT '主题',
  `expect_types` varchar(255) DEFAULT NULL COMMENT '期望产出类型，逗号分隔',
  `raw_paths` text COMMENT '勾选 raw 路径 JSON 数组（相对 rawRoot）',
  `status` varchar(32) NOT NULL DEFAULT 'created' COMMENT 'created/planned/generating/reviewing/committed/cancelled',
  `plan_version` int DEFAULT 0 COMMENT '当前 plan 版本（0=未规划）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `is_delete` int DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_ingest_job_space` (`space_id`, `status`),
  KEY `idx_ingest_job_create` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Ingest 批次任务';

-- -------------------------------------------------------------
-- 2. Ingest 批次 Plan 版本（每次生成/编辑 append 一版）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_ingest_plan` (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '所属批次任务ID',
  `version` int NOT NULL COMMENT '版本号（从1递增）',
  `plan_json` mediumtext COMMENT 'Plan JSON（create/enrich/skip/edges/conflicts）',
  `source` varchar(16) DEFAULT NULL COMMENT 'llm/manual/skeleton',
  `provider` varchar(32) DEFAULT NULL COMMENT 'LLM 提供方',
  `model` varchar(64) DEFAULT NULL COMMENT '模型名',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ingest_plan_job_ver` (`job_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Ingest 批次规划版本';

-- -------------------------------------------------------------
-- 3. （占位，T15b/c）每页草稿 + 批次落盘记录
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_ingest_draft` (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '所属批次任务ID',
  `slug` varchar(255) NOT NULL COMMENT '目标 slug',
  `kb_type` varchar(32) DEFAULT NULL COMMENT '类型 article/guide/...',
  `action` varchar(16) DEFAULT NULL COMMENT 'create/enrich',
  `baseline` mediumtext COMMENT 'enrich 基线（当前 wiki 全文）',
  `draft` mediumtext COMMENT '草稿正文',
  `approval` varchar(16) DEFAULT 'draft' COMMENT 'draft/approved/rejected',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ingest_draft_job_slug` (`job_id`, `slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Ingest 批次每页草稿';

CREATE TABLE IF NOT EXISTS `kb_ingest_commit` (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '所属批次任务ID',
  `files_json` text COMMENT '写入文件列表 JSON',
  `sync_batch_no` varchar(64) DEFAULT NULL COMMENT '关联 Sync 批次号',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '落盘时间',
  PRIMARY KEY (`id`),
  KEY `idx_ingest_commit_job` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Ingest 批次落盘记录';

-- -------------------------------------------------------------
-- 4. 菜单：知识库 → Ingest 工作台（C 页面，ID 906）
-- -------------------------------------------------------------
INSERT INTO `sys_menu` VALUES
(906, 1, NOW(), 1, NOW(), 'Ingest 工作台', 'Ingest Workbench', 'Ingest ワークベンチ', 900,
 'ingest', 'knowledge/ingest/index', 'KnowledgeIngest', 'C', 'kb:ingest:list', 1, 'build', 6)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), menu_name_en = VALUES(menu_name_en), menu_name_ja = VALUES(menu_name_ja),
  parent_id = VALUES(parent_id), path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
  menu_type = VALUES(menu_type), perms = VALUES(perms), status = VALUES(status), icon = VALUES(icon),
  order_num = VALUES(order_num), update_time = NOW();

-- 动作权限（906）：批次操作 + 落盘（T15c 用 commit）
INSERT INTO `sys_action` (`perm_code`, `resource`, `action`, `name`, `menu_id`, `order_num`, `status`) VALUES
('kb:ingest:job',    'kb', 'ingestJob',    'Ingest批次规划',   906, 1, 1),
('kb:ingest:commit', 'kb', 'ingestCommit', 'Ingest落盘提交',   906, 2, 1)
ON DUPLICATE KEY UPDATE
  resource = VALUES(resource), action = VALUES(action), name = VALUES(name),
  menu_id = VALUES(menu_id), order_num = VALUES(order_num), status = VALUES(status);

-- 角色授权：系统管理员(2)、研发(3) 可见页面 + 批次动作
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(910900907, 2, 906),
(910903906, 3, 906)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), menu_id = VALUES(menu_id);

INSERT INTO `sys_role_action` (`role_id`, `perm_code`) VALUES
(2, 'kb:ingest:job'), (2, 'kb:ingest:commit'),
(3, 'kb:ingest:job')
ON DUPLICATE KEY UPDATE perm_code = VALUES(perm_code);
