# [개발계획서] SwService 커버리지 상향 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-26
- **기획서**: `docs/product-specs/coverage-sw-service.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-26)**. T-1~13 구현 green. 전역 LINE 70.33%→70.76%(floor 0.67→0.68), INSTR→57.64%. dual-review 합의 5건 반영.

---

## 1. 작업 개요

`SwServiceTest`(신규) 1클래스 + (상승 시) pom floor. mock(swProjectRepository, contStatMstRepository) 생성자/필드 주입. 기존 SwServiceNormalizeTest·SwServiceSearchTest 불변. 프로덕션 변경 0.

SwService 는 필드주입(@Autowired 2) → reflection 주입.

---

## 2. 테스트 케이스 (T-n)

### 위임
| ID | 검증 |
|----|------|
| T-1 | getList: swProjectRepository.findAll(pageable) 위임 반환 |
| T-2 | getProject: findById→orElse(null) (존재/미존재) |
| T-3 | save: swProjectRepository.save 위임 반환 |
| T-4 | delete: deleteById 위임 verify |
| T-5 | search(keyword, pageable) 오버로드 → 5인자 위임 (전 null → findAll(pageable)) |

### search 분기
| ID | 검증 |
|----|------|
| T-6 | search 전 파라미터 null/blank → findAll(pageable) 단축(spec 미사용). findAll(Specification, ...) 미호출 verify |
| T-7 | 필터 존재(예: city) → findAll(spec, pageable) 호출. ArgumentCaptor<Specification> 캡처 |

### buildSearchSpec 람다 (T-7 에서 캡처한 spec.toPredicate 실행)
- 공통(codex 보강): mock(CriteriaQuery). **Root 는 deep stub 대신 필드별 Path mock 명시**(`when(root.get("cityNm"))`/`("distNm")`/`("year")`/… → 서로 다른 Path mock; `root.get("year").as(String.class)` 도 stub). **CriteriaBuilder 는 cb.equal/like/lower/upper/or/and/conjunction 이 mock Predicate/Expression 을 반환하도록 명시 stub**(deep stub 만 의존 금지 — JPA 제네릭/오버로드로 흔들림). lenient. contStatMstRepository.findAll() 은 **search() 호출 전 stub**(spec 생성 시점 호출).

| ID | 검증 |
|----|------|
| T-8 | 필터만(kw=null): year/city/district/sysNmEn equal 분기 — search(kw=null, year="2026", city="서울", district=null, sysNmEn="upis") 로 spec 캡처 후 toPredicate → cb.equal 호출(year/city/sysNmEn). NPE 없이 Predicate 반환 |
| T-9 | kw 비숫자: search(kw="강진", ...) → toPredicate → cb.like 다컬럼 OR, year LIKE, **숫자파싱 실패(예외 무시)** → year equal 미추가. cb.or/cb.and 호출 |
| T-10 | kw 숫자: search(kw="2026", ...) → 숫자파싱 성공 → root.get("year") equal 추가 경로 실행 |
| T-11 | stat 매칭: contStatMstRepository.findAll() → ContStatMst(nm="진행중",cd="P"), kw="진행" → matchedCodes 에 "P" → cb.equal(stat, "P") 추가. + nm==null 통계 1건은 무시(NPE 없이) |
| T-12 | stat 매칭 없음: kw 와 무관한 stat 목록 → matchedCodes 비어도 정상 |
| T-13 | search() 정규화: `search(null,null,null,"x",null,pageable)` (city=null,district="x") → spec 캡처 후 toPredicate → **`verify(root, never()).get("distNm")`** 로 distNm 경로 자체 미생성 검증(distN=null 정규화, L61-62). (codex: "predicate 미포함"보다 "경로 미생성"이 정확·견고) |

---

## 3. 구현 순서 (S-n)

| ID | 단계 |
|----|------|
| S-1 | SwServiceTest 작성: new SwService() + reflection 주입(swProjectRepository, contStatMstRepository). |
| S-2 | T-1~T-5 위임 + T-6/T-7 search 분기. |
| S-3 | spec 캡처 헬퍼: `ArgumentCaptor<Specification<SwProject>>` → controller.search 후 capture → `spec.toPredicate(mockRoot, mockQuery, mockCb)`. mock 은 RETURNS_DEEP_STUBS+lenient. |
| S-4 | T-8~T-13 람다 분기. cb.equal/like/or/and 호출 검증(verify atLeastOnce 또는 결과 non-null). |
| S-5 | `./mvnw -o test -Dtest=SwServiceTest` green. |
| S-6 | `./mvnw -o clean verify`(전체 + JaCoCo 게이트) green + csv 로 SwService·전역 상승 측정. |
| S-7 | floor 판단(실측−~3pp, 게인 작으면 유지). |
| S-8 | (작업완료) dual-review → 합의 반영 → 커밋 + 듀얼푸시. |

---

## 4. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-5 compile |
| NFR-2 | S-5 SwServiceTest green + 기존 Normalize/Search 불변 |
| NFR-3 | S-6 clean verify |
| NFR-4 | S-6/S-7 상승 + 게이트 |
| NFR-5 | S-8 dual-review + 듀얼푸시 |

---

## 5. 롤백

단일 커밋(테스트 + pom). 문제 시 `git revert`. 프로덕션 영향 없음.

---

## 6. 커밋

- `test(coverage): SwService 단위테스트 N (beyond-A)`(+ floor 상향 시 동봉).
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
