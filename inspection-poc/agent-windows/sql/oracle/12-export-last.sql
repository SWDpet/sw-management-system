-- id: db.oracle.export_last  (Oracle 11.2 호환 — dba_scheduler_job_run_details 사용)
SELECT TO_CHAR(MAX(actual_start_date),'YYYY-MM-DD HH24:MI:SS') FROM dba_scheduler_job_run_details WHERE REGEXP_LIKE(job_name,'EXP|DUMP','i');
