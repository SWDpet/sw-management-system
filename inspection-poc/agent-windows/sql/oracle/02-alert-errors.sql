-- id: db.oracle.alert_errors_24h
-- label: Oracle Alert Log 에러 (24h)
-- columns: error_count
-- threshold: warn=1, crit=10
SELECT COUNT(*) FROM v$diag_alert_ext
 WHERE message_level <= 8
   AND originating_timestamp > systimestamp - 1;
