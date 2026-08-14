-- =============================================================
-- SSO 菜单按系统隔离 · sys_menu.system_id
-- 设计：docs/design/sso-menu-system-isolation.md
-- 运行顺序：在 scripts/moli.sql 及既有菜单增量（04/08/11/12/28 等）之后
-- 合并基线：实现后由 sql-migration-baseline skill 并入 scripts/moli.sql
-- =============================================================
SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- 1. Schema：可空 FK，NULL = 全系统共享
-- -------------------------------------------------------------
ALTER TABLE `sys_menu`
  ADD COLUMN `system_id` BIGINT NULL DEFAULT NULL COMMENT '所属业务系统 sys_system.id；NULL=全系统共享' AFTER `order_num`;

-- 逻辑外键（与项目惯例一致，不建物理 FK 约束）
CREATE INDEX `idx_sys_menu_system_id` ON `sys_menu` (`system_id`);

-- -------------------------------------------------------------
-- 2. Backfill：按 menu_id 段位 → sys_system.id
-- 种子 id 见 scripts/moli.sql sys_system INSERT
-- -------------------------------------------------------------
-- 默认 INTERNAL 宿主：moli-admin (id=1)
SET @SYS_MOLI_ADMIN := 1;
SET @SYS_AI_COPILOT := 4;      -- ai-copilot · ChatGPT 段 500（演示/未来 INTERNAL）
SET @SYS_BI_REPORT := 6;       -- bi-report · 烛龙 BI 段 600（演示/未来 INTERNAL）
SET @SYS_MOLI_KNOWLEDGE := 39; -- moli-knowledge · 知识库 900 段（EXTERNAL，菜单归属登记）

-- 2.1 系统管理（parent 1 及子树，不含 800/810 根）
UPDATE `sys_menu` SET `system_id` = @SYS_MOLI_ADMIN
WHERE `id` = 1 OR (`parent_id` = 1 AND `id` NOT IN (800, 810));

-- 2.2 身份与门户（800 段）
UPDATE `sys_menu` SET `system_id` = @SYS_MOLI_ADMIN
WHERE `id` = 800 OR `parent_id` = 800;

-- 2.3 安全审计（810 段）
UPDATE `sys_menu` SET `system_id` = @SYS_MOLI_ADMIN
WHERE `id` = 810 OR `parent_id` = 810;

-- 2.4 运营管理（400 段）
UPDATE `sys_menu` SET `system_id` = @SYS_MOLI_ADMIN
WHERE `id` = 400 OR `parent_id` = 400;

-- 2.5 ChatGPT（500 段）
UPDATE `sys_menu` SET `system_id` = @SYS_AI_COPILOT
WHERE `id` = 500 OR `parent_id` = 500;

-- 2.6 烛龙 BI（600 段）
UPDATE `sys_menu` SET `system_id` = @SYS_BI_REPORT
WHERE `id` = 600 OR `parent_id` = 600;

-- 2.7 洞察与控制（700 段 · 暂归 moli-admin，见设计文档开放问题）
UPDATE `sys_menu` SET `system_id` = @SYS_MOLI_ADMIN
WHERE `id` = 700 OR `parent_id` = 700;

-- 2.8 企业知识库（900 段含 906/910/920 等后续增量）
-- 定案 Q5-A：内嵌于 moli-admin；sys_system id=39 为门户 EXTERNAL 第二入口，菜单归属仍为 1
UPDATE `sys_menu` SET `system_id` = @SYS_MOLI_ADMIN
WHERE `id` = 900 OR `parent_id` = 900;

-- 2.9 按钮权限（F）随父菜单：按 parent 递归已在 UPDATE 中覆盖子 C/M；
--     孤立 F 行（parent 为叶子 C）由 parent_id 关联，无需单独段

-- 2.10 未命中行保持 NULL（全系统共享）或人工补录后重跑
-- SELECT id, menu_name, parent_id FROM sys_menu WHERE system_id IS NULL AND menu_type IN ('M','C');
