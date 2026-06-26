# [기획서] 컨트롤러 통합영역 커버리지 상향 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-26
- **트랙**: beyond-A 커버리지 스프린트. 선행 `coverage-core-services`(서비스 단위) 후속 — **컨트롤러 계층** 진입.
- **상태**: ⚠ **정식 절차 소급 적용**. 구현·dual-review·verify게이트·듀얼푸시는 간소화 절차로 선행 완료(커밋 `6c6a307`~`8ef732f`, 15컨트롤러). 본 기획서/개발계획으로 **codex 검토 + 사용자 최종승인** 정식 게이트를 소급 보강한다. → ✅ **완료**: codex APPROVE(2026-06-26, FR-1~5·NFR-1~5 전항목 충족) + **사용자 최종승인(2026-06-26)**. 정식 절차 소급 보강 종료.

---

## 1. 배경 / 목표

코어 서비스(DesignEstimate/InterimReport/Document/InspectReport/Quotation 등) 커버리지 소진 후 **남은 대량 미커버는 컨트롤러 계층**(대부분 1~10% 핫스팟)이었다. 컨트롤러는 권한 가드·분기·응답 조립을 담아 회귀 리스크가 높은데도 테스트 공백이 컸다.

**목표**: 비-license 컨트롤러를 권한 가드·분기·해피패스 위주 단위테스트로 끌어올려 전역 커버리지 상향 + JaCoCo floor ratchet. **프로덕션 코드 변경 0(순수 테스트만)** — 회귀 리스크 최소.

---

## 2. 대상 컨트롤러 (15종, 영구제외 license 계열 제외)

| # | 컨트롤러 | 인증 방식 | 비고 |
|---|---|---|---|
| 1 | DocumentController | SecurityContextHolder + reflection 주입 | 1369줄, 최대 |
| 2 | QuotationController | 생성자 주입 + SecurityContextHolder | checkView/EditAuth throw |
| 3 | OpsDocController | 생성자(17) + @AuthenticationPrincipal | 첨부4종 기존테스트 보완 |
| 4 | InspectReportController | 생성자 + SecurityContextHolder | extractSnapshotSpecs 포함 |
| 5 | WorkPlanController | 필드 주입 + SecurityContextHolder | |
| 6 | OpsKbController | 생성자 + 혼합(param+ROLE_ADMIN) | 상태전이 KB |
| 7 | PerformanceController | 필드 주입 + SecurityContextHolder | 12개월 차트 집계 |
| 8 | MainController | 필드 주입, 컨트롤러 가드없음 | 대시보드 집계 |
| 9 | InfraController | 필드 주입 + SecurityContextHolder | |
| 10 | PersonController | 필드 주입 + SecurityContextHolder | |
| 11 | PartnerController | 생성자 + @AuthenticationPrincipal | |
| 12 | DocumentParticipantController | 생성자 + SecurityContextHolder | |
| 13 | OrgUnitController | 필드 주입, 컨트롤러 가드없음 | |
| 14 | MyPageController | 필드 주입 + SecurityContextHolder | 기존 guard 시뮬테스트 보완 |
| 15 | InspectAgentController | 생성자 + @AuthenticationPrincipal | manifest 픽스처 |

**영구제외**: LicenseRegistry/Geonuris/QrLicense (license 스프린트 영구패스, AGENTS.md §6).

---

## 3. 테스트 컨벤션 (공통 패턴)

- **인증이 SecurityContextHolder 기반인 컨트롤러**: `new Controller()` 후 의존성 mock 을 reflection(또는 생성자) 주입하고, `SecurityContextHolder` 에 `CustomUserDetails` principal 을 직접 세팅 후 메서드 직접 호출. (풀 `@SpringBootTest`+MockMvc 불필요 → 실 Postgres 무접촉, 기본 CI 에서 floor 반영)
- **인증이 `@AuthenticationPrincipal` 파라미터 기반인 컨트롤러**: 생성자 mock 주입 + 사용자 principal 을 파라미터로 직접 전달(SecurityContext 불요). `isAdmin()`만 SecurityContextHolder 인 경우 관리자 시나리오만 ROLE_ADMIN 세팅.
- **가드 검증**: 권한 미달 시 redirect/403/throw + **서비스 미호출 verify**(부수효과 0).
- **해피패스**: mock 서비스로 분기·매핑 커버. 상태/필드 매핑은 ArgumentCaptor 로 단언(toString 의존 금지).
- **선례**: `OpsDocControllerAttachmentGuardTest`(직접호출), `DashboardAndAdminLogsSecurityIntegrationTest`(MockMvc) 패턴 확장.

---

## 4. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 15개 컨트롤러 각각에 mock 단위테스트 추가(운영DB 무관, 기본 CI 실행). |
| FR-2 | 모든 권한 가드 분기(NONE/VIEW/EDIT/ADMIN·throw/403/redirect) 커버 + 서비스 미호출 verify. |
| FR-3 | 해피패스·예외·입력검증 분기 커버. 상태전이/필드매핑은 ArgumentCaptor 값 단언. |
| FR-4 | 프로덕션 코드 변경 0 (순수 테스트만). 컨트롤러 시그니처·동작 불변. |
| FR-5 | InspectAgent 는 meta() 파싱 경로 커버 위해 test classpath 픽스처(`agent/release-manifest.json`) 배치(테스트 전용, main jar 미포함). |

---

## 5. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile 성공. |
| NFR-2 | 각 컨트롤러 테스트 클래스 전건 green. |
| NFR-3 | 전체 `./mvnw clean verify`(전체 테스트 + JaCoCo 게이트) green. ⚠`clean` 필수(jacoco append 누적 오염 방지). |
| NFR-4 | 컨트롤러별·전역 커버리지 상승. floor 상향은 실측−~3pp 버퍼, 1pp 단위, 게인 작은 라운드는 유지. |
| NFR-5 | 매 커밋 dual-review(codex+Opus4.8 이중검수) 후 듀얼푸시. |

---

## 6. 의사결정

- **6-1 풀 MockMvc 대신 직접호출 단위테스트** ✅: 컨트롤러 권한·분기 로직 커버가 목적이고 실 Postgres 무접촉이 필요 → 직접호출이 우월(기본 CI 반영, 결정적). 필터체인 자체 검증은 별도 통합테스트(기존 Dashboard 테스트)로 분리 유지.
- **6-2 reflection 필드주입 vs 생성자주입** ✅: 컨트롤러가 필드주입이면 reflection, 생성자주입이면 생성자. 스위트 전반 일관 적용(dual-review 가 반복 지적했으나 컨트롤러 리팩터는 본 트랙 범위 밖 — 별건).
- **6-3 license 계열 영구제외** ✅: AGENTS.md §6 영구패스 준수.
- **6-4 floor ratchet 측정법** ✅: jacoco.csv PACKAGE 슬래시표기(`$2 ~ /swmanager/`) 합산 = 게이트(BUNDLE)와 일치. `clean` 후 측정.

---

## 7. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Test(신규) | 15개 `*ControllerTest.java` | 신규 테스트 |
| Test resource(신규) | `src/test/resources/agent/release-manifest.json` | 픽스처(테스트 전용) |
| Build(수정) | `pom.xml` | JaCoCo floor ratchet |

프로덕션 코드 변경 0. DB/API/UI 변경 0 → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 가드 우회 회귀 미포착 | 낮음 | FR-2 서비스 미호출 verify |
| floor 과다상향 CI 깨짐 | 낮음 | 실측−~3pp 버퍼, 게인 작으면 유지 |
| test 픽스처 main 오염 | 없음 | src/test/resources 전용(main jar 미포함) |

---

## 8. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다. (구현은 간소화 절차로 선행 완료 — 본 게이트로 정식 절차 소급 보강)
