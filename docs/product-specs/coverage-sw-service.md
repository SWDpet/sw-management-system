# [기획서] SwService 커버리지 상향 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-26
- **트랙**: beyond-A 커버리지 — 컨트롤러 계층 마무리 후 서비스 잔여 핫스팟. SwService(사업 검색/CRUD).
- **상태**: ✅ **완료(2026-06-26)**. SwServiceTest 13건. SwService LINE 23%→**98.4%**. dual-review 합의 반영(spec 단언 강화·필드별 Path mock). 듀얼푸시.

---

## 1. 배경 / 목표

`SwService`(181줄)는 현재 LINE **23%**(미커버 약 47줄). 기존 테스트는 ① `SwServiceNormalizeTest`(normalize/parseYearSafe/normalizeUpper 순수유틸 커버) ② `SwServiceSearchTest`(@SpringBootTest **DB-gated** — RUN_DB_TESTS 미설정 시 skip). 따라서 기본 CI 에서 search/getList/getProject/save/delete 위임 + **buildSearchSpec Specification 람다**(검색 조건 조립, 미커버 대부분)가 비커버.

**목표**: mock 기반 단위테스트로 위 경로를 실DB 없이 커버. **프로덕션 코드 변경 0(순수 테스트만)**.

---

## 2. 대상 / 접근

| 대상 | 커버 방법 |
|---|---|
| getList(pageable) | swProjectRepository.findAll(pageable) mock → 위임 verify |
| search(kw,year,city,district,sysNmEn,pageable) | (a) 전 파라미터 null/blank → findAll(pageable). (b) 필터 있으면 findAll(Specification, pageable). city=null 시 district 무시(FR-10). year String 파싱. |
| search(keyword, pageable) 오버로드 | 5-인자 위임 |
| getProject/save/delete | repository 위임 verify |
| **buildSearchSpec 람다** | search() 가 `findAll(spec, pageable)` 호출 → **ArgumentCaptor<Specification> 로 spec 캡처 → spec.toPredicate(mockRoot, mockQuery, mockCb) 직접 호출**해 람다 본문 실행. CriteriaBuilder/Root/Predicate/Path/Expression mock. contStatMstRepository.findAll() (spec 빌드 시점 호출) mock. kw 숫자/비숫자·stat 코드매칭 분기 포함 |

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | getList·search(양 오버로드)·getProject·save·delete 위임 커버(mock verify). |
| FR-2 | search 전체 null → findAll(pageable) 단축 경로 / 필터 존재 → findAll(spec,pageable) 분기 커버. |
| FR-3 | buildSearchSpec 람다 본문 실행: year/city/district/sysNmEn equal, kw OR LIKE(다컬럼). **분기 명시(codex)**: kw 숫자 파싱 **성공/실패 양쪽**(L106-110), stat 매칭 **있음(code OR 추가)/없음/nm==null 무시**(L119-124). ⚠**city=null→district 무시는 buildSearchSpec 아닌 search() 정규화(L61-62) 책임** → "search() 경유 시 distN=null" 로 검증(private 직접호출 아님). |
| FR-4 | 프로덕션 코드 변경 0. 기존 SwServiceNormalizeTest·SwServiceSearchTest 불변. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile 성공. |
| NFR-2 | 신규 SwServiceTest green. 기존 Sw 서비스 테스트 불변 green. |
| NFR-3 | 전체 `./mvnw -o clean verify` green. |
| NFR-4 | SwService·전역 커버리지 상승. floor 상향은 실측−~3pp, 게인 작으면 유지. |
| NFR-5 | 구현 후 dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 Specification 람다 = 캡처+toPredicate 실행** ✅: private buildSearchSpec 를 직접 호출 않고, search() 가 repository.findAll(spec,pageable) 에 넘긴 spec 을 ArgumentCaptor 로 받아 mock Criteria 로 toPredicate 실행 → 람다 분기 커버(리플렉션 불필요). ⚠**contStatMstRepository.findAll() 은 spec 생성 시점(=search() 호출 시) 호출 → search() 전에 stub 준비**(codex).
- **5-2 mock Criteria 깊이** ✅: CriteriaBuilder/Root 는 **RETURNS_DEEP_STUBS + lenient**(codex) 로 체인(root.get(..).as(..), cb.lower/upper/like/equal/or/and/conjunction) non-null 반환 + UnnecessaryStubbing 회피. 값 단언보다 "호출되고 NPE 없이 Predicate 반환"이 목표(람다 라인 커버). 핵심 분기는 호출/존재로 검증.
- **5-3 DB 테스트와 분리** ✅: 기존 SwServiceSearchTest(DB) 유지 — 본 단위테스트는 mock 으로 기본 CI 커버 보강(중복 아님, 실행환경 다름).

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Test(신규) | `service/SwServiceTest.java` | 신규 테스트 |
| Build(수정) | `pom.xml` | floor ratchet(상승 시) |

프로덕션 변경 0. DB/API/UI 변경 0 → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| mock Criteria 빌더 NPE/취약 | 중 | lenient + RETURNS_DEEP_STUBS 또는 명시 stub 으로 체인 non-null. 분기별 최소 단언 |
| 기존 SwServiceTest 이름 충돌 | 낮음 | 신규 클래스명 SwServiceTest 부재 확인(Normalize/Search 만 존재) |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
