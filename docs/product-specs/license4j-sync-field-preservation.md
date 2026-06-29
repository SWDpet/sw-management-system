# 기획서 — License4J 연동: 사용자 큐레이션 필드 보존 (field-preservation)

> 상태: **구현 완료 (codex 검토=방향승인+구현지침, 결정 확정, 단위테스트 통과)** · 2026-06-29
> 구현: LicenseRegistryService.copyAllFields→copyUpdatableFields(보존 10필드 제외) + PRESERVED_USER_FIELDS 상수. classifyUpsert UPDATE 경로가 호출. 수동 업로드·월간 연동 공유. 테스트 preservesUserCuratedFieldsOnUpdate 통과.
> 선행: license4j-monthly-sync(구현·커밋 `b397cb2`). 본 건은 그 후속 보완.

---

## 1. 배경 (사고에서 출발)

license4j-monthly-sync 첫 실연동에서 **기존 대장의 `license_string`이 과거 CSV 업로드 때 다 잘려 있어** 거의 모든 행(1531건)이 "변경됨"으로 판정 → `copyAllFields`가 **전 필드를 Derby 원본으로 덮어씀** → 사용자가 한글화한 등록자/회사/지역 등 **1355행 중 ~950행의 한글이 로마자로 되돌아감**. 02:00 백업으로 복원 완료.

근본 원인: 갱신 시 **사용자 큐레이션 필드까지 무차별 덮어쓰기**. 사용자는 "한글화 덮어쓰기 허용"을 승인했으나 갱신이 드물 것이라는 전제였고, 실제로는 사실상 전건 갱신이라 대량 손실.

---

## 2. 목적

연동 **갱신** 시, **사용자가 수기로 큐레이션(한글화)하는 필드는 보존**하고, **License4J가 권위를 갖는 기술 필드만 갱신**한다. 이로써 매월 연동을 켜도 한글 작업이 보존된다.

---

## 3. 필드 정책 (확정 필요)

### 보존(P) — 사용자 큐레이션, 갱신 시 덮어쓰지 않음
대장의 등록자/연락처/기관 정보 = License4J "user*" 등록 필드:
`registered_to, full_name, company, country, email, telephone, fax, street, city, zip_code`

### 갱신(U) — License4J 권위, 갱신
나머지 전부: `license_string, hardware_id, license_type, valid_product_edition/version, validity_period, maintenance_period, generation_source, generation_date_time, quantity, allowed_use_count` + 모든 activation/floating/ntp/web/custom-features/date-time-check 등 기술 파라미터.

> **핵심 키(불변)**: license_id, product_id(중복키). **시스템 필드**: upload_date/uploaded_by(갱신 시각 기록).

### 신규(NEW) 라이선스
보존 정책 **미적용** — 전 필드 Derby 값으로 신규 등록(사용자가 이후 한글화). 보존은 **기존 행 갱신 시에만**.

### 보존 방식 (택1, 기본=B1)
- **B1 (단순·안전, 권장)**: 보존 필드는 갱신 시 **항상 기존값 유지**(Derby값 무시).
- B2 (fill-if-blank): 보존 필드가 기존에 **비어있으면** Derby값으로 채우고, 값이 있으면 유지.

---

## 4. 적용 범위 — 수동 업로드도 동일

`classifyUpsert`/`copyAllFields`는 **수동 CSV 업로드와 월간 연동이 공유**한다. 본 정책을 공유 코어에 넣으면 **수동 재업로드도 한글 보존**(동일 사고 방지). 단, 이는 기존 수동 업로드의 "전 필드 덮어쓰기" 동작을 **의도적으로 변경**함(개선). 기존 회귀 테스트 영향 점검 필요.

---

## 5. 변경 판정 (불변)
기존 그대로: 기존 행 + (`hardware_id` 또는 `license_string` 변경) → UPDATED(단, **보존 필드 제외 갱신**) / 완전 동일 → DUPLICATE / 없음 → NEW.
- 주의: 보존 필드만 다르고 hardware_id·license_string 동일하면 → DUPLICATE(갱신 안 함). 의도대로.

---

## 6. 요구사항

### FR
- **FR-1** 갱신 시 보존(P) 필드는 기존값을 유지하고, 갱신(U) 필드만 Derby값으로 갱신한다.
- **FR-2** 신규 행은 전 필드 Derby값으로 등록(보존 미적용).
- **FR-3** 변경 판정(hardware_id/license_string)·중복키(license_id+product_id)는 기존과 동일.
- **FR-4** 수동 CSV 업로드도 동일 정책 적용(공유 코어).

### NFR
- **NFR-1** 보존 필드 목록은 한 곳(상수)에서 관리(휴먼 오류 방지).
- **NFR-2** 기존 수동 업로드의 신규/중복 카운트 의미 유지(보존은 "갱신 내용"에만 영향).

### T
- **T-1** 기존 행, license_string만 변경 → UPDATED, license_string 갱신되고 company/registered_to(한글) **보존**.
- **T-2** 신규 행 → 전 필드 Derby값.
- **T-3** 보존 필드만 다르고 기술 필드 동일 → DUPLICATE(갱신 안 함).
- **T-4** B1/B2 정책대로 동작(채택안 기준).
- **T-5** 수동 업로드 회귀: 신규/중복 카운트 동일, 갱신 시 한글 보존.

---

## 7. 승인 요청 (2026-06-29 확정)
- [x] 보존 필드 집합(§3-P) = **사용자 필드 10개 전체**(registered_to, full_name, company, country, email, telephone, fax, street, city, zip_code)
- [x] 보존 방식 = **B1(항상 기존값 유지)**
- [x] 수동 업로드도 **동일 적용**(§4)
