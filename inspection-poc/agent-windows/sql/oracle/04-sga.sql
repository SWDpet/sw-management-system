-- id: db.oracle.sga
-- label: Oracle SGA 구성 (총 메모리)
-- columns: total_mb
SELECT ROUND(SUM(value)/1048576) FROM v$sga;
