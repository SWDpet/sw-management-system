-- V20260601 rollback — tb_infra_server.host_name 제거
-- ⚠ 데이터 손실성 롤백: 사용자가 입력한 host_name 값이 함께 삭제됨.
--    운영 롤백 전 백업 권고:  SELECT server_id, host_name FROM tb_infra_server WHERE host_name IS NOT NULL;

ALTER TABLE tb_infra_server DROP COLUMN IF EXISTS host_name;
