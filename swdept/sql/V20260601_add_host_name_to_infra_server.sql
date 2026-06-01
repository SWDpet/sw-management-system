-- V20260601 — tb_infra_server.host_name 컬럼 신설
-- sprint: inspect-infra-diff-alert (점검내역서 인프라값 ↔ 현장 수집값 차이 알림)
-- 근거: 기획서 v1.4 §7-1 (B안) / 개발계획서 v3 Step 1
-- 성격: additive, nullable, 기존 row 무영향 (위험도 0). 매칭 키(server_role, host_name) + 비교 대상.

ALTER TABLE tb_infra_server ADD COLUMN IF NOT EXISTS host_name VARCHAR(64);

COMMENT ON COLUMN tb_infra_server.host_name IS '인프라 호스트명 — 현장 스냅샷(InspectMetricSnapshot.host_name) 매칭/비교 키 (inspect-infra-diff-alert sprint, 2026-06-01)';
