/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : moli

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 10/11/2026 07:49:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_trace
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_trace`;
CREATE TABLE `ai_chat_trace`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对外 traceId',
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话',
  `user_id` bigint NULL DEFAULT NULL COMMENT '提问者',
  `question` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '自然语言',
  `final_sql` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '最终执行 SQL（拒答/异常为空）',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SUCCESS|REJECTED|ERROR',
  `reject_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '§1.3 拒答码',
  `reject_reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '人类可读拒答原因',
  `row_count` int NULL DEFAULT NULL COMMENT '结果行数',
  `latency_ms` bigint NULL DEFAULT NULL COMMENT '耗时毫秒',
  `retry` int NOT NULL DEFAULT 0 COMMENT '自纠错次数',
  `steps_json` json NULL COMMENT 'BiTraceStep 决策链路 JSON',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ai_chat_trace_id`(`trace_id` ASC) USING BTREE,
  INDEX `idx_ai_chat_trace_user`(`user_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_ai_chat_trace_session`(`session_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'ChatBI 问答审计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_agentic_trace
-- ----------------------------
DROP TABLE IF EXISTS `kb_agentic_trace`;
CREATE TABLE `kb_agentic_trace`  (
  `id` bigint NOT NULL COMMENT '主键',
  `qa_log_id` bigint NULL DEFAULT NULL COMMENT '关联 kb_qa_log.id',
  `space_id` bigint NULL DEFAULT NULL COMMENT '空间ID（单空间时）',
  `user_id` bigint NULL DEFAULT NULL COMMENT '提问用户ID',
  `question` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始问题',
  `rewritten` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '改写后主问',
  `sub_questions_json` json NULL COMMENT '拆解子问题 JSON 数组',
  `rounds` int NOT NULL DEFAULT 1 COMMENT '实际编排轮次',
  `steps_json` json NULL COMMENT '每轮 queries/slugs/coverage/unsupported/latencyMs',
  `coverage` decimal(5, 4) NULL DEFAULT NULL COMMENT '末轮自检 coverage',
  `degraded` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否降级',
  `latency_ms` bigint NULL DEFAULT NULL COMMENT '总耗时毫秒',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_agentic_trace_qa`(`qa_log_id` ASC) USING BTREE,
  INDEX `idx_kb_agentic_trace_user`(`user_id` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Agentic RAG 编排 trace' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_attachment
-- ----------------------------
DROP TABLE IF EXISTS `kb_attachment`;
CREATE TABLE `kb_attachment`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `file_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
  `object_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MinIO对象键',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `content_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_attachment_document`(`document_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档附件' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_category
-- ----------------------------
DROP TABLE IF EXISTS `kb_category`;
CREATE TABLE `kb_category`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父分类ID',
  `category_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `dir_slug` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '绑定的 wiki 子目录名（分类=目录）',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_category_dir`(`space_id` ASC, `dir_slug` ASC) USING BTREE,
  INDEX `idx_kb_category_space`(`space_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `kb_chat_message`;
CREATE TABLE `kb_chat_message`  (
  `id` bigint NOT NULL COMMENT '主键',
  `session_id` bigint NOT NULL COMMENT '逻辑 FK → kb_chat_session.id',
  `role` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'user / assistant',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'user=提问原文；assistant=answer',
  `qa_log_id` bigint NULL DEFAULT NULL COMMENT 'assistant 行可链 kb_qa_log.id',
  `token_est` int NULL DEFAULT NULL COMMENT 'P0 粗估 len/4',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_chat_message_session`(`session_id` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库多轮对话消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_chat_session
-- ----------------------------
DROP TABLE IF EXISTS `kb_chat_session`;
CREATE TABLE `kb_chat_session`  (
  `id` bigint NOT NULL COMMENT '主键（对外 chatSessionId）',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `user_id` bigint NOT NULL COMMENT '会话归属用户（Shiro）',
  `space_id` bigint NULL DEFAULT NULL COMMENT '会话默认空间；消息级仍可覆盖',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '首问截断；P0 不走 LLM 短标题',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '滚摘要；P0 不写',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1进行中 0已关闭',
  `last_message_at` datetime NULL DEFAULT NULL COMMENT '列表排序',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_chat_session_user_time`(`user_id` ASC, `last_message_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库多轮对话会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_comment
-- ----------------------------
DROP TABLE IF EXISTS `kb_comment`;
CREATE TABLE `kb_comment`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父评论ID',
  `content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_comment_document`(`document_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档评论' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_document
-- ----------------------------
DROP TABLE IF EXISTS `kb_document`;
CREATE TABLE `kb_document`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `slug` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '空间内唯一标识/URL',
  `source` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'manual' COMMENT '来源 kb/manual',
  `source_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'kb/ 原始 markdown 路径',
  `content_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '正文+frontmatter 的 SHA-256',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `summary` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摘要',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '正文',
  `doc_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'markdown' COMMENT '内容格式 markdown/rich',
  `kb_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '知识类型 guide/service/concept/article/interview/output',
  `domain` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '领域 FE/AP/DB...',
  `status` int NULL DEFAULT 0 COMMENT '0草稿 1已发布 2已归档',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `version_no` int NULL DEFAULT 1 COMMENT '版本号',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_document_slug`(`space_id` ASC, `slug` ASC) USING BTREE,
  INDEX `idx_kb_document_space`(`space_id` ASC) USING BTREE,
  INDEX `idx_kb_document_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_kb_document_status`(`status` ASC) USING BTREE,
  INDEX `idx_kb_document_type`(`kb_type` ASC) USING BTREE,
  FULLTEXT INDEX `ftx_kb_document`(`title`, `summary`, `content`) WITH PARSER `ngram`
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识文档' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_document_chunk
-- ----------------------------
DROP TABLE IF EXISTS `kb_document_chunk`;
CREATE TABLE `kb_document_chunk`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `document_id` bigint NOT NULL COMMENT 'kb_document.id',
  `space_id` bigint NOT NULL COMMENT '冗余 ACL/filter',
  `slug` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '冗余引用',
  `kb_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '冗余体裁',
  `category_id` bigint NULL DEFAULT NULL COMMENT '冗余分类',
  `status` tinyint NULL DEFAULT 1 COMMENT '与文档一致 0草稿1发布2归档',
  `chunk_index` int NOT NULL DEFAULT 0 COMMENT '页内顺序 0-based',
  `heading` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节标题',
  `heading_level` tinyint NULL DEFAULT 0 COMMENT '0页首 2=## 3=###',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '切段正文（含标题行）',
  `char_count` int NULL DEFAULT 0,
  `content_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SHA-256',
  `is_delete` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_chunk_document`(`document_id` ASC, `chunk_index` ASC) USING BTREE,
  INDEX `idx_kb_chunk_space`(`space_id` ASC) USING BTREE,
  INDEX `idx_kb_chunk_slug`(`space_id` ASC, `slug`(191) ASC) USING BTREE,
  FULLTEXT INDEX `ftx_kb_document_chunk`(`heading`, `content`) WITH PARSER `ngram`
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库文档切段（ask 召回）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_document_tag
-- ----------------------------
DROP TABLE IF EXISTS `kb_document_tag`;
CREATE TABLE `kb_document_tag`  (
  `id` bigint NOT NULL COMMENT '主键',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_document_tag`(`document_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_kb_document_tag_tag`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档标签关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_document_version
-- ----------------------------
DROP TABLE IF EXISTS `kb_document_version`;
CREATE TABLE `kb_document_version`  (
  `id` bigint NOT NULL COMMENT '主键',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '正文',
  `content_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '该版本内容 SHA-256',
  `change_log` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更说明',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_doc_version`(`document_id` ASC, `version_no` ASC) USING BTREE,
  INDEX `idx_kb_document_version_doc`(`document_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档版本' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_eval_run
-- ----------------------------
DROP TABLE IF EXISTS `kb_eval_run`;
CREATE TABLE `kb_eval_run`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `run_at` datetime NOT NULL COMMENT '报告 time',
  `strategy` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ngram/hybrid/hybrid-rerank',
  `use_llm` tinyint(1) NOT NULL DEFAULT 0 COMMENT '检索式0/生成式1',
  `golden_total` int NOT NULL DEFAULT 0 COMMENT '报告 total',
  `answerable_total` int NOT NULL DEFAULT 0 COMMENT '可答题数',
  `negative_total` int NOT NULL DEFAULT 0 COMMENT 'negative 题数',
  `errors` int NOT NULL DEFAULT 0 COMMENT 'HTTP/请求错误数',
  `hit1` decimal(5, 4) NULL DEFAULT NULL COMMENT 'hit@1',
  `hit3` decimal(5, 4) NULL DEFAULT NULL COMMENT 'hit@3',
  `hit5` decimal(5, 4) NULL DEFAULT NULL COMMENT 'hit@5',
  `hit8` decimal(5, 4) NULL DEFAULT NULL COMMENT 'hit@8',
  `mrr` decimal(5, 4) NULL DEFAULT NULL COMMENT 'MRR',
  `coverage` decimal(5, 4) NULL DEFAULT NULL COMMENT 'coverage',
  `refusal_accuracy` decimal(5, 4) NULL DEFAULT NULL COMMENT '拒答准确率',
  `p95_ms` int NULL DEFAULT NULL COMMENT 'P95 延迟毫秒',
  `by_difficulty_json` json NULL COMMENT 'by_difficulty 原样',
  `report_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'kb/eval/reports 相对路径',
  `git_sha` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联 git 提交',
  `gate_pass` tinyint(1) NULL DEFAULT NULL COMMENT '§1.2 门禁是否通过',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '落库时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_eval_run_at`(`run_at` ASC) USING BTREE,
  INDEX `idx_kb_eval_strategy_run`(`strategy` ASC, `run_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库评测回归记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_favorite
-- ----------------------------
DROP TABLE IF EXISTS `kb_favorite`;
CREATE TABLE `kb_favorite`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_favorite`(`user_id` ASC, `document_id` ASC) USING BTREE,
  INDEX `idx_kb_favorite_document`(`document_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档收藏' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_ingest_commit
-- ----------------------------
DROP TABLE IF EXISTS `kb_ingest_commit`;
CREATE TABLE `kb_ingest_commit`  (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '所属批次任务ID',
  `files_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '写入文件列表 JSON',
  `sync_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联 Sync 批次号',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '落盘时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ingest_commit_job`(`job_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Ingest 批次落盘记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_ingest_draft
-- ----------------------------
DROP TABLE IF EXISTS `kb_ingest_draft`;
CREATE TABLE `kb_ingest_draft`  (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '所属批次任务ID',
  `slug` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标 slug',
  `kb_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型 article/guide/...',
  `action` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'create/enrich',
  `baseline` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'enrich 基线（当前 wiki 全文）',
  `patch` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'enrich 追加段落（EnrichWriter patch）',
  `draft` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '草稿正文',
  `approval` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'draft' COMMENT 'draft/approved/rejected',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ingest_draft_job_slug`(`job_id` ASC, `slug` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Ingest 批次每页草稿' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_ingest_job
-- ----------------------------
DROP TABLE IF EXISTS `kb_ingest_job`;
CREATE TABLE `kb_ingest_job`  (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint NOT NULL COMMENT '目标空间ID',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号（与 log.md 一致）',
  `topic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主题',
  `expect_types` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '期望产出类型，逗号分隔',
  `raw_paths` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '勾选 raw 路径 JSON 数组（相对 rawRoot）',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'created' COMMENT 'created/planned/generating/reviewing/committed/cancelled',
  `plan_version` int NULL DEFAULT 0 COMMENT '当前 plan 版本（0=未规划）',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ingest_job_space`(`space_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_ingest_job_create`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Ingest 批次任务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_ingest_plan
-- ----------------------------
DROP TABLE IF EXISTS `kb_ingest_plan`;
CREATE TABLE `kb_ingest_plan`  (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '所属批次任务ID',
  `version` int NOT NULL COMMENT '版本号（从1递增）',
  `plan_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Plan JSON（create/enrich/skip/edges/conflicts）',
  `source` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'llm/manual/skeleton',
  `provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'LLM 提供方',
  `model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ingest_plan_job_ver`(`job_id` ASC, `version` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Ingest 批次规划版本' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_ingest_template
-- ----------------------------
DROP TABLE IF EXISTS `kb_ingest_template`;
CREATE TABLE `kb_ingest_template`  (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint NOT NULL COMMENT '目标空间ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `topic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '默认主题',
  `expect_types` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '期望产出类型，逗号分隔',
  `raw_paths` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'raw 路径 JSON 数组',
  `plan_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '可选 Plan JSON 快照',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ingest_tpl_space`(`space_id` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Ingest 批次模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_lint_issue
-- ----------------------------
DROP TABLE IF EXISTS `kb_lint_issue`;
CREATE TABLE `kb_lint_issue`  (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `document_id` bigint NULL DEFAULT NULL COMMENT '相关文档ID',
  `issue_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题类型',
  `detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '问题详情',
  `status` tinyint NULL DEFAULT 0 COMMENT '0待处理 1已忽略 2已修复',
  `assignee_id` bigint NULL DEFAULT NULL COMMENT '处理人用户ID',
  `priority` tinyint NULL DEFAULT 0 COMMENT '0普通 1高 2紧急',
  `scan_time` datetime NULL DEFAULT NULL COMMENT '扫描时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_lint_space`(`space_id` ASC) USING BTREE,
  INDEX `idx_kb_lint_document`(`document_id` ASC) USING BTREE,
  INDEX `idx_kb_lint_type_status`(`issue_type` ASC, `status` ASC) USING BTREE,
  INDEX `idx_kb_lint_assignee`(`assignee_id` ASC) USING BTREE,
  INDEX `idx_kb_lint_priority_status`(`priority` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识体检问题' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_llm_call_log
-- ----------------------------
DROP TABLE IF EXISTS `kb_llm_call_log`;
CREATE TABLE `kb_llm_call_log`  (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint NULL DEFAULT NULL COMMENT '空间ID（Ask/Wiki/Ingest 等；连通性测试可为空）',
  `user_id` bigint NULL DEFAULT NULL COMMENT '触发用户ID',
  `scene` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '场景 ask/ingest_plan/ingest_generate/ingest_enrich/wiki_revise/wiki_enrich/llm_test',
  `provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'LLM 提供方',
  `model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'success' COMMENT 'success/fail',
  `latency_ms` int NULL DEFAULT NULL COMMENT '耗时毫秒',
  `cache_hit` tinyint(1) NOT NULL DEFAULT 0 COMMENT '语义缓存命中',
  `failover` tinyint(1) NOT NULL DEFAULT 0 COMMENT '经 fallback 成功',
  `prompt_tokens_est` int NULL DEFAULT NULL COMMENT '估算 prompt tokens',
  `completion_tokens_est` int NULL DEFAULT NULL COMMENT '估算 completion tokens',
  `estimated_cost_usd` decimal(12, 6) NULL DEFAULT NULL COMMENT '估算成本 USD；缓存命中为 0',
  `error_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败摘要',
  `create_time` datetime NULL DEFAULT NULL COMMENT '调用时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_llm_call_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_kb_llm_call_space`(`space_id` ASC) USING BTREE,
  INDEX `idx_kb_llm_call_scene`(`scene` ASC) USING BTREE,
  INDEX `idx_kb_llm_call_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库 LLM 调用审计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_platform_llm_config
-- ----------------------------
DROP TABLE IF EXISTS `kb_platform_llm_config`;
CREATE TABLE `kb_platform_llm_config`  (
  `id` bigint NOT NULL COMMENT '固定 1（平台单例）',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `enabled` tinyint NOT NULL DEFAULT 0 COMMENT '1启用 0停用',
  `call_log_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '1记录LLM调用日志(成本统计) 0关闭',
  `provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'deepseek' COMMENT 'deepseek/qwen/glm/custom',
  `base_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'https://api.deepseek.com/v1' COMMENT 'OpenAI 兼容 base-url',
  `api_key_cipher` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AES-GCM 密文 Base64',
  `api_key_mask` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脱敏展示 ****末4位',
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'deepseek-chat' COMMENT '默认模型',
  `temperature` decimal(4, 2) NOT NULL DEFAULT 0.30 COMMENT '采样温度',
  `timeout_seconds` int NOT NULL DEFAULT 90 COMMENT 'HTTP 超时秒',
  `extra_models` json NULL COMMENT '治理/Ingest 可选模型列表 JSON 数组',
  PRIMARY KEY (`id`) USING BTREE,
  CONSTRAINT `chk_kb_platform_llm_singleton` CHECK (`id` = 1)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库平台 LLM 配置（Web 系统设置）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_qa_log
-- ----------------------------
DROP TABLE IF EXISTS `kb_qa_log`;
CREATE TABLE `kb_qa_log`  (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint NULL DEFAULT NULL COMMENT '空间ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '提问用户ID',
  `question` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题',
  `answer` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '答案',
  `citations` json NULL COMMENT '引用来源 JSON',
  `scope` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '识别的检索作用域 type/tags',
  `provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'LLM 提供方 deepseek/qwen/glm',
  `model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名',
  `prompt_tokens` int NULL DEFAULT NULL COMMENT '输入 token',
  `completion_tokens` int NULL DEFAULT NULL COMMENT '输出 token',
  `useful` tinyint NULL DEFAULT NULL COMMENT '反馈 1有用 0无用 NULL未评',
  `create_time` datetime NULL DEFAULT NULL COMMENT '提问时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_qa_space`(`space_id` ASC) USING BTREE,
  INDEX `idx_kb_qa_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_kb_qa_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识问答日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_relation
-- ----------------------------
DROP TABLE IF EXISTS `kb_relation`;
CREATE TABLE `kb_relation`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `source_doc_id` bigint NOT NULL COMMENT '源文档ID',
  `target_doc_id` bigint NULL DEFAULT NULL COMMENT '目标文档ID（断链时为空）',
  `target_title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标标题（断链时保留原始[[标题]]）',
  `relation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'links_to/same_tag/related/supersedes/references',
  `resolved` tinyint NULL DEFAULT 1 COMMENT '1已解析 0断链',
  `weight` int NULL DEFAULT 1 COMMENT '权重',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_relation`(`source_doc_id` ASC, `target_doc_id` ASC, `relation_type` ASC) USING BTREE,
  INDEX `idx_kb_relation_space`(`space_id` ASC) USING BTREE,
  INDEX `idx_kb_relation_source`(`source_doc_id` ASC) USING BTREE,
  INDEX `idx_kb_relation_target`(`target_doc_id` ASC) USING BTREE,
  INDEX `idx_kb_relation_resolved`(`resolved` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档关系/图谱边' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_research_run
-- ----------------------------
DROP TABLE IF EXISTS `kb_research_run`;
CREATE TABLE `kb_research_run`  (
  `id` bigint NOT NULL COMMENT '主键',
  `run_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对外 runId',
  `user_id` bigint NULL DEFAULT NULL COMMENT '调用用户',
  `space_ids_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'ACL 空间 JSON',
  `topic` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '脱敏主题',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PENDING|RUNNING|SUCCEEDED|FAILED|DEGRADED',
  `degraded` tinyint(1) NULL DEFAULT 0,
  `degrade_reason` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `outline_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Planner 产出',
  `sections_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '每节检索 evidence',
  `citations_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '全局 citations',
  `coverage` double NULL DEFAULT NULL COMMENT 'Reviewer coverage（Phase B）',
  `report_md` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '报告正文（Phase B）',
  `ingest_job_id` bigint NULL DEFAULT NULL COMMENT '回写 job（Phase B）',
  `latency_ms` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_run_id`(`run_id` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'DeepResearch 运行 trace' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_space
-- ----------------------------
DROP TABLE IF EXISTS `kb_space`;
CREATE TABLE `kb_space`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `space_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '空间编码',
  `space_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '空间名称',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `visibility` int NULL DEFAULT 1 COMMENT '0私有 1内部 2公开',
  `owner_id` bigint NULL DEFAULT NULL COMMENT '负责人',
  `status` int NULL DEFAULT 1 COMMENT '1启用 0停用',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_space_code`(`space_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识空间' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_space_member
-- ----------------------------
DROP TABLE IF EXISTS `kb_space_member`;
CREATE TABLE `kb_space_member`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `member_type` tinyint NOT NULL DEFAULT 0 COMMENT '0用户 1角色',
  `member_id` bigint NOT NULL COMMENT '用户ID或角色ID',
  `role` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'viewer' COMMENT 'viewer/editor/admin',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_space_member`(`space_id` ASC, `member_type` ASC, `member_id` ASC) USING BTREE,
  INDEX `idx_kb_space_member_space`(`space_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识空间成员' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_sync_log
-- ----------------------------
DROP TABLE IF EXISTS `kb_sync_log`;
CREATE TABLE `kb_sync_log`  (
  `id` bigint NOT NULL COMMENT '主键',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '批次号',
  `space_id` bigint NULL DEFAULT NULL COMMENT '空间ID',
  `document_id` bigint NULL DEFAULT NULL COMMENT '文档ID',
  `source_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'kb/ 原始路径',
  `action` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'insert/update/delete/skip',
  `content_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '同步时内容 SHA-256',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'success' COMMENT 'success/fail',
  `message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明/错误信息',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_sync_batch`(`batch_no` ASC) USING BTREE,
  INDEX `idx_kb_sync_document`(`document_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识同步日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_tag
-- ----------------------------
DROP TABLE IF EXISTS `kb_tag`;
CREATE TABLE `kb_tag`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `tag_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名',
  `color` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '颜色',
  `is_delete` int NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_tag_name`(`space_id` ASC, `tag_name` ASC) USING BTREE,
  INDEX `idx_kb_tag_space`(`space_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识标签' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for operation_component_deploy_info
-- ----------------------------
DROP TABLE IF EXISTS `operation_component_deploy_info`;
CREATE TABLE `operation_component_deploy_info`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `component_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件名',
  `server_id` bigint NULL DEFAULT NULL COMMENT '服务器ID',
  `server_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务器IP',
  `account` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '账户',
  `password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码',
  `deploy_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部署路径',
  `port` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '端口',
  `version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '版本',
  `environment` int NULL DEFAULT NULL COMMENT '环境',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint NULL DEFAULT 0 COMMENT '健康 0未知 1可达 2不可达 3跳过',
  `last_check_time` datetime NULL DEFAULT NULL COMMENT '最近探测时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operation_component_server_id`(`server_id` ASC) USING BTREE,
  INDEX `idx_operation_component_env`(`environment` ASC) USING BTREE,
  INDEX `idx_operation_component_name`(`component_name`(64) ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '组件部署信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operation_platform_info
-- ----------------------------
DROP TABLE IF EXISTS `operation_platform_info`;
CREATE TABLE `operation_platform_info`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `platform_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '平台名称',
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'URL',
  `account` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '账户',
  `password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码(配置存储，勿写死密钥)',
  `environment` int NULL DEFAULT NULL COMMENT '环境 1dev 2test 3pre 4pro',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '运营平台信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operation_port_matrix
-- ----------------------------
DROP TABLE IF EXISTS `operation_port_matrix`;
CREATE TABLE `operation_port_matrix`  (
  `id` bigint NOT NULL COMMENT '主键',
  `matrix_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '矩阵主键，如 user-center',
  `display_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '展示名',
  `expected_port` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '期望端口',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `source` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ops-console' COMMENT '来源说明',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_matrix_key`(`matrix_key` ASC) USING BTREE,
  INDEX `idx_port_matrix_enabled`(`enabled` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运维端口矩阵' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for operation_port_matrix_alias
-- ----------------------------
DROP TABLE IF EXISTS `operation_port_matrix_alias`;
CREATE TABLE `operation_port_matrix_alias`  (
  `id` bigint NOT NULL COMMENT '主键',
  `matrix_id` bigint NOT NULL COMMENT 'operation_port_matrix.id',
  `alias` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '别名，全局唯一',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_alias`(`alias` ASC) USING BTREE,
  INDEX `idx_matrix_alias_matrix_id`(`matrix_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运维端口矩阵别名' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for operation_project_component
-- ----------------------------
DROP TABLE IF EXISTS `operation_project_component`;
CREATE TABLE `operation_project_component`  (
  `id` bigint NOT NULL COMMENT '主键',
  `project_id` bigint NOT NULL COMMENT '项目 ID',
  `component_id` bigint NOT NULL COMMENT '组件 ID',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '依赖说明，如 业务库/会话缓存',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_operation_project_component`(`project_id` ASC, `component_id` ASC) USING BTREE,
  INDEX `idx_opc_component`(`component_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目→组件依赖（手工维护）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for operation_project_deploy_info
-- ----------------------------
DROP TABLE IF EXISTS `operation_project_deploy_info`;
CREATE TABLE `operation_project_deploy_info`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `server_id` bigint NULL DEFAULT NULL COMMENT '服务器ID',
  `server_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务器IP',
  `inner_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内网IP',
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'URL',
  `project_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目名称',
  `deploy_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部署路径',
  `port` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '端口',
  `environment` int NULL DEFAULT NULL COMMENT '环境',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deploy_running` tinyint(1) NULL DEFAULT NULL COMMENT '部署进程是否运行（定时同步）',
  `last_deploy_check_time` datetime NULL DEFAULT NULL COMMENT '最近部署状态同步时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operation_project_deploy_server_id`(`server_id` ASC) USING BTREE,
  INDEX `idx_operation_project_env`(`environment` ASC) USING BTREE,
  INDEX `idx_operation_project_name`(`project_name`(64) ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '项目部署信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operation_server_component
-- ----------------------------
DROP TABLE IF EXISTS `operation_server_component`;
CREATE TABLE `operation_server_component`  (
  `id` bigint NOT NULL COMMENT '主键',
  `server_id` bigint NULL DEFAULT NULL COMMENT '服务器ID',
  `component_id` bigint NULL DEFAULT NULL COMMENT '组件ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_server_component`(`server_id` ASC, `component_id` ASC) USING BTREE,
  INDEX `idx_operation_server_component_server_id`(`server_id` ASC) USING BTREE,
  INDEX `idx_operation_server_component_component_id`(`component_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务器-组件关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operation_server_info
-- ----------------------------
DROP TABLE IF EXISTS `operation_server_info`;
CREATE TABLE `operation_server_info`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `server_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务器名',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP',
  `inner_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内网IP',
  `port` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '端口',
  `environment` int NULL DEFAULT NULL COMMENT '环境',
  `server_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色 app/db/cache/mq/gateway/bastion/middleware/other',
  `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签 JSON 数组，如 [\"gz\",\"knowledge\"]',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint NULL DEFAULT 0 COMMENT '健康 0未知 1可达 2不可达 3跳过',
  `last_check_time` datetime NULL DEFAULT NULL COMMENT '最近探测时间',
  `ssh_port` int NULL DEFAULT 22 COMMENT 'SSH 端口',
  `ssh_user` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'ubuntu' COMMENT 'SSH 登录用户',
  `ssh_auth_type` tinyint NULL DEFAULT 1 COMMENT '1私钥 2密码',
  `ssh_private_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'SSH 私钥 AES-GCM 密文',
  `ssh_passphrase` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '私钥口令/登录密码 AES-GCM 密文',
  `conn_pref` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'auto' COMMENT '连接偏好 auto/inner/public',
  `upload_allowed_roots` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '该服务器允许上传的路径前缀，逗号或换行分隔',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operation_server_env`(`environment` ASC) USING BTREE,
  INDEX `idx_operation_server_ip`(`ip` ASC) USING BTREE,
  INDEX `idx_operation_server_role`(`server_role` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务器信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operation_server_project
-- ----------------------------
DROP TABLE IF EXISTS `operation_server_project`;
CREATE TABLE `operation_server_project`  (
  `id` bigint NOT NULL COMMENT '主键(实体字段名为 Id，映射列为 id)',
  `server_id` bigint NULL DEFAULT NULL COMMENT '服务器ID',
  `project_id` bigint NULL DEFAULT NULL COMMENT '项目ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_server_project`(`server_id` ASC, `project_id` ASC) USING BTREE,
  INDEX `idx_operation_server_project_server_id`(`server_id` ASC) USING BTREE,
  INDEX `idx_operation_server_project_project_id`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务器-项目关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operation_task
-- ----------------------------
DROP TABLE IF EXISTS `operation_task`;
CREATE TABLE `operation_task`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `task_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'deploy/upload/command',
  `server_id` bigint NULL DEFAULT NULL COMMENT '目标服务器',
  `project_id` bigint NULL DEFAULT NULL COMMENT '关联项目台账 ID（deploy 从项目页跳转）',
  `service_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'user-center/gateway/knowledge',
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'start/stop/restart/upload 等',
  `target_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标描述',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'pending' COMMENT 'pending/running/success/failed',
  `progress` int NULL DEFAULT 0 COMMENT '0-100',
  `task_log` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '执行日志',
  `message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '结果摘要',
  `finish_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operation_task_server`(`server_id` ASC) USING BTREE,
  INDEX `idx_operation_task_type_status`(`task_type` ASC, `status` ASC) USING BTREE,
  INDEX `idx_operation_task_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_operation_task_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '运维异步任务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for seckill_activity
-- ----------------------------
DROP TABLE IF EXISTS `seckill_activity`;
CREATE TABLE `seckill_activity`  (
  `id` bigint NOT NULL COMMENT 'activity id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `stock` bigint NOT NULL DEFAULT 0 COMMENT 'initial stock',
  `sold` bigint NOT NULL DEFAULT 0 COMMENT 'sold count (async sync)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=active 0=closed',
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Seckill activity' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for seckill_order
-- ----------------------------
DROP TABLE IF EXISTS `seckill_order`;
CREATE TABLE `seckill_order`  (
  `id` bigint NOT NULL COMMENT 'order id (snowflake)',
  `activity_id` bigint NOT NULL,
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=success',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_user`(`activity_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Seckill order (async persist)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_action
-- ----------------------------
DROP TABLE IF EXISTS `sys_action`;
CREATE TABLE `sys_action`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `perm_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限码，全局唯一',
  `resource` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源，如 user',
  `action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动作，如 add',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '显示名称',
  `menu_id` bigint NULL DEFAULT NULL COMMENT '关联 C 页面，仅 UI 分组',
  `order_num` int NULL DEFAULT 0,
  `status` tinyint NULL DEFAULT 1 COMMENT '1启用 0停用',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_perm_code`(`perm_code` ASC) USING BTREE,
  INDEX `idx_menu_id`(`menu_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 405 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统动作目录（非导航）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL COMMENT '主键（应用侧雪花 ID）',
  `config_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '参数键名，必须是 ConfigKey 注册表中已声明的 key',
  `config_value` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '覆盖值，按声明的 valueType 解析；无覆盖时应删除该行而非留空',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统参数运行期覆盖值' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级id',
  `dept_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门名称',
  `order_num` int NULL DEFAULT NULL COMMENT '排序号',
  `status` int NULL DEFAULT NULL COMMENT '1:正常 0:停用',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_dept_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '部门' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `sort` int NULL DEFAULT NULL COMMENT '字典排序',
  `dict_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典键',
  `dict_value` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典值',
  `dict_value_en` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典值(英文)',
  `dict_value_ja` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典值(日文)',
  `dict_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典类型',
  `status` int NULL DEFAULT NULL COMMENT '1正常 0停用',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_dict_data_dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `dict_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典名称',
  `dict_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典类型',
  `status` int NULL DEFAULT NULL COMMENT '1正常 0停用',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_dict_type_dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP',
  `login_address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录地址',
  `browser` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作系统',
  `status` int NULL DEFAULT NULL COMMENT '1成功 0失败',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `login_time` datetime NULL DEFAULT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_login_log_login_time`(`login_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '登录日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `menu_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `menu_name_en` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单名称(英文)',
  `menu_name_ja` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单名称(日文)',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父ID',
  `path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由',
  `component` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件/路由名',
  `route_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Vue路由名称(name)',
  `menu_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'M目录 C菜单 F按钮',
  `perms` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `status` int NULL DEFAULT NULL COMMENT '1启用 0禁用',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `order_num` int NULL DEFAULT NULL COMMENT '显示顺序',
  `system_id` bigint NULL DEFAULT NULL COMMENT '所属业务系统 sys_system.id；NULL=全系统共享',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_menu_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_sys_menu_system_id`(`system_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint NOT NULL COMMENT '主键（应用侧雪花 ID）',
  `notice_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `notice_type` int NOT NULL COMMENT '类型，对应字典 sys_notice_type：1通知 2公告 3维护',
  `notice_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '公告正文（Markdown 源文）',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知公告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_notice_read_cursor
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_read_cursor`;
CREATE TABLE `sys_notice_read_cursor`  (
  `user_id` bigint NOT NULL COMMENT '用户 ID（sys_user.id），一用户一行',
  `last_read_time` datetime NOT NULL COMMENT '最后一次读公告列表的时间水位',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户公告已读水位' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL COMMENT '主键',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `business_type` int NULL DEFAULT NULL COMMENT '业务类型',
  `method_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '方法名',
  `request_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'HTTP方法',
  `user_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `request_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求IP',
  `request_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'URL',
  `request_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `response_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '返回结果',
  `status` int NULL DEFAULT NULL COMMENT '1正常 0异常',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `request_location` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作地点',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_operation_log_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '岗位编码',
  `post_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '岗位名称',
  `status` int NULL DEFAULT NULL COMMENT '1正常 0停用',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_post_post_code`(`post_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '岗位' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `role_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  `order_num` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '排序(实体为 String)',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` int NULL DEFAULT NULL COMMENT '1正常 0停用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_action
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_action`;
CREATE TABLE `sys_role_action`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `perm_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限码',
  PRIMARY KEY (`role_id`, `perm_code`) USING BTREE,
  INDEX `idx_perm_code`(`perm_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色-动作权限' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL COMMENT '主键',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `menu_id` bigint NULL DEFAULT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_role_menu_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_sys_role_menu_menu_id`(`menu_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色-菜单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_system
-- ----------------------------
DROP TABLE IF EXISTS `sys_system`;
CREATE TABLE `sys_system`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `system_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '系统编码',
  `system_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '系统名称',
  `base_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '访问根 URL',
  `icon` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` int NULL DEFAULT 1 COMMENT '1启用 0停用',
  `sso_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'INTERNAL' COMMENT 'INTERNAL/EXTERNAL',
  `entry_path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '/sso/login' COMMENT 'SSO 入口路径',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `system_group` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'business' COMMENT '门户分组',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_system_code`(`system_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '业务系统' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `work_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工号',
  `nick_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓名',
  `user_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码',
  `identity_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '身份证',
  `sex` int NULL DEFAULT NULL COMMENT '性别',
  `telephone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '电话(业务上唯一)',
  `address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `work_time` datetime NULL DEFAULT NULL COMMENT '入职日期',
  `is_job` int NULL DEFAULT NULL COMMENT '是否在职 0在职 1离职',
  `status` int NULL DEFAULT NULL COMMENT '是否锁定 0未锁 1已锁',
  `error_num` int NULL DEFAULT NULL COMMENT '密码错误次数',
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
  `language` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'zh-CN' COMMENT '界面语言',
  `salt` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '盐',
  `is_delete` int NULL DEFAULT 0 COMMENT '是否删除 0未删 1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_user_name`(`user_name` ASC) USING BTREE,
  UNIQUE INDEX `uk_sys_user_telephone`(`telephone` ASC) USING BTREE,
  INDEX `idx_sys_user_dept_id`(`dept_id` ASC) USING BTREE,
  INDEX `idx_sys_user_is_delete`(`is_delete` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `post_id` bigint NULL DEFAULT NULL COMMENT '岗位ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_user_post_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_sys_user_post_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户-岗位' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_user_role_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_sys_user_role_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户-角色' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_system
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_system`;
CREATE TABLE `sys_user_system`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `system_id` bigint NOT NULL COMMENT '系统ID',
  `is_default` int NULL DEFAULT 0 COMMENT '1默认 0否',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_user_system_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_sys_user_system_system_id`(`system_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户-系统' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
