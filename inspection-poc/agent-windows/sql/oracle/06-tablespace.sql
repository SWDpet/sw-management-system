-- id: db.oracle.tablespace
-- label: Oracle Tablespace 사용률
-- columns: tablespace_name, used_pct
-- threshold: warn=80, crit=90
-- evaluate: max(used_pct) 기준
SELECT tablespace_name, ROUND(used_percent, 1)
  FROM dba_tablespace_usage_metrics
 ORDER BY used_percent DESC;
