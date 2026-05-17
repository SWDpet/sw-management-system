-- id: db.oracle.redo_logs
-- label: Oracle Redo Log 그룹
-- columns: combined  (groups|members|invalid_status_count)
-- evaluate: invalid_status > 0 → warn
SELECT COUNT(DISTINCT group#) || '|' ||
       SUM(members)           || '|' ||
       SUM(CASE WHEN status NOT IN ('CURRENT','ACTIVE','INACTIVE') THEN 1 ELSE 0 END)
  FROM v$log;
