# 개발계획서 — 업무계획 대상 인프라 캐스케이드 + 미계약 대상 (workplan-target-infra-cascade)

- **상태**: v1.1 (codex 검토 반영 — 사용자 승인 대기)
- **작성일**: 2026-06-11 (v1) / v1.1 동일자 (codex 반영: FR-8 API권한·FR-10 표시지점 체크리스트·FR-7 복원계약·FR-11 검증UX·DDL drift)
- **스프린트명**: `workplan-target-infra-cascade`
- **기획서**: `docs/product-specs/workplan-target-infra-cascade.md` (v0.3, commit c298413)

---

## 0. 개요
기획서 FR-1~FR-12 를 구현. 단일 `<select infraId>` → **시도→시군구→시스템 3단 캐스케이드**, 미계약(SUPPORT) 직접입력, `tb_work_plan` region 4필드(서버 재계산·표시 fallback). **기존 `infra_id` 경로는 100% 보존**(회귀 0 목표).

## 1. 재사용 가능한 기존 자산 (신규 최소화)
- `SigunguCodeRepository.findDistinctSidoNm()` / `findBySidoNm(sido)` — 시도·시군구 캐스케이드 **그대로 사용**. (self-행 17개 포함되어 도청/본청 자동 노출 — FR-12)
- `SigunguCode`(admSectC/sidoNm/sggNm) 엔티티 존재.
- `WorkPlanService.saveWorkPlan` 의 infra 연동부(118~123행) — 여기에 region 재계산 추가.
- `WorkPlanController.addFormAttributes`(299행) — 모델에 sido 목록 추가.

## 2. 작업 분해 (T-n)

### T-1 (DB) `tb_work_plan` additive 4컬럼
- `db_init_phase2.sql` 의 tb_work_plan 블록 + `swdept/sql/V20260611_add_region_to_work_plan.sql`(신규), 멱등 `IF NOT EXISTS`.
```sql
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS region_code     VARCHAR(10);
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS region_city_nm  VARCHAR(40);
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS region_dist_nm  VARCHAR(40);
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS target_sys_nm   VARCHAR(100);
CREATE INDEX IF NOT EXISTS idx_work_plan_region_code ON tb_work_plan(region_code);
```
- FK 미설정(기획 §8-1, Document 와 제약 방식 다름 — 앱 검증 FR-11).
- **(codex) drift 방지**: `db_init_phase2.sql`(부팅 DbInitRunner 적용)와 `swdept/sql/V20260611_*.sql` 는 **동일 DDL**. 적용 후 검증 쿼리로 확인:
  ```sql
  SELECT column_name FROM information_schema.columns
   WHERE table_name='tb_work_plan' AND column_name LIKE 'region_%' OR column_name='target_sys_nm';
  -- 4개 컬럼 + idx_work_plan_region_code 존재 확인
  ```

### T-2 (Entity) `WorkPlan.java`
- 4필드 + `@Column` 매핑 추가(`regionCode`/`regionCityNm`/`regionDistNm`/`targetSysNm`). nullable. 기존 `infra` 관계 유지.

### T-3 (DTO) `WorkPlanDTO.java`
- 4필드 추가. `fromEntity`(49행)에서 채움.
- **표시 fallback(FR-10)**: 라벨 헬퍼 `targetLabel()` 신설 —
  `infra != null` → `cityNm distNm - sysNm`, 아니면 `regionCityNm regionDistNm - targetSysNm`. null-safe.
- 캘린더/상세에서 이 헬퍼 사용.

### T-4 (캐스케이드 API) `WorkPlanController`
- `GET /workplan/api/sgg?sido=` → `findBySidoNm` → `[{admSectC, sggNm}]` (JSON).
- `GET /workplan/api/infra-by-region?admSectC=` → `sigungu` 에서 `sido/sgg` 해석 → **이름 매칭**으로 Infra 조회 → `[{infraId, sysNm}]`.
  - 신규 repo: `InfraRepository.findByCityNmAndDistNm(cityNm, distNm)` (value=infra_id, label=sys_nm).
- 시도 목록 = `addFormAttributes` 에 `sidoList = findDistinctSidoNm()` 추가.
- **(codex) 권한 명시(FR-8)**: 두 조회 API 진입 시 `getAuth()` 확인 → **`NONE` 이면 403/빈배열 차단**. (기존 `/api/detail/{id}` 227행이 권한체크 없이 DTO 반환하는 패턴을 답습하지 말 것.) 저장/상태변경은 기존대로 `EDIT`.

### T-5 (Service) `WorkPlanService.saveWorkPlan` — 핵심 정합
infra 연동부(118~123행)를 region 재계산 포함으로 확장:
1. **계약(infra_id 있음)**: `infra` 로드 → `region_city_nm=infra.cityNm`, `region_dist_nm=infra.distNm`, `target_sys_nm=infra.sysNm`, `region_code=` (cityNm,distNm)→sigungu 조회(정규화). **클라이언트 region 입력 무시(서버 재계산, FR-9b)**.
2. **미계약(infra_id 없음 + region 입력 있음)**: FR-9c-① 게이트 — `planType != SUPPORT` 면 거부(IllegalState). `region_code` 존재검증(FR-11), `target_sys_nm` trim·1~100자·제어/HTML 정제(빈값이면 거부). region 4필드 저장, `infra=null`.
3. **대상 없음(둘 다 없음)**: `infra=null` + region 4필드 **전부 null**(FR-9c-②, planType 무관).
- **지역해석 헬퍼**(신규 `resolveRegion`): (cityNm,distNm)→`adm_sect_c`. 정규화 규칙(§5-4): 괄호 접미사 제거, `도청`/`본청`→해당 시도 self-행(`sgg_name=sido_name`), 군위군→대구 alias. `SigunguCodeRepository` 에 `findBySidoNmAndSggNm` 추가.
- **(codex) 검증 실패 UX(FR-11)**: 위 게이트/검증 실패(`IllegalArgumentException`/`IllegalState`)는 컨트롤러 `save`(235행)에서 **try/catch → `redirect:/workplan/new`(or edit) + flash errorMessage + 입력값 유지**. 절대 500 노출 금지. (현재 `save` 는 예외 미처리 → 본 작업에서 추가.)

### T-6 (Form UI) `workplan-form.html` — 🎨 토큰 재사용
- 184~193행 단일 select 제거 → **시도/시군구/시스템 3 select** + (SUPPORT 시) 시스템명 직접입력 input.
- JS: ① 시도 change→`/api/sgg` ② 시군구 change→`/api/infra-by-region` ③ 시스템 "목록에 없음" → input 토글 ④ planType change→SUPPORT 외면 "목록에 없음" 옵션·input 숨김(FR-6) ⑤ **수정 진입 복원**(FR-7): `infraId` 있으면 그 인프라의 시도/시군구/시스템, 없고 `regionCode` 있으면 그 지역+직접입력값으로 초기 선택.
- **(codex) FR-7 복원 데이터 계약(확정)**: 별도 화면상태 필드 신설 없이 **DTO 기존/신규 필드로 복원**한다 — 계약 대상=`cityNm/distNm/sysNm`+`infraId`(fromEntity 가 infra 에서 채움), 미계약=`regionCityNm/regionDistNm/targetSysNm`+`regionCode`. JS 는 이 값으로 **캐스케이드 API 를 순차 재호출**해 select 를 채우고 마지막 값을 선택(미계약이면 직접입력 모드). → FR-7 테스트는 두 케이스(infraId / regionCode) DTO 입력 → 복원 결과로 검증.
- 기존 `.form-table select` 스타일 상속(신규 토큰/`:root` 없음 → 다크모드·specificity 무위험, 기획 §6-2).

### T-7 (표시 회귀 차단) FR-10 — **단일 `targetLabel()` 적용 체크리스트**(codex)
모든 대상 표기 지점을 `targetLabel()`(시군구 - 시스템, 미계약 fallback 포함) 하나로 통일:
- [ ] 캘린더 이벤트 `extendedProps.infraName`(`WorkPlanController` 178행) — 현재 시도/시군구만·**시스템명 누락** → `targetLabel()` 로 교체(시스템 포함).
- [ ] 업무 상세 Ajax(`/api/detail/{id}` 227행)가 반환하는 DTO 표시 — 화면 측에서 `targetLabel` 사용.
- [ ] 캘린더 상세 모달(이벤트 클릭 팝업)의 대상 표기.
- [ ] `process-status.html`(152행) — `cityNm/distNm` 직접 참조 → DTO 라벨로 교체.
- [ ] 기타: grep `getCityNm`/`getDistNm`/`getSysNm` 표시 사용처 전수 점검 후 동일 교체.

### T-8 (테스트) 기획 §11
- `WorkPlanServiceTest`(신규/보강): SUPPORT+직접입력 저장 / 비-SUPPORT 직접입력 거부 / 대상없음 저장 / infra 선택 시 region 서버 재계산 / `resolveRegion` 정규화(고성군·도청·본청·군위군) / `target_sys_nm` 검증.
- DTO `targetLabel` fallback 단위테스트(infraId / regionCode 두 케이스 — FR-7/FR-10).
- 컨트롤러: 검증 실패 시 **redirect+flash error**(500 아님, FR-11), 캐스케이드 조회 API **`NONE` 권한 차단**(FR-8).
- 캐스케이드 API 슬라이스(선택): 시군구·infra-by-region JSON.

## 3. 구현 순서
T-1(DB) → T-2(Entity) → T-3(DTO) → T-4(API)+T-5(Service) → T-6(UI) → T-7(회귀) → T-8(Test) → codex 구현검토.

## 4. 회귀 / 롤백
- **회귀 표면**: ① 기존 infra_id 저장/표시 ② 캘린더/목록/상세 라벨 ③ 폼 등록·수정. T-7 로 표시 fallback 보강해 미계약 빈칸 방지.
- **롤백**: 코드 revert + `tb_work_plan` 신규컬럼은 nullable additive 라 잔존해도 무해(드롭 불요). UI/Service 만 되돌리면 기존 동작 복귀.

## 5. 마이그레이션 주의
- 운영 반영 시 `db_init_phase2.sql` 멱등 ALTER 가 부팅 시 적용(DbInitRunner). `swdept/sql` V스크립트와 내용 일치.
- 기존 행 region 4필드 NULL → 표시 fallback 이 infra 기준이라 영향 없음.

## 6. 미결(구현 중 확정)
- `resolveRegion` 정규화에서 이름 미매칭 잔여(고성군 동명·군위군) 매핑 테이블을 코드 상수로 둘지 보조 alias 컬럼으로 둘지 — 소량(8건)이라 **코드 상수 맵**으로 시작.
