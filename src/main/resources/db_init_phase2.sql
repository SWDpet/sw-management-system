-- ============================================================
-- Phase 2: DB 증분 DDL 스크립트
-- ============================================================
-- ⚠ 본 스크립트는 phase1 이후의 **증분 DDL** 입니다.
--   신규 환경 초기화 시 phase1 → [phase1_sigungu] → phase2 → V*.sql 순서로 실행.
--
-- 전제: db_init_phase1.sql (19 테이블 + 마스터 시드 ~54건) 선행 실행됨.
--   phase1 테이블 (19): sw_pjt, users, tb_infra_master, tb_infra_server,
--   tb_infra_software, tb_infra_link_upis, tb_infra_link_api, tb_infra_memo,
--   access_logs, ps_info, sys_mst, sigungu_code, prj_types, maint_tp_mst,
--   cont_stat_mst, cont_frm_mst, tb_pjt_target, tb_pjt_manpower_plan, tb_pjt_schedule
--
-- 추가 의존:
--   tb_performance_summary 는 V100 (work_plan_performance_tables.sql) 에서 생성됨
--   pjt_equip 은 V025 에서 DROPPED (2026-04-22, 미사용 테이블 제거)
--
-- phase1 정식화 스프린트: phase1-ddl-formalization (2026-04-27)
-- 감사 2026-04-18 P2 2-2 조치 (스프린트 2a) → 본 스프린트로 후속 완료.
-- phase2-V018-init-ordering (2026-05-11) — V018 의 UNIQUE 제약·INDEX 를 phase2 의 INSERT 앞에 선이동. V018 무수정. 선행: dbinitrunner-dollar-quote-aware (ba12fc6).
-- phase2-tb_ops_doc-forward-ref (2026-05-11) — tb_ops_doc 의 forward-reference 해소를 위해 tb_work_plan + tb_org_unit 블록을 tb_ops_doc 직전으로 선이동. schema/seed 의미 변경 0.
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

-- [phase2-V018-init-ordering] V018 의 UNIQUE 제약을 INSERT 앞에 선이동 (멱등 가드).
-- V018 (4) 와 동일 식별자/동일 컬럼. V018 재진입 시 conname EXISTS 로 무해 PASS.
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_process_master_sys_name') THEN
    ALTER TABLE tb_process_master
      ADD CONSTRAINT uq_process_master_sys_name
      UNIQUE (sys_nm_en, process_name);
  END IF;
END $$ LANGUAGE plpgsql;

-- 3. 시스템별 용역 목적/과업 내용 마스터
CREATE TABLE IF NOT EXISTS tb_service_purpose (
    purpose_id SERIAL PRIMARY KEY,
    sys_nm_en VARCHAR(30),
    purpose_type VARCHAR(20),
    purpose_text TEXT,
    sort_order INTEGER DEFAULT 0,
    use_yn VARCHAR(1) DEFAULT 'Y'
);

-- [phase2-V018-init-ordering] V018 의 표현식 UNIQUE INDEX 를 INSERT 앞에 선이동 (PG 9.5+ 멱등).
-- V018 (4) 와 동일 식별자/동일 표현식. V018 재진입 시 IF NOT EXISTS 로 무해 PASS.
CREATE UNIQUE INDEX IF NOT EXISTS uq_service_purpose_sys_type_md5
  ON tb_service_purpose (sys_nm_en, purpose_type, md5(purpose_text));

-- [phase2-V018-init-ordering NFR-3-x] UNIQUE 제약·INDEX 등록 게이트 (필수, v1.1 강화).
-- 두 INSERT 가 ON CONFLICT 추론 실패(42P10) 또는 UNIQUE 미등록 상태로 진행되는 회귀를 fast-fail.
-- 트랜잭션 경계 부재의 보강책 — 게이트 통과 못하면 INSERT 진입 자체를 차단.
-- conrelid/contype/tablename 까지 검사 — 이름만 동일한 다른 객체 false PASS 차단.
DO $$
DECLARE
  uq_proc_cnt int;
  uq_purp_cnt int;
BEGIN
  SELECT COUNT(*) INTO uq_proc_cnt
    FROM pg_constraint
   WHERE conname = 'uq_process_master_sys_name'
     AND conrelid = 'public.tb_process_master'::regclass
     AND contype = 'u';
  SELECT COUNT(*) INTO uq_purp_cnt
    FROM pg_indexes
   WHERE schemaname = 'public'
     AND indexname = 'uq_service_purpose_sys_type_md5'
     AND tablename = 'tb_service_purpose';
  IF uq_proc_cnt <> 1 THEN
    RAISE EXCEPTION 'HALT [phase2-V018-init-ordering NFR-3-x]: uq_process_master_sys_name 미등록 또는 정의 불일치 (count=%) — INSERT 진행 차단', uq_proc_cnt;
  END IF;
  IF uq_purp_cnt <> 1 THEN
    RAISE EXCEPTION 'HALT [phase2-V018-init-ordering NFR-3-x]: uq_service_purpose_sys_type_md5 미등록 또는 정의 불일치 (count=%) — INSERT 진행 차단', uq_purp_cnt;
  END IF;
  RAISE NOTICE 'PASS [phase2-V018-init-ordering NFR-3-x]: UNIQUE 제약·INDEX 등록 확인 — INSERT 진행 가능';
END $$ LANGUAGE plpgsql;

-- 기본 공정명 데이터
-- V018 (process-master-dedup 2026-04-20) 이후: UNIQUE(sys_nm_en, process_name) 적용됨
-- [phase2-V018-init-ordering 2026-05-11] phase2 에서 선이동됨 — fresh-init 환경에서도 본 INSERT 의 ON CONFLICT 가 동작.
INSERT INTO tb_process_master (sys_nm_en, process_name, sort_order) VALUES
('UPIS', '도시계획정보체계용 GIS SW 유지관리', 1),
('KRAS', '부동산종합공부시스템용 GIS SW 유지관리', 1),
('IPSS', '지하시설물관리시스템용 GIS SW 유지관리', 1),
('GIS_SW', 'GIS SW 유지관리', 1),
('APIMS', '도로관리시스템용 GIS SW 유지관리', 1)
ON CONFLICT (sys_nm_en, process_name) DO NOTHING;

-- 기본 용역 목적 데이터
-- V018 이후: UNIQUE INDEX uq_service_purpose_sys_type_md5 (sys_nm_en, purpose_type, md5(purpose_text)) 적용됨
-- [phase2-V018-init-ordering 2026-05-11] phase2 에서 선이동됨 — fresh-init 환경에서도 본 INSERT 의 ON CONFLICT 가 동작.
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

-- [inspection-report-d-v5 2026-05-15] 시안D v5 NEXT ROUND + 핵심발견사항 수동입력 컬럼
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS key_findings       TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS recommendation_1   TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS recommendation_2   TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS recommendation_3   TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS followup_1         TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS followup_2         TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS followup_3         TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS next_schedule_note VARCHAR(300);

-- [site-setup-revamp 2026-05-31] 신 site_code 규칙({adm_sect_c}_{sys_nm_en}) 전환에 따른
-- 구 site_code 별칭 컬럼 (예: 'gangjin'). findBySiteCode 미스 시 별칭으로 폴백. 멱등.
ALTER TABLE sw_pjt ADD COLUMN IF NOT EXISTS site_code_alias VARCHAR(64);

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
-- UPIS DBMS (Oracle) 점검결과 — v6 modern (inspect-report-d-v6 Phase 0, 2026-05-17)
-- 기존 OS 수준 17 항목(호스트네임/oslevel/SID/Archive Mode 등)에서 운영 깊이로 교체.
-- 매핑: inspection-poc/agent-windows/checks/db-oracle.ps1 + sql/oracle/*.sql (17 SQL)
-- 근거: docs/product-specs/inspection-report-d-v6.md §6-3 (DBMS), V030 마이그레이션과 동기화.
-- ============================================================
INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) VALUES
('UPIS', 'DBMS', '오라클', 'Archive Log 모드',                'SELECT log_mode FROM v$database',                                     1),
('UPIS', 'DBMS', '오라클', 'Alert Log 에러 (24h)',            'alert_*.log tail -n + grep ORA- (24h)',                               2),
('UPIS', 'DBMS', '오라클', 'Redo Log 그룹',                   'SELECT groups, members, invalid_count FROM v$log',                    3),
('UPIS', 'DBMS', '오라클', 'SGA 구성',                        'SELECT SUM(value)/1024/1024 FROM v$sga',                              4),
('UPIS', 'DBMS', '오라클', 'PGA 사용량',                      'SELECT target_mb, allocated_mb FROM v$pgastat',                       5),
('UPIS', 'DBMS', '오라클', 'Tablespace 사용률',               'SELECT tablespace_name, used_pct FROM dba_tablespace_usage_metrics',  6),
('UPIS', 'DBMS', '오라클', 'Datafile 상태',                   'SELECT total, offline_count FROM dba_data_files',                     7),
('UPIS', 'DBMS', '오라클', 'INVALID 객체 수',                 'SELECT COUNT(*) FROM dba_objects WHERE status=''INVALID''',           8),
('UPIS', 'DBMS', '오라클', '세션 사용률',                     'SELECT active, total, sess_limit FROM v$session + v$resource_limit',  9),
('UPIS', 'DBMS', '오라클', 'Top Wait Events (Idle 제외 top5)', 'SELECT event, total_waits, time_waited FROM v$system_event',         10),
('UPIS', 'DBMS', '오라클', 'RMAN 백업 이력 (7d)',             'SELECT last_job, last_success FROM v$rman_status (7d)',               11),
('UPIS', 'DBMS', '오라클', '마지막 Datapump Export',          '#crontab -l, ls -lt *.dmp',                                           12),
('UPIS', 'DBMS', '오라클', 'Standby Lag (Data Guard)',        'SELECT apply_lag, transport_lag FROM v$dataguard_stats',              13),
('UPIS', 'DBMS', '오라클', '동적 변경 파라미터 수',           'SELECT COUNT(*) FROM v$parameter WHERE ismodified<>''FALSE''',        14),
('UPIS', 'DBMS', '오라클', 'UNDO Tablespace 사용률',          'SELECT tablespace_name, used_pct FROM dba_tablespace_usage_metrics', 15),
('UPIS', 'DBMS', '오라클', 'TEMP Tablespace 사용률',          'SELECT used_pct FROM v$temp_space_header',                           16),
('UPIS', 'DBMS', '오라클', 'Controlfile 다중화',              'SELECT COUNT(*) FROM v$controlfile',                                 17)
ON CONFLICT DO NOTHING;

-- ============================================================
-- UPIS GIS엔진 점검결과 — v6 (inspect-report-d-v6 Phase 0, 2026-05-17)
-- 기존 6항목 (sort 1~6, P9 점검표) + 신규 6항목 (sort 7~12, P10 카드)
-- P10 카드 = UWES Store 총용량 + GSS/GWS 30일 ERROR/WARN/catalina + stdout 크기 + DEM/SLOP 보존
-- 매핑: inspection-poc/agent-windows/checks/gis-*.ps1, manifest.json GIS
-- 근거: docs/product-specs/inspection-report-d-v6.md §3-2 §6-3 §6-4
-- ============================================================
INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) VALUES
-- 기존 6 항목 (sort 1~6 — P9 점검표)
('UPIS', 'GIS', 'GeoNURIS Spatial Server (GSS)', 'GSS 구동확인',                    'ps -ef | grep GSS 실행 확인',                                                          1),
('UPIS', 'GIS', 'GeoNURIS Spatial Server (GSS)', 'GSS 로그파일 삭제',               '/GeoNURIS_Spatial_Server/log 경로의 로그 중 1달 전 파일 삭제',                         2),
('UPIS', 'GIS', 'GeoNURIS Spatial Server (GSS)', 'Desktop Pro 데이터저장소 구동확인', 'Desktop Pro 실행 후 데이터저장소에서 GSS 데이터 불러오기',                            3),
('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'GWS 구동확인',                    '윈도우 서비스 "GeoNURIS GeoWeb Server 64bit" 구동 상태 확인',                          4),
('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'GWS 로그파일 삭제',               'C:\\Program Files\\GeoNURIS_GeoWeb_Server_64\\webapps\\uwes\\store (DEM/SLOP 제외)',    5),
('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'GWS 서비스 확인',                 'http://웹서버IP:8880/uwes 관리자 접속 → WMS → Preview → 지도 표출',                    6),
-- 신규 6 항목 (sort 7~12 — P10 카드, v6 추가)
('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'UWES Store 총용량',               'Get-ChildItem store -Recurse | Measure-Object Length -Sum',                            7),
('UPIS', 'GIS', 'GeoNURIS Spatial Server (GSS)', '30일 ERROR 카운트',               'Select-String -Pattern ''ERROR'' catalina.out (30일 누적)',                            8),
('UPIS', 'GIS', 'GeoNURIS Spatial Server (GSS)', '30일 WARN 카운트',                'Select-String -Pattern ''WARN'' catalina.out (30일 누적)',                             9),
('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'catalina ERROR 카운트',           'Select-String -Pattern ''ERROR'' catalina.out',                                       10),
('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'stdout 로그 크기 (MB)',           'Get-Item geowebservice64-stdout*.log | Measure Length -Sum',                          11),
('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'UWES DEM/SLOP 보존 확인',         'Test-Path store\\DEM, store\\SLOP + Measure 크기',                                    12)
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
('KRAS', 'GIS', 'GeoNURIS Spatial Server (GSS)', 'GSS 구동확인', 'ps –ef | grep –i geo 실행 확인', 1),
('KRAS', 'GIS', 'GeoNURIS Spatial Server (GSS)', 'GSS 상태확인', 'GSS -I aliveness | GSS -I connections', 2),
('KRAS', 'GIS', 'GeoNURIS Spatial Server (GSS)', 'GSS 로그확인', '/kras_home/geonuris/GeoNURIS_Spatail_Server3.6/logs 최신 로그일자 파일 확인 tail –f catalina.out', 3),
('KRAS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)', 'GWS로그확인', '/kras_home/app/MapStudio/log 최신 로그일자 파일 확인', 4),
('KRAS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)', 'GWS로그확인', '/kras_home/app/MapStudio/log 최신 로그일자 파일 확인', 5),
('KRAS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)', 'GWS서비스 확인', 'http://웹서버 IP:9080/msp 로 관리자페이지 접속 | 로그인 > Spatial > 공간데이터 정상표출 확인 | wms, wfs, wfs transaction 정상표출 확인', 6),
('KRAS', 'GIS', '측량성과 프로그램', '부동산종합공부시스템 실행', 'C/S 실행 확인', 7),
('KRAS', 'GIS', 'GeoNURIS Desktop Pro', 'Desktop Pro 구동확인', '바탕화면의 Desktop Pro 실행', 8),
('KRAS', 'GIS', 'GeoNURIS Desktop Pro', 'Map Display 확인', '데이터저장소를 통해 데이터 목록 갱신 | 공간 데이터 표출 확인', 9)
ON CONFLICT DO NOTHING;

-- ============================================================
-- UPIS 표준시스템(APP) — 14 메뉴 (시안D v5 8장 APPLICATION)
-- 칼럼: 대분류(category) | 중분류(item_name) | 점검 내용(item_method) | 결과
-- 카테고리 마스터(check_category_mst.section='APP')는 V027 에서 시드 (서버 재시작 시
-- DELETE 대상이 아니므로 유지). 본 INSERT 는 inspect_template DELETE 직후 재시드를 보장.
-- ============================================================
INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) VALUES
  ('UPIS', 'APP', '도시계획',     '조회/검색',           '도시계획 정보 조회 화면 진입 → 검색 결과 정상 표출 확인',         411),
  ('UPIS', 'APP', '도시계획',     'KRAS 연계',           'KRAS 연계 메뉴 클릭 → 연계 응답 정상 확인',                       412),
  ('UPIS', 'APP', '도시계획',     '토지이용계획확인서',  '토지이용계획확인서 발급 화면 진입 → 발급 정상 확인',              413),
  ('UPIS', 'APP', '도시계획',     '건축물대장',          '건축물대장 조회 → 정상 표출 확인',                                414),
  ('UPIS', 'APP', '도시계획',     '통계',                '통계 메뉴 진입 → 차트/표 정상 표출 확인',                         415),
  ('UPIS', 'APP', '전자심의',     '전자심의',            '전자심의 게시판 진입 → 정상 표출 및 첨부 접근 확인',              421),
  ('UPIS', 'APP', '지구단위계획', '지구단위계획',        '지구단위계획 조회 메뉴 진입 → 결과 정상 표출',                    431),
  ('UPIS', 'APP', '비정형',       '비정형',              '비정형 메뉴 진입 → 정상 응답 및 작성/저장 동작 확인',             441),
  ('UPIS', 'APP', '관리자',       '관리자',              '관리자 로그인 → 관리 메뉴 정상 표출 및 사용자 조회 확인',         451),
  ('UPIS', 'APP', 'GIS 연동',     '지도요청',            '지도 표출 정상 (Tile/WMS 응답 확인)',                             461),
  ('UPIS', 'APP', 'GIS 연동',     '필지이동',            '필지 검색 → 필지 위치로 지도 이동 동작 확인',                     462),
  ('UPIS', 'APP', 'GIS 연동',     '하일라이팅',          '필지 선택 시 하일라이트(강조 표시) 동작 확인',                    463),
  ('UPIS', 'APP', 'GIS 연동',     '필지정보',            '필지 클릭 시 정보 팝업 정상 표출',                                464),
  ('UPIS', 'APP', 'GIS 연동',     '이력정보',            '이력 조회 메뉴 동작 및 결과 정상 표출 확인',                      465)
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

-- [doc-signed-scan-upload] 최종 날인본 스캔 PDF 보관 (additive, nullable) — 2026-06-09
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_path        VARCHAR(500);
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_orig_name   VARCHAR(255);
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_size        BIGINT;
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_uploaded_at TIMESTAMP;
ALTER TABLE tb_document ADD COLUMN IF NOT EXISTS signed_scan_uploaded_by BIGINT REFERENCES users(user_id);
COMMENT ON COLUMN tb_document.signed_scan_path IS '최종 도장 날인본 스캔 PDF 절대경로(파일시스템) — doc-signed-scan-upload (2026-06-09)';

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

-- ============================================================
-- [phase2-tb_ops_doc-forward-ref 2026-05-11] tb_ops_doc 의 forward-reference 해소를 위해
-- 본 두 블록 (tb_work_plan + tb_org_unit) 을 tb_ops_doc 직전으로 선이동. 원본 위치: 본 파일 후반부.
-- schema/seed 의미 변경 0. CREATE TABLE IF NOT EXISTS / IF NOT EXISTS 멱등성으로 운영 영향 0.
-- ============================================================

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

-- [workplan-target-infra-cascade 2026-06-11] 대상=사업(sw_pjt) 기준 + 미계약 지역+시스템 보관 (additive, nullable)
-- 계약 대상은 proj_id(sw_pjt) 로 식별, 미계약/표시·통계는 region_* 로. swdept/sql/V20260611_add_region_to_work_plan.sql 동일.
-- (사업 관련 기본 마스터 = sw_pjt. infra_id 는 레거시 표시용으로만 유지.)
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS proj_id        BIGINT REFERENCES sw_pjt(proj_id);
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS region_code    VARCHAR(10);
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS region_city_nm VARCHAR(40);
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS region_dist_nm VARCHAR(40);
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS target_sys_nm  VARCHAR(100);
CREATE INDEX IF NOT EXISTS idx_work_plan_region_code ON tb_work_plan(region_code);
COMMENT ON COLUMN tb_work_plan.region_code   IS '대상 시군구코드(sigungu_code.adm_sect_c). 계약·미계약 모두 채움 — workplan-target-infra-cascade (2026-06-11)';
COMMENT ON COLUMN tb_work_plan.target_sys_nm IS '대상 시스템명. 계약=Infra.sys_nm 복사, 미계약=직접입력 — workplan-target-infra-cascade (2026-06-11)';

CREATE INDEX IF NOT EXISTS idx_tb_work_plan_infra    ON tb_work_plan(infra_id);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_assignee ON tb_work_plan(assignee_id);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_parent   ON tb_work_plan(parent_plan_id);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_status   ON tb_work_plan(status);
CREATE INDEX IF NOT EXISTS idx_tb_work_plan_start    ON tb_work_plan(start_date);

-- ============================================================
-- 스프린트 5 (2026-04-19): 조직도 + 문서 선택 UI 통일 + 운영/테스트 구분
-- 기획서: docs/product-specs/doc-selector-org-env.md
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

-- ============================================================
-- [ops-fault-support M1] users 조직 매핑 (멱등) — tb_org_unit seed 뒤(FK 참조)
-- 기획서: docs/product-specs/ops-fault-support-improvement.md / 개발계획: docs/exec-plans/ops-fault-support-improvement.md (Step 1)
-- ============================================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS org_unit_id BIGINT;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_users_org_unit') THEN
        ALTER TABLE users ADD CONSTRAINT fk_users_org_unit FOREIGN KEY (org_unit_id)
            REFERENCES tb_org_unit(unit_id) ON DELETE SET NULL;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_users_org_unit ON users(org_unit_id);
-- SW지원팀 엔지니어 매핑 (멱등) — 현재 3명. 그 외 인원 seed 는 후속 ops-org-seed-revise
UPDATE users SET org_unit_id = (SELECT unit_id FROM tb_org_unit WHERE name='SW지원팀' AND unit_type='TEAM')
 WHERE username IN ('박욱진','김한준','서현규') AND org_unit_id IS NULL;

-- ============================================================
-- [ops-fault-support staff] 직원 디렉터리 (조직도 인원 + 직원 요청자). users(로그인 계정)와 분리.
-- 인원 seed 는 db_seed_ops_staff.sql (조직도 41이미지 전사).
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_staff (
    staff_id    BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    position    VARCHAR(50),
    org_unit_id BIGINT REFERENCES tb_org_unit(unit_id) ON DELETE SET NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,   -- 재직 true / 퇴사 false
    tel         VARCHAR(40),
    email       VARCHAR(100),
    sort_order  INTEGER DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_staff_org_unit ON tb_staff(org_unit_id);

-- ============================================================
-- doc-split-ops (2026-04-29): 운영·유지보수 문서 신규 테이블 + 레거시 제거
-- 기획서: docs/product-specs/doc-split-ops.md (v3)
-- 개발계획: docs/exec-plans/doc-split-ops.md (v2)
-- ============================================================

-- [doc-split-ops 안전망] DocumentType 5 종 enum 제거 전 row 부재 보장 (멱등)
DELETE FROM tb_document WHERE doc_type IN ('INSPECT','FAULT','SUPPORT','INSTALL','PATCH');

-- [doc-split-ops] 레거시 점검 테이블 DROP (S1 inspect-comprehensive-redesign 후 deprecated)
DROP TABLE IF EXISTS tb_inspect_checklist CASCADE;
DROP TABLE IF EXISTS tb_inspect_issue CASCADE;

-- 운영·유지보수 문서 (5 종: INSPECT/FAULT/SUPPORT/INSTALL/PATCH)
CREATE TABLE IF NOT EXISTS tb_ops_doc (
    doc_id              BIGSERIAL PRIMARY KEY,
    doc_no              VARCHAR(50) NOT NULL UNIQUE,            -- codex: NOT NULL 강제
    doc_type            VARCHAR(30) NOT NULL,
    sys_type            VARCHAR(20),
    region_code         VARCHAR(10),
    org_unit_id         BIGINT REFERENCES tb_org_unit(unit_id),
    environment         VARCHAR(20),
    support_target_type VARCHAR(20),
    infra_id            BIGINT REFERENCES tb_infra_master(infra_id),
    plan_id             BIGINT REFERENCES tb_work_plan(plan_id),
    title               VARCHAR(500) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    author_id           BIGINT NOT NULL REFERENCES users(user_id),
    approver_id         BIGINT REFERENCES users(user_id),
    approved_at         TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),
    CONSTRAINT ck_tb_ops_doc_type   CHECK (doc_type IN ('INSPECT','FAULT','SUPPORT','INSTALL','PATCH')),
    CONSTRAINT ck_tb_ops_doc_status CHECK (status IN ('DRAFT','COMPLETED')),
    CONSTRAINT ck_tb_ops_doc_env    CHECK (environment IS NULL OR environment IN ('PROD','TEST')),
    CONSTRAINT ck_tb_ops_doc_target CHECK (support_target_type IS NULL OR support_target_type IN ('EXTERNAL','INTERNAL')),
    CONSTRAINT ck_tb_ops_doc_combo  CHECK (
        -- INSPECT: 점검내역서 본 데이터(inspect_report)가 infra 와 직접 연결되지 않으므로
        --         doc_type 만 검사. sys_type 도 nullable (점검 폼이 sys_type 미입력 케이스 있음).
        -- [ops-doc-region-cascade] 4종 모두 시도→시군구→시스템 기반(region_code+sys_type). INSTALL/PATCH 도 region 기반으로 통일.
        doc_type = 'INSPECT' OR
        (doc_type IN ('FAULT','SUPPORT','INSTALL','PATCH') AND region_code IS NOT NULL AND sys_type IS NOT NULL)
    )
);

-- doc-split-ops: 이미 strict 한 CHECK 가 적용된 환경에서도 동일하게 갱신 (멱등)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_tb_ops_doc_combo') THEN
        ALTER TABLE tb_ops_doc DROP CONSTRAINT ck_tb_ops_doc_combo;
    END IF;
    ALTER TABLE tb_ops_doc ADD CONSTRAINT ck_tb_ops_doc_combo CHECK (
        -- [ops-doc-region-cascade] 4종 모두 시도→시군구→시스템 기반(region_code+sys_type). INSTALL/PATCH 도 region 기반으로 통일.
        doc_type = 'INSPECT' OR
        (doc_type IN ('FAULT','SUPPORT','INSTALL','PATCH') AND region_code IS NOT NULL AND sys_type IS NOT NULL)
    );
END $$;

CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_type_status  ON tb_ops_doc(doc_type, status);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_region_sys   ON tb_ops_doc(region_code, sys_type);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_proj         ON tb_ops_doc(plan_id);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_infra        ON tb_ops_doc(infra_id);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_org_unit     ON tb_ops_doc(org_unit_id);

-- ============================================================
-- [ops-fault-support M2/Step 2] 담당 엔지니어 + 요청자(공무원) — 멱등, 제약별 개별 가드
-- requester_contact_id FK 는 P3(tb_partner_contact 생성) 에서 추가. CHECK 는 FAULT/SUPPORT 한정.
-- ============================================================
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS engineer_id          BIGINT;
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS requester_person_id  BIGINT;
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS requester_contact_id BIGINT;  -- FK 는 P3
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS requester_staff_id   BIGINT;  -- [staff] 직원 요청자
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_ops_doc_req_staff') THEN
    ALTER TABLE tb_ops_doc ADD CONSTRAINT fk_ops_doc_req_staff FOREIGN KEY (requester_staff_id) REFERENCES tb_staff(staff_id);
END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_ops_doc_engineer') THEN
    ALTER TABLE tb_ops_doc ADD CONSTRAINT fk_ops_doc_engineer FOREIGN KEY (engineer_id) REFERENCES users(user_id);
END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_ops_doc_req_person') THEN
    ALTER TABLE tb_ops_doc ADD CONSTRAINT fk_ops_doc_req_person FOREIGN KEY (requester_person_id) REFERENCES ps_info(id);
END IF; END $$;

-- ============================================================
-- [ops-support-doc-upload] 업무지원(SUPPORT) 지원문서 단일파일 메타 (additive, nullable) — 2026-06-16
-- 착수계 날인본(tb_document.signed_scan_*) 패턴 미러. 파일 실저장은 D:\swmanager-scan.
-- ============================================================
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_path        VARCHAR(500);
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_orig_name   VARCHAR(255);
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_ext         VARCHAR(10);
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_size        BIGINT;
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_uploaded_at TIMESTAMP;
ALTER TABLE tb_ops_doc ADD COLUMN IF NOT EXISTS support_file_uploaded_by BIGINT;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_ops_doc_support_uploader') THEN
    ALTER TABLE tb_ops_doc ADD CONSTRAINT fk_ops_doc_support_uploader FOREIGN KEY (support_file_uploaded_by) REFERENCES users(user_id);
END IF; END $$;
COMMENT ON COLUMN tb_ops_doc.support_file_path      IS '업무지원 지원문서 절대경로(파일시스템) — ops-support-doc-upload (2026-06-16)';
COMMENT ON COLUMN tb_ops_doc.support_file_orig_name IS '업로드 원본 파일명';
COMMENT ON COLUMN tb_ops_doc.support_file_ext       IS '확장자(hwp/hwpx/xls/xlsx/doc/docx/pdf, 소문자)';
-- [staff] 요청자 3종(공무원/업체담당자/직원) 중 정확히 1 — 기존 제약 DROP 후 재생성
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_ops_doc_req_required') THEN
        ALTER TABLE tb_ops_doc DROP CONSTRAINT ck_ops_doc_req_required;
    END IF;
    ALTER TABLE tb_ops_doc ADD CONSTRAINT ck_ops_doc_req_required
        CHECK ( doc_type NOT IN ('FAULT','SUPPORT')          -- FAULT/SUPPORT 만 요청자 필수
                OR (requester_person_id IS NOT NULL)::int + (requester_contact_id IS NOT NULL)::int
                   + (requester_staff_id IS NOT NULL)::int = 1 ) NOT VALID;
END $$;
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_engineer ON tb_ops_doc(engineer_id);

-- ============================================================
-- [ops-fault-support M2/Step 3] 외부업체 + 담당자 + 문서-협력업체 다대다
-- 순서: tb_partner -> tb_partner_contact -> fk_ops_doc_req_contact -> tb_ops_doc_partner
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_partner (
    partner_id   BIGSERIAL PRIMARY KEY,
    name         VARCHAR(200) NOT NULL,
    partner_type VARCHAR(20),
    biz_no       VARCHAR(20),
    main_tel     VARCHAR(40),
    note         VARCHAR(1000),
    use_yn       VARCHAR(1) NOT NULL DEFAULT 'Y',
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_partner_type CHECK (partner_type IS NULL OR partner_type IN ('사업단','유지보수','DB','SI','기타'))
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_partner_biz_no ON tb_partner(biz_no) WHERE biz_no IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_partner_use ON tb_partner(use_yn);

CREATE TABLE IF NOT EXISTS tb_partner_contact (
    contact_id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL REFERENCES tb_partner(partner_id) ON DELETE RESTRICT,
    name       VARCHAR(100) NOT NULL,
    position   VARCHAR(50),
    tel        VARCHAR(40),
    email      VARCHAR(100),
    use_yn     VARCHAR(1) NOT NULL DEFAULT 'Y',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_partner_contact_partner ON tb_partner_contact(partner_id);

-- P2 에서 미룬 요청자=업체담당자 FK (tb_partner_contact 생성 후)
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_ops_doc_req_contact') THEN
    ALTER TABLE tb_ops_doc ADD CONSTRAINT fk_ops_doc_req_contact
        FOREIGN KEY (requester_contact_id) REFERENCES tb_partner_contact(contact_id);
END IF; END $$;

-- 문서-협력업체 다대다 (역할 라벨, 동일 업체 복수역할 허용)
CREATE TABLE IF NOT EXISTS tb_ops_doc_partner (
    doc_id     BIGINT NOT NULL REFERENCES tb_ops_doc(doc_id) ON DELETE CASCADE,
    partner_id BIGINT NOT NULL REFERENCES tb_partner(partner_id) ON DELETE RESTRICT,
    role_label VARCHAR(50) NOT NULL DEFAULT '',
    PRIMARY KEY (doc_id, partner_id, role_label)
);
CREATE INDEX IF NOT EXISTS idx_ops_doc_partner_partner ON tb_ops_doc_partner(partner_id);

-- ============================================================
-- [ops-fault-support M3/Step 4] 장애/지원 지식베이스(KB) + 채택 피드백
-- 순서: tb_ops_kb -> tb_ops_kb_feedback -> 인덱스. pg_trgm 실패 시 LIKE 폴백(매처).
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_ops_kb (
    kb_id        BIGSERIAL PRIMARY KEY,
    kb_code      VARCHAR(20) NOT NULL UNIQUE,
    gubun        VARCHAR(10),
    sys_type     VARCHAR(20),
    category     VARCHAR(30),
    symptom      VARCHAR(200),
    cause        VARCHAR(200),
    summary      TEXT,
    symptom_desc TEXT,
    cause_desc   TEXT,
    action       TEXT,
    prevention   TEXT,
    keywords     TEXT,
    case_count   INT DEFAULT 0,
    rewritten    BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS tb_ops_kb_feedback (
    feedback_id BIGSERIAL PRIMARY KEY,
    kb_id       BIGINT NOT NULL REFERENCES tb_ops_kb(kb_id) ON DELETE CASCADE,
    doc_id      BIGINT REFERENCES tb_ops_doc(doc_id) ON DELETE SET NULL,
    fb_action   VARCHAR(20),   -- APPLIED / IGNORED
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
-- pg_trgm + GIN (권한 없으면 LIKE 폴백 — 실패해도 init 계속)
DO $$ BEGIN
    CREATE EXTENSION IF NOT EXISTS pg_trgm;
    CREATE INDEX IF NOT EXISTS idx_ops_kb_keywords ON tb_ops_kb USING gin (keywords gin_trgm_ops);
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE '[ops-kb] pg_trgm 미적용 (LIKE 폴백): %', SQLERRM;
END $$;
CREATE INDEX IF NOT EXISTS idx_ops_kb_filter ON tb_ops_kb(gubun, sys_type);

-- [ops-kb-workbench] MANUAL 직접등록 확장 (멱등 + 부분적용 보정)
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS source     VARCHAR(10) DEFAULT 'SEED';
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS created_by VARCHAR(50);
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS status     VARCHAR(10) DEFAULT 'ACTIVE';  -- ACTIVE/DELETED
ALTER TABLE tb_ops_kb ALTER COLUMN source SET DEFAULT 'SEED';
ALTER TABLE tb_ops_kb ALTER COLUMN status SET DEFAULT 'ACTIVE';
UPDATE tb_ops_kb SET source='SEED'   WHERE source IS NULL;
UPDATE tb_ops_kb SET status='ACTIVE' WHERE status IS NULL;
ALTER TABLE tb_ops_kb ALTER COLUMN source SET NOT NULL;
ALTER TABLE tb_ops_kb ALTER COLUMN status SET NOT NULL;
CREATE SEQUENCE IF NOT EXISTS seq_ops_kb_manual START 1;
CREATE INDEX IF NOT EXISTS idx_ops_kb_status_source ON tb_ops_kb(status, source);

-- [ops-kb-approval] 등록 승인 워크플로 (편집권한자 등록 → PENDING → 관리자 승인 → ACTIVE)
-- status 값 확장: ACTIVE / PENDING / REJECTED / DELETED. status 는 VARCHAR(10) (REJECTED=8자, 적합).
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS reviewed_by   VARCHAR(50);
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS reviewed_at   TIMESTAMP;
ALTER TABLE tb_ops_kb ADD COLUMN IF NOT EXISTS reject_reason TEXT;

-- 운영문서 섹션 상세 (jsonb)
CREATE TABLE IF NOT EXISTS tb_ops_doc_detail (
    detail_id    BIGSERIAL PRIMARY KEY,
    doc_id       BIGINT NOT NULL REFERENCES tb_ops_doc(doc_id) ON DELETE CASCADE,
    section_key  VARCHAR(50) NOT NULL,
    section_data JSONB NOT NULL DEFAULT '{}'::jsonb,
    sort_order   INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_detail_doc ON tb_ops_doc_detail(doc_id);

-- 운영문서 변경 이력
CREATE TABLE IF NOT EXISTS tb_ops_doc_history (
    history_id    BIGSERIAL PRIMARY KEY,
    doc_id        BIGINT NOT NULL REFERENCES tb_ops_doc(doc_id) ON DELETE CASCADE,
    action        VARCHAR(30) NOT NULL,
    changed_field VARCHAR(100),
    old_value     TEXT,
    new_value     TEXT,
    actor_id      BIGINT NOT NULL REFERENCES users(user_id),
    comment       TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_history_doc ON tb_ops_doc_history(doc_id);

-- 운영문서 첨부파일
CREATE TABLE IF NOT EXISTS tb_ops_doc_attachment (
    attach_id   BIGSERIAL PRIMARY KEY,
    doc_id      BIGINT NOT NULL REFERENCES tb_ops_doc(doc_id) ON DELETE CASCADE,
    file_name   VARCHAR(300) NOT NULL,
    file_path   VARCHAR(500) NOT NULL,
    file_size   BIGINT,
    mime_type   VARCHAR(100),
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_attachment_doc ON tb_ops_doc_attachment(doc_id);

-- 운영문서 서명 (DB §A 권고 — Base64 PNG 흡수 위해 TEXT 확장)
CREATE TABLE IF NOT EXISTS tb_ops_doc_signature (
    sign_id         BIGSERIAL PRIMARY KEY,
    doc_id          BIGINT NOT NULL REFERENCES tb_ops_doc(doc_id) ON DELETE CASCADE,
    signer_type     VARCHAR(30) NOT NULL,
    signer_name     VARCHAR(50) NOT NULL,
    signer_org      VARCHAR(200),
    signature_image TEXT,
    signed_at       TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_tb_ops_doc_signature_doc ON tb_ops_doc_signature(doc_id);

-- [phase2-tb_ops_doc-forward-ref 2026-05-11] 본 위치의 tb_work_plan + tb_org_unit 블록은 tb_ops_doc 직전으로 선이동됨.

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


-- ============================================================
-- S8 qt-quotation-domain-normalize (2026-04-22):
--   qt_category_mst 초기 시드 3행 (유지보수/용역/제품)
-- ============================================================
INSERT INTO qt_category_mst (category_code, category_label, display_order) VALUES
  ('유지보수', '유지보수', 1),
  ('용역',     '용역',     2),
  ('제품',     '제품',     3)
ON CONFLICT (category_code) DO NOTHING;


-- ============================================================
-- S16 tb-work-plan-decision (2026-04-22):
--   work_plan_type_mst 10행 + work_plan_status_mst 7행 초기 시드
-- ============================================================
INSERT INTO work_plan_type_mst (type_code, type_label, color, display_order) VALUES
  ('CONTRACT',   '계약',     '#1565c0', 1),
  ('INSTALL',    '설치',     '#2e7d32', 2),
  ('PATCH',      '패치',     '#00897b', 3),
  ('INSPECT',    '점검',     '#ff9800', 4),
  ('PRE_CONTACT','사전연락', '#9e9e9e', 5),
  ('FAULT',      '장애처리', '#e74a3b', 6),
  ('SUPPORT',    '업무지원', '#6a1b9a', 7),
  ('SETTLE',     '기성/준공','#5c6bc0', 8),
  ('COMPLETE',   '준공',     '#37474f', 9),
  ('ETC',        '기타',     '#795548',10)
ON CONFLICT (type_code) DO NOTHING;

INSERT INTO work_plan_status_mst (status_code, status_label, display_order) VALUES
  ('PLANNED',     '예정',     1),
  ('CONTACTED',   '연락완료', 2),
  ('CONFIRMED',   '확정',     3),
  ('IN_PROGRESS', '진행중',   4),
  ('COMPLETED',   '완료',     5),
  ('POSTPONED',   '연기',     6),
  ('CANCELLED',   '취소',     7)
ON CONFLICT (status_code) DO NOTHING;

-- ============================================================
-- 점검 QR 배치 (PoC 자동수집 원본 보존 — inspection-qr-batch sprint)
-- 폐쇄망 점검 에이전트 → QR → PWA 스캐너 → SW Manager 업로드 흐름의 마지막 한 단계.
-- payload JSON 을 JSONB 로 원본 보존 + inspect_report DRAFT 자동생성을 연결.
-- ============================================================
CREATE TABLE IF NOT EXISTS inspect_qr_batch (
    id                BIGSERIAL PRIMARY KEY,
    payload_id        VARCHAR(64) NOT NULL UNIQUE,    -- payload.id (멱등 키, 예: "dyg-2026-05")
    report_id         BIGINT REFERENCES inspect_report(id) ON DELETE SET NULL,
    site_code         VARCHAR(32) NOT NULL,           -- payload.site
    inspect_round     VARCHAR(7),                     -- payload.round (예: "2026-05")
    payload_ts        BIGINT,                         -- payload.ts (unix seconds)
    source_inspector  VARCHAR(50),                    -- payload.inspector (이름)
    header_hash       VARCHAR(16),                    -- header.hash (sha1 hex[:6])
    raw_bytes         INT,
    gz_bytes          INT,
    payload_json      JSONB NOT NULL,                 -- 원본 페이로드 전체 보존 (audit)
    hash_check        VARCHAR(10) DEFAULT 'skip',     -- 'ok' | 'warn' | 'skip' (NFR-3 warn-only)
    uploaded_by       BIGINT REFERENCES users(user_id),
    uploaded_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qr_batch_site_round ON inspect_qr_batch(site_code, inspect_round);
CREATE INDEX IF NOT EXISTS idx_qr_batch_report ON inspect_qr_batch(report_id);

-- inspect_report: 자동수집 출처 + 멱등 응답용 batch_id
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS source VARCHAR(20) DEFAULT 'manual';
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS batch_id VARCHAR(64);
CREATE UNIQUE INDEX IF NOT EXISTS uq_inspect_report_batch_id
    ON inspect_report(batch_id) WHERE batch_id IS NOT NULL;

-- sw_pjt: site_code 매핑 컬럼 (payload.site → pjt_id 변환용)
ALTER TABLE sw_pjt ADD COLUMN IF NOT EXISTS site_code VARCHAR(32);
CREATE UNIQUE INDEX IF NOT EXISTS uq_sw_pjt_site_code
    ON sw_pjt(site_code) WHERE site_code IS NOT NULL;

-- inspection-report-d-v6 Phase J: soft delete (V031)
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ DEFAULT NULL;
CREATE INDEX IF NOT EXISTS idx_inspect_report_active
    ON inspect_report (pjt_id, created_at DESC) WHERE deleted_at IS NULL;

-- V029: inspect_metric_snapshot (Phase A)
CREATE TABLE IF NOT EXISTS inspect_metric_snapshot (
    snapshot_id     BIGSERIAL    PRIMARY KEY,
    pjt_id          BIGINT       NOT NULL REFERENCES sw_pjt(proj_id) ON DELETE CASCADE,
    server_role     VARCHAR(16)  NOT NULL,
    host_name       VARCHAR(64)  DEFAULT '',
    host_ip         VARCHAR(45),
    collected_at    TIMESTAMPTZ  NOT NULL,
    cpu_pct         NUMERIC(5,2),
    mem_pct         NUMERIC(5,2),
    disk_pct        NUMERIC(5,2),
    raw_payload     JSONB,
    created_at      TIMESTAMPTZ  DEFAULT now() NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_metric_pjt_role_host_time ON inspect_metric_snapshot (pjt_id, server_role, COALESCE(host_name, ''), collected_at);
CREATE INDEX IF NOT EXISTS idx_metric_pjt_role_time ON inspect_metric_snapshot (pjt_id, server_role, collected_at DESC);

-- ============================================================================
-- [log-management-improvement] access_logs 통계/필터 인덱스 (2026-06-16)
--   - 대시보드 통계(최근 30일 집계)·로그관리 탭 기간필터의 access_time 범위 조회
--   - 메뉴별 TOP / 탭 분리(menu_nm) + 액션별 집계(action_type)
-- ============================================================================
CREATE INDEX IF NOT EXISTS ix_access_logs_time ON access_logs (access_time);
CREATE INDEX IF NOT EXISTS ix_access_logs_menu_action ON access_logs (menu_nm, action_type);
