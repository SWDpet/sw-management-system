-- V004: 기존 시드 패턴 초기화
-- 기존 분석 기반 137개 패턴을 삭제하고, 사용자가 직접 패턴을 등록할 수 있도록 합니다.
-- 이 SQL은 선택적으로 실행합니다. (웹 UI의 '전체 초기화' 버튼으로도 동일한 작업 가능)

TRUNCATE TABLE qt_product_pattern RESTART IDENTITY;
