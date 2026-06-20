# PIT 뮤테이션 테스트 — 1차 베이스라인 (beyond-A)

- **측정일**: 2026-06-20 (회사 PC)
- **도구**: pitest-maven 1.15.0 + pitest-junit5-plugin 1.2.1
- **실행**: `./mvnw -Ppit test-compile org.pitest:pitest-maven:mutationCoverage`
- **리포트**: `target/pit-reports/index.html` (HTML) / `mutations.xml`
- **스코프**: 순수-단위 테스트가 대응하는 고신호 클래스 **12종**(클래스단위 화이트리스트). @SpringBootTest/DB 테스트는 targetTests 화이트리스트로 자동 배제.

> **두 가지 점수 구분(중요)**:
> - **분석용 score = KILLED/(KILLED+SURVIVED)** — 테스트가 *닿은* 변이의 kill 비율(이 문서 표 기준). NO_COVERAGE(미커버 메서드)는 분모 제외.
> - **PIT 게이트 score = KILLED/TOTAL**(NO_COVERAGE 포함) — `mutationThreshold` 가 쓰는 공식. 부분커버 클래스는 미커버 메서드 변이가 NO_COVERAGE 로 분모에 들어가 점수가 낮아짐.
> → **게이트는 클래스 전체가 거의 완전커버되는 7종에만 적용**(아래 §게이트). 부분커버 진단 클래스(ExcelExportService rounddown·SwService·LogService·LoginAttemptService)는 KILLED/TOTAL 이 왜곡되어 게이트 부적합 → 정규 테스트로만 보호.

## 게이트 (ratchet floor) — `mutationThreshold` 90

`pit` profile 에 `<mutationThreshold>90</mutationThreshold>` + `<failWhenNoMutations>true</failWhenNoMutations>` 적용. **게이트 스코프 = 완전커버 7종**(ApiResult·MaskingDetector·SensitiveMask·MessageResolver·EnumErrorResponseFactory·RemarksRenderer·InspectMaintProfile). 현재 **KILLED/TOTAL = 105/110 = 95%** ≥ 90 → 통과. 회귀로 점수가 90 미만이면 `./mvnw -Ppit ...mutationCoverage` **BUILD FAILURE**(검증: broad 스코프 43%<90 에서 실패 확인). QUALITY_CHARTER S2 목표(PIT 최소점수 게이트) 충족. floor 는 부분커버 클래스 보강·편입 시 상향.

## 클래스별 베이스라인 (Step 3 보강 후, 12클래스)

| 클래스 | targetTest | KILLED | SURVIVED | NO_COVERAGE | mutation score |
|---|---|---:|---:|---:|---:|
| `response.ApiResult` | ApiResultTest | 5 | 0 | 0 | **100%** |
| `i18n.MessageResolver` | MessageResolver(Fallback)Test | 2 | 0 | 0 | **100%** |
| `exception.EnumErrorResponseFactory` | EnumErrorResponseFactoryTest | 10 | 0 | 0 | **100%** |
| `quotation.service.RemarksRenderer` | RemarksRendererTest | 11 | 0 | 0 | **100%** |
| `service.inspection.InspectMaintProfile` | InspectMaintProfileTest | 21 | 0 | 2 | **100%** |
| `service.LoginAttemptService` | LoginAttemptServiceWithPropertiesTest | 13 | 0 | 11 | **100%** ⬆(85%→100%) |
| `util.MaskingDetector` | MaskingDetectorTest | 27 | 0 | 1 | **100%** ⬆(82%→100%) |
| `util.SensitiveMask` | SensitiveMaskTest | 29 | 2 | 0 | **94%** ⬆(29%→94%, 2=equivalent) |
| `service.ExcelExportService` | ExcelExportServiceRounddownTest | 12 | 0 | 177 | **100%** (rounddown만; 177=스코프밖) |
| `service.LogService` | LogServiceActionNormalize+OrphanGuardTest | 24 | 0 | 7 | **100%** ⬆(60%→100%) |
| `service.SwService` | SwServiceNormalizeTest | 16 | 1 | 24 | **94%** (1=equivalent) |
| `service.inspection.InspectionQrBatchService` | InspectionQrBatchServiceTest | 69 | 89 | 105 | **44%** ⚠ 백로그 |

**집계(스코프 내, score=K/(K+S))**:
- **고신호 11종**(InspectionQrBatchService 제외): KILLED=170, SURVIVED=3 → **98.3%**. 잔존 3 = 전부 equivalent(SensitiveMask 2 + SwService 1). 완전커버 9종 = 0 생존.
- **전체 12종**: KILLED=239, SURVIVED=92 → 72.2%. (저하는 InspectionQrBatchService 89 생존 단독 — 아래 백로그.)
- NO_COVERAGE(다수)는 스코프밖이라 분모 제외(ExcelExportService 177·InspectionQrBatch 105·SwService 24 등).

## Step 3 보강 사례 (뮤테이션 테스트 효용 입증) — `MaskingDetector` 82% → **100%**

생존 변이의 정체 = `isMaskedTel/Email/Ssn/Address` 의 **1차 동등비교 분기**(`if (currentDbValue != null && input.equals(sensitiveMask.xxx(db)))`) negated conditional. 원인 = 기존 테스트가 동등분기를 *정규식도 매치하는 값*으로만 검증 → 분기를 negate 해도 fallback 정규식이 true 라 구분 불가.
- **T3b/T5b/T6c (동등분기 양성)**: 포맷불일치 마스크(`***-****-****`/`***@***`/`******`)는 정규식 불일치 → 동등분기만이 true 를 만드는 입력 → tel/email/ssn 의 `equals` negation kill.
- **T8b (address 동등분기 음성, codex 제안)**: 평문 입력 ≠ 마스킹 DB값 + 별표 없음 → 동등·정규식 모두 false 여야 함 → address 의 `equals` negation kill. (⚠ 처음엔 equivalent 로 오판했으나 codex 지적으로 음성 케이스가 kill 함을 확인 — equivalent 아님.)
- **T9b 확장 (`!= null` 분기)**: `isMaskedAddress("", null)` → address(null)="" 와 동등이 되어선 안 됨 → L59 의 `currentDbValue != null` negation kill.
- 결과: MaskingDetector **생존 0 (100%)**. 보안민감(PII 마스킹 회귀방지) 로직의 분기를 전수 검증하게 됨.

## Step 3-b 보강 — `LogService` 60% → **100%**

생존 8건 = `applyOrphanGuard` 분기(null/blank·SYSTEM_USERIDS contains·return value·existsByUserid) negated/boundary 4 + `logInternal` 의 `AccessLog.setUserid/Username/IpAddr/ActionDetail` removed-call 4. 원인 = 기존 action-normalize 테스트가 *actionType/menuNm 만 검증*하고 나머지 필드·가드 분기를 안 봄.
- `LogServiceActionNormalizeTest.logInternal_populates_all_accesslog_fields`: ArgumentCaptor 로 userid(anonymousUser)/username("")/ipAddr(127.0.0.1)/actionDetail 검증 → 세터 4건 kill.
- 신규 `LogServiceOrphanGuardTest`(pom targetTests 등록): applyOrphanGuard 4규칙(null/blank→anon, 화이트리스트→원본·DB생략, 존재→원본, orphan→anon) 직접 검증 → 가드 분기 4건 kill.

## Step 3-c 보강 — `SensitiveMask` 29%→94%, `LoginAttemptService` 85%→100% (스코프 확장)

- **SensitiveMask(보안 PII 마스킹)**: 직접 테스트가 없어 MaskingDetectorTest 가 *입력 생성용*으로만 호출 → 마스킹 출력 형식 자체 미검증(20 생존). 신규 `SensitiveMaskTest`(ssn/tel/email/address 각 정상·포맷불일치 fallback·null/empty·토큰경계 출력 고정)로 18건 kill → 94%. 잔존 2 = tel L59 경계(아래 equivalent).
- **LoginAttemptService(로그인 잠금)**: `loginSucceeded` 미검증으로 `setLockTime(null)` removed-call + `>0` 경계 생존. 잠김상태→해제 검증 + 실패0 no-op(save 미호출) 검증 추가 → 100%.

## 잔존 생존 = equivalent mutant (정상)

- **SwService.normalize L146** `t.length() > KW_MAX_LEN ? t.substring(0,KW_MAX_LEN) : t` 경계(`>`→`>=`): length==100 일 때 `substring(0,100)`==100자자신 → 출력 동일 → kill 불가.
- **SensitiveMask.tel L59** `if (first > 0 && last > first)` 경계 2건: 정규식 매치 입력은 `first`(첫 '-' 위치)가 항상 ≥2, `last>first`(하이픈 2개)도 항상 참 → `>`↔`>=` 출력 무차이 → kill 불가.

## InspectionQrBatchService — 순수 헬퍼 보강 44% → 66% (KILLED/TOTAL 26%→51%)

대형 서비스(QR 배치 생성, 763줄). 기존 InspectionQrBatchServiceTest(12개)는 흐름만 돌려 *결과 포맷팅·파싱 로직이 대부분 NO_COVERAGE/SURVIVED*(89생존+105 NO_COV).
- **신규 InspectionQrBatchFormatTest(19개)**: 순수 static 헬퍼(`resolveSection`·`extractPercent`·`parseNumeric`·`formatValue`·`formatValueWithContext`·`buildRemarks`)를 package-private 노출 후 직접 검증. 점검 결과값→사용자 화면 문자열 변환 로직(특히 formatValueWithContext 122줄)의 분기 전수.
- 결과: KILLED 69→**134**, SURVIVED 89→69, NO_COVERAGE 105→60. 분석score(K/(K+S)) **44%→66%**.
- **잔여 69 생존 = DB/빌더 메서드**(saveMetricSnapshot 23·toIdempotentResponse 12·buildBatch 10·saveCheckResults 10·buildReport 5 등): **후속 백로그**(글루코드, 이번 증분 밖). codex 권고 = private 노출 대신 *public flow + Mockito `ArgumentCaptor`* 로 저장 엔티티/upsert 인자 필드 검증. (장기: 순수 포맷터를 `InspectionQrBatchFormatter` package-private 클래스로 분리 검토.)
- 게이트 미편입: NO_COVERAGE 60 남아 KILLED/TOTAL=51% < 90. 전체 메서드 커버(DB/빌더 포함) 후 편입 검토.

## 운영 메모

- PIT 는 `pit` profile 안에만 있고 기본 lifecycle 미바인딩 → `./mvnw test`/`verify` 불변(검증: 434 green, PIT 미실행).
- 스코프 확장: pom `pit` profile 의 `targetClasses`/`targetTests` 에 *순수-단위 테스트가 있는* 클래스만 추가. 컨트롤러/@SpringBootTest 영역은 후속(느림·DB).
- 게이트(mutationThreshold=90) **도입 완료**(완전커버 7종, 위 §게이트). 측정용 broad 스코프(부분커버 포함)는 진단 시 pom 에 임시 추가해 실행.
