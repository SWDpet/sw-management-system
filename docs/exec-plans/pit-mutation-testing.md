# 개발계획서 — PIT 뮤테이션 테스트 도입 (pit-mutation-testing, beyond-A)

- **상태**: v0.2 (codex 승인가능 + 보완 3건 반영)
- **작성일**: 2026-06-20 (v0.1) / v0.2 동일자 (codex: Step1 mutant>0 기준·baseline 3분류 집계·JaCoCo argLine 주의)
- **기획서**: `docs/product-specs/pit-mutation-testing.md` (v0.2, codex 4건 반영·사용자 승인)
- **안전망**: 기본 `./mvnw test` 410 green — PIT 는 `pit` profile 밖에서 안 돎(NFR-1). 각 Step 후 기본 빌드 불변 확인.
- **원칙**: 운영코드 0 변경. pom(profile) + 문서 + (선택)테스트 보강만. 클래스단위 화이트리스트 스코프.

---

## Step 0 — 매핑·버전 확정 (구현 전)

### 0-1. targetClasses ↔ targetTests 매핑 (실측 완료)
| targetClass | targetTest(순수단위) | 커버 범위 |
|---|---|---|
| `response.ApiResult` | `response.ApiResultTest`(6) | 전체(완전) |
| `util.MaskingDetector` | `util.MaskingDetectorTest`(11) | 전체(+SensitiveMask/AuthSummary 전이) |
| `i18n.MessageResolver` | `i18n.MessageResolverTest`(4)·`MessageResolverFallbackTest`(3) | 전체 |
| `service.ExcelExportService` | `service.ExcelExportServiceRounddownTest` | rounddown static 3종만(나머지 NO_COVERAGE) |
| `service.SwService` | `service.SwServiceNormalizeTest` | normalize 계열만 |
| `service.LogService` | `service.LogServiceActionNormalizeTest` | action normalize만 |
- 앞 3개 = 완전커버(고신호 baseline 핵심). 뒤 3개 = 부분커버(NO_COVERAGE 혼재, 보조 신호).

### 0-2. 버전 핀 (codex 제안 + 구현 결정 로그)
- codex 제안: `pitest-maven 1.25.5` + `pitest-junit5-plugin 1.2.3`.
- **구현 채택: `pitest-maven 1.15.0` + `pitest-junit5-plugin 1.2.1`** (검증된 안정 조합, Step 1 스모크에서 의존성 해소·완주 확인). 1차 측정 목적엔 충분 → 더 낮은 핀으로 확정. 후속 게이트 도입 시 최신(1.25.5/1.2.3)으로 상향 검토.

**검증:** 없음(준비).

## Step 1 — pom `pit` profile + 1클래스 스모크

**1-1.** `pom.xml` 에 `<profiles>` 추가(없으면 신설), `pit` profile 에 pitest-maven 플러그인(기획서 5-1 구성). **lifecycle phase 바인딩 없음**(execution 없이 명시 goal).
- **1차 스모크 한정**: targetClasses/targetTests 를 `ApiResult`/`ApiResultTest` **1쌍만** 으로 임시 축소.

**1-2.** 실행: `./mvnw -Ppit test-compile org.pitest:pitest-maven:mutationCoverage`
- 기대: PIT 완주, `target/pit-reports/index.html` + `mutations.xml` 생성, ApiResult 변이 killed/survived 표시("no tests found" 아니어야 함 → junit5-plugin 정상).

**검증(FR-1·FR-2·R-3) — codex 성공기준:**
- `mutations.xml` 생성 + **ApiResult mutant 총수 > 0** + PIT 로그에 `No tests found`/`No mutations found` **없음**.
- **기본 빌드 불변**: `./mvnw -q test` 410 green, PIT 미실행(profile 밖) 확인.
- ⚠ JaCoCo `prepare-agent` 가 `argLine` 설정(pom:172). PIT minion JVM 에 노이즈/충돌 시 `pit` profile 의 PIT `<jvmArgs/>` 명시 비움으로 보완(스모크에서 관찰).

## Step 2 — 1차 스코프 전체 + 베이스라인

**2-1.** targetClasses/targetTests 를 0-1 의 6클래스/7테스트로 확장.
**2-2.** 실행 → mutation score 집계: 클래스별 killed/survived/no_coverage/총. `target/pit-reports` 확인.
**2-3.** 베이스라인을 `docs/exec-plans/pit-baseline.md` 에 기록 — **codex: KILLED/SURVIVED/NO_COVERAGE 를 분리 집계**(mutation score = KILLED/(KILLED+SURVIVED), NO_COVERAGE 는 스코프밖으로 별도 표기). 생존 변이 약점 패턴 메모.

**검증(FR-2·FR-3·FR-4):** DB 없이 완주(targetTests 화이트리스트로 @SpringBootTest 배제). 리포트의 NO_COVERAGE 는 스코프밖으로 해석. 베이스라인 기록 완료.

## Step 3 (선택) — 약점 1~2건 보강

- 완전커버 3클래스(ApiResult/MaskingDetector/MessageResolver)에서 **생존 변이(SURVIVED)** 1~2건 선정 → 원인(약한 assertion) 파악 → 테스트 assertion 보강 → 재실행 시 **kill 전환** 확인.
- 운영코드 불변, 테스트코드만. 기본 410 green 유지(+추가 assertion).

**검증(FR-5):** 보강 후 해당 변이 KILLED. `./mvnw test` green.

## Step 4 — 문서화 + 기본빌드 불변 + 커밋

**4-1.** 실행법 문서화(`docs/QUALITY_*` 또는 README 섹션): profile, 명령, 리포트 위치, 스코프 확장법.
**4-2.** **NFR-1 최종 확인**: `./mvnw verify`(JaCoCo 게이트 포함) 통과 + 소요시간 PIT 미포함.
**4-3.** codex 검증 → 커밋 분리: `build(pit): pitest profile + 1차 스코프 (beyond-A)` / (선택)`test: 생존변이 보강` / `docs(pit): 실행법·베이스라인`.
**4-4.** `git push origin master`(듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)
| 항목 | Step |
|---|---|
| FR-1 기본빌드 불변(profile 격리) | Step 1·4 (test 410 green, PIT 미실행) |
| FR-2 PIT 완주+score | Step 1(스모크)·2(전체) |
| FR-3 DB불필요/@SpringBootTest 배제 | Step 2 (targetTests 화이트리스트) |
| FR-4 베이스라인 기록 | Step 2 |
| FR-5 약점 보강(선택) | Step 3 |
| NFR-1 소요 불변 | Step 4 (verify) |
| NFR-4 재현가능 | Step 4 (문서) |

## 리스크 / 함정
| ID | 리스크 | 완화 |
|---|---|---|
| R-1 | junit5-plugin 버전 불일치 → "no tests found" | Step 1 1클래스 스모크로 선검출. 버전핀 1.25.5/1.2.3. 실패 시 조합 조정. |
| R-2 | targetTests 가 @SpringBootTest 끌어옴 | 클래스단위 화이트리스트(애노테이션 의존 X). Step 2 에서 DB 없이 완주 확인. |
| R-3 | PIT 가 기본 lifecycle 에 누수 | profile 내부 + phase 바인딩 없음. Step 1·4 에서 기본 test/verify 불변 검증. |
| R-4 | 의존성 해소 실패(버전 오타/부재) | Step 1 빌드 fast-fail → 즉시 fallback 버전. |
| R-5 | score 낮게 나옴 → 부실 인상 | 1차는 측정·가시화 목적(게이트 아님). 약점은 후속 백로그. |

## 롤백
- pom profile + 문서 추가가 전부 → `git revert`. 운영코드 무변경이라 영향 0. Step 3 보강은 테스트 추가분만.
