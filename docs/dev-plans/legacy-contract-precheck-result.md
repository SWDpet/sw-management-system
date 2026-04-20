# FR-0 사전검증 결과

- 실행일: Mon Apr 20 15:47:09 KST 2026

## 1. 테이블 존재 확인 (to_regclass)

- tb_contract 존재: false
- tb_contract_target 존재: false

✅ 두 테이블 모두 이미 없음 — DROP 작업 **SKIP 가능**

## 2. 레코드 수

- tb_contract: 0 ✅
- tb_contract_target: 0 ✅

## 3. 외부 FK (대상 테이블을 참조하는 다른 테이블의 FK)

| referencing | constraint_name | fk_column | target |
|---|---|---|---|

총 0행

✅ 외부 FK 없음

## 4. 비-FK 의존성 (pg_depend — view/rule/function 등)

| target_table | deptype | dependent_obj | objid | classid |
|---|---|---|---|---|

총 0행

✅ 비-FK 의존성 없음

---

## 최종 판정

✅ **PASS — DROP 진행 가능**.
