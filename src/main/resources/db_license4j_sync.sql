-- ============================================================
-- License4J 매월 자동 연동 — 신규 테이블 DDL
-- ============================================================
-- 스프린트: license4j-monthly-sync (2026-06-29)
-- 기획서: docs/product-specs/license4j-monthly-sync.md (v0.4)
-- 개발계획: docs/exec-plans/license4j-monthly-sync.md (P1)
--
-- 전제: license_registry / license_upload_history 기존 테이블 존재(불변).
-- 적용: 운영DB 직접 실행([[feedback_swmanager_prod_db_direct_ok]]). ddl-auto=none.
-- 롤백: 본 파일 하단 §ROLLBACK 참조.
-- ============================================================

-- 1. 연동 이력 (D1) — 월간 자동/수동 연동 전용. 기존 license_upload_history 와 분리.
CREATE TABLE IF NOT EXISTS license_sync_history (
    id                   BIGSERIAL PRIMARY KEY,
    run_type             VARCHAR(10)  NOT NULL,              -- AUTO / MANUAL
    status               VARCHAR(10)  NOT NULL,              -- RUNNING / SUCCESS / PARTIAL / FAIL
    started_at           TIMESTAMP    NOT NULL,
    finished_at          TIMESTAMP,
    total_count          INTEGER      DEFAULT 0,
    new_count            INTEGER      DEFAULT 0,
    updated_count        INTEGER      DEFAULT 0,
    duplicate_count      INTEGER      DEFAULT 0,
    fail_count           INTEGER      DEFAULT 0,
    snapshot_id          VARCHAR(40),                        -- license_registry_backup 묶음 키(pre-sync 스냅샷)
    source_snapshot_path VARCHAR(500),                       -- 그 달 Derby 폴더 백업본 경로
    message              TEXT,                               -- 요약/에러(민감값 마스킹)
    triggered_by         VARCHAR(100)                        -- 수동=사용자ID, 자동=SYSTEM
);

CREATE INDEX IF NOT EXISTS idx_license_sync_history_started
    ON license_sync_history (started_at DESC);

-- 동시 실행 방지(원자적): RUNNING 상태 행은 동시에 최대 1개만 허용.
CREATE UNIQUE INDEX IF NOT EXISTS uq_license_sync_running
    ON license_sync_history (status) WHERE status = 'RUNNING';

-- 2. 대장 pre-sync 스냅샷 (FR-4) — 연동 직전 license_registry 전량 백업(jsonb payload, 스키마 변화 내성).
CREATE TABLE IF NOT EXISTS license_registry_backup (
    id              BIGSERIAL PRIMARY KEY,
    snapshot_id     VARCHAR(40)  NOT NULL,                   -- 한 번의 연동 = 하나의 snapshot_id
    snapshot_at     TIMESTAMP    NOT NULL,
    sync_history_id BIGINT REFERENCES license_sync_history(id),
    registry_id     BIGINT,                                  -- 원본 license_registry.id
    license_id      VARCHAR(50),
    product_id      VARCHAR(100),
    payload         JSONB        NOT NULL                    -- to_jsonb(license_registry row) 전체
);

CREATE INDEX IF NOT EXISTS idx_license_registry_backup_snapshot
    ON license_registry_backup (snapshot_id);

-- ============================================================
-- §ROLLBACK (적용 취소 시)
-- ============================================================
-- DROP INDEX IF EXISTS idx_license_registry_backup_snapshot;
-- DROP TABLE IF EXISTS license_registry_backup;
-- DROP INDEX IF EXISTS uq_license_sync_running;
-- DROP INDEX IF EXISTS idx_license_sync_history_started;
-- DROP TABLE IF EXISTS license_sync_history;
-- ============================================================
