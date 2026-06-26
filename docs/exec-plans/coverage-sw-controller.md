# [개발계획서] SwController 커버리지 상향 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-26
- **기획서**: `docs/product-specs/coverage-sw-controller.md` (codex APPROVE-WITH-FIX 보완 반영 + 사용자 최종승인 완료)
- **상태**: ✅ **완료(2026-06-26)**. SwControllerTest 39 green + clean verify + dual-review 합의3 반영(T-26 명명·날짜루프 .as/per-iter editor) + 듀얼푸시. floor LINE 0.66→0.67·INSTR 0.53→0.54.

---

## 1. 작업 개요

`SwControllerTest`(신규) 1클래스 추가 + (상승 시) pom floor. 필드주입(10) reflection mock 주입 +
SecurityContextHolder 직접세팅 후 메서드 직접호출. 기존 `SwControllerLoggingTest` 불변.

주입 대상 mock: swService, logService, swProjectRepository, userRepository, sigunguRepository,
sysMstRepository, contFrmRepository, prjTypesRepository, maintTpRepository, contStatRepository.

---

## 2. 테스트 케이스 (T-n)

### 시군구 API (가드 없음)
| ID | 테스트 | 검증 |
|----|--------|------|
| T-1 | districts_delegates | getDistricts(sido) → sigunguRepository.findBySidoNmOrderBySggNm 위임 |
| T-2 | distOptions_blankCity_empty | city null/blank → 빈 List, repo 미호출 |
| T-3 | distOptions_withCity_delegates | city 있으면 swProjectRepository.findAllDistinctDistNmsByCity 위임 |

### 웹 목록/상세 (조회: NONE throw, ADMIN 우회 없음)
| ID | 테스트 | 검증 |
|----|--------|------|
| T-4 | list_notLoggedIn_redirectsLogin | getCurrentUser null → redirect:/login |
| T-5 | list_none_throws | authProject NONE → InsufficientPermissionException, swService.search 미호출 |
| T-6 | list_view_renders | VIEW → swService.search(Page.empty) + 옵션 4종 stub → "project-list", page<0 보정(=0) |
| T-7 | list_withCity_loadsDistrictOptions | city 지정 → findAllDistinctDistNmsByCity 호출(districtOptions) |
| T-8 | detail_none_throws | NONE → throw |
| T-9 | detail_found_renders | getProject → SwProject, "project-detail" + 참조데이터 |
| T-10 | detail_notFound_throws | getProject null → ResourceNotFoundException |

### 웹 폼/저장/삭제 (편집: EDIT 전용, ADMIN 우회 없음)
| ID | 테스트 | 검증 |
|----|--------|------|
| T-11 | newProject_nonEdit_throws | authProject≠EDIT(VIEW) → throw |
| T-12 | newProject_adminButNotEdit_throws | role ADMIN + authProject NONE → **여전히 throw**(웹 ADMIN 우회 없음) |
| T-13 | newProject_edit_renders | EDIT → "project-form" + 새 DTO |
| T-14 | form_noId_newDto | id null → 빈 DTO, "project-form" |
| T-15 | form_withId_loadsProject | id + getProject → DTO 채움 |
| T-16 | form_notFound_throws | id + getProject null → ResourceNotFound |
| T-17 | save_nonEdit_throws | ≠EDIT → throw, swService.save 미호출 |
| T-18 | save_new_redirectsStatus | projId null → save + CREATE 로그 → redirect:/projects/status |
| T-19 | save_update_redirectsStatus | projId set → save + UPDATE 로그 |
| T-20 | delete_nonEdit_throws | ≠EDIT → throw, delete 미호출 |
| T-21 | delete_edit_redirects | EDIT → getProject + delete + DELETE 로그 → redirect |

### REST API (ROLE_ADMIN 우회 있음, 404는 getProjectApi 한정)
| ID | 테스트 | 검증 |
|----|--------|------|
| T-22 | getApi_notLoggedIn_401 | null → 401 |
| T-23 | getApi_none_403 | authProject NONE + 비ADMIN → 403 |
| T-24 | getApi_view_ok | VIEW → getProject → 200 ApiResponse |
| T-25 | getApi_admin_ok_evenNone | ROLE_ADMIN + authProject NONE → 200(우회) |
| T-26 | getApi_notFound_404 | 권한 OK + getProject null → ResourceNotFoundException |
| T-27 | createApi_notLoggedIn_401 | null → 401 |
| T-28 | createApi_nonEdit_403 | 비ADMIN + ≠EDIT → 403, save 미호출 |
| T-29 | createApi_edit_ok | EDIT → save → 200 |
| T-30 | updateApi_admin_ok | ROLE_ADMIN → setProjId(id) + save → 200 |
| T-31 | deleteApi_nonEdit_403 | 비권한 → 403, delete 미호출 |
| T-32 | deleteApi_edit_ok_targetNull_no404 | EDIT + getProject null → delete 호출 후 **200**(404 아님) |
| T-37 | updateApi_notLoggedIn_401 | null → 401, save 미호출 (codex 보완: REST 4종 401 완전매핑) |
| T-38 | updateApi_nonEdit_403 | 비ADMIN + ≠EDIT → 403, save 미호출 |
| T-39 | deleteApi_notLoggedIn_401 | null → 401, delete 미호출 |

### InitBinder LocalDate PropertyEditor
| ID | 테스트 | 검증 |
|----|--------|------|
| T-33 | binder_parsesMultipleFormats | WebDataBinder 에 initBinder 적용 → findCustomEditor(LocalDate) 추출 → "2026-02-13"/"2026.02.13"/"20260213" 등 setAsText → 동일 LocalDate |
| T-34 | binder_blank_null | "" / " " → setValue(null) → getValue null |
| T-35 | binder_invalid_throws | "bad-date" → IllegalArgumentException |
| T-36 | binder_getAsText_formats | LocalDate → "yyyy-MM-dd" 문자열 |

---

## 3. 구현 순서 (S-n)

| ID | 단계 |
|----|------|
| S-1 | `SwControllerTest` 작성: reflection inject 헬퍼 + login(authProject, role) 헬퍼(setUserRole 필수) + SecurityContext clear(setUp/tearDown). |
| S-2 | T-1~T-36 작성. REST 응답은 ApiResponse status/typed body 단언. save 분기는 projId 유무로만(과한 로그 단언 회피). |
| S-3 | `./mvnw -o test -Dtest=SwControllerTest` 전건 green. (캐시 미스 시 `-o` 제거) |
| S-4 | `./mvnw -o clean verify`(전체 + JaCoCo 게이트) green + csv 로 SwController·전역 상승 측정. |
| S-5 | floor 상향 판단(실측−~3pp, 게인 작으면 유지) → pom 갱신 후 재 verify. |
| S-6 | (작업완료 트리거) dual-review(codex+Opus4.8) → 합의 반영 → 사용자 확인 → 커밋 + 듀얼푸시. |

---

## 4. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-3 compile |
| NFR-2 | S-3 SwControllerTest green + 기존 LoggingTest 불변 |
| NFR-3 | S-4 clean verify green |
| NFR-4 | S-4/S-5 상승 + 게이트 |
| NFR-5 | S-6 dual-review + 듀얼푸시 |

---

## 5. 롤백

단일 커밋(테스트 + pom). 문제 시 `git revert`. 프로덕션 영향 없음.

---

## 6. 커밋 (작업완료 후)

- `test(coverage): SwController 단위테스트 N + JaCoCo floor 상향 (beyond-A)`.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획서·개발계획 ✅ 완료 갱신 동봉.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
