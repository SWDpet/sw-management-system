-- ============================================================
-- Phase 2: DB 증분 DDL 스크립트
-- ============================================================
-- ⚠ 본 스크립트는 phase1 이후의 **증분 DDL** 입니다.
--   이 파일만으로는 신규 환경 초기화가 불가능하며, 별도 phase1
--   스키마(또는 JPA DDL 생성) 가 선행되어야 합니다.
--
-- 전제 테이블 목록 (phase1 에서 생성되어 있어야 함):
--   sw_pjt, users, tb_infra_master, tb_infra_server,
--   tb_infra_software, tb_infra_link_upis, tb_infra_link_api,
--   tb_infra_memo, access_logs, ps_info, tb_performance_summary,
--   sys_mst, sigungu_code, prj_types, maint_tp_mst,
--   cont_stat_mst, cont_frm_mst, pjt_equip, tb_pjt_target,
--   tb_pjt_manpower_plan, tb_pjt_schedule
--
-- phase1 DDL 정리는 향후 별도 스프린트에서 일괄 정비 예정
-- (감사 2026-04-18 P2 2-2 조치 기록, 스프린트 2a).
-- ============================================================

-- 1. 사업별 과업참여자 배정
CREATE TABLE IF NOT EXISTS tb_contract_participant (
    participant_id SERIAL PRIMARY KEY,
    proj_id BIGINT REFERENCES sw_pjt(proj_id),
    user_id BIGINT REFERENCES users(user_id),
    role_type VARCHAR(30),
    tech_grade VARCHAR(30),
    task_desc VARCHAR(500),
    is_site_rep BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0
);

-- 2. 시스템별 공정명 마스터
CREATE TABLE IF NOT EXISTS tb_process_master (
    process_id SERIAL PRIMARY KEY,
    sys_nm_en VARCHAR(30),
    process_name VARCHAR(200),
    sort_order INTEGER DEFAULT 0,
    use_yn VARCHAR(1) DEFAULT 'Y'
);

-- 3. 시스템별 용역 목적/과업 내용 마스터
CREATE TABLE IF NOT EXISTS tb_service_purpose (
    purpose_id SERIAL PRIMARY KEY,
    sys_nm_en VARCHAR(30),
    purpose_type VARCHAR(20),
    purpose_text TEXT,
    sort_order INTEGER DEFAULT 0,
    use_yn VARCHAR(1) DEFAULT 'Y'
);

-- 기본 공정명 데이터
-- V018 (process-master-dedup 2026-04-20) 이후: UNIQUE(sys_nm_en, process_name) 적용됨
INSERT INTO tb_process_master (sys_nm_en, process_name, sort_order) VALUES
('UPIS', '도시계획정보체계용 GIS SW 유지관리', 1),
('KRAS', '부동산종합공부시스템용 GIS SW 유지관리', 1),
('IPSS', '지하시설물관리시스템용 GIS SW 유지관리', 1),
('GIS_SW', 'GIS SW 유지관리', 1),
('APIMS', '도로관리시스템용 GIS SW 유지관리', 1)
ON CONFLICT (sys_nm_en, process_name) DO NOTHING;

-- 기본 용역 목적 데이터
-- V018 이후: UNIQUE INDEX uq_service_purpose_sys_type_md5 (sys_nm_en, purpose_type, md5(purpose_text)) 적용됨
INSERT INTO tb_service_purpose (sys_nm_en, purpose_type, purpose_text, sort_order) VALUES
('UPIS', 'PURPOSE', '도시계획정보체계(UPIS)의 최신 버전 유지와 원활한 서비스를 제공', 1),
('KRAS', 'PURPOSE', '부동산종합공부시스템(KRAS)의 최신 버전 유지와 원활한 서비스를 제공', 1),
('IPSS', 'PURPOSE', '지하시설물관리시스템(IPSS)의 최신 버전 유지와 원활한 서비스를 제공', 1),
('GIS_SW', 'PURPOSE', 'GIS SW의 최신 버전 유지와 원활한 서비스를 제공', 1),
('APIMS', 'PURPOSE', '도로관리시스템(APIMS)의 최신 버전 유지와 원활한 서비스를 제공', 1)
ON CONFLICT (sys_nm_en, purpose_type, md5(purpose_text)) DO NOTHING;

-- users.field_role 컬럼 추가 (분야별: 유지보수책임기술자/유지보수참여기술자)
ALTER TABLE users ADD COLUMN IF NOT EXISTS field_role VARCHAR(50);
UPDATE users SET field_role = '유지보수책임기술자' WHERE username = '박욱진' AND (field_role IS NULL OR field_role = '');
UPDATE users SET field_role = '유지보수참여기술자' WHERE username IN ('김한준','서현규') AND (field_role IS NULL OR field_role = '');

-- users.career_years 컬럼 추가 (경력 연수, 예: "22년")
ALTER TABLE users ADD COLUMN IF NOT EXISTS career_years VARCHAR(20);
UPDATE users SET career_years = '22년' WHERE username = '박욱진' AND (career_years IS NULL OR career_years = '');
UPDATE users SET career_years = '13년' WHERE username = '김한준' AND (career_years IS NULL OR career_years = '');
UPDATE users SET career_years = '8년'  WHERE username = '서현규' AND (career_years IS NULL OR career_years = '');

-- ============================================================
-- 점검내역서 관련 테이블
-- ============================================================

-- 점검내역서 마스터 (표지)
CREATE TABLE IF NOT EXISTS inspect_report (
    id              BIGSERIAL PRIMARY KEY,
    pjt_id          BIGINT NOT NULL REFERENCES sw_pjt(proj_id) ON DELETE CASCADE,
    inspect_month   VARCHAR(7),
    sys_type        VARCHAR(20),
    doc_title       VARCHAR(300),
    insp_company    VARCHAR(100),
    insp_name       VARCHAR(50),
    conf_org        VARCHAR(100),
    conf_name       VARCHAR(50),
    insp_dbms       VARCHAR(200),
    insp_gis        VARCHAR(200),
    dbms_ip         VARCHAR(50),
    status          VARCHAR(20) DEFAULT 'DRAFT',
    insp_sign       TEXT,                          -- 점검자 서명 Base64 PNG (감사 P1 2-1)
    conf_sign       TEXT,                          -- 확인자 서명 Base64 PNG (감사 P1 2-1)
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

-- [감사 P1 2-1] inspect_report 엔티티-스키마 정합성 보정 — 기존 DB 인스턴스 대응 (멱등)
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS insp_sign TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS conf_sign TEXT;

CREATE INDEX IF NOT EXISTS idx_inspect_report_pjt ON inspect_report(pjt_id);
CREATE INDEX IF NOT EXISTS idx_inspect_report_month ON inspect_report(inspect_month);

-- 방문이력 (업무/증상/조치내용)
CREATE TABLE IF NOT EXISTS inspect_visit_log (
    id              BIGSERIAL PRIMARY KEY,
    report_id       BIGINT NOT NULL REFERENCES inspect_report(id) ON DELETE CASCADE,
    visit_year      VARCHAR(4),
    visit_month     VARCHAR(2),
    visit_day       VARCHAR(2),
    task            VARCHAR(200),
    symptom         VARCHAR(500),
    action          VARCHAR(500),
    sort_order      INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_visit_log_report ON inspect_visit_log(report_id);

-- 점검결과 항목별 기록
CREATE TABLE IF NOT EXISTS inspect_check_result (
    id              BIGSERIAL PRIMARY KEY,
    report_id       BIGINT NOT NULL REFERENCES inspect_report(id) ON DELETE CASCADE,
    section         VARCHAR(20) NOT NULL,
    category        VARCHAR(50),
    item_name       VARCHAR(200),
    item_method     VARCHAR(300),
    result          VARCHAR(500),
    remarks         VARCHAR(300),
    sort_order      INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_check_result_report ON inspect_check_result(report_id);
CREATE INDEX IF NOT EXISTS idx_check_result_section ON inspect_check_result(report_id, section);

-- 점검 템플릿 (시스템별 점검항목 마스터)
CREATE TABLE IF NOT EXISTS inspect_template (
    id              BIGSERIAL PRIMARY KEY,
    template_type   VARCHAR(20) NOT NULL,
    section         VARCHAR(20) NOT NULL,
    category        VARCHAR(50),
    item_name       VARCHAR(200) NOT NULL,
    item_method     VARCHAR(300),
    sort_order      INT DEFAULT 0,
    use_yn          VARCHAR(1) DEFAULT 'Y',
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inspect_template_type ON inspect_template(template_type, section);

-- ============================================================
-- 점검 템플릿 초기 데이터 (docx 샘플과 100% 일치)
-- ============================================================

-- 기존 데이터 삭제 후 재삽입
DELETE FROM inspect_template;

-- ============================================================
-- UPIS (HW+SW) DB서버 점검결과 - 점검내역서_UPIS_SW_HW샘플.docx Table 11
-- section='DB', 칼럼: 구분 | 점검 항목 | 수행 명령/방법 | 결과
-- ============================================================
INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) VALUES
('UPIS', 'DB', '육안점검', 'H/W LED 상태 점검', '육안 확인', 1),
('UPIS', 'DB', '육안점검', '디스크 LED 상태 점검', '육안 확인', 2),
('UPIS', 'DB', '육안점검', 'FAN LED 상태 점검', '육안 확인', 3),
('UPIS', 'DB', '육안점검', '전원 LED 상태 점검', '육안 확인', 4),
('UPIS', 'DB', '육안점검', '케이블 연결상태 점검', '육안 확인', 5),
('UPIS', 'DB', '구성 점검', 'CPU 점검', 'lsdev -Cc processor', 6),
('UPIS', 'DB', '구성 점검', 'MEMORY 점검', 'lsdev -Cc memory', 7),
('UPIS', 'DB', '구성 점검', 'Adapter 점검', 'lsdev -Cc adapter', 8),
('UPIS', 'DB', '구성 점검', 'DISK 점검', 'lsdev -Cc disk, lspv', 9),
('UPIS', 'DB', '구성 점검', 'Network 점검', 'netstat -a', 10),
('UPIS', 'DB', '성능 점검', 'CPU 사용률', 'topas, nmon', 11),
('UPIS', 'DB', '성능 점검', 'MEMORY 사용률', 'topas, nmon', 12),
('UPIS', 'DB', '성능 점검', 'SWAP 사용률', 'lsps -a', 13),
('UPIS', 'DB', '성능 점검', 'I/O 사용률', 'iostat 1 10', 14),
('UPIS', 'DB', '성능 점검', '네트워크 사용률', 'topas, netstat -ni', 15),
('UPIS', 'DB', 'DATA 점검', '디스크 사용량', 'df -gH', 16),
('UPIS', 'DB', 'DATA 점검', 'I-node 사용량', 'df -h', 17),
('UPIS', 'DB', 'DATA 점검', '미러 상태', 'lsvg -l rootvg', 18),
('UPIS', 'DB', '네트워크', 'Link 상태', 'netstat -na', 19),
('UPIS', 'DB', '네트워크', 'Ping 상태', 'ping gateway', 20),
('UPIS', 'DB', '네트워크', 'Collisions', 'netstat -ni', 21),
('UPIS', 'DB', '프로세서', '각 프로세서 상태', 'topas, nmon', 22),
('UPIS', 'DB', '로그', '시스템 로그', 'errpt', 23),
('UPIS', 'DB', '로그', '접속 로그', 'who, lastlog', 24)
ON CONFLICT DO NOTHING;

-- ============================================================
-- UPIS (HW+SW) AP서버 점검결과 - 점검내역서_UPIS_SW_HW샘플.docx Table 14
-- section='AP', 칼럼: 종류 | 점검 항목 | 점검 내용 | 점검 기준 | 결과
-- item_method 형식: "점검 내용||점검 기준"
-- ============================================================
INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) VALUES
('UPIS', 'AP', 'H/W', '시스템 LED', 'Front panel LED 육안 확인||적색등 유무', 1),
('UPIS', 'AP', 'H/W', 'Power Supply', 'Power Supply 육안 확인||적색등 유무', 2),
('UPIS', 'AP', 'H/W', 'Disk', 'LED 육안 확인||적색등 유무', 3),
('UPIS', 'AP', 'H/W', 'CPU', 'CPU 상태 확인||정상용량 확인', 4),
('UPIS', 'AP', 'H/W', 'Memory', 'Memory 상태 확인||정상용량 확인', 5),
('UPIS', 'AP', 'H/W', 'Adapter', 'LED 및 Cable 연결상태 확인||적색등 유무', 6),
('UPIS', 'AP', 'OS', '로그 점검', 'eventlog||error 유무', 7),
('UPIS', 'AP', 'OS', 'Security log', 'Security log 확인||error 유무', 8),
('UPIS', 'AP', 'OS', 'Disk 여유공간', '내컴퓨터||디스크 용량 확인', 9),
('UPIS', 'AP', 'OS', 'Network 점검', 'netstat -r, netstat -e||라우팅/네트워크 에러', 10),
('UPIS', 'AP', 'OS', 'IP 정보', 'ipconfig /all||IP/링크 상태 확인', 11),
('UPIS', 'AP', '보안', '사용자 계정', '사용자 계정 확인||특이계정 유무', 12),
('UPIS', 'AP', '성능', 'CPU', '관리도구-성능 (processor%)||CPU 사용량', 13),
('UPIS', 'AP', '성능', 'Memory', '관리도구-성능 (Memory%)||Memory 사용량', 14)
ON CONFLICT DO NOTHING;

-- ============================================================
-- UPIS (HW+SW) DBMS (Oracle) 점검결과 - 점검내역서_UPIS_SW_HW샘플.docx Table 17
-- section='DBMS', 칼럼: 종류 | 점검 항목 | 수행 명령/쿼리 | 결과
-- ============================================================
INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) VALUES
('UPIS', 'DBMS', '오라클', '호스트네임', '#hostname', 1),
('UPIS', 'DBMS', '오라클', 'O/S 정보', '#oslevel -s', 2),
('UPIS', 'DBMS', '오라클', 'DB 버전', '#sqlplus', 3),
('UPIS', 'DBMS', '오라클', 'SID 확인', '#echo $ORACLE_SID', 4),
('UPIS', 'DBMS', '오라클', '오라클 로그', '#vi alert_ort.log', 5),
('UPIS', 'DBMS', '오라클', 'Archive Mode', '>archive log list;', 6),
('UPIS', 'DBMS', '오라클', '리두 로그', '>select * from v$logfile;', 7),
('UPIS', 'DBMS', '오라클', '컨트롤 파일', '>select * from v$controlfile;', 8),
('UPIS', 'DBMS', '오라클', 'SGA', '>show sga;', 9),
('UPIS', 'DBMS', '오라클', 'Tablespace 상태', '>select status from dba_tablespaces;', 10),
('UPIS', 'DBMS', '오라클', 'Tablespace 용량', '>select a.tablespace_name ...', 11),
('UPIS', 'DBMS', '오라클', 'Datafile 상태', '>select d.status, v.status ...', 12),
('UPIS', 'DBMS', '오라클', 'Datafile 용량', '>select sum from dba_data_files;', 13),
('UPIS', 'DBMS', '오라클', 'Export 백업', '#crontab -l, ls', 14),
('UPIS', 'DBMS', '오라클', 'Home size', '#df -gP', 15),
('UPIS', 'DBMS', '오라클', 'Oradata Size', '#df -gP', 16),
('UPIS', 'DBMS', '오라클', 'Backup Size', '#df -gP', 17)
ON CONFLICT DO NOTHING;

-- ============================================================
-- UPIS GIS엔진 점검결과 - 6항목 (S1 에서 UPIS_SW 통합)
-- 칼럼: 대상 | 점검 항목 | 점검 내용 및 방법 | 결과
-- ============================================================
INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) VALUES
('UPIS', 'GIS', 'GeoNURIS Spatial Server (GSS)', 'GSS 구동확인', 'ps -ef | grep GSS 실행 확인', 1),
('UPIS', 'GIS', 'GeoNURIS Spatial Server (GSS)', 'GSS 로그파일 삭제', '/GeoNURIS_Spatial_Server/log 경로의 로그 중 1달 전 파일 삭제', 2),
('UPIS', 'GIS', 'GeoNURIS Spatial Server (GSS)', 'Desktop Pro 데이터저장소 구동확인', 'Desktop Pro 실행 후 데이터저장소에서 GSS 데이터 불러오기', 3),
('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)', 'GWS 구동확인', '윈도우 서비스 "GeoNURIS GeoWeb Server 64bit" 구동 상태 확인', 4),
('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)', 'GWS 로그파일 삭제', 'C:\\Program Files\\GeoNURIS_GeoWeb_Server_64\\webapps\\uwes\\store (DEM/SLOP 제외)', 5),
('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)', 'GWS 서비스 확인', 'http://웹서버IP:8880/uwes 관리자 접속 → WMS → Preview → 지도 표출', 6)
-- S1 inspect-comprehensive-redesign (2026-04-21): UPIS_SW 6행은 UPIS 와 중복이라 제거
ON CONFLICT DO NOTHING;

-- ============================================================
-- S1 inspect-comprehensive-redesign (2026-04-21):
--   UPIS/UPIS_SW 표준시스템 점검결과 (APP 섹션 28행) 전체 제거 (A3 opt1)
--   사용자 결정: "UPIS APP와 UPIS_SW APP는 사용하지 않음" (2026-04-20)
-- ============================================================

-- ============================================================
-- KRAS GIS엔진 점검결과 - 점검내역서_KRAS샘플.docx Table 8
-- 칼럼: 대상 | 점검 내용 및 방법 | 결과 (3칼럼, 점검 항목 없음)
-- item_name에 점검 내용, item_method는 빈값 (프론트에서 대상+내용만 표시)
-- ============================================================
INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) VALUES
('KRAS', 'GIS', 'GeoNURIS Spatial Server(GSS)', 'GSS 구동확인', 'ps –ef | grep –i geo 실행 확인', 1),
('KRAS', 'GIS', 'GeoNURIS Spatial Server(GSS)', 'GSS 상태확인', 'GSS -I aliveness | GSS -I connections', 2),
('KRAS', 'GIS', 'GeoNURIS Spatial Server(GSS)', 'GSS 로그확인', '/kras_home/geonuris/GeoNURIS_Spatail_Server3.6/logs 최신 로그일자 파일 확인 tail –f catalina.out', 3),
('KRAS', 'GIS', 'GeoNURIS GeoWeb Server(GWS)', 'GWS로그확인', '/kras_home/app/MapStudio/log 최신 로그일자 파일 확인', 4),
('KRAS', 'GIS', 'GeoNURIS GeoWeb Server(GWS)', 'GWS로그확인', '/kras_home/app/MapStudio/log 최신 로그일자 파일 확인', 5),
('KRAS', 'GIS', 'GeoNURIS GeoWeb Server(GWS)', 'GWS서비스 확인', 'http://웹서버 IP:9080/msp 로 관리자페이지 접속 | 로그인 > Spatial > 공간데이터 정상표출 확인 | wms, wfs, wfs transaction 정상표출 확인', 6),
('KRAS', 'GIS', '측량성과 프로그램', '부동산종합공부시스템 실행', 'C/S 실행 확인', 7),
('KRAS', 'GIS', 'GeoNURIS Desktop Pro', 'Desktop Pro 구동확인', '바탕화면의 Desktop Pro 실행', 8),
('KRAS', 'GIS', 'GeoNURIS Desktop Pro', 'Map Display 확인', '데이터저장소를 통해 데이터 목록 갱신 | 공간 데이터 표출 확인', 9)
ON CONFLICT DO NOTHING;

-- pjt_server_info 테이블 제거 (더 이상 사용하지 않음)
DROP TABLE IF EXISTS pjt_server_info CASCADE;

-- ============================================================
-- 문서 관리 계열 테이블 (감사 P2 2-2 조치, 스프린트 2a)
-- Entity 기준: Document, DocumentDetail, DocumentHistory,
--              DocumentAttachment, DocumentSignature,
--              InspectChecklist, InspectIssue, WorkPlan
-- 모두 IF NOT EXISTS 로 멱등.
-- ============================================================

-- 문서 마스터
CREATE TABLE IF NOT EXISTS tb_document (
    doc_id          BIGSERIAL PRIMARY KEY,
    doc_no          VARCHAR(50) UNIQUE,
    doc_type        VARCHAR(30) NOT NULL,
    sys_type        VARCHAR(20),
    infra_id        BIGINT REFERENCES tb_infra_master(infra_id),
    plan_id         BIGINT,
    proj_id         BIGINT REFERENCES sw_pjt(proj_id),
    title           VARCHAR(500) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    author_id       BIGINT NOT NULL REFERENCES users(user_id),
    approver_id     BIGINT REFERENCES users(user_id),
    approved_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tb_document_proj   ON tb_document(proj_id);
CREATE INDEX IF NOT EXISTS idx_tb_document_infra  ON tb_document(infra_id);
CREATE INDEX IF NOT EXISTS idx_tb_document_type   ON tb_document(doc_type);
CREATE INDEX IF NOT EXISTS idx_tb_document_status ON tb_document(status);

-- 문서 섹션 상세 (jsonb)
CREATE TABLE IF NOT EXISTS tb_document_detail (
    detail_id       BIGSERIAL PRIMARY KEY,
    doc_id          BIGINT NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    section_key     VARCHAR(50) NOT NULL,
    section_data    JSONB NOT NULL,
    sort_order      INTEGER DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_tb_document_detail_doc ON tb_document_detail(doc_id);

-- 문서 변경 이력
CREATE TABLE IF NOT EXISTS tb_document_history (
    history_id      BIGSERIAL PRIMARY KEY,
    doc_id          BIGINT NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    action          VARCHAR(30) NOT NULL,
    changed_field   VARCHAR(100),
    old_value       TEXT,
    new_value       TEXT,
    actor_id        BIGINT NOT NULL REFERENCES users(user_id),
    comment         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tb_document_history_doc ON tb_document_history(doc_id);

-- 문서 첨부파일
CREATE TABLE IF NOT EXISTS tb_document_attachment (
    attach_id       BIGSERIAL PRIMARY KEY,
    doc_id          BIGINT NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    file_name       VARCHAR(300) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    file_size       BIGINT,
    mime_type       VARCHAR(100),
    uploaded_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tb_document_attachment_doc ON tb_document_attachment(doc_id);

-- 문서 서명
CREATE TABLE IF NOT EXISTS tb_document_signature (
    sign_id         BIGSERIAL PRIMARY KEY,
    doc_id          BIGINT NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    signer_type     VARCHAR(30) NOT NULL,
    signer_name     VARCHAR(50) NOT NULL,
    signer_org      VARCHAR(200),
    signature_image VARCHAR(500),
    signed_at       TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tb_document_signature_doc ON tb_document_signature(doc_id);

-- 점검 체크리스트 (문서 기반)
CREATE TABLE IF NOT EXISTS tb_inspect_checklist (
    check_id        BIGSERIAL PRIMARY KEY,
    doc_id          BIGINT NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    inspect_month   VARCHAR(7),
    target_sw       VARCHAR(50),
    check_item      VARCHAR(300) NOT NULL,
    check_method    VARCHAR(500),
    check_result    VARCHAR(20),
    sort_order      INTEGER DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tb_inspect_checklist_doc ON tb_inspect_checklist(doc_id);

-- 점검 이슈/방문이력 (문서 기반)
CREATE TABLE IF NOT EXISTS tb_inspect_issue (
    issue_id        BIGSERIAL PRIMARY KEY,
    doc_id          BIGINT NOT NULL REFERENCES tb_document(doc_id) ON DELETE CASCADE,
    issue_year      VARCHAR(4),
    issue_month     VARCHAR(2),
    issue_day       VARCHAR(2),
    task_type       VARCHAR(50),
    symptom         TEXT,
    action_taken    TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tb_inspect_issue_doc ON tb_inspect_issue(doc_id);

-- 작업/점검 계획 (self-FK: parent_plan_id)
CREATE TABLE IF NOT EXISTS tb_work_plan (
    plan_id         BIGSERIAL PRIMARY KEY,
    infra_id        BIGINT REFERENCES tb_infra_master(infra_id),
    plan_type       VARCHAR(30) NOT NULL,
    process_step    VARCHAR(100),
    title           VARCHAR(300) NOT NULL,
    description     TEXT,
    assignee_id     BIGINT REFERENCES users(user_id),
    start_date      DATE NOT NULL,
    end_date        DATE,
    location        VARCHAR(300),
    repeat_type     VARCHAR(20) NOT NULL,
    parent_plan_id  BIGINT REFERENCES tb_work_plan(plan_id),
    status          VARCHAR(20) NOT NULL,
    status_reason   VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_tb_work_plan_infra    ON tb_work_plan(infra_id);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_assignee ON tb_work_plan(assignee_id);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_parent   ON tb_work_plan(parent_plan_id);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_status   ON tb_work_plan(status);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_start    ON tb_work_plan(start_date);

-- ============================================================
-- 스프린트 5 (2026-04-19): 조직도 + 문서 선택 UI 통일 + 운영/테스트 구분
-- 기획서: docs/plans/doc-selector-org-env.md
-- ============================================================

-- 조직도 마스터 (self-FK 로 가변 계층)
CREATE TABLE IF NOT EXISTS tb_org_unit (
    unit_id       BIGSERIAL PRIMARY KEY,
    parent_id     BIGINT REFERENCES tb_org_unit(unit_id) ON DELETE RESTRICT,
    unit_type     VARCHAR(20) NOT NULL CHECK (unit_type IN ('DIVISION','DEPARTMENT','TEAM')),
    name          VARCHAR(100) NOT NULL,
    sort_order    INTEGER DEFAULT 0,
    use_yn        VARCHAR(1) DEFAULT 'Y',
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_org_unit_parent ON tb_org_unit(parent_id);
CREATE INDEX IF NOT EXISTS idx_org_unit_type   ON tb_org_unit(unit_type);
CREATE INDEX IF NOT EXISTS idx_org_unit_use    ON tb_org_unit(use_yn);

-- 조직도 초기 seed — 독립 INSERT (DbInitRunner 세미콜론 분리기 호환, 각 행 멱등)
-- DIVISION (최상위)
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', '경영관리본부', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='경영관리본부' AND unit_type='DIVISION');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', '글로벌사업본부', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='글로벌사업본부' AND unit_type='DIVISION');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', '글로벌기획본부', 30 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='글로벌기획본부' AND unit_type='DIVISION');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', 'AI전략기획본부', 40 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='AI전략기획본부' AND unit_type='DIVISION');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', 'AI GIS 연구본부', 50 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='AI GIS 연구본부' AND unit_type='DIVISION');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', 'SW기술연구소', 60 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='SW기술연구소' AND unit_type='DIVISION');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', 'SW영업본부', 70 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='SW영업본부' AND unit_type='DIVISION');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', 'GIS사업본부', 80 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='GIS사업본부' AND unit_type='DIVISION');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', '도시계획사업본부', 90 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='도시계획사업본부' AND unit_type='DIVISION');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT NULL, 'DIVISION', '스마트시티본부', 100 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='스마트시티본부' AND unit_type='DIVISION');
-- 하위 부서·팀
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='경영관리본부' AND unit_type='DIVISION'), 'DEPARTMENT', '인사총무부', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='인사총무부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='경영관리본부' AND unit_type='DIVISION'), 'DEPARTMENT', '재무회계부', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='재무회계부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='경영관리본부' AND unit_type='DIVISION'), 'TEAM', '베트남 경영관리팀', 30 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='베트남 경영관리팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='글로벌사업본부' AND unit_type='DIVISION'), 'DEPARTMENT', '해외사업부', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='해외사업부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='해외사업부' AND unit_type='DEPARTMENT'), 'TEAM', '해외사업팀', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='해외사업팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='AI전략기획본부' AND unit_type='DIVISION'), 'TEAM', 'AI기획팀', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='AI기획팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='AI전략기획본부' AND unit_type='DIVISION'), 'TEAM', '디자인팀', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='디자인팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='AI GIS 연구본부' AND unit_type='DIVISION'), 'DEPARTMENT', 'AI GIS 연구부', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='AI GIS 연구부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='AI GIS 연구부' AND unit_type='DEPARTMENT'), 'TEAM', '연구1팀', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='연구1팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='AI GIS 연구부' AND unit_type='DEPARTMENT'), 'TEAM', '연구2팀', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='연구2팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='AI GIS 연구본부' AND unit_type='DIVISION'), 'DEPARTMENT', 'R&D기획부', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='R&D기획부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='R&D기획부' AND unit_type='DEPARTMENT'), 'TEAM', 'R&D기획팀', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='R&D기획팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='AI GIS 연구본부' AND unit_type='DIVISION'), 'DEPARTMENT', 'SW지원부', 30 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='SW지원부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='SW지원부' AND unit_type='DEPARTMENT'), 'TEAM', 'SW지원팀', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='SW지원팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='SW기술연구소' AND unit_type='DIVISION'), 'DEPARTMENT', '응용개발연구부', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='응용개발연구부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='SW기술연구소' AND unit_type='DIVISION'), 'DEPARTMENT', '제품개발연구부', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='제품개발연구부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='SW영업본부' AND unit_type='DIVISION'), 'DEPARTMENT', 'GIS SW영업부', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='GIS SW영업부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='GIS SW영업부' AND unit_type='DEPARTMENT'), 'TEAM', 'GIS SW영업팀', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='GIS SW영업팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='SW영업본부' AND unit_type='DIVISION'), 'DEPARTMENT', '도시계획 SW영업부', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='도시계획 SW영업부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='도시계획 SW영업부' AND unit_type='DEPARTMENT'), 'TEAM', '도시계획 SW영업팀', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='도시계획 SW영업팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='GIS사업본부' AND unit_type='DIVISION'), 'DEPARTMENT', 'GIS사업1부', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='GIS사업1부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='GIS사업1부' AND unit_type='DEPARTMENT'), 'TEAM', 'GIS1부 사업1팀', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='GIS1부 사업1팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='GIS사업1부' AND unit_type='DEPARTMENT'), 'TEAM', 'GIS1부 사업2팀', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='GIS1부 사업2팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='GIS사업본부' AND unit_type='DIVISION'), 'DEPARTMENT', 'GIS사업2부', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='GIS사업2부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='GIS사업2부' AND unit_type='DEPARTMENT'), 'TEAM', 'GIS2부 사업팀', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='GIS2부 사업팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='GIS사업본부' AND unit_type='DIVISION'), 'TEAM', 'GIS 사업4팀', 30 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='GIS 사업4팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='도시계획사업본부' AND unit_type='DIVISION'), 'DEPARTMENT', '도시계획사업부', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='도시계획사업부' AND unit_type='DEPARTMENT');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='도시계획사업부' AND unit_type='DEPARTMENT'), 'TEAM', '도시계획사업1팀', 10 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='도시계획사업1팀' AND unit_type='TEAM');
INSERT INTO tb_org_unit (parent_id, unit_type, name, sort_order) SELECT (SELECT unit_id FROM tb_org_unit WHERE name='도시계획사업부' AND unit_type='DEPARTMENT'), 'TEAM', '도시계획사업2팀', 20 WHERE NOT EXISTS (SELECT 1 FROM tb_org_unit WHERE name='도시계획사업2팀' AND unit_type='TEAM');

-- tb_document 3 컬럼 보강
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS support_target_type VARCHAR(20);
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS org_unit_id BIGINT;
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS environment VARCHAR(20);

-- FK/CHECK 는 멱등적으로 추가
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_tb_document_org_unit') THEN
        ALTER TABLE tb_document ADD CONSTRAINT fk_tb_document_org_unit FOREIGN KEY (org_unit_id) REFERENCES tb_org_unit(unit_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_tb_document_support_target_type') THEN
        ALTER TABLE tb_document ADD CONSTRAINT ck_tb_document_support_target_type CHECK (support_target_type IS NULL OR support_target_type IN ('EXTERNAL','INTERNAL'));
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_tb_document_environment') THEN
        ALTER TABLE tb_document ADD CONSTRAINT ck_tb_document_environment CHECK (environment IS NULL OR environment IN ('PROD','TEST'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_tb_document_org_unit     ON tb_document(org_unit_id);
CREATE INDEX IF NOT EXISTS idx_tb_document_environment  ON tb_document(environment);
CREATE INDEX IF NOT EXISTS idx_tb_document_support_type ON tb_document(support_target_type);

-- ============================================================
-- 스프린트 5 v2 추가 (2026-04-19): 4개 문서(장애/업무지원/설치/패치)용
-- 지역 식별 컬럼. sigungu_code.adm_sect_c FK.
-- 기존 sys_type 은 sys_mst.cd 값 (UPIS, KRAS 등) 재사용.
-- 이 4개 문서는 사업(sw_pjt)·인프라(tb_infra_master) 와 독립된 성과·히스토리
-- 관리용 — region_code + sys_type 2 컬럼이 단일 식별 키.
-- ============================================================

ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS region_code VARCHAR(10);
CREATE INDEX IF NOT EXISTS idx_tb_document_region ON tb_document(region_code);
-- FK 는 sigungu_code 존재성 확인 후 멱등 적용
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_tb_document_region_code')
       AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='sigungu_code') THEN
        ALTER TABLE tb_document ADD CONSTRAINT fk_tb_document_region_code
            FOREIGN KEY (region_code) REFERENCES sigungu_code(adm_sect_c);
    END IF;
END $$;
