-- id: db.oracle.datafile_status
-- label: Oracle Datafile 상태
-- columns: combined  (total|offline)
-- evaluate: offline > 0 → crit
SELECT COUNT(*) || '|' ||
       SUM(CASE WHEN status<>'AVAILABLE' THEN 1 ELSE 0 END)
  FROM dba_data_files;
