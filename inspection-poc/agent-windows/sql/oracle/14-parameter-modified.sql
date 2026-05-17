-- id: db.oracle.param_modified
-- label: Oracle 동적 변경된 파라미터 수
-- columns: count
SELECT COUNT(*) FROM v$parameter WHERE ismodified <> 'FALSE';
