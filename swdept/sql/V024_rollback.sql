-- V024 Rollback
BEGIN;
ALTER TABLE qt_quotation DROP CONSTRAINT IF EXISTS fk_qt_category;
DROP TABLE IF EXISTS qt_category_mst;
COMMIT;
