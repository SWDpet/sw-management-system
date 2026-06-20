# PIT 뮤테이션 테스트 — 1차 베이스라인 (beyond-A)

- **측정일**: 2026-06-20 (회사 PC)
- **도구**: pitest-maven 1.15.0 + pitest-junit5-plugin 1.2.1
- **실행**: `./mvnw -Ppit test-compile org.pitest:pitest-maven:mutationCoverage`
- **리포트**: `target/pit-reports/index.html` (HTML) / `mutations.xml`
- **스코프**: 순수-단위 테스트가 대응하는 고신호 클래스 6종(클래스단위 화이트리스트). @SpringBootTest/DB 테스트는 targetTests 화이트리스트로 자동 배제.

> **mutation score = KILLED / (KILLED + SURVIVED)**. NO_COVERAGE 는 *해당 클래스에서 1차 targetTests 가 안 닿는 메서드*의 변이 → **스코프 밖**으로 분리(점수 분모 제외). 부분커버 클래스(ExcelExportService/SwService/LogService)는 NO_COVERAGE 가 크다(정상 — 1차는 특정 유틸 메서드만 타깃).

## 클래스별 베이스라인 (Step 3 보강 후)

| 클래스 | targetTest | KILLED | SURVIVED | NO_COVERAGE | mutation score |
|---|---|---:|---:|---:|---:|
| `response.ApiResult` | ApiResultTest | 5 | 0 | 0 | **100%** |
| `i18n.MessageResolver` | MessageResolver(Fallback)Test | 2 | 0 | 0 | **100%** |
| `util.MaskingDetector` | MaskingDetectorTest | 27 | 0 | 1 | **100%** ⬆(82%→100%) |
| `service.SwService` | SwServiceNormalizeTest | 16 | 1 | 24 | **94%** (1=equivalent) |
| `service.ExcelExportService` | ExcelExportServiceRounddownTest | 12 | 0 | 177 | **100%** (rounddown만; 177=스코프밖) |
| `service.LogService` | LogServiceActionNormalize+OrphanGuardTest | 24 | 0 | 7 | **100%** ⬆(60%→100%) |

**전체(스코프 내) 집계**: KILLED=86, SURVIVED=1, NO_COVERAGE=209. mutation score = 86/(86+1) = **98.9%**. (NO_COVERAGE 209 는 스코프밖이라 분모 제외 — 대부분 ExcelExportService 177·SwService 24.) 잔존 생존 1 = SwService(equivalent, 아래). 완전커버 5종 = 0 생존.

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

## 잔존 생존 = equivalent mutant (정상)

- **SwService.normalize L146** `t.length() > KW_MAX_LEN ? t.substring(0,KW_MAX_LEN) : t` 의 경계 변이(`>`→`>=`): length==100 일 때 `substring(0,100)` 은 100자 문자열 자신과 동일 → 두 분기 출력 같음 → **코드변경 없이 kill 불가(equivalent)**. 정당한 잔존.

## 운영 메모

- PIT 는 `pit` profile 안에만 있고 기본 lifecycle 미바인딩 → `./mvnw test`/`verify` 불변(검증: 413 green, PIT 미실행).
- 스코프 확장: pom `pit` profile 의 `targetClasses`/`targetTests` 에 *순수-단위 테스트가 있는* 클래스만 추가. 컨트롤러/@SpringBootTest 영역은 후속(느림·DB).
- 게이트(mutationThreshold)는 미설정(1차 측정만). 베이스라인 안정 후 ratchet 도입은 별도 스프린트.
