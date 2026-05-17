-- id: db.oracle.pga
-- label: Oracle PGA 사용량
-- columns: combined  (target_mb|allocated_mb)
-- threshold: warn=80, crit=90 (used_pct = allocated/target*100)
SELECT ROUND(MAX(DECODE(name,'aggregate PGA target parameter',value))/1048576) || '|' ||
       ROUND(MAX(DECODE(name,'total PGA allocated',value))/1048576)
  FROM v$pgastat;
