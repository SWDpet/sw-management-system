# [개발계획서] 감사 후속조치 스프린트 2c — P2 보안·로깅 4건

- **작성팀**: 개발팀
- **작성일**: 2026-04-19
- **근거 기획서**: [docs/product-specs/audit-fix-p2-security-logging.md](../plans/audit-fix-p2-security-logging.md) (v1, 승인)
- **상태**: 초안 (codex 검토 대기)

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 사전 스캔 / 회귀 근거 확보
1. `rg -n "macAddr" src/main/resources/templates/` → `doc-inspect.html` 등에서 **0 hit** (사용처 없음) 재확인.
2. `rg -n "org.hibernate.SQL|BasicBinder" src/main/resources/` → `logback-spring.xml` 에만 hit.
3. `git check-ignore -v src/main/resources/application-local.properties` → `.gitignore:46` hit 재확인.

### Step 2 — 1-4 `DocumentController.getInfraServers` 보호·민감키 제거
2-1. 메서드 진입부에 `getAuth()` 검사 추가:
```java
if ("NONE".equals(getAuth())) {
    Map<String, Object> forbidden = new LinkedHashMap<>();
    forbidden.put("success", false);
    forbidden.put("error", Map.of("code", "FORBIDDEN", "message", "조회 권한이 없습니다"));
    return ResponseEntity.status(403).body(forbidden);
}
```
2-2. `m.put("macAddr", s.getMacAddr());` 한 줄 삭제 (line 1466).
2-3. 응답 구조 최종 키 목록 주석 추가: `serverId, serverType, ipAddr, osNm, serverModel, serialNo, cpu/memory/disk/network/power/os/rack/note, softwares` (macAddr 제외됨).

### Step 3 — 5-1 `application.properties` + `logback-spring.xml` + example 파일
3-1. `application.properties`:
```properties
# 운영 기본값 OFF. 로컬 디버깅은 application-local.properties.example 참조.
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
```
3-2. `logback-spring.xml`:
- `<logger name="org.hibernate.SQL" level="DEBUG">` → `level="WARN"`
- `<logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE">` → `level="WARN"`
- 두 로거 주석에 "운영 기본 WARN. 로컬 디버깅 시 DEBUG/TRACE 로 임시 변경" 명시.

3-3. 신규 `src/main/resources/application-local.properties.example` 작성:
```properties
# ========================================
# 로컬 개발 예시 (git 포함). 실제 파일은:
#   cp application-local.properties.example application-local.properties
# 로 복사하여 사용 — application-local.properties 는 .gitignore 대상.
#
# 활성 방법: ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# ========================================

server.port=9090
spring.datasource.url=jdbc:postgresql://211.104.137.55:5881/SW_Dept
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

# 로컬 SQL 디버그 활성
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# logback 로거를 로컬에서만 DEBUG 로 올리려면 logback-spring.xml 에
# <springProfile name="local"> 블록을 추가하거나, Java 실행 시
# -Dlogging.level.org.hibernate.SQL=DEBUG 옵션을 붙인다.
```

### Step 4 — 5-2 `GeonurisLicenseService` debug 로그 값 제거
4-1. `setStr(...)` 내:
- `log.debug("LicenseVo.{} = \"{}\"", fieldName, value);` → `log.debug("LicenseVo.{} 세팅 완료", fieldName);`
- setter fallback: `log.debug("LicenseVo.set{}(\"{}\") [setter]", fieldName, value);` → `log.debug("LicenseVo.set{} [setter] 세팅 완료", fieldName);`

4-2. `setInt(...)` 내:
- `log.debug("LicenseVo.{} = {}", fieldName, value);` → `log.debug("LicenseVo.{} 세팅 완료", fieldName);`

4-3. `log.warn(...)` 실패 로그들(`ex.getMessage()` 포함) — 유지.

### Step 5 — 5-3 `GlobalExceptionHandler.handleValidationException` 로그 안전화
5-1. line 114:
- `log.error("ValidationException: {}", e.getMessage());` → `log.error("ValidationException 발생 (path={})", request.getRequestURI());`

### Step 6 — 감사 보고서 체크박스
- 1-4 / 5-1 / 5-2 / 5-3 각각 ☑ 조치함 + 1줄 요약.

### Step 7 — 빌드 / 재기동 / 스모크
7-1. `./mvnw -q compile` — BUILD SUCCESS.
7-2. `bash server-restart.sh` — 성공 + ERROR 0건.
7-3. 기동 후 10초 뒤 `server.log` 에서 검증:
- `grep -c "org.hibernate.SQL.*DEBUG" server.log` → **0**
- `grep -c "LicenseVo\..* = \"" server.log` → 0 (라이선스 발급 경로가 기동 직후 호출되지 않으므로 일반적으로 0)
7-4. 스모크 HTTP (본문 포함 검증):
- 비인증: `curl -s -o /dev/null -w "%{http_code}" "http://localhost:9090/document/api/infra-servers?distNm=x&sysNmEn=y"` → 302 (로그인 리다이렉트).
- 인증 쿠키(EDIT 사용자) 보유 시 GET 본문 검증: `curl -s --cookie "SWMANAGER_SESSION=..." "http://localhost:9090/document/api/infra-servers?distNm=양양군&sysNmEn=UPIS" | grep -c '"macAddr"'` → **0** (키 없음 확인).
- 인증 쿠키(NONE 사용자) 보유 시: 위 응답이 403 JSON (`"code":"FORBIDDEN"`) 인지 `grep '"FORBIDDEN"'` 으로 확인.
- 수동 쿠키 확보 어려우면, 최소한 302 + 변경 커밋 직후 EDIT 사용자로 화면(문서 점검내역서 작성)에서 서버 정보 불러와도 오류 없는지 UI 확인 (T2b 가 정적으로 민감 필드 부재를 이미 보장).

### Step 8 — codex Specs 검증

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | `getInfraServers` 권한 가드 존재 | `rg -n "NONE.*getAuth|getAuth.*NONE" src/main/java/com/swmanager/system/controller/DocumentController.java` → getInfraServers 메서드 근처 1 hit | ≥ 1 |
| T2 | macAddr 응답 키 제거 | `rg -n 'm\.put\("macAddr"' src/main/java/com/swmanager/system/controller/DocumentController.java` | 0 hits |
| T2b | infra-servers 응답 민감필드 회귀 방지 (FR-1-4-D) | `getInfraServers` 메서드 블록(line 1450~1490) 내 `rg "accId\|accPw\|acc_id\|acc_pw\|sw_acc_id\|sw_acc_pw\|macAddr"` | 0 hits |
| T3 | show-sql OFF | `rg -n "spring\.jpa\.show-sql=false" src/main/resources/application.properties` | 1 hit |
| T4 | format_sql OFF | `rg -n "format_sql=false" src/main/resources/application.properties` | 1 hit |
| T5 | logback SQL WARN | `rg -n "org.hibernate.SQL.*WARN" src/main/resources/logback-spring.xml` | 1 hit |
| T5b | logback BasicBinder WARN | `rg -n "BasicBinder.*WARN" src/main/resources/logback-spring.xml` | 1 hit |
| T6 | 로컬 예시 파일 존재 | `test -f src/main/resources/application-local.properties.example` | exit 0 |
| T7 | GeonurisLicenseService 값 로그 제거 (setter fallback 포함) | `rg -nE 'LicenseVo\.\{\} = \\"|LicenseVo\.set\{\}\\("' src/main/java/com/swmanager/system/geonuris/service/GeonurisLicenseService.java` | 0 hits |
| T8 | Validation getMessage 로그 제거 | `rg -n 'log\.error\("ValidationException: \{\}"' src/main/java/com/swmanager/system/exception/GlobalExceptionHandler.java` | 0 hits |
| T8b | 런타임 `rejected value` 로그 미노출 (NFR-4) | 서버 재기동 후 임의의 /api validation 실패를 1회 유발한 뒤 `grep -c "rejected value" server.log` | 0 |
| T9 | Maven compile | `./mvnw -q compile` | BUILD SUCCESS |
| T10 | 서버 재기동 | `bash server-restart.sh` | `Started SwmanagerApplication` + ERROR 0 |
| T11 | SQL DEBUG 로그 0 | `grep -c "org.hibernate.SQL.*DEBUG" server.log` | 0 |
| T12 | 감사 체크박스 | `grep "☑ 조치함" docs/generated/audit/2026-04-18-system-audit.md` 에 1-4/5-1/5-2/5-3 포함 | 4 추가 |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Step 2 컴파일 실패 | Edit 되돌림 후 `getAuth()` 정의·import 재확인 |
| Step 3 서버 기동 중 SQL 로그가 여전히 나옴 | `logback-spring.xml` 의 상위 `<root level=...>` / `additivity` 확인. 필요 시 `<springProfile>` 로 wrap |
| T11 실패 | show-sql/format_sql/logback 세 값이 실제 적용됐는지 `server.log` 의 startup 블록에서 Hibernate 설정 라인 확인 |
| 라이선스 발급 기능에서 디버깅 필요 | 임시로 `setStr` 내 log.debug 를 원복하거나, `-Dlogging.level.com.swmanager.system.geonuris=DEBUG` 로 재활성 가능 |
| Validation 에러 디버깅 필요 | path 만 남기므로 필요 시 임시로 `log.debug("binding error: {}", e.getMessage())` 추가 (운영 커밋 전 제거) |

---

## 4. 리스크·완화 재확인

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| `NONE` 사용자가 기존에 정상 호출 중인 외부 스크립트 존재 | 낮음 | 해당 API 는 `/document/api/infra-servers` 내부 UI용. 외부 스크립트 가능성 낮음. 회귀 시 `NONE` 예외 팀 문의 경로 안내 |
| `logback-spring.xml` 수정으로 다른 로그 출력까지 영향 | 낮음 | WARN 은 기존 ROOT 레벨(INFO) 보다 한 단계 높음 — 경고 이상은 계속 출력됨. 로그 누락 없음 |
| `.example` 파일을 팀원이 복사 안 해 로컬 SQL 디버그 안 됨 | 낮음 | README 에 안내 추가는 본 스프린트 범위 외 (기획서 주석에 가이드 포함) |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
