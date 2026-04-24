# [기획서] 감사 후속조치 스프린트 2c — P2 보안·로깅 4건

- **작성팀**: 기획팀
- **작성일**: 2026-04-19
- **선행**: `914fa5c` (스프린트 2b P2 Dead code)
- **상태**: 초안 (codex 검토 대기)

---

## 1. 배경 / 목표

감사 P2 보안·로깅 그룹 4건 처리:

| ID | 내용 | 권고 요약 |
|----|------|-----------|
| 1-4 | `/document/api/infra-servers` 가 `macAddr` 평문 반환 | 응답에서 `macAddr` 제외 + 권한검사 추가 |
| 5-1 | `spring.jpa.show-sql=true` 기본값 | 기본 false, 로컬만 true |
| 5-2 | `GeonurisLicenseService` 필드값 debug 로그 (MAC 등) | 값 마스킹 / 필드명만 기록 |
| 5-3 | `GlobalExceptionHandler` 의 `e.getMessage()` 원문 로그 | 고정 문구 + 필드명만 남기기 |

**재검증(2026-04-19)**:
- 1-4: `DocumentController.java:1466` `m.put("macAddr", s.getMacAddr())` 확인. 프런트 `doc-inspect.html` 2곳(680, 1433) 에서 해당 API 를 호출하지만 `macAddr` 필드는 **사용하지 않음** → 응답에서 제거 가능(프런트 영향 0).
- 5-1: `application.properties:29` `spring.jpa.show-sql=true` + `format_sql=true` 확인. `application-local.properties` 에는 없음.
- 5-2: `GeonurisLicenseService.java:193, 200, 218` 에서 value 포함 log.debug 확인.
- 5-3: `GlobalExceptionHandler.java:114` `log.error("ValidationException: {}", e.getMessage())`. `e.getMessage()` 에 `rejected value` 포함 가능.

**목표**: 운영 환경에서 민감정보가 응답·로그를 통해 유출될 확률을 낮추고, 개발 환경 편의성(show-sql 등)은 유지.

---

## 2. 기능 요건 (FR)

### 1-4. infra-servers MAC 주소 응답 노출 제거

| ID | 내용 |
|----|------|
| FR-1-4-A | `DocumentController.getInfraServers` 응답 `Map` 에서 `macAddr` 키 **삭제** (line 1466). |
| FR-1-4-B | 메서드 진입부에 `getAuth()` 권한 검사 추가 — `"NONE".equals(getAuth())` 이면 HTTP 403 (code=`FORBIDDEN`, message=`조회 권한이 없습니다`). 실제 `authDocument` 권한은 **NONE / VIEW / EDIT 3상태** (codex 재검증 반영). VIEW·EDIT 모두 통과, NONE 만 차단 — 감사 권고 "VIEW 이상"에 정확히 부합. |
| FR-1-4-C | 응답 구조 유지를 위해 프런트 호환성 확인: `doc-inspect.html` 에서 `macAddr` 참조 0건(검증 완료). 다른 화면/JS 참조도 `grep` 으로 0건 재확인. |
| FR-1-4-D | `macAddr` 외에 동일 응답의 다른 민감 필드(`acc_id/acc_pw/sw_acc_id/sw_acc_pw`) 가 응답에 **포함되지 않음** 을 재확인. 현 코드에는 포함되지 않지만 회귀 방지를 위해 개발계획서 T# 에 grep 테스트 추가. |

### 5-1. show-sql 기본값 OFF

| ID | 내용 |
|----|------|
| FR-5-1-A | `application.properties` 의 `spring.jpa.show-sql=true` → `false`, `spring.jpa.properties.hibernate.format_sql=true` → `false`. |
| FR-5-1-B | `logback-spring.xml` 의 `<logger name="org.hibernate.SQL" level="DEBUG">` → `level="WARN"`, `<logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE">` → `level="WARN"` 로 변경. (codex 지적 반영: `show-sql=false` 만으로는 SQL 로그가 제거되지 않음 — 두 로거가 별도 경로로 DEBUG/TRACE 출력) |
| FR-5-1-C | **신규 파일** `src/main/resources/application-local.properties.example` (저장소 포함) 에 로컬 개발용 예시 추가: `spring.jpa.show-sql=true`, `spring.jpa.properties.hibernate.format_sql=true`, 그리고 SQL 로거를 다시 DEBUG 로 올리는 방법 주석. `application-local.properties` 자체는 `.gitignore` 유지 — 팀원이 example 을 복사해 사용. |
| FR-5-1-D | `application.properties` 의 JPA 섹션에 주석: "운영 기본값 OFF. 로컬 디버깅은 `cp application-local.properties.example application-local.properties` 후 `-Dspring.profiles.active=local` 실행." |

### 5-2. GeonurisLicenseService 값 마스킹

| ID | 내용 |
|----|------|
| FR-5-2-A | `setStr(...)` 내 `log.debug("LicenseVo.{} = \"{}\"", fieldName, value)` → 값 부분 제거. 새 로그: `log.debug("LicenseVo.{} 세팅 완료", fieldName)` (값 미포함). |
| FR-5-2-B | setter fallback 로그 `log.debug("LicenseVo.set{}(\"{}\") [setter]", fieldName, value)` → `log.debug("LicenseVo.set{} [setter] 세팅 완료", fieldName)`. |
| FR-5-2-C | `setInt(...)` 로그 `log.debug("LicenseVo.{} = {}", fieldName, value)` → int 값은 식별정보가 아님(유효기간 일수 등) 이지만 일관성을 위해 동일 방식: `log.debug("LicenseVo.{} 세팅 완료", fieldName)`. |
| FR-5-2-D | `log.warn(...)` 실패 로그의 `ex.getMessage()` 는 유지 (예외 원인 파악 필수). 단, warn 메시지에 fieldName 만 노출. |

### 5-3. GlobalExceptionHandler Validation 로그 안전화

| ID | 내용 |
|----|------|
| FR-5-3-A | `handleValidationException` 의 `log.error("ValidationException: {}", e.getMessage())` → `log.error("ValidationException 발생 (path={})", request.getRequestURI())` 로 변경. `e.getMessage()` 로그 제거. |
| FR-5-3-B | 응답으로 반환되는 `errorMessage` (BindingResult 의 `getDefaultMessage`) 는 **검증 룰 설명 문구** 로 민감값 포함 가능성 낮음 → 본 범위에서 건드리지 않음. 단, 리스크 표에 "Validation 메시지에 rejected value 포함 케이스 발견 시 별도 조치" 명시. |
| FR-5-3-C | 다른 `GlobalExceptionHandler` 의 로그 (`handleException`, `handleResourceNotFound`, `handleDuplicateResource`) 는 본 범위 외 — 스택트레이스·코드로 디버깅에 필요하므로 유지. 추가 점검은 별도 이슈. |

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 컴파일 성공 (Maven). |
| NFR-2 | 서버 재기동 후 `/document/api/infra-servers` 정상 응답 (macAddr 키 없음, 200/302/403 중 하나). |
| NFR-3 | 기동 직후 `server.log` 에 `org.hibernate.SQL` / `BasicBinder` DEBUG/TRACE 출력 **0건** 달성 (logback WARN 레벨 + show-sql=false 조합). 정량 기준: `grep -c "org.hibernate.SQL.*DEBUG"` = 0. |
| NFR-4 | 5-2/5-3 변경 후 `LicenseVo` 값·`rejected value` 문자열이 로그에 남지 않음 — `grep "LicenseVo\..* = \"" server.log` 0건. |
| NFR-5 | 운영 프로필(`-Dspring.profiles.active=local` 미지정) 기본 동작은 show-sql=false. 로컬 프로필 활성 시 true. (수동 검증 권장) |

---

## 4. 의사결정 / 우려사항

### 4-1. MAC 제거 vs 마스킹 — ✅ 제거 확정
- 프런트에서 `macAddr` 사용 0건 확인 → 응답에서 완전 제거. 마스킹 문자열("XX:XX:...") 도 불필요.
- 향후 MAC 이 필요한 기능이 생기면 별도 `/secure` 엔드포인트 (EDIT+ 권한) 로 제공 (1-3 조치의 Option C 패턴 재사용).

### 4-2. show-sql 로컬 유지 방식 — ✅ .example 템플릿 신설 (codex 지적 반영)
- `application-local.properties` 는 `.gitignore` 에 등록되어 PR 반영 불가. 대신 `application-local.properties.example` 을 저장소에 포함시켜 팀원이 복사해 쓰도록 가이드.
- SQL 로그 실효성 확보를 위해 `logback-spring.xml` 의 SQL/BasicBinder 로거 레벨도 함께 낮춤 (본 스프린트 포함).

### 4-3. GeonurisLicenseService 값 노출 범위 — ✅ 전체 값 제거
- MAC 뿐 아니라 모든 라이선스 필드값(만료일·D-day 등 식별 가능) 을 debug 로그에서 제거. 필드명만 남겨 디버깅은 가능.

### 4-4. Validation 로그 전체 제거 vs 부분 마스킹 — ✅ 부분(message만 제거)
- `e.getMessage()` 만 제거, path 는 유지 → 어느 경로에서 검증 실패했는지는 알 수 있음.
- rejected value 가 `getDefaultMessage()` 에 포함되는 경우가 관찰되면 별도 스프린트에서 추가 마스킹.

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller (수정) | `src/main/java/com/swmanager/system/controller/DocumentController.java` | `getInfraServers`: `macAddr` 제거 + `getAuth() != NONE` 검사 |
| Config (수정) | `src/main/resources/application.properties` | `show-sql=false`, `format_sql=false` + 로컬 활성 가이드 주석 |
| Config (수정) | `src/main/resources/logback-spring.xml` | `org.hibernate.SQL` DEBUG→WARN, `BasicBinder` TRACE→WARN |
| Config (신규) | `src/main/resources/application-local.properties.example` | 로컬 SQL 디버그 활성 예시 (복사본 용도) |
| Service (수정) | `src/main/java/com/swmanager/system/geonuris/service/GeonurisLicenseService.java` | debug 로그 3곳 값 제거 |
| Exception (수정) | `src/main/java/com/swmanager/system/exception/GlobalExceptionHandler.java` | `handleValidationException` 로그 수정 |
| Docs (수정) | `docs/generated/audit/2026-04-18-system-audit.md` | 1-4, 5-1, 5-2, 5-3 체크박스 ☑ 조치함 |

**수정 6 파일 + 신규 1 파일 (`application-local.properties.example`). DB/Entity/API 계약 변경은 `macAddr` 응답 키 제거 1건 (프런트 미사용 확인됨).**

---

## 6. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| `macAddr` 응답 제거로 외부 통합/스크립트 회귀 | 낮음 | 코드베이스 내 사용처 0 확인. 외부 통합은 해당 엔드포인트가 `/document/api/*` 내부 API 라 외부 노출 가능성 낮음 |
| show-sql OFF 로 인한 개발자 디버깅 불편 | 낮음 | `application-local.properties` 에 명시 유지 + README/운영문서에 `-Dspring.profiles.active=local` 활성 방법 언급 |
| debug 로그 값 제거로 라이선스 발급 문제 디버깅 어려움 | 중간 | 필드명 + 예외 메시지(`ex.getMessage()`) 는 warn 레벨에 유지. 필요 시 DEBUG → 별도 `@Profile("local")` 부가 로거로 확장 가능 |
| Validation 메시지 제거로 400 원인 추적 어려움 | 낮음 | path 는 유지됨. 응답 본문(`errorMessage`)에는 규칙 설명 포함되므로 사용자도 확인 가능 |
| `getAuth()` 만 기반으로 판별하여 VIEW 권한이 별도 존재할 경우 과도한 차단 | 낮음 | 코드베이스 현황상 EDIT/NONE 2상태 — 현재 구조에서는 문제 없음. VIEW 도입 시 재조정 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
