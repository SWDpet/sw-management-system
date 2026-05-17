-- id: db.oracle.controlfile
-- label: Oracle Controlfile 다중화
-- columns: count
-- evaluate: count < 2 → warn (단일 controlfile)
SELECT COUNT(*) FROM v$controlfile;
