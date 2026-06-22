-- =============================================================
-- 企业知识库 Schema（重新设计 · 2026-06-22）
-- 范式：LLM-Wiki（kb/ markdown 为唯一写入源，本库为下游只读门面 + 对外 Web/API）
-- 运行顺序：在 scripts/moli.sql（基础库）之后，对数据库 `moli` 执行
-- 设计说明见同目录 KNOWLEDGE_SCHEMA.md
--
-- 表分组：
--   核心内容：kb_space / kb_category / kb_document / kb_tag / kb_document_tag
--             kb_comment / kb_document_version / kb_favorite / kb_attachment
--   图谱治理：kb_relation / kb_lint_issue
--   同步     ：kb_sync_log
--   权限     ：kb_space_member
--   问答     ：kb_qa_log
-- 通用约定：bigint 雪花主键；create_id/create_time/update_id/update_time 审计字段；
--           is_delete 逻辑删除（0未删/1已删）；utf8mb4。
-- =============================================================

-- -------------------------------------------------------------
-- 1. 知识空间（多租户 / 权限隔离边界）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_space` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `space_code` varchar(64) NOT NULL COMMENT '空间编码',
  `space_name` varchar(128) NOT NULL COMMENT '空间名称',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `icon` varchar(64) DEFAULT NULL COMMENT '图标',
  `visibility` int DEFAULT 1 COMMENT '0私有 1内部 2公开',
  `owner_id` bigint DEFAULT NULL COMMENT '负责人',
  `status` int DEFAULT 1 COMMENT '1启用 0停用',
  `sort` int DEFAULT 0 COMMENT '排序',
  `is_delete` int DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_space_code` (`space_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识空间';

-- -------------------------------------------------------------
-- 2. 分类树（空间内 parent_id 自关联）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_category` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `parent_id` bigint DEFAULT 0 COMMENT '父分类ID',
  `category_name` varchar(128) NOT NULL COMMENT '分类名称',
  `icon` varchar(64) DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT 0 COMMENT '排序',
  `is_delete` int DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_kb_category_space` (`space_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识分类';

-- -------------------------------------------------------------
-- 3. 知识文档（核心表）
--    新增（对齐 kb→DB 同步 / Query / 面试题系列）：
--      slug         空间内唯一标识，kb 同步幂等主键 + 干净 URL
--      source_path  kb/ 中原始 markdown 相对路径
--      content_hash 正文+frontmatter 的 SHA-256，增量同步比对
--      kb_type      知识类型 guide/service/concept/article/interview/output（frontmatter type）
--      domain       领域 FE/AP/DB/...（frontmatter domain，作用域过滤）
--      source       来源 kb（同步只读）/ manual（界面创建）
--    注意：doc_type=内容格式(markdown/rich)，kb_type=知识类型，二者不同。
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_document` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `slug` varchar(160) DEFAULT NULL COMMENT '空间内唯一标识/URL',
  `source` varchar(16) DEFAULT 'manual' COMMENT '来源 kb/manual',
  `source_path` varchar(512) DEFAULT NULL COMMENT 'kb/ 原始 markdown 路径',
  `content_hash` char(64) DEFAULT NULL COMMENT '正文+frontmatter 的 SHA-256',
  `title` varchar(256) NOT NULL COMMENT '标题',
  `summary` varchar(512) DEFAULT NULL COMMENT '摘要',
  `content` longtext COMMENT '正文',
  `doc_type` varchar(32) DEFAULT 'markdown' COMMENT '内容格式 markdown/rich',
  `kb_type` varchar(32) DEFAULT NULL COMMENT '知识类型 guide/service/concept/article/interview/output',
  `domain` varchar(32) DEFAULT NULL COMMENT '领域 FE/AP/DB...',
  `status` int DEFAULT 0 COMMENT '0草稿 1已发布 2已归档',
  `view_count` int DEFAULT 0 COMMENT '浏览次数',
  `like_count` int DEFAULT 0 COMMENT '点赞数',
  `version_no` int DEFAULT 1 COMMENT '版本号',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `is_delete` int DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_document_slug` (`space_id`, `slug`),
  KEY `idx_kb_document_space` (`space_id`),
  KEY `idx_kb_document_category` (`category_id`),
  KEY `idx_kb_document_status` (`status`),
  KEY `idx_kb_document_type` (`kb_type`),
  FULLTEXT KEY `ftx_kb_document` (`title`, `summary`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文档';

-- -------------------------------------------------------------
-- 4. 标签
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_tag` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `tag_name` varchar(64) NOT NULL COMMENT '标签名',
  `color` varchar(16) DEFAULT NULL COMMENT '颜色',
  `is_delete` int DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_tag_name` (`space_id`, `tag_name`),
  KEY `idx_kb_tag_space` (`space_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识标签';

-- -------------------------------------------------------------
-- 5. 文档-标签关联
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_document_tag` (
  `id` bigint NOT NULL COMMENT '主键',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_document_tag` (`document_id`, `tag_id`),
  KEY `idx_kb_document_tag_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档标签关联';

-- -------------------------------------------------------------
-- 6. 评论（支持楼中楼 parent_id）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_comment` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `parent_id` bigint DEFAULT 0 COMMENT '父评论ID',
  `content` varchar(1024) NOT NULL COMMENT '评论内容',
  `is_delete` int DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_kb_comment_document` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档评论';

-- -------------------------------------------------------------
-- 7. 文档版本历史
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_document_version` (
  `id` bigint NOT NULL COMMENT '主键',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `title` varchar(256) DEFAULT NULL COMMENT '标题',
  `content` longtext COMMENT '正文',
  `content_hash` char(64) DEFAULT NULL COMMENT '该版本内容 SHA-256',
  `change_log` varchar(512) DEFAULT NULL COMMENT '变更说明',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_doc_version` (`document_id`, `version_no`),
  KEY `idx_kb_document_version_doc` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本';

-- -------------------------------------------------------------
-- 8. 个人收藏
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_favorite` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `create_time` datetime DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_favorite` (`user_id`, `document_id`),
  KEY `idx_kb_favorite_document` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档收藏';

-- -------------------------------------------------------------
-- 9. 附件（MinIO 对象存储）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_attachment` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `file_name` varchar(256) NOT NULL COMMENT '文件名',
  `object_key` varchar(512) NOT NULL COMMENT 'MinIO对象键',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `content_type` varchar(128) DEFAULT NULL COMMENT 'MIME类型',
  `is_delete` int DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_kb_attachment_document` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档附件';

-- -------------------------------------------------------------
-- 10. 文档关系（图谱落库 + 断链记录）
--     替代运行时解析：kb 同步 / 手动编辑时写入。
--     relation_type：links_to(正文[[]]引用) / same_tag(同标签) /
--                    related(相关) / supersedes(取代旧页) / references(引用来源)
--     resolved=0 表示目标未解析到文档（断链），此时 target_doc_id 为空、
--     target_title 保留原始 [[标题]]，供 lint 体检直接读取。
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_relation` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `source_doc_id` bigint NOT NULL COMMENT '源文档ID',
  `target_doc_id` bigint DEFAULT NULL COMMENT '目标文档ID（断链时为空）',
  `target_title` varchar(256) DEFAULT NULL COMMENT '目标标题（断链时保留原始[[标题]]）',
  `relation_type` varchar(32) NOT NULL COMMENT 'links_to/same_tag/related/supersedes/references',
  `resolved` tinyint DEFAULT 1 COMMENT '1已解析 0断链',
  `weight` int DEFAULT 1 COMMENT '权重',
  `is_delete` int DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_relation` (`source_doc_id`, `target_doc_id`, `relation_type`),
  KEY `idx_kb_relation_space` (`space_id`),
  KEY `idx_kb_relation_source` (`source_doc_id`),
  KEY `idx_kb_relation_target` (`target_doc_id`),
  KEY `idx_kb_relation_resolved` (`resolved`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档关系/图谱边';

-- -------------------------------------------------------------
-- 11. 体检问题（Lint 结果持久化 + 处理状态）
--     issue_type：broken_link/orphan/no_summary/duplicate/stale/conflict
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_lint_issue` (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `document_id` bigint DEFAULT NULL COMMENT '相关文档ID',
  `issue_type` varchar(32) NOT NULL COMMENT '问题类型',
  `detail` varchar(512) DEFAULT NULL COMMENT '问题详情',
  `status` tinyint DEFAULT 0 COMMENT '0待处理 1已忽略 2已修复',
  `scan_time` datetime DEFAULT NULL COMMENT '扫描时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_kb_lint_space` (`space_id`),
  KEY `idx_kb_lint_document` (`document_id`),
  KEY `idx_kb_lint_type_status` (`issue_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识体检问题';

-- -------------------------------------------------------------
-- 12. 同步日志（kb→kb_document 单向增量同步审计）
--     action：insert/update/delete/skip
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_sync_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `batch_no` varchar(64) NOT NULL COMMENT '批次号',
  `space_id` bigint DEFAULT NULL COMMENT '空间ID',
  `document_id` bigint DEFAULT NULL COMMENT '文档ID',
  `source_path` varchar(512) DEFAULT NULL COMMENT 'kb/ 原始路径',
  `action` varchar(16) NOT NULL COMMENT 'insert/update/delete/skip',
  `content_hash` char(64) DEFAULT NULL COMMENT '同步时内容 SHA-256',
  `status` varchar(16) DEFAULT 'success' COMMENT 'success/fail',
  `message` varchar(512) DEFAULT NULL COMMENT '说明/错误信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_kb_sync_batch` (`batch_no`),
  KEY `idx_kb_sync_document` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识同步日志';

-- -------------------------------------------------------------
-- 13. 空间成员（空间级 ACL，复用用户中心用户/角色）
--     member_type：0用户 1角色；role：viewer/editor/admin
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_space_member` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `space_id` bigint NOT NULL COMMENT '空间ID',
  `member_type` tinyint NOT NULL DEFAULT 0 COMMENT '0用户 1角色',
  `member_id` bigint NOT NULL COMMENT '用户ID或角色ID',
  `role` varchar(16) NOT NULL DEFAULT 'viewer' COMMENT 'viewer/editor/admin',
  `is_delete` int DEFAULT 0 COMMENT '0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_space_member` (`space_id`, `member_type`, `member_id`),
  KEY `idx_kb_space_member_space` (`space_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识空间成员';

-- -------------------------------------------------------------
-- 14. 问答日志（/kb/ask 历史 + 引用 + 反馈，支撑评测与好答案回写）
--     citations：JSON 数组 [{docId,title,slug,snippet}]
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `kb_qa_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint DEFAULT NULL COMMENT '空间ID',
  `user_id` bigint DEFAULT NULL COMMENT '提问用户ID',
  `question` varchar(1024) NOT NULL COMMENT '问题',
  `answer` longtext COMMENT '答案',
  `citations` json DEFAULT NULL COMMENT '引用来源 JSON',
  `scope` varchar(128) DEFAULT NULL COMMENT '识别的检索作用域 type/tags',
  `provider` varchar(32) DEFAULT NULL COMMENT 'LLM 提供方 deepseek/qwen/glm',
  `model` varchar(64) DEFAULT NULL COMMENT '模型名',
  `prompt_tokens` int DEFAULT NULL COMMENT '输入 token',
  `completion_tokens` int DEFAULT NULL COMMENT '输出 token',
  `useful` tinyint DEFAULT NULL COMMENT '反馈 1有用 0无用 NULL未评',
  `create_time` datetime DEFAULT NULL COMMENT '提问时间',
  PRIMARY KEY (`id`),
  KEY `idx_kb_qa_space` (`space_id`),
  KEY `idx_kb_qa_user` (`user_id`),
  KEY `idx_kb_qa_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识问答日志';

-- =============================================================
-- 演示种子数据
-- =============================================================
INSERT INTO `kb_space` VALUES (900000000000000001, 1, NOW(), 1, NOW(), 'enterprise-kb', '企业知识库', '公司级知识沉淀与协作空间', 'book-open', 1, 1, 1, 1, 0);

INSERT INTO `kb_category` VALUES (900000000000000101, 1, NOW(), 1, NOW(), 900000000000000001, 0, '产品文档', NULL, 1, 0);
INSERT INTO `kb_category` VALUES (900000000000000102, 1, NOW(), 1, NOW(), 900000000000000001, 0, '研发规范', NULL, 2, 0);
INSERT INTO `kb_category` VALUES (900000000000000103, 1, NOW(), 1, NOW(), 900000000000000001, 900000000000000101, '需求说明', NULL, 1, 0);

INSERT INTO `kb_tag` VALUES (900000000000000201, 1, NOW(), 1, NOW(), 900000000000000001, '入门', '#409EFF', 0);
INSERT INTO `kb_tag` VALUES (900000000000000202, 1, NOW(), 1, NOW(), 900000000000000001, '最佳实践', '#67C23A', 0);

INSERT INTO `kb_document`
  (`id`,`create_id`,`create_time`,`update_id`,`update_time`,`space_id`,`category_id`,
   `slug`,`source`,`source_path`,`content_hash`,`title`,`summary`,`content`,
   `doc_type`,`kb_type`,`domain`,`status`,`view_count`,`like_count`,`version_no`,`publish_time`,`is_delete`)
VALUES
  (900000000000000301, 1, NOW(), 1, NOW(), 900000000000000001, 900000000000000103,
   'kb-quickstart', 'manual', NULL, NULL, '知识库快速上手', '介绍企业知识库的核心功能与使用方式',
   '# 企业知识库\n\n## 功能概览\n- 空间管理\n- 分类树\n- 文档编辑与发布\n- 标签检索\n- 版本历史\n- 评论与收藏',
   'markdown', 'guide', NULL, 1, 0, 0, 1, NOW(), 0);

-- SSO 门户注册（已有库增量执行时可单独跑本段）
INSERT INTO `sys_system` VALUES (39, 1, '2026-06-21 00:00:00', 1, '2026-06-21 00:00:00', 'moli-knowledge', '企业知识库', 'http://127.0.0.1:21000/KnowledgeServer', 'book-open', 61, 1, 'EXTERNAL', '/sso/login', 'Moli 知识库微服务：空间/分类/文档/标签/搜索', 'business')
ON DUPLICATE KEY UPDATE
  system_name = VALUES(system_name),
  base_url = VALUES(base_url),
  icon = VALUES(icon),
  sort = VALUES(sort),
  remark = VALUES(remark),
  system_group = VALUES(system_group),
  update_time = VALUES(update_time);

INSERT INTO `sys_user_system` VALUES (720358942831542272, 720351341083361280, 39, 0)
ON DUPLICATE KEY UPDATE system_id = VALUES(system_id);

INSERT INTO `sys_user_system` VALUES (720358942835736576, 719712653013942272, 39, 0)
ON DUPLICATE KEY UPDATE system_id = VALUES(system_id);
