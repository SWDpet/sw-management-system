-- id: db.oracle.sessions
-- label: Oracle 세션 사용률
-- columns: active, total, sess_limit
-- threshold: warn=70, crit=90 (used_pct = total/sess_limit*100)
SELECT
  (SELECT COUNT(*) FROM v$session WHERE status='ACTIVE') || '|' ||
  (SELECT COUNT(*) FROM v$session)                       || '|' ||
  (SELECT value    FROM v$parameter WHERE name='sessions')
FROM dual;
