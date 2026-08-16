-- =============================================================
-- 移除日本語試験空间（jp-fe-ap-exam）及误入的 FE/AP 文档
-- 在已有库执行一次；新环境勿再导入 04/16 jp 种子（已删除）
-- 导入：mysql --default-character-set=utf8mb4 moli < docs/sql/40_purge_jp_exam.sql
-- =============================================================

SET NAMES utf8mb4;

SET @JP_SPACE := 900000000000000002;

DROP TEMPORARY TABLE IF EXISTS _jp_doc_purge;
CREATE TEMPORARY TABLE _jp_doc_purge AS
SELECT id FROM kb_document
WHERE space_id = @JP_SPACE
   OR slug LIKE 'fe/%'
   OR slug LIKE 'ap/%'
   OR slug LIKE 'certify/%'
   OR slug LIKE 'interview/fe%'
   OR slug LIKE '%fe_kamoku%'
   OR slug LIKE '%kamoku_b%'
   OR domain IN ('JP-FE', 'JP-AP');

DELETE c FROM kb_document_chunk c
INNER JOIN _jp_doc_purge p ON p.id = c.document_id;

DELETE v FROM kb_document_version v
INNER JOIN _jp_doc_purge p ON p.id = v.document_id;

DELETE dt FROM kb_document_tag dt
INNER JOIN _jp_doc_purge p ON p.id = dt.document_id;

DELETE cm FROM kb_comment cm
INNER JOIN _jp_doc_purge p ON p.id = cm.document_id;

DELETE f FROM kb_favorite f
INNER JOIN _jp_doc_purge p ON p.id = f.document_id;

DELETE a FROM kb_attachment a
INNER JOIN _jp_doc_purge p ON p.id = a.document_id;

DELETE r FROM kb_relation r
INNER JOIN _jp_doc_purge p ON p.id = r.source_doc_id OR p.id = r.target_doc_id;

DELETE d FROM kb_document d
INNER JOIN _jp_doc_purge p ON p.id = d.id;

DROP TEMPORARY TABLE IF EXISTS _jp_doc_purge;

DELETE FROM kb_tag WHERE space_id = @JP_SPACE;
DELETE FROM kb_category WHERE space_id = @JP_SPACE;
DELETE FROM kb_space_member WHERE space_id = @JP_SPACE;
DELETE FROM kb_ingest_job WHERE space_id = @JP_SPACE;
DELETE FROM kb_lint_issue WHERE space_id = @JP_SPACE;
DELETE FROM kb_sync_log WHERE space_id = @JP_SPACE;
DELETE FROM kb_space WHERE id = @JP_SPACE OR space_code = 'jp-fe-ap-exam';
