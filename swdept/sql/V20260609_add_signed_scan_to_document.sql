-- V20260609 — tb_document 최종 날인본 스캔 PDF 컬럼 신설
-- sprint: doc-signed-scan-upload (착수/기성/준공 도장 날인본 스캔 보관)
-- 근거: 기획서 v0.2 §6 (DB팀 권고 B안) / 개발계획서 v1 묶음 A
-- 성격: additive, nullable, 기존 row 무영향 (위험도 0). 파일시스템 저장(D드라이브), DB엔 경로/메타만.

ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_path        VARCHAR(500);
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_orig_name   VARCHAR(255);
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_size        BIGINT;
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_uploaded_at TIMESTAMP;
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_uploaded_by BIGINT REFERENCES users(user_id);

COMMENT ON COLUMN tb_document.signed_scan_path        IS '최종 도장 날인본 스캔 PDF 절대경로(파일시스템) — doc-signed-scan-upload (2026-06-09)';
COMMENT ON COLUMN tb_document.signed_scan_orig_name   IS '업로드 원본 파일명';
COMMENT ON COLUMN tb_document.signed_scan_size        IS '파일 크기(byte)';
COMMENT ON COLUMN tb_document.signed_scan_uploaded_at IS '업로드 일시';
COMMENT ON COLUMN tb_document.signed_scan_uploaded_by IS '업로더 — users(user_id) FK (author_id/approver_id 와 동일 관례)';
