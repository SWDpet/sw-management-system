-- id: db.oracle.undo
-- label: Oracle UNDO Tablespace 사용률
-- columns: combined  (tablespace_name|used_pct)
-- threshold: warn=80, crit=90
SELECT MAX(tablespace_name) || '|' || ROUND(MAX(used_percent),1)
  FROM dba_tablespace_usage_metrics
 WHERE tablespace_name LIKE 'UNDOTBS%';
