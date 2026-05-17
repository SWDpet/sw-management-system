-- id: db.oracle.rman_backup
-- label: Oracle RMAN 백업 이력 (7d)
-- columns: combined  (last_job|last_success)
-- evaluate: last_success 비어있음 → crit (7일 내 성공 백업 없음)
SELECT TO_CHAR(MAX(end_time),'YYYY-MM-DD HH24:MI:SS') || '|' ||
       TO_CHAR(MAX(CASE WHEN status='COMPLETED' THEN end_time END),'YYYY-MM-DD HH24:MI:SS')
  FROM v$rman_backup_job_details
 WHERE start_time > sysdate - 7;
