-- id: db.oracle.invalid_objects
-- label: Oracle INVALID 객체 수
-- columns: count
-- threshold: warn=1, crit=50
SELECT COUNT(*) FROM dba_objects WHERE status='INVALID';
