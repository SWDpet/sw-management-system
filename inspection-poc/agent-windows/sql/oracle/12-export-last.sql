-- id: db.oracle.export_last
-- label: Oracle 마지막 Datapump Export
-- columns: last_export
-- evaluate: 결과 비어있음 → warn (export 이력 없음)
SELECT TO_CHAR(MAX(start_time),'YYYY-MM-DD HH24:MI:SS') FROM dba_datapump_jobs;
