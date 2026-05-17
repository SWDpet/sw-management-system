-- id: db.oracle.wait_events
-- label: Oracle Top Wait Events (Idle 제외 상위 5)
-- columns: event, total_waits, time_waited
SELECT event || '|' || total_waits || '|' || time_waited
  FROM (
    SELECT event, total_waits, time_waited
      FROM v$system_event
     WHERE wait_class <> 'Idle'
     ORDER BY time_waited DESC
  )
 WHERE ROWNUM <= 5;
