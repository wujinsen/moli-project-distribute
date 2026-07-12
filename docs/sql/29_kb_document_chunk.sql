-- KB chunk 切段表（/kb/ask 按段召回）
-- 可重复执行：表已存在则跳过
-- 执行顺序：见 docs/ops/sql-migration-order.md（在 03_knowledge_schema 之后）

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `kb_document_chunk` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `document_id` bigint NOT NULL COMMENT 'kb_document.id',
  `space_id` bigint NOT NULL COMMENT '冗余 ACL/filter',
  `slug` varchar(512) NOT NULL COMMENT '冗余引用',
  `kb_type` varchar(32) DEFAULT NULL COMMENT '冗余体裁',
  `category_id` bigint DEFAULT NULL COMMENT '冗余分类',
  `status` tinyint DEFAULT 1 COMMENT '与文档一致 0草稿1发布2归档',
  `chunk_index` int NOT NULL DEFAULT 0 COMMENT '页内顺序 0-based',
  `heading` varchar(255) DEFAULT NULL COMMENT '节标题',
  `heading_level` tinyint DEFAULT 0 COMMENT '0页首 2=## 3=###',
  `content` mediumtext NOT NULL COMMENT '切段正文（含标题行）',
  `char_count` int DEFAULT 0,
  `content_hash` char(64) DEFAULT NULL COMMENT 'SHA-256',
  `is_delete` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_kb_chunk_document` (`document_id`, `chunk_index`),
  KEY `idx_kb_chunk_space` (`space_id`),
  KEY `idx_kb_chunk_slug` (`space_id`, `slug`(191)),
  FULLTEXT KEY `ftx_kb_document_chunk` (`heading`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档切段（ask 召回）';
