-- =====================================================
-- 보안 패치 마이그레이션 (2026-03-17)
-- =====================================================

-- #3 로그인 시도 제한: users 테이블에 실패 횟수/잠금 시간 컬럼 추가
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_attempts INTEGER DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS lock_time TIMESTAMP;

-- 기존 데이터 초기화
UPDATE users SET failed_attempts = 0 WHERE failed_attempts IS NULL;
