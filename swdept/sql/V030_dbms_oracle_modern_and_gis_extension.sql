-- ============================================================
-- V030: inspect-report-d-v6 Phase 0 — DBMS Oracle 운영 깊이 재시드 + GIS sort 7~12 확장
-- Sprint: inspection-report-d-v6 (2026-05-17)
-- 근거: docs/product-specs/inspection-report-d-v6.md §6-3 (매핑 표)
--       inspection-poc/agent-windows/manifest.json (DBMS 17 + GIS 12)
--       inspection-poc/agent-windows/checks/db-oracle.ps1 + sql/oracle/*.sql (17 SQL)
--
-- 변경 사유:
--   1. DBMS 17 — 기존 시드(호스트네임/oslevel/SID/Archive Mode 등 OS 수준)는 점검 깊이 부족.
--      db-oracle.ps1 의 운영 점검(archive_mode/RMAN/Data Guard/UNDO/Wait Events 등)으로 교체.
--   2. GIS sort 7~12 신설 — P10 카드(GSS/GWS/UWES Store) 데이터용. 자동수집 5건 + UWES Store 1건.
--
-- 멱등성: DELETE-then-INSERT (UPIS+DBMS) + INSERT ... ON CONFLICT DO NOTHING (GIS 추가)
-- 영향: inspect_check_result 의 기존 row 는 그대로 (sort_order 기반 매핑이므로 행 수만 동일하면 OK).
-- ============================================================
BEGIN;

-- ═══════════════════════════════════════════════════════════
-- Phase 1: DBMS 17 항목 재시드 (DELETE + INSERT)
-- ═══════════════════════════════════════════════════════════
DELETE FROM inspect_template WHERE template_type='UPIS' AND section='DBMS';

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
  ('UPIS', 'DBMS', '오라클', 'UNDO Tablespace 사용률',          'SELECT tablespace_name, used_pct FROM dba_tablespace_usage_metrics',  15),
  ('UPIS', 'DBMS', '오라클', 'TEMP Tablespace 사용률',          'SELECT used_pct FROM v$temp_space_header',                            16),
  ('UPIS', 'DBMS', '오라클', 'Controlfile 다중화',              'SELECT COUNT(*) FROM v$controlfile',                                  17)
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════
-- Phase 2: GIS 항목 12로 확장 (기존 6 + 신규 6)
-- 기존 sort 1~6 유지 + sort 7~12 신규
-- ═══════════════════════════════════════════════════════════
INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) VALUES
  ('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'UWES Store 총용량',           'Get-ChildItem store -Recurse | Measure-Object Length -Sum',          7),
  ('UPIS', 'GIS', 'GeoNURIS Spatial Server (GSS)', '30일 ERROR 카운트',           'Select-String -Pattern ''ERROR'' catalina.out (30일 누적)',          8),
  ('UPIS', 'GIS', 'GeoNURIS Spatial Server (GSS)', '30일 WARN 카운트',            'Select-String -Pattern ''WARN'' catalina.out (30일 누적)',           9),
  ('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'catalina ERROR 카운트',       'Select-String -Pattern ''ERROR'' catalina.out',                     10),
  ('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'stdout 로그 크기 (MB)',       'Get-Item geowebservice64-stdout*.log | Measure Length -Sum',        11),
  ('UPIS', 'GIS', 'GeoNURIS GeoWeb Server (GWS)',  'UWES DEM/SLOP 보존 확인',     'Test-Path store\DEM, store\SLOP + Measure 크기',                    12)
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════
-- Phase 3: 검증 (V022/V028 패턴)
-- ═══════════════════════════════════════════════════════════
DO $$
DECLARE
  dbms_cnt int; gis_cnt int; ap_cnt int; db_cnt int; app_cnt int;
BEGIN
  SELECT COUNT(*) INTO dbms_cnt FROM inspect_template WHERE template_type='UPIS' AND section='DBMS';
  IF dbms_cnt <> 17 THEN RAISE EXCEPTION 'HALT V030 Phase 1: DBMS count=% (need 17)', dbms_cnt; END IF;

  SELECT COUNT(*) INTO gis_cnt FROM inspect_template WHERE template_type='UPIS' AND section='GIS';
  IF gis_cnt <> 12 THEN RAISE EXCEPTION 'HALT V030 Phase 2: GIS count=% (need 12)', gis_cnt; END IF;

  -- 다른 섹션 영향 없음 확인 (regression guard)
  SELECT COUNT(*) INTO ap_cnt  FROM inspect_template WHERE template_type='UPIS' AND section='AP';
  SELECT COUNT(*) INTO db_cnt  FROM inspect_template WHERE template_type='UPIS' AND section='DB';
  SELECT COUNT(*) INTO app_cnt FROM inspect_template WHERE template_type='UPIS' AND section='APP';
  IF ap_cnt <> 14  THEN RAISE EXCEPTION 'HALT V030 regression: AP count=% (need 14, 본 마이그 비범위)', ap_cnt; END IF;
  IF db_cnt <> 24  THEN RAISE EXCEPTION 'HALT V030 regression: DB count=% (need 24)', db_cnt; END IF;
  IF app_cnt <> 14 THEN RAISE EXCEPTION 'HALT V030 regression: APP count=% (need 14)', app_cnt; END IF;

  RAISE NOTICE 'PASS V030: DBMS=17 modern, GIS=12 (6+6 extension), regressions OK (AP=14, DB=24, APP=14)';
END $$ LANGUAGE plpgsql;

COMMIT;
