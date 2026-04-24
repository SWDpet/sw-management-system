# [기획서] 감사 후속조치 스프린트 1 — P1 보안 4건

- **작성팀**: 기획팀
- **작성일**: 2026-04-18
- **선행**: 감사 보고서 `docs/generated/audit/2026-04-18-system-audit.md`
- **상태**: 초안 (codex 검토 대기)

---

## 1. 배경 / 목표

### 배경
Phase 1 감사에서 **P1 (즉시 조치 필요)** 로 분류된 4건:
- **1-1** DB 비밀번호 하드코딩 (application.properties 기본값)
- **1-2** DocumentController 권한 검사 누락 (4개 엔드포인트)
- **1-3** 응답 DTO 에 ssn/email/certificate 평문 노출 (4곳)
- **2-1** inspect_report 엔티티-DB 컬럼 불일치 (insp_sign, conf_sign)

### 목표
- 4건을 **한 스프린트**로 묶어 정식 워크플로우로 처리.
- 보안/데이터 정합성 긴급 이슈를 일관된 커밋에 담아 추적성 확보.
- 사용자 조치 필요 항목(DB 비밀번호 실제 회전 등)은 명확히 분리.

---

## 2. 사용자 시나리오

스프린트 완료 후:
1. 관리자가 기존과 동일하게 업무 수행 — 권한 있는 사용자만 문서 수정/삭제 가능 (이전에는 누구나 가능)
2. `/api/user/{userSeq}`, `/api/contract-participants/{projId}` 응답에서 주민번호·이메일·자격증 등 민감 필드 사라짐 (마스킹 or 제거)
3. 서버 재기동 시 DB 비밀번호는 환경변수에서만 읽음 (코드에 기본값 없음) — 환경변수 누락 시 에러 → 운영자가 즉시 인지
4. `inspect_report` 테이블에 `insp_sign`, `conf_sign` 컬럼이 존재 → 점검 리포트 저장/조회 시 SQL 오류 없음

---

## 3. 기능 요건 (FR)

### 1-1. DB 비밀번호
| ID | 내용 |
|----|------|
| FR-1-1 | `application.properties`·`application-local.properties` 에서 `spring.datasource.password` 의 **하드코딩된 기본값 제거**. 환경변수 `${DB_PASSWORD}` 참조만 유지. |
| FR-1-2 | 환경변수 미설정 시 **서버 부팅 실패** (Spring Boot 기본 동작 — placeholder 해결 실패). 이를 명확히 README 나 주석에 명시. |
| FR-1-3 | **사용자 조치 영역**: (a) DB 에서 `ALTER USER postgres PASSWORD '<새비밀번호>'` 실행 — 코드·본 스프린트 범위 밖 (b) 운영 환경변수에 새 비밀번호 설정 — 운영자 책임 |

### 1-2. DocumentController 권한 검사
| ID | 내용 |
|----|------|
| FR-2-1 | 4개 엔드포인트에 기존 패턴 `if (!"EDIT".equals(getAuth())) { 403 return }` 추가: `DocumentController.java:1031` (POST /api/contract-participants), `:1147`, `:1256`, `:1322` |
| FR-2-2 | 403 반환 시 JSON `{error:{code:"FORBIDDEN",message:"수정 권한이 없습니다"}}` 로 일관 (시스템 감사 FR-13 참조) |
| FR-2-3 | 변경 후 **비-EDIT 권한 사용자가 수정 시도 → 403** 수동 테스트 |

### 1-3. 응답 민감 필드 제거
| ID | 내용 |
|----|------|
| FR-3-1 | `DocumentController.java:594, 607, 1007, 1022` 의 @ResponseBody 응답에서 **`ssn`, `email`, `certificate`, `password` 등 민감 필드 제거**. |
| FR-3-2 | 제거 방식: (a) `Map<String,Object>` 직접 조립 시 해당 key 넣지 않음, (b) 엔티티 반환 시 DTO 로 변환하여 허용 필드만. 둘 중 기존 코드 패턴에 맞춰 선택. |
| FR-3-3 | **프론트 영향 확인**: 기존 화면에서 이 필드를 사용하는지 점검. 사용 중이면 프론트도 함께 수정 (이번 범위 포함). |

### 2-1. inspect_report 스키마 정합
| ID | 내용 |
|----|------|
| FR-4-1 | `db_init_phase2.sql` 의 `inspect_report` CREATE 문에 `insp_sign TEXT`, `conf_sign TEXT` 컬럼 추가 |
| FR-4-2 | 기존 DB 인스턴스 대응 — 멱등 `ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS insp_sign TEXT; ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS conf_sign TEXT;` 도 포함 (운영 DB 이미 생성된 경우 대비) |
| FR-4-3 | 서버 재기동 시 InspectReport 엔티티 접근이 SQL 오류 없이 성공해야 함 |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 이번 스프린트는 **기능 변경 없음** — 기존 사용자 경험(권한 있는 경우) 동일. |
| NFR-2 | 서버 재기동 필수. 재기동 시간 30초 이내. |
| NFR-3 | 응답 민감 필드 제거 후 **관련 화면 회귀 테스트** 필수 — 사용자 관리/과업참여자/점검 화면. |
| NFR-4 | 기존 테스트(manual) 통과 확인. 깨진 테스트는 본 스프린트 범위 내에서 수정 또는 별도 이슈로 분리. |
| NFR-5 | 변경 후 감사 보고서의 해당 4개 항목 체크박스는 "☑ 조치함" 으로 업데이트 (최종 커밋에 포함). |

---

## 5. DB팀 자문

### 영향받는 테이블
- **`inspect_report`**: `insp_sign TEXT`, `conf_sign TEXT` 2컬럼 추가 (nullable)

### 마이그레이션
- 기존 DB 에는 **`ALTER TABLE ... ADD COLUMN IF NOT EXISTS`** 로 멱등 적용.
- 기존 데이터 손실 없음 (새 컬럼은 NULL 로 시작).

### DB 비밀번호 회전
- 본 스프린트에서는 **코드 기본값만 제거**. 실제 `ALTER USER` 는 사용자 수동 실행.
- DB 세션이 연결된 상태에서 비밀번호 바꾸면 기존 세션은 유지, 새 연결부터 새 비밀번호 필요 — 무중단 가능.

**DB팀 의견: 승인 (마이그레이션 안전).**

---

## 6. 의사결정 / 우려사항

### 6-1. DB 비밀번호 회전 실행 주체 — ✅ 확정
- **코드 변경**: Claude (이번 스프린트)
- **실제 DB `ALTER USER` + 환경변수 설정**: 사용자 (별도)
- 개발계획서에서 **사용자 조치 체크리스트** 명시

### 6-2. 민감 필드 제거 범위 — ✅ 확정
- 감사 보고서가 명시한 4개 라인만 우선 처리. 그 외 비슷한 패턴이 있더라도 본 스프린트 범위 밖 (별도 스프린트에서 추가 감사 후 조치).

### 6-3. getAuth() 실패 시 응답 — 확정
- 기존 패턴(HTTP 403 + JSON body) 따름. 새 JSON 포맷은 FR-2-2 참조.

### 6-4. insp_sign/conf_sign 의미
- 확인 필요: 점검자 서명 / 확인자 서명 의 Base64 (이전 세션 컨텍스트 기반 추정).
- TEXT 타입으로 충분 — 실제 길이는 Base64 PNG 데이터라면 수십~수백 KB. `TEXT` (PostgreSQL) 는 제한 없음.

### 6-5. 기존 세션 무효화 — 범위 밖
- 권한 검사 추가 후 기존 로그인 세션 로그아웃 강제는 하지 않음 (다음 요청에서 권한 체크됨 — 충분).

---

## 7. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Config | `src/main/resources/application.properties` | 하드코딩 비밀번호 기본값 제거 |
| Config | `src/main/resources/application-local.properties` | 동일 |
| DB | `src/main/resources/db_init_phase2.sql` | `inspect_report` CREATE 에 컬럼 2개 + ALTER ADD COLUMN IF NOT EXISTS 2행 |
| Backend | `src/main/java/com/swmanager/system/controller/DocumentController.java` | 권한 검사 4곳 추가, 민감 필드 제거 4곳 |
| Frontend | (필요 시) 사용처 화면 | ssn/email/certificate 를 쓰던 페이지 있으면 조정 |
| Docs | `docs/generated/audit/2026-04-18-system-audit.md` | 해당 4개 항목 체크박스 `☑ 조치함` |

**신규 0개 + 수정 ~5개. DB 변경 있음 (ALTER — 기존 데이터 영향 없음).**

---

## 8. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 환경변수 미설정으로 재기동 실패 | 중간 | 개발계획서 체크리스트에 환경변수 설정 사전 확인 단계 |
| 민감 필드 제거로 기존 기능 깨짐 | 중간 | FR-3-3 — 해당 필드 사용처 코드/템플릿 grep 으로 사전 점검 |
| 권한 검사 추가로 정당한 사용자가 차단됨 | 낮음 | 기존 권한 체계 재사용 — "EDIT" 권한 가진 사용자만 필요한 작업이므로 |
| inspect_report ALTER 가 대규모 DB 에서 느림 | 낮음 | `ADD COLUMN` 만 있고 DEFAULT 없음 → PostgreSQL 은 즉시 완료 (메타데이터 변경만) |
| DB 비밀번호 회전 실수로 애플리케이션 장애 | 중간 | 사용자 작업 — 본 스프린트 범위 밖이나 개발계획서에 순서 명시 |

---

## 9. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
