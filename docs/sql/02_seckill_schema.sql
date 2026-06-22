-- Seckill tables for load-test / flash-sale scenarios
-- Run after scripts/moli.sql on database `moli`

CREATE TABLE IF NOT EXISTS `seckill_activity` (
  `id`           BIGINT       NOT NULL COMMENT 'activity id',
  `name`         VARCHAR(128) NOT NULL DEFAULT '',
  `stock`        BIGINT       NOT NULL DEFAULT 0 COMMENT 'initial stock',
  `sold`         BIGINT       NOT NULL DEFAULT 0 COMMENT 'sold count (async sync)',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=active 0=closed',
  `start_time`   DATETIME     NULL,
  `end_time`     DATETIME     NULL,
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seckill activity';

CREATE TABLE IF NOT EXISTS `seckill_order` (
  `id`           BIGINT       NOT NULL COMMENT 'order id (snowflake)',
  `activity_id`  BIGINT       NOT NULL,
  `user_id`      VARCHAR(64)  NOT NULL,
  `request_id`   VARCHAR(64)  NULL,
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=success',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_activity_user` (`activity_id`, `user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seckill order (async persist)';

-- Default activity for smoke / million-QPS scripts
INSERT INTO `seckill_activity` (`id`, `name`, `stock`, `sold`, `status`, `create_time`)
VALUES (1, 'million-qps-demo', 1000000, 0, 1, NOW())
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `stock` = VALUES(`stock`), `sold` = 0, `status` = 1;
