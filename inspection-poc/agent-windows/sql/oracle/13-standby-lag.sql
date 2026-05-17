-- id: db.oracle.standby_lag
-- label: Oracle Standby Lag (Data Guard)
-- columns: combined  (apply_lag|transport_lag)
-- evaluate: 둘 다 비어있음 → n/a (DG 환경 아님)
SELECT MAX(DECODE(name,'apply lag',value)) || '|' ||
       MAX(DECODE(name,'transport lag',value))
  FROM v$dataguard_stats;
