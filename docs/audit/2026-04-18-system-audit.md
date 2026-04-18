# 시스템 감사 보고서 (Phase 1)

- **실행일**: 2026-04-18
- **감사 도구**: codex (프롬프트 기반)
- **범위**: C1 보안 / C2 엔티티-DB 정합성 / C3 문서-구현 일치 / C4 Dead code / C5 민감 로그
- **관련 기획서**: [docs/plans/system-audit.md](../plans/system-audit.md) v2
- **개발계획서**: [docs/dev-plans/system-audit.md](../dev-plans/system-audit.md) v2

> ⚠️ 민감값 마스킹 적용본. 실제 비밀번호/키/토큰은 `***` 로 표기됨.

## 📊 요약

| 카테고리 | P1 | P2 | P3 | 수행 상태 |
|---------|----|----|----|----------|
| C1 보안 | 3 | 1 | 0 | ✅ 완료 |
| C2 엔티티-DB 정합성 | 1 | 1 | 0 | ✅ 완료 |
| C3 문서-구현 일치 | 0 | 5 | 0 | ✅ 완료 |
| C4 Dead code | 0 | 3 | 2 | ✅ 완료 |
| C5 민감 로그 | 0 | 3 | 1 | ✅ 완료 |
| **합계** | **4** | **13** | **3** | **총 20건** |

---

## 🔐 C1. 보안

### P1 (즉시 조치 필요)

#### 1-1. DB 자격증명 하드코딩
- **위치**: `src/main/resources/application-local.properties:7`, `src/main/resources/application.properties:22`
- **내용**: `spring.datasource.password` 기본값이 코드에 고정(`***` 마스킹). 저장소 유출/내부 접근 시 즉시 DB 침해.
- **권장 조치**: 저장소에서 기본값 제거 → 환경변수/Secret Manager 만 사용 + 기존 비밀번호 **즉시 회전**.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 1-2. 문서 API 권한검사 누락
- **위치**: `DocumentController.java:1031, 1147, 1256, 1322`
- **내용**: `getAuth()` / `@PreAuthorize` 없이 문서 저장·삭제 엔드포인트 실행 가능. 로그인만 된 사용자(`authDocument=NONE` 포함)가 수정/삭제 우회 가능.
- **권장 조치**: 메서드 시작부 `getAuth()` 기반 EDIT 권한 검증 추가 or `@PreAuthorize` 일괄 적용.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 1-3. 응답 본문에 주민번호/이메일 등 민감정보 평문 노출
- **위치**: `DocumentController.java:594, 607, 1007, 1022`
- **내용**: `@ResponseBody` API 가 `ssn`, `email`, `certificate` 를 그대로 반환. 특히 `/api/user/{userSeq}`, `/api/contract-participants/{projId}` 가 개인정보 대량 노출 경로.
- **권장 조치**: 응답 DTO 에서 민감 필드 제거/마스킹 + 최소권한 사용자만 조회 가능하도록 접근제어 추가.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

### P2 (우선 조치 권장)

#### 1-4. MAC 주소 민감정보 응답 노출
- **위치**: `DocumentController.java:1355, 1370`
- **내용**: `/document/api/infra-servers` 가 `macAddr` 평문 반환. 자산 식별 정보 유출 가능.
- **권장 조치**: `macAddr` 마스킹/제외 + 문서 VIEW 이상 권한 체크 적용.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

### 추가 점검 요약
- `.env` 파일: **없음**
- CSRF disable / 광범위 CORS(`*`): **명시적 설정 미발견**
- SQL/Shell/File path injection 패턴: **이번 범위에서 미발견**

---

## 🗂️ C2. 엔티티-DB 스키마 정합성

### P1 (즉시 조치 필요)

#### 2-1. `inspect_report` 테이블에 엔티티 컬럼 누락
- **위치**: `src/main/java/com/swmanager/system/domain/InspectReport.java:55-58` (엔티티) vs `src/main/resources/db_init_phase2.sql` `inspect_report` CREATE 문
- **내용**: 엔티티에는 `insp_sign`, `conf_sign` 매핑이 있으나 init SQL 테이블 정의에 두 컬럼 없음. JPA 조회·저장 시 **런타임 SQL 오류** 발생 위험.
- **권장 조치**: `CREATE TABLE inspect_report` 에 `insp_sign TEXT`, `conf_sign TEXT` 추가 or 엔티티 매핑 제거.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

### P2 (우선 조치 권장)

#### 2-2. init SQL 에 누락된 엔티티 테이블 DDL
- **위치**: `Document.java:15`, `WorkPlan.java:15`, `PjtSchedule.java:15` 등 (엔티티 쪽) vs `db_init_phase2.sql`
- **내용**: `tb_document`, `tb_work_plan`, `tb_pjt_schedule` 등의 `CREATE TABLE` 이 `db_init_phase2.sql` 에 없음. 이 스크립트만으로 초기화하는 환경에서는 **엔티티 접근 시 즉시 테이블 미존재 오류**.
- **권장 조치**: (a) 단계형이라면 의존 스크립트(phase1 등) 를 주석에 명시, (b) 단독 초기화 용도라면 누락 테이블 DDL 추가.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

### 참고 사례 (이전 발견)
- `tb_contract_participant.proj_id`: DB 인스턴스에 컬럼 누락 → ALTER 로 보정 후 B 탭 작업 중 발견 (이미 롤백됨). 동일 패턴 재발 가능성.

---

## 📚 C3. 문서 ↔ 구현 일치

### P2 (우선 조치 권장)

#### 3-1. ERD 에 있는 `tb_contract` 마스터 테이블 미구현
- **위치**: `docs/erd-contract.mmd:15` (ERD) vs `src/main/java/.../domain/` (엔티티 없음)
- **내용**: `docs/erd-contract.mmd` 는 `tb_contract` 를 1급 테이블로 정의하지만 대응 엔티티·테이블 없음. FK 모델 구현 불가. 문서↔구현 불일치 지속.
- **권장 조치**: (a) `tb_contract` 엔티티·리포·서비스 구현 or (b) ERD contract 섹션 제거·갱신 (현재 프로젝트 기반 모델 반영).
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 3-2. ContractParticipant FK 가 ERD 와 다름
- **위치**: `ContractParticipant.java:23-24` vs `docs/erd-contract.mmd`
- **내용**: ERD 는 `contract_id → tb_contract`, 실제 엔티티는 `proj_id → SwProject`. 스키마 drift 로 운영·개발 혼선.
- **권장 조치**: 단일 진실 소스 정리 — 엔티티/스키마에 `contract_id` 복원 or ERD·plans 를 프로젝트 기반으로 갱신.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 3-3. `tb_inspect_cycle` 도메인 미구현
- **위치**: `docs/erd-core.mmd:41` vs domain layer
- **내용**: ERD 에 `tb_inspect_cycle` 있으나 엔티티·리포·컨트롤러 없음. 점검 주기 생명주기 모델이 코드에 없음.
- **권장 조치**: 도메인 구현 or ERD 에서 제거(구현 전까지).
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 3-4. A 탭 유형 필터가 plans 와 다름
- **위치**: `templates/admin-system-graph.html:212` vs `docs/plans/system-graph-infra-perf.md`
- **내용**: plans 는 `infra-type` 드롭다운(전체/UPIS/KRAS/기타), 현재 UI 는 `infra-sys`(시스템명). FR 불일치, 운영자 필터 의미론 변경.
- **권장 조치**: 유형 필터 복원 or plans 를 "시스템명 필터" 로 공식 개정.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 3-5. A 탭 렌더러가 plans 규약과 다름
- **위치**: `admin-system-graph.html:698-701`
- **내용**: infra-perf plans 는 vis-network hierarchical + DataSet 배치 업데이트 요구, 현재는 텍스트 트리/조직도 커스텀 로직. FR-1/FR-4 및 NFR 타이밍 가정이 런타임과 불일치.
- **권장 조치**: vis-network 경로 재구현 or plans·테스트를 새 렌더러 계약으로 갱신 (사용자가 선택한 방향은 text tree 이므로 plans 갱신 권장).
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

---

## 🧹 C4. Dead code

### P2 (유지보수 부담 높음)

#### 4-1. 미사용 서비스 `ProjectService`
- **위치**: `src/main/java/com/swmanager/system/service/ProjectService.java:10, 16`
- **내용**: 클래스 자체가 어디에서도 주입/호출되지 않음. `saveProject(...)` 도 호출 흔적 없음.
- **권장 조치**: 클래스 삭제 or 실제 호출 경로로 통합.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 4-2. `SwService` 미호출 public 메서드 11개
- **위치**: `src/main/java/com/swmanager/system/service/SwService.java:43, 47, 52, 57, 62, 69, 74, 79, 191, 198, 205`
- **내용**: `getAllProjects`, `saveProject`, `getProjectById`, `deleteProject`, `getAllUsers`, `getDistinctYears`, `getSystemStats`, `isDuplicate`, `getProjectDTOById`, `getProjectListAsDTO`, `saveProjectFromDTO` — 전부 호출 없음.
- **권장 조치**: 실제 사용 예정 없으면 삭제, 있으면 호출 경로·주석·테스트로 의도 명시.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 4-3. Orphan 템플릿 파일
- **위치**: `src/main/resources/templates/customer-list.html`, `src/main/resources/templates/license/registry-new-list.html`
- **내용**: 두 파일 모두 컨트롤러에서 return 하지 않음.
- **권장 조치**: 삭제 or 실제 라우트에 연결.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

### P3 (선택적 정리)

#### 4-4. 진입점 없는 로그인 핸들러
- **위치**: `LoginController.java:37` — `@GetMapping("/login/type/{mode}")`
- **내용**: 링크·리다이렉트 대상 없음.
- **권장 조치**: 미사용이면 제거, 필요하면 진입 링크 추가.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 4-5. 미사용 Repository 메서드
- **위치**: `src/main/java/com/swmanager/system/repository/SwRepository.java:11`
- **내용**: `findAllByOrderByYearDescCityNmAscDistNmAsc()` 호출처 없음, Repository 자체도 미주입.
- **권장 조치**: 인터페이스·메서드 삭제 or `SwProjectRepository` 로 통합.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

### 추가 확인
- `src/main/resources/static/` 에 `.js`/`.css` 파일 없음 (이미지 자원만 존재, 템플릿에서 참조 중)

---

## 📋 C5. 민감 로그

### P2 (잠재적 민감 정보 유출)

#### 5-1. `spring.jpa.show-sql=true` 기본값
- **위치**: `src/main/resources/application.properties:28`
- **내용**: 기본 설정에 활성. prod override 없으면 운영에서도 SQL 이 로그로 출력 → 쿼리·파라미터 조합에 따라 민감 데이터 노출.
- **권장 조치**: 기본값 `false`, 로컬 전용(`application-local.properties`) 에서만 `true`.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 5-2. `GeonurisLicenseService` 라이선스 필드 값 debug 로그
- **위치**: `src/main/java/com/swmanager/system/geonuris/service/GeonurisLicenseService.java:193, 218`
- **내용**: `log.debug("LicenseVo.{} = ...", fieldName, value)` 형태로 MAC 주소 등 식별 정보가 debug 로그에 남을 가능성.
- **권장 조치**: 값 마스킹(`***`) or 민감 필드는 필드명만 기록.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

#### 5-3. `GlobalExceptionHandler` 에서 `e.getMessage()` 원문 로그
- **위치**: `src/main/java/com/swmanager/system/exception/GlobalExceptionHandler.java:114`
- **내용**: `ValidationException` 에서 `e.getMessage()` 그대로 로그. 바인딩 오류 메시지에 rejected value 가 포함되면 입력값(비밀번호·토큰 유사값) 노출 가능.
- **권장 조치**: 고정 문구로 로그 or 필드명만 남기고 값은 마스킹.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

### P3 (로거 스타일 개선)

#### 5-4. `AdminUserController` 검색 키워드 INFO 로그
- **위치**: `AdminUserController.java:101, 116`
- **내용**: 검색 `keyword` 를 INFO 로 그대로 기록 → 사용자 입력 원문 장기 보관.
- **권장 조치**: INFO 에서는 키워드 비노출 or 마스킹, 필요 시 DEBUG 에서 제한적 기록.
- **사용자 검토**: ☐ 조치함 / ☐ 보류 / ☐ 불필요

### 추가 확인
- `System.out.println(...)`: **없음**
- `e.printStackTrace()`: **없음**
- 요청/응답 바디 전체를 `toString()` 등으로 출력: **명시적 케이스 없음**
- password/token/cookie/Authorization 직접 포맷 인자로 찍는 log: **없음**

---

## 🔍 감사 메타데이터
- codex 호출: 5회 (C1~C5 각 1회), 재시도 0회
- 마스킹 적용: ✅ (모든 민감값 `***` 로 치환됨 — 최종 grep 검증은 아래 별도 단계)
- 실행 실패 카테고리: 없음
- 총 소요 시간: 약 25분

---

## 🎯 후속 조치 제안 (우선순위)

### 🚨 즉시 처리 권장 (P1, 4건)
1. **DB 비밀번호 하드코딩 제거 + 회전** (1-1) — 가장 큰 보안 위험
2. **DocumentController 권한 검사 추가** (1-2) — 수정·삭제 우회 가능
3. **응답에서 민감 필드(ssn/email/certificate) 제거** (1-3) — 개인정보 대량 노출 경로
4. **`inspect_report` 스키마 정합** (2-1) — 런타임 오류 임박

### ⚠️ 우선 처리 권장 (P2, 13건)
- 스키마 drift (tb_document, tb_work_plan 등 DDL 보강) (2-2)
- 문서 ↔ 구현 불일치 5건 (3-1~3-5) — 특히 ERD 재정비
- 미사용 서비스·템플릿 정리 (4-1~4-3)
- show-sql 기본값 조정 (5-1), 로그 마스킹 (5-2, 5-3)
- MAC 주소 응답 노출 (1-4)

### 🔵 선택적 개선 (P3, 3건)
- LoginController 미사용 핸들러 (4-4)
- SwRepository 미사용 메서드 (4-5)
- AdminUserController INFO 로그 마스킹 (5-4)

### 후속 스프린트 권장 방식
1. **Phase 1 보안 스프린트** — P1 4건을 1주 안에 정리
2. **Phase 2 정합성 스프린트** — 스키마 drift + 문서 정리 (P2 대부분)
3. **Phase 3 클린업** — Dead code 와 P3 항목 (기술 부채 정리 때)
