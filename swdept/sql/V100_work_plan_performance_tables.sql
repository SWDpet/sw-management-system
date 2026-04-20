-- ============================================================
-- SW지원부 업무계획 및 성과 전산화 - 전체 테이블 생성 스크립트
-- Version: V100
-- Date: 2026-04-02
-- ============================================================

-- ============================================================
-- 1. tb_infra_server 기존 테이블 확장 (H/W 스펙 컬럼 추가)
-- ============================================================
ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS server_model VARCHAR(200);
ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS serial_no VARCHAR(100);
ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS cpu_spec VARCHAR(200);
ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS memory_spec VARCHAR(100);
ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS disk_spec VARCHAR(300);
ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS network_spec VARCHAR(200);
ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS power_spec VARCHAR(100);
ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS os_detail VARCHAR(300);
ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS rack_location VARCHAR(100);
ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS note TEXT;

COMMENT ON COLUMN tb_infra_server.server_model IS '제품명/모델';
COMMENT ON COLUMN tb_infra_server.serial_no IS '시리얼번호';
COMMENT ON COLUMN tb_infra_server.cpu_spec IS 'CPU 상세 스펙';
COMMENT ON COLUMN tb_infra_server.memory_spec IS '메모리 상세 스펙';
COMMENT ON COLUMN tb_infra_server.disk_spec IS '디스크 상세 스펙';
COMMENT ON COLUMN tb_infra_server.network_spec IS '네트워크 상세 스펙';
COMMENT ON COLUMN tb_infra_server.power_spec IS '전원 상세 스펙';
COMMENT ON COLUMN tb_infra_server.os_detail IS '운영체제 상세';
COMMENT ON COLUMN tb_infra_server.rack_location IS '랙 위치';
COMMENT ON COLUMN tb_infra_server.note IS '비고';

-- ============================================================
-- 2. users 테이블 확장 (업무계획/문서관리/성과 권한 추가)
-- ============================================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_work_plan VARCHAR(20) DEFAULT 'NONE';
ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_document VARCHAR(20) DEFAULT 'NONE';
ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_contract VARCHAR(20) DEFAULT 'NONE';
ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_performance VARCHAR(20) DEFAULT 'NONE';
ALTER TABLE users ADD COLUMN IF NOT EXISTS position VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS tech_grade VARCHAR(20);

COMMENT ON COLUMN users.auth_work_plan IS '업무계획 권한';
COMMENT ON COLUMN users.auth_document IS '문서관리 권한';
COMMENT ON COLUMN users.auth_contract IS '사업현황 권한';
COMMENT ON COLUMN users.auth_performance IS '성과통계 권한';
COMMENT ON COLUMN users.position IS '직위';
COMMENT ON COLUMN users.tech_grade IS '기술자등급(고급/중급/초급)';

-- ============================================================
-- 3. tb_contract (계약/사업 정보)
-- ⚠️ DROPPED (2026-04-20, sprint: legacy-contract-tables-drop, 옵션 B)
-- 사유: 완전 고아 테이블 (Entity/Repository/Service 0, 데이터 0, ERD 제거).
--      sw_pjt + ps_info + sigungu_code 로 기능 완전 대체.
-- 롤백 필요 시: git show 8d79f82:swdept/sql/V100_work_plan_performance_tables.sql 라인 49-93 참조
-- ============================================================
/*
CREATE TABLE IF NOT EXISTS tb_contract (
    contract_id     SERIAL PRIMARY KEY,
    infra_id        BIGINT REFERENCES tb_infra_master(infra_id),
    contract_name   VARCHAR(300) NOT NULL,
    contract_no     VARCHAR(100),
    contract_type   VARCHAR(30) NOT NULL DEFAULT 'UNIT_PRICE',
    contract_method VARCHAR(100),
    contract_law    VARCHAR(100),
    contract_clause VARCHAR(100),
    contract_amount BIGINT DEFAULT 0,
    guarantee_amount BIGINT DEFAULT 0,
    guarantee_rate  DECIMAL(5,2),
    contract_date   DATE,
    start_date      DATE,
    end_date        DATE,
    actual_end_date DATE,
    progress_status VARCHAR(30) NOT NULL DEFAULT 'BUDGET',
    contract_year   INTEGER,
    client_org      VARCHAR(200),
    client_addr     VARCHAR(500),
    client_phone    VARCHAR(50),
    client_fax      VARCHAR(50),
    client_dept     VARCHAR(100),
    client_contact  VARCHAR(50),
    client_contact_phone VARCHAR(50),
    prime_contractor    VARCHAR(200),
    subcontract_amount  BIGINT,
    subcontract_type    VARCHAR(100),
    purchase_order_no   VARCHAR(100),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE tb_contract IS '계약/사업 정보';
CREATE INDEX idx_contract_infra ON tb_contract(infra_id);
CREATE INDEX idx_contract_year ON tb_contract(contract_year);
CREATE INDEX idx_contract_status ON tb_contract(progress_status);
*/

-- ============================================================
-- 4. tb_contract_participant (과업참여자)
-- ⚠️ LEGACY DEFINITION (여기 정의는 사용 안 함)
-- 사유: 이 파일의 정의는 contract_id FK 를 사용하는 구버전.
--      실제 사용되는 DDL 은 src/main/resources/db_init_phase2.sql:21-27 의
--      proj_id → sw_pjt 버전. 본 스프린트에서 contract_id 컬럼/FK/INDEX 모두 DROP.
--      V100 은 신규 환경에서 실수로 실행돼도 재생성 안 되도록 주석 처리.
-- ============================================================
/*
CREATE TABLE IF NOT EXISTS tb_contract_participant (
    participant_id  SERIAL PRIMARY KEY,
    contract_id     INTEGER NOT NULL REFERENCES tb_contract(contract_id) ON DELETE CASCADE,
    user_id         BIGINT REFERENCES users(user_id),
    role_type       VARCHAR(20) NOT NULL DEFAULT 'PARTICIPANT',
    tech_grade      VARCHAR(20),
    task_desc       VARCHAR(500),
    is_site_rep     BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order      INTEGER DEFAULT 0
);
COMMENT ON TABLE tb_contract_participant IS '과업참여자';
CREATE INDEX idx_participant_contract ON tb_contract_participant(contract_id);
*/

-- ============================================================
-- 5. tb_contract_target (유지보수 대상)
-- ⚠️ DROPPED (2026-04-20, sprint: legacy-contract-tables-drop, 옵션 B)
-- 롤백: git show 8d79f82:swdept/sql/V100_work_plan_performance_tables.sql 라인 113-128
-- ============================================================
/*
CREATE TABLE IF NOT EXISTS tb_contract_target (
    target_id       SERIAL PRIMARY KEY,
    contract_id     INTEGER NOT NULL REFERENCES tb_contract(contract_id) ON DELETE CASCADE,
    target_category VARCHAR(30),
    product_name    VARCHAR(200) NOT NULL,
    product_detail  VARCHAR(500),
    quantity        INTEGER DEFAULT 1,
    sw_id           BIGINT REFERENCES tb_infra_software(sw_id),
    sort_order      INTEGER DEFAULT 0
);
COMMENT ON TABLE tb_contract_target IS '유지보수 대상 S/W 목록';
CREATE INDEX idx_target_contract ON tb_contract_target(contract_id);
*/

-- ============================================================
-- 6. tb_inspect_cycle (정기점검 주기 설정)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_inspect_cycle (
    cycle_id        SERIAL PRIMARY KEY,
    infra_id        BIGINT NOT NULL REFERENCES tb_infra_master(infra_id),
    cycle_type      VARCHAR(20) NOT NULL DEFAULT 'QUARTERLY',
        -- MONTHLY, QUARTERLY, HALF_YEARLY
    assignee_id     BIGINT REFERENCES users(user_id),
    contact_name    VARCHAR(50),
    contact_phone   VARCHAR(50),
    contact_email   VARCHAR(100),
    pre_contact_days INTEGER NOT NULL DEFAULT 7,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tb_inspect_cycle IS '정기점검 주기 설정';
CREATE INDEX idx_cycle_infra ON tb_inspect_cycle(infra_id);
CREATE INDEX idx_cycle_assignee ON tb_inspect_cycle(assignee_id);

-- ============================================================
-- 7. tb_work_plan (업무계획)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_work_plan (
    plan_id         SERIAL PRIMARY KEY,
    infra_id        BIGINT REFERENCES tb_infra_master(infra_id),
    plan_type       VARCHAR(30) NOT NULL,
        -- CONTRACT, INSTALL, PATCH, INSPECT, PRE_CONTACT, FAULT, SUPPORT, SETTLE, COMPLETE
    process_step    INTEGER CHECK (process_step BETWEEN 1 AND 7),
    title           VARCHAR(300) NOT NULL,
    description     TEXT,
    assignee_id     BIGINT REFERENCES users(user_id),
    start_date      DATE NOT NULL,
    end_date        DATE,
    location        VARCHAR(300),
    repeat_type     VARCHAR(20) NOT NULL DEFAULT 'NONE',
        -- NONE, MONTHLY, QUARTERLY, HALF_YEARLY
    parent_plan_id  INTEGER REFERENCES tb_work_plan(plan_id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
        -- PLANNED, CONTACTED, CONFIRMED, IN_PROGRESS, COMPLETED, POSTPONED, CANCELLED
    status_reason   VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT REFERENCES users(user_id)
);

COMMENT ON TABLE tb_work_plan IS '업무계획';
CREATE INDEX idx_plan_infra ON tb_work_plan(infra_id);
CREATE INDEX idx_plan_assignee ON tb_work_plan(assignee_id);
CREATE INDEX idx_plan_type ON tb_work_plan(plan_type);
CREATE INDEX idx_plan_date ON tb_work_plan(start_date, end_date);
CREATE INDEX idx_plan_parent ON tb_work_plan(parent_plan_id);
CREATE INDEX idx_plan_status ON tb_work_plan(status);

-- ============================================================
-- 8. tb_document (문서 마스터)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_document (
    doc_id          SERIAL PRIMARY KEY,
    doc_no          VARCHAR(50) UNIQUE,
        -- 자동채번: (주)정도UIT {연도}-{일련번호}호
    doc_type        VARCHAR(30) NOT NULL,
        -- COMMENCE, INTERIM, COMPLETION, INSPECT, FAULT, SUPPORT, INSTALL, PATCH
    sys_type        VARCHAR(20),
        -- UPIS, KRAS, IPSS, GIS_SW, APIMS, ETC (점검내역서 양식 분기용)
    infra_id        BIGINT REFERENCES tb_infra_master(infra_id),
    plan_id         INTEGER REFERENCES tb_work_plan(plan_id),
    contract_id     INTEGER REFERENCES tb_contract(contract_id),
    title           VARCHAR(500) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
        -- DRAFT, SUBMITTED, APPROVED, REJECTED
    author_id       BIGINT NOT NULL REFERENCES users(user_id),
    approver_id     BIGINT REFERENCES users(user_id),
    approved_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tb_document IS '문서 마스터';
CREATE INDEX idx_doc_type ON tb_document(doc_type);
CREATE INDEX idx_doc_infra ON tb_document(infra_id);
CREATE INDEX idx_doc_contract ON tb_document(contract_id);
CREATE INDEX idx_doc_author ON tb_document(author_id);
CREATE INDEX idx_doc_status ON tb_document(status);
CREATE INDEX idx_doc_created ON tb_document(created_at);

-- ============================================================
-- 9. tb_document_detail (문서 상세 - JSONB 기반)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_document_detail (
    detail_id       SERIAL PRIMARY KEY,
    doc_id          INTEGER NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    section_key     VARCHAR(50) NOT NULL,
        -- 착수계: letter/commence/schedule/site_rep/security/participants/pledge
        -- 기성계: letter/inspector/detail_sheet
        -- 준공계: letter/completion/inspector/target/inspect_summary
        -- 점검내역서: cover/report/monthly_detail
    section_data    JSONB NOT NULL DEFAULT '{}',
    sort_order      INTEGER DEFAULT 0
);

COMMENT ON TABLE tb_document_detail IS '문서 상세 (섹션별 JSONB)';
CREATE INDEX idx_detail_doc ON tb_document_detail(doc_id);
CREATE INDEX idx_detail_section ON tb_document_detail(doc_id, section_key);

-- ============================================================
-- 10. tb_inspect_checklist (점검 체크리스트 항목)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_inspect_checklist (
    check_id        SERIAL PRIMARY KEY,
    doc_id          INTEGER NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    inspect_month   INTEGER,
    target_sw       VARCHAR(50),
        -- GSS, GWS, DESKTOP_PRO, SURVEY_PROGRAM 등
    check_item      VARCHAR(300) NOT NULL,
    check_method    VARCHAR(500),
    check_result    VARCHAR(20) DEFAULT 'NORMAL',
        -- NORMAL(정상), INSPECT(점검)
    sort_order      INTEGER DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tb_inspect_checklist IS '점검 체크리스트 항목';
CREATE INDEX idx_checklist_doc ON tb_inspect_checklist(doc_id);
CREATE INDEX idx_checklist_month ON tb_inspect_checklist(doc_id, inspect_month);

-- ============================================================
-- 11. tb_inspect_issue (점검 이슈/장애조치 이력)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_inspect_issue (
    issue_id        SERIAL PRIMARY KEY,
    doc_id          INTEGER NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    issue_year      INTEGER,
    issue_month     INTEGER,
    issue_day       INTEGER,
    task_type       VARCHAR(50),
    symptom         TEXT,
    action_taken    TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tb_inspect_issue IS '점검 시 발견 이슈/장애조치 이력';
CREATE INDEX idx_issue_doc ON tb_inspect_issue(doc_id);

-- ============================================================
-- 12. tb_document_history (문서 변경 이력)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_document_history (
    history_id      SERIAL PRIMARY KEY,
    doc_id          INTEGER NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    action          VARCHAR(30) NOT NULL,
        -- CREATE, UPDATE, STATUS_CHANGE, APPROVE, REJECT
    changed_field   VARCHAR(100),
    old_value       TEXT,
    new_value       TEXT,
    actor_id        BIGINT NOT NULL REFERENCES users(user_id),
    comment         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tb_document_history IS '문서 변경 이력';
CREATE INDEX idx_history_doc ON tb_document_history(doc_id);
CREATE INDEX idx_history_actor ON tb_document_history(actor_id);

-- ============================================================
-- 13. tb_document_attachment (첨부파일)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_document_attachment (
    attach_id       SERIAL PRIMARY KEY,
    doc_id          INTEGER NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    file_name       VARCHAR(300) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    file_size       BIGINT,
    mime_type       VARCHAR(100),
    uploaded_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tb_document_attachment IS '문서 첨부파일';
CREATE INDEX idx_attachment_doc ON tb_document_attachment(doc_id);

-- ============================================================
-- 14. tb_document_signature (전자서명)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_document_signature (
    sign_id         SERIAL PRIMARY KEY,
    doc_id          INTEGER NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    signer_type     VARCHAR(30) NOT NULL,
        -- INSPECTOR(점검자), CONFIRMER(확인자), REPRESENTATIVE(대표자)
    signer_name     VARCHAR(50) NOT NULL,
    signer_org      VARCHAR(200),
    signature_image VARCHAR(500),
    signed_at       TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tb_document_signature IS '전자서명';
CREATE INDEX idx_signature_doc ON tb_document_signature(doc_id);

-- ============================================================
-- 15. tb_performance_summary (성과 집계)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_performance_summary (
    summary_id          SERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(user_id),
    period_year         INTEGER NOT NULL,
    period_month        INTEGER NOT NULL,
    install_count       INTEGER DEFAULT 0,
    patch_count         INTEGER DEFAULT 0,
    fault_count         INTEGER DEFAULT 0,
    fault_avg_hours     DECIMAL(8,2),
    support_count       INTEGER DEFAULT 0,
    inspect_plan_count  INTEGER DEFAULT 0,
    inspect_done_count  INTEGER DEFAULT 0,
    interim_count       INTEGER DEFAULT 0,
    completion_count    INTEGER DEFAULT 0,
    infra_count         INTEGER DEFAULT 0,
    plan_total_count    INTEGER DEFAULT 0,
    plan_ontime_count   INTEGER DEFAULT 0,
    calculated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, period_year, period_month)
);

COMMENT ON TABLE tb_performance_summary IS '월별 성과 집계';
CREATE INDEX idx_performance_user ON tb_performance_summary(user_id);
CREATE INDEX idx_performance_period ON tb_performance_summary(period_year, period_month);

-- ============================================================
-- 문서번호 자동채번 시퀀스
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS seq_doc_no START WITH 1 INCREMENT BY 1;

-- ============================================================
-- updated_at 자동 갱신 트리거 함수
-- ============================================================
CREATE OR REPLACE FUNCTION fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 각 테이블에 트리거 적용
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN SELECT unnest(ARRAY[
        'tb_contract', 'tb_inspect_cycle', 'tb_work_plan',
        'tb_document', 'tb_performance_summary'
    ])
    LOOP
        EXECUTE format('
            DROP TRIGGER IF EXISTS trg_%s_updated ON %I;
            CREATE TRIGGER trg_%s_updated
                BEFORE UPDATE ON %I
                FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();
        ', replace(tbl, 'tb_', ''), tbl, replace(tbl, 'tb_', ''), tbl);
    END LOOP;
END;
$$;
