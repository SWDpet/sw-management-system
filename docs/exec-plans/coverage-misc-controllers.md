# [개발계획서] 잔여 소형 컨트롤러 일괄 커버리지 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-26
- **기획서**: `docs/product-specs/coverage-misc-controllers.md` (codex APPROVE-WITH-FIX 보완 반영 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-26)**. 6클래스 21테스트 green + clean verify + dual-review 합의3 반영(QrBatch 본문 isSameAs·Login 15분 단언) + 듀얼푸시. 6 컨트롤러 전부 INSTR/LINE 100%.

---

## 1. 작업 개요

소형 컨트롤러 6종에 직접호출 단위테스트 6클래스 추가 + (상승 시) pom floor. 의존성 mock 주입(필드=reflection, 생성자=생성자). 프로덕션 변경 0.

---

## 2. 테스트 케이스 (T-n)

### LoginControllerTest (의존 0)
| ID | 검증 |
|----|------|
| T-L1 | error+locked=true+minutes=10 → loginError "약 10분" 포함, "login" 뷰 |
| T-L2 | error+locked=true+minutes=null → 기본 "15분" |
| T-L3 | error(일반, locked 아님) → "아이디 또는 비밀번호" 메시지 |
| T-L4 | logout != null → logoutMsg |
| T-L5 | expired=true → 세션 만료 loginError |
| T-L6 | 무파라미터 → "login" 뷰, 메시지 없음 |

### SignupControllerTest (userRepo·BCryptPasswordEncoder·logService mock)
| ID | 검증 |
|----|------|
| T-S1 | signupForm() → "signup" |
| T-S2 | register: passwordEncoder.encode 적용·enabled false·ROLE_USER·authDashboard/Project/Person="NONE"·save 호출(ArgumentCaptor)·log 호출 → "redirect:/login?msg=pending" (나머지 auth 필드 미검증) |

### UserApiControllerTest (userRepo mock)
| ID | 검증 |
|----|------|
| T-U1 | getAllWithDisabled: findAll → UserLightDto 매핑(1건 + 빈) |
| T-U2 | getActive: findByEnabledTrue → 매핑 |

### LogoutControllerTest (의존 0, MockHttpServletRequest/Response)
| ID | 검증 |
|----|------|
| T-G1 | auth 존재(SecurityContext 세팅) → logout → "redirect:/login?logout=true" + 컨텍스트 클리어 |
| T-G2 | auth null(clearContext) → "redirect:/login?logout=true" |

### SystemGraphControllerTest (erd·infra service mock, 생성자)
| ID | 검증 |
|----|------|
| T-Y1 | view() → "admin-system-graph" |
| T-Y2 | getErdGraph() → erdGraphService.getGraph 위임 |
| T-Y3 | getInfraGraph() → infraGraphService.getGraph 위임 |

### InspectionQrBatchControllerTest (service·ObjectMapper·Validator mock, 생성자)
| ID | 검증 |
|----|------|
| T-Q1 | upload 정상: 실 ObjectNode normalize + objectMapper.treeToValue→req mock + validator.validate→빈셋 + service.upload→res → 200, userId 전달 |
| T-Q2 | upload malformed: objectMapper.treeToValue throw → IllegalArgumentException |
| T-Q3 | upload 검증실패: validator.validate→non-empty → ConstraintViolationException, service 미호출 |
| T-Q4 | upload me=null → userId null 로 service.upload 호출(200) |
| T-Q5 | handleSiteNotMapped(SiteNotMappedException) → 422 UNPROCESSABLE_ENTITY + body.error="site_not_mapped"·siteCode |

---

## 3. 구현 순서 (S-n)

| ID | 단계 |
|----|------|
| S-1 | 6개 테스트 클래스 작성(T-L*/S*/U*/G*/Y*/Q*). Login/Logout 무의존, Signup/UserApi 필드주입 reflection, SystemGraph/InspectionQrBatch 생성자. |
| S-2 | `./mvnw -o test -Dtest=LoginControllerTest,SignupControllerTest,UserApiControllerTest,LogoutControllerTest,SystemGraphControllerTest,InspectionQrBatchControllerTest` 전건 green. |
| S-3 | `./mvnw -o clean verify`(전체 + JaCoCo 게이트) green + csv 로 각 컨트롤러·전역 상승 측정. |
| S-4 | floor 상향 판단(실측−~3pp, 게인 작으면 유지) → pom 갱신 후 재 verify. |
| S-5 | (작업완료) dual-review → 합의 반영 → 사용자 확인 → 커밋 + 듀얼푸시. |

---

## 4. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-2 compile |
| NFR-2 | S-2 6클래스 green |
| NFR-3 | S-3 clean verify green |
| NFR-4 | S-3/S-4 상승 + 게이트 |
| NFR-5 | S-5 dual-review + 듀얼푸시 |

---

## 5. 롤백

단일 커밋(테스트 6 + pom). 문제 시 `git revert`. 프로덕션 영향 없음.

---

## 6. 커밋 (작업완료 후)

- `test(coverage): 잔여 소형 컨트롤러 6종 단위테스트 + JaCoCo floor 상향 (beyond-A)`.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획서·개발계획 ✅ 완료 갱신 동봉.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
