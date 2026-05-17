-- id: db.oracle.archive_mode
-- label: Oracle Archive Log 모드
-- columns: log_mode
-- evaluate: ARCHIVELOG → ok / NOARCHIVELOG → crit / 그 외 → warn
SELECT log_mode FROM v$database;
