-- V011: 견적서 작성자를 userid에서 실명(username)으로 변환
-- qt_quotation 테이블
UPDATE qt_quotation q
SET created_by = u.username
FROM users u
WHERE q.created_by = u.userid
  AND u.username IS NOT NULL
  AND q.created_by != u.username;

-- qt_quotation_ledger 테이블
UPDATE qt_quotation_ledger l
SET created_by = u.username
FROM users u
WHERE l.created_by = u.userid
  AND u.username IS NOT NULL
  AND l.created_by != u.username;
