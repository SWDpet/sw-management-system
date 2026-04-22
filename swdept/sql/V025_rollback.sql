-- V025 Rollback: pjt_equip 테이블 복원 (참고용)
-- 실측 row=0 이므로 데이터 복원은 없음. 구조만 복원.
BEGIN;

CREATE TABLE IF NOT EXISTS pjt_equip (
  equip_id    BIGSERIAL PRIMARY KEY,
  proj_id     BIGINT NOT NULL,
  equip_type  VARCHAR(20) NOT NULL,
  equip_name  VARCHAR(200) NOT NULL,
  spec        VARCHAR(500),
  unit_price  BIGINT,
  quantity    INTEGER,
  remark      VARCHAR(500),
  sort_order  INTEGER,
  reg_dt      TIMESTAMP
);

COMMIT;
