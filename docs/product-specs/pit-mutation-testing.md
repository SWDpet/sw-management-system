# 기획서 — PIT 뮤테이션 테스트 도입 (pit-mutation-testing, beyond-A)

- **상태**: v0.2 (codex 수정필요 4건 반영 — 사용자 최종승인 대기)
- **작성일**: 2026-06-20 (v0.1) / v0.2 동일자 (codex: excludedTestClasses=클래스명glob·클래스단위 화이트리스트·버전핀 1.25.5/1.2.3·명령형)
- **스프린트명**: `pit-mutation-testing`
- **요청자**: 박욱진 (사용자) — S-tier(beyond-A) 품질 로드맵. "전 항목 A" 천장 위 신호 확보.
- **선행**: S3 테스트=A(JaCoCo 게이트 + green 스위트 410). 본 건은 *커버리지가 못 보는 테스트 강도*를 뮤테이션으로 측정.
- **성격**: **테스트/빌드 인프라 도입**. 운영코드·UI·DB·API 변경 0. UI 키워드 없음 → 디자인팀 자문 대상 아님.

---

## 0. 한 줄 요약

라인 커버리지(현 ~22%)는 "실행됐다"만 보장할 뿐 "검증한다"는 보장하지 못한다. **PIT(pitest)** 로 코드에 변이(mutation)를 주입하고 기존 테스트가 그 변이를 *죽이는지(kill)* 측정해, **테스트가 실제 버그를 잡는 강도(mutation score)** 를 수치화한다. 1차는 **고신호 순수-단위 패키지(response/util + 검증된 service 유틸)** 에 스코프해 베이스라인을 잡고, 기본 빌드와 분리(느림)해 온디맨드 실행한다.

## 1. 배경 / 목적

- 커버리지는 "그 라인이 실행됐나"만 본다. assertion 이 약하면(예: 결과를 안 봄) 커버리지는 높아도 버그를 못 잡는다. **뮤테이션 테스트**는 `>`→`>=`, `+`→`-`, `return x`→`return 0`, 조건 부정 등 의도적 변이를 넣고 *테스트가 실패하는지* 본다. 실패=변이 kill(좋은 테스트), 생존=테스트 구멍.
- beyond-A 품질의 핵심 신호: "우리 테스트가 회귀를 실제로 막는가"를 수치(mutation score)로.
- 단, PIT 는 전 클래스 대상이면 느리고 @SpringBootTest(DB) 와 안 맞는다. **스코프·분리**가 도입 성패.

## 2. 범위 (In / Out)

**In**
- **A. PIT 플러그인 도입.** `pitest-maven` + `pitest-junit5-plugin`(JUnit5 필수). 별도 maven profile `pit` 또는 명시 goal 로만 실행 — **기본 `./mvnw test`/`verify` 에 절대 포함 안 함**(느림·CI 영향).
- **B. 1차 스코프(고신호 순수-단위).** `targetClasses` = `com.swmanager.system.response.*`(ApiResult)·`com.swmanager.system.util.*`(MaskingDetector 등)·검증된 service 유틸(예: ExcelExportService rounddown static, LogService action normalize, SwService normalize, i18n.MessageResolver). `targetTests` = 대응 순수-단위 테스트만. **@SpringBootTest/DB-gated 테스트는 제외**(targetTests 화이트리스트 + excludedTestClasses).
- **C. 베이스라인 측정 + 기록.** 1차 스코프 mutation score(killed/총 변이) 측정 → 리포트(`target/pit-reports`) 확인 → 베이스라인 수치를 docs 에 기록.
- **D. 약점 1~2건 보강(선택, 시간 허용 시).** 생존 변이(surviving mutant) 중 명백한 테스트 구멍 1~2개를 골라 assertion 보강 → kill 전환 시연(뮤테이션 테스트의 효용 입증).
- **E. 실행법 문서화.** README/QUALITY 문서에 "PIT 돌리는 법"(profile, 스코프 조정, 리포트 위치) 추가.

**Out (이번 미포함 — 후속)**
- **전 패키지 PIT**: 컨트롤러/@SpringBootTest 영역은 느리고 DB 의존 → 후속.
- **mutationThreshold 게이트(빌드 실패 조건)**: 1차는 *측정만*. 게이트(ratchet)는 베이스라인 안정 후 별도 스프린트(JaCoCo 게이트 패턴 차용).
- **CI 파이프라인 연동**: 별도.
- 운영 로직 리팩토링: 본 건은 측정 인프라. 단 D의 테스트 보강은 테스트코드만.

## 3. 개발자 스토리

> 유지보수자로서, 커버리지 22% 라는 숫자만으로는 "이 테스트가 실제 회귀를 막는지" 확신할 수 없다. 핵심 유틸/응답 로직에 변이를 넣어 테스트가 그걸 잡아내는지(mutation score) 보고, 약한 테스트를 짚어 보강하고 싶다.

## 4. 기능 요구사항 (FR)

| ID | 요구사항 |
|---|---|
| FR-1 | `pitest-maven` + `pitest-junit5-plugin` 도입. **별도 profile/goal** 로만 실행. 기본 `./mvnw test`·`./mvnw verify` 동작·소요시간 불변(PIT 미실행). |
| FR-2 | 1차 스코프(response/util + 지정 service 유틸)에서 PIT 가 **정상 완주**하고 mutation score(killed/생존/총) 리포트 생성. |
| FR-3 | @SpringBootTest/DB-gated 테스트는 PIT 실행에서 제외(느림·DB 불필요). PIT 는 DB 없이 완주. |
| FR-4 | 베이스라인 mutation score 를 문서에 기록. 생존 변이 목록에서 약점 패턴 식별. |
| FR-5 (선택) | 생존 변이 1~2건 → 테스트 assertion 보강으로 kill 전환. 기존 410 green 유지. |

## 5. 설계

### 5-1. pom 구성 (codex 반영 — 버전핀·클래스단위 화이트리스트)
```xml
<profile>
  <id>pit</id>
  <build><plugins>
    <plugin>
      <groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId>
      <version>1.15.0</version>                      <!-- 구현 채택(안정 조합). codex 제안 1.25.5 는 후속 게이트시 상향 -->
      <dependencies>
        <dependency>                                  <!-- 플러그인 dependency (프로젝트 dep 아님) -->
          <groupId>org.pitest</groupId>
          <artifactId>pitest-junit5-plugin</artifactId>
          <version>1.2.1</version>                    <!-- 1.15.0 호환 -->
        </dependency>
      </dependencies>
      <configuration>
        <targetClasses>                                <!-- 클래스 단위(패키지 와일드카드 X) -->
          <param>com.swmanager.system.response.ApiResult</param>
          <param>com.swmanager.system.util.MaskingDetector</param>
          <param>com.swmanager.system.i18n.MessageResolver</param>
          <param>com.swmanager.system.service.ExcelExportService</param>
          <param>com.swmanager.system.service.SwService</param>
          <param>com.swmanager.system.service.LogService</param>
        </targetClasses>
        <targetTests>                                  <!-- 대응 순수-단위만 정확히 -->
          <param>com.swmanager.system.response.ApiResultTest</param>
          <param>com.swmanager.system.util.MaskingDetectorTest</param>
          <param>com.swmanager.system.i18n.MessageResolverTest</param>
          <param>com.swmanager.system.i18n.MessageResolverFallbackTest</param>
          <param>com.swmanager.system.service.ExcelExportServiceRounddownTest</param>
          <param>com.swmanager.system.service.SwServiceNormalizeTest</param>
          <param>com.swmanager.system.service.LogServiceActionNormalizeTest</param>
        </targetTests>
        <excludedTestClasses>                          <!-- 보강 안전망(클래스명 glob) -->
          <param>*IT</param><param>*IntegrationTest</param>
          <param>com.swmanager.system.SwManagerApplicationTests</param>
        </excludedTestClasses>
        <timestampedReports>false</timestampedReports>
        <outputFormats><param>HTML</param><param>XML</param></outputFormats>
        <!-- mutationThreshold 미설정(1차 측정만) -->
      </configuration>
    </plugin>
  </plugins></build>
</profile>
```
- 실행(컴파일 보장): `./mvnw -Ppit test-compile org.pitest:pitest-maven:mutationCoverage`. lifecycle phase 바인딩 없이 명시 goal.
- ⚠ `excludedTestClasses` 는 **클래스명 glob**(애노테이션 아님, codex). 따라서 1차 안전은 **targetTests 화이트리스트**로 보장(지정 테스트만 실행 → @SpringBootTest 자동 배제). excluded 는 보조.
- ExcelExportService/SwService/LogService 는 일부 메서드만 순수-단위 커버 → 미커버 메서드 변이는 **NO_COVERAGE**(SURVIVED 와 구분되어 리포트됨). 신호 해석 시 NO_COVERAGE 는 "이 스코프 밖" 으로 읽는다.

### 5-2. 스코프 확정 원칙
- **순수-단위 테스트가 대응하는 클래스만** 1차 타깃(targetClasses)·그 테스트만 targetTests. 개발계획 Step 0 에서 클래스↔테스트 정확 매핑 확정(이미 실측: ApiResult/MaskingDetector/MessageResolver 는 완전 순수-단위, ExcelExportService rounddown·SwService normalize·LogService action 은 부분).
- DB/Spring 컨텍스트 필요 클래스(컨트롤러·@SpringBootTest)는 targetTests 화이트리스트에서 자동 배제.

## 6. 비기능 요구사항 (NFR)

| ID | 요구사항 |
|---|---|
| NFR-1 (안전망) | 기본 `./mvnw test` 410 green·소요시간 **불변**(PIT 는 profile 밖에서 안 돎). |
| NFR-2 | PIT 1차 실행 소요 **수 분 내**(스코프 좁게). DB 불필요. |
| NFR-3 | 도입은 pom + 문서 + (선택)테스트보강만. 운영코드 0 변경. |
| NFR-4 | 재현가능: 문서의 명령으로 누구나 같은 리포트 생성. |

## 7. 리스크 / 함정

| ID | 리스크 | 완화 |
|---|---|---|
| R-1 | PIT 가 @SpringBootTest 까지 끌어와 느리거나 DB 없어서 실패 | targetTests 화이트리스트 + excludedTestClasses. 순수-단위만. |
| R-2 | targetClasses 에 테스트 없는 클래스 → score 0·노이즈 | Step 0 매핑으로 *테스트 있는 클래스만* 타깃. |
| R-3 | pitest-junit5-plugin 버전 불일치로 "no tests found" | 검증된 호환 버전 핀(개발계획). 1클래스로 스모크 후 확장. |
| R-4 | 기본 빌드에 PIT 누수 → CI/일상 빌드 느려짐 | profile 격리 + goal 명시 호출. 기본 lifecycle 바인딩 금지. NFR-1 로 검증. |
| R-5 | 변이 score 가 낮게 나와 "테스트 부실" 인상 | 1차는 *측정·가시화*가 목적. 게이트 아님(Out). 약점은 후속 보강 백로그. |

## 8. 단계 (개발계획서에서 상세화)

1. **Step 0**: main 클래스 ↔ 순수-단위 테스트 매핑 확정 + 호환 PIT 버전 핀.
2. **Step 1**: pom `pit` profile + 플러그인. 1클래스(예: ApiResult) 스모크 — PIT 완주·리포트 생성 확인.
3. **Step 2**: 1차 스코프로 확장 실행 → mutation score 베이스라인 측정·기록.
4. **Step 3(선택)**: 생존 변이 1~2건 테스트 보강 → kill 전환.
5. **Step 4**: 실행법 문서화 + 기본 빌드 불변 검증(NFR-1) → codex 검증 → 커밋·푸시.

## 9. 완료 기준 (DoD)

- `./mvnw -Ppit ...mutationCoverage` 가 DB 없이 완주, mutation score 리포트 생성.
- 기본 `./mvnw test` 410 green·소요 불변(NFR-1).
- 베이스라인 score + 약점 패턴 문서화. (선택) 약점 1~2건 보강.
- codex 검토 통과 + 사용자 승인.

---

### codex 검토 라인 (workflow)
> 요청 → **기획서(본 문서)** → codex 검토 → 사용자 최종승인 → 개발계획서 → codex 검토 → 승인 → 구현 → codex 검증 → 작업완료.
