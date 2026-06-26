# [기획서] 잔여 소형 컨트롤러 일괄 커버리지 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-26
- **트랙**: beyond-A 커버리지 — 컨트롤러 통합영역([[coverage-controller-integration]]) 마무리. 비-license 잔여 소형 컨트롤러 6종 일괄.
- **상태**: ✅ **완료(2026-06-26)**. 기획/개발계획 codex APPROVE(-WITH-FIX 보완)+사용자승인 / 구현 6클래스 21테스트(전부 100%) / dual-review 합의3 반영 / 듀얼푸시. 전역 LINE 69.88→70.31%(70%돌파)·INSTR 56.97→57.21%. floor 유지(게인 작음).

---

## 1. 배경 / 목표

SwController 까지 대형 컨트롤러 정리 후, 남은 비-license 컨트롤러는 모두 소형(34~84줄)이고 커버리지 1~47%다. 인증/단순 위임/페이지 라우팅 위주라 직접호출로 빠르게 커버 가능.

**목표**: 6종 소형 컨트롤러를 직접호출 단위테스트로 커버. **프로덕션 코드 변경 0(순수 테스트만)**.

---

## 2. 대상 컨트롤러 (6종)

| # | 컨트롤러 | 의존 | 핵심 분기 |
|---|---|---|---|
| 1 | LoginController | 없음 | loginPage: error/locked(+minutes)/logout/expired → model 메시지 분기 |
| 2 | SignupController | userRepo·BCryptPasswordEncoder·logService | signupForm 뷰 / register: pw 인코딩·기본값(enabled false·ROLE_USER·auth NONE)·save·log → redirect |
| 3 | UserApiController | userRepo | getAllWithDisabled / getActive → UserLightDto 매핑 위임 |
| 4 | LogoutController | 없음 | auth null / 존재 → SecurityContextLogoutHandler → redirect:/login?logout=true |
| 5 | SystemGraphController | erdGraphService·infraGraphService | view / getErdGraph / getInfraGraph 위임(클래스 @PreAuthorize ADMIN — 메서드 보안은 단위테스트 범위 밖) |
| 6 | InspectionQrBatchController | service·ObjectMapper·Validator | upload: normalize→treeToValue→validate→service.upload(200) / malformed→IllegalArgumentException / 검증실패→ConstraintViolationException. handleSiteNotMapped → 422 |

**영구제외**: license 계열.

---

## 3. 테스트 컨벤션

[[coverage-controller-integration]] §3. 컨트롤러 자체 가드가 없는 것(Login/Signup/UserApi/Logout/SystemGraph)은 의존성 mock 주입(필드=reflection, 생성자=생성자) 후 직접호출. LogoutController 는 `MockHttpServletRequest/Response` + SecurityContext 세팅. InspectionQrBatch 는 ObjectMapper/Validator/Service 를 mock 으로 주입해 upload 분기(정상/malformed/검증실패)와 @ExceptionHandler(422)를 결정적으로 커버. 실 Postgres 무접촉.

---

## 4. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | LoginController: error+locked(minutes)/error(일반)/logout/expired 4분기 model 메시지 + 무파라미터 정상. |
| FR-2 | SignupController: signupForm 뷰 / register 가 pw 인코딩·**컨트롤러가 직접 세팅하는 필드만**(enabled=false·ROLE_USER·authDashboard/authProject/authPerson="NONE")·save·log 후 redirect:/login?msg=pending. ⚠나머지 auth 필드(authInfra/Quotation/WorkPlan/Document/Contract/Performance/License)는 @PrePersist 소관 → mock-save 단위테스트에선 미실행이므로 검증 제외(codex 보완). |
| FR-3 | UserApiController: 2 엔드포인트 위임 + DTO 매핑(빈 리스트 + 1건). |
| FR-4 | LogoutController: auth 존재(로그아웃 핸들러 호출 후 컨텍스트 클리어) / auth null 모두 redirect:/login?logout=true. |
| FR-5 | SystemGraphController: view 뷰명 + erd/infra API 위임. |
| FR-6 | InspectionQrBatchController: upload 정상(200)/malformed(IllegalArgumentException)/검증실패(ConstraintViolationException) + handleSiteNotMapped 422 본문(site_not_mapped). |
| FR-7 | 프로덕션 코드 변경 0. |

---

## 5. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile 성공. |
| NFR-2 | 신규 테스트 클래스(6) 전건 green. |
| NFR-3 | 전체 `./mvnw -o clean verify` green. |
| NFR-4 | 각 컨트롤러·전역 커버리지 상승. floor 상향은 실측−~3pp, 게인 작으면 유지. |
| NFR-5 | (작업완료 후) dual-review → 사용자 확인 → 듀얼푸시. |

---

## 6. 의사결정

- **6-1 6종 1트랙·1커밋** ✅: 모두 소형·저위험이라 묶어서 1기획/1개발계획/1커밋. (대형은 SwController 처럼 개별)
- **6-2 InspectionQrBatch upload 는 ObjectMapper/Validator mock** ✅: 실 JSON 스키마·Hibernate 검증 대신 mock 으로 분기만 결정적 커버. normalize(static)는 실 ObjectNode 전달.
- **6-3 SystemGraph @PreAuthorize 메서드 보안 제외** ✅: 컨트롤러 단위테스트는 위임만 검증. 메서드 보안은 SecurityConfig/통합 영역.

---

## 7. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Test(신규) | 6개 `*ControllerTest.java`(또는 묶음) | 신규 테스트 |
| Build(수정) | `pom.xml` | floor ratchet(상승 시) |

프로덕션 변경 0. DB/API/UI 변경 0 → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| InspectionQrBatch mock 과다 → 실제 경로 괴리 | 낮음 | normalize 는 실제 호출, treeToValue/validate/service 만 mock. malformed/검증실패/정상 3분기 명시 |
| LogoutController servlet mock | 낮음 | MockHttpServletRequest/Response 표준 |

---

## 8. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
