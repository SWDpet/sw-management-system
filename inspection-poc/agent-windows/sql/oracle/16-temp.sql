-- id: db.oracle.temp
-- label: Oracle TEMP Tablespace 사용률
-- columns: used_pct
-- threshold: warn=80, crit=90
SELECT ROUND(used_percent,1)
  FROM dba_tablespace_usage_metrics
 WHERE tablespace_name = 'TEMP';
