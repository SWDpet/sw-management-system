# [기획서] 시스템 관계도 — C. ERD 인터랙티브 한글 설명 추가

- **작성팀**: 기획팀
- **작성일**: 2026-04-18
- **선행 커밋**: `af2a1b0` (C. ERD 인터랙티브 본체)
- **선행 문서**: [docs/plans/system-graph-erd.md](./system-graph-erd.md) v2 (승인됨)
- **상태**: v2 (codex 재검토 대기) — 1차 검토 2건 반영

### 개정 이력
| 버전 | 변경 |
|------|------|
| v1 | 초안 |
| v2 | codex 검토 반영: ①fallback 약속을 filesystem-only 로 축소(FR-10) ②desc 렌더링 시 HTML 이스케이프 필수(FR-7/8/9) — XSS 방어 |

---

## 1. 배경 / 목표

### 배경
현재 `/admin/system-graph` C 탭(ERD 인터랙티브) 의 상세 패널은 테이블명·컬럼명·타입·PK/FK 배지만 표시한다. 스키마 이해도가 낮은 사용자(신규 팀원, 운영자, 기획자 등)는 `tb_contract_participant` 같은 기술적 이름만 보고 용도를 파악하기 어렵다.

### 목표
- 각 테이블·컬럼에 **한글 설명**을 덧붙여 비개발자도 ERD 를 읽을 수 있게 한다.
- 설명은 **별도 YAML 파일(`docs/erd-descriptions.yml`)** 에서 관리하여 `.mmd` 와 분리된 독립 유지보수 자산으로 둔다.
- 설명이 없는 항목은 **현재 동작 그대로** 표시 (점진적 채움 허용).
- 기존 C 탭 구현(`af2a1b0`) 은 최소 변경으로 설명을 덧붙이는 형태. 기능 회귀 없음.

---

## 2. 사용자 시나리오

1. 관리자가 `/admin/system-graph` → C 탭 진입.
2. 기존과 동일하게 테이블 네트워크 그래프 표시.
3. **노드에 마우스 호버** → 테이블 이름 외에 **한글 설명** 툴팁 표시 (예: "tb_contract_participant — 계약 참여자").
4. 노드 클릭 → 우측 상세 패널에 다음이 표시됨:
   - 테이블 이름 + **한글 설명** (예: "tb_contract_participant · 계약 참여자")
   - 각 컬럼: `컬럼명 (한글 설명)` 형태 (예: `role_type (역할 구분)`)
5. 설명이 없는 컬럼은 현재처럼 컬럼명·타입만 표시 — 누락이 기능 오류로 오해되지 않도록 구분.
6. YAML 파일을 수정하고 서버 재기동 → 즉시 반영.

---

## 3. 화면 / UI 흐름

### 3-1. 상세 패널 (변경 후)

```
tb_contract_participant                       ← 테이블명 (기존)
계약 참여자                                    ← 신규: 한글 설명
[contract]                                     ← 도메인 배지 (기존)

🔑 participant_id (참여자 식별자)              ← 신규: 괄호 안에 한글 설명
   int
🔗 FK→tb_contract contract_id (계약 식별자)
   int
🔗 FK→users user_id (참여 사용자)
   bigint
   role_type (역할 구분)
   varchar
   is_site_rep (현장 대표 여부)
   boolean
```

- 테이블 설명은 타이틀 아래에 별도 줄로 배치 (회색 작은 글씨).
- 컬럼 설명은 컬럼명 바로 뒤 괄호 안에 배치 (회색 작은 글씨).
- 설명이 없으면 괄호 자체를 생략 (빈 괄호 `()` 노출 금지).

### 3-2. 노드 호버 툴팁

- 현재는 툴팁 없음(혹은 vis 기본).
- 변경 후: `tb_contract_participant\n계약 참여자` 형태로 vis-network `title` 속성에 삽입.
- 한글 설명 없으면 테이블명만 표시.

### 3-3. FK 배지 호버

- 기존: `tb_contract` 같이 참조 테이블 이름만 표시.
- 변경 후: `tb_contract · 계약 마스터` 같이 참조 테이블 한글 설명도 포함 (있을 때).

---

## 4. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 서버 기동 시 `docs/erd-descriptions.yml` 파일을 파싱해 메모리 캐시에 저장. 파일 부재·파싱 실패 시 ERD 로딩은 계속 진행(기존 동작 유지) + 경고 로그. |
| FR-2 | YAML 스키마: 최상위 `tables` 아래에 테이블명 키, 값은 `desc`(문자열) 와 `columns`(컬럼명→설명 문자열 맵). 아래 "YAML 스키마" 참고. |
| FR-3 | API 응답 DTO(`ErdGraphDTO.Node`, `ErdGraphDTO.Column`) 에 `desc` 문자열 필드 추가. 설명이 없으면 `null`. |
| FR-4 | 테이블이 YAML 에 있지만 `.mmd` 에 없는 경우 → 해당 YAML 엔트리는 **무시** + debug 로그 1회. |
| FR-5 | 테이블이 `.mmd` 정본에 있지만 YAML 에 없는 경우 → `desc=null` 로 정상 반환 (누락을 에러로 취급하지 않음). |
| FR-6 | 컬럼 레벨도 동일: YAML 에 있는 컬럼명이 정본에 없으면 무시, 정본에 있지만 YAML 에 없으면 `desc=null`. |
| FR-7 | Frontend 상세 패널: `desc != null && !empty` 일 때만 설명 렌더링. null/빈 문자열이면 완전 미노출 (빈 괄호·공백 없음). **렌더링 시 기존 `escapeHtml()` 을 반드시 통과시킨다** — YAML 에 `<`, `>`, `&`, `"`, `'` 등이 들어 있어도 문자 그대로 표시되고 마크업/스크립트로 해석되지 않아야 한다 (XSS 방어). |
| FR-8 | Frontend 노드 호버: vis-network `title` 에 `{테이블명}\n{한글설명}` 합성. 설명 없으면 테이블명만. **`title` 속성에 넣기 전에 `escapeHtml()` 적용**. |
| FR-9 | Frontend FK 배지 호버 툴팁: 참조 테이블에 한글 설명 있으면 `{참조테이블명} · {참조한글설명}` 형식으로 강화. **참조 테이블명·설명 모두 `escapeHtml()`/`escapeAttr()` 경유**. |
| FR-10 | 설명 파일은 `application.properties` 의 신규 설정 `app.erd.descriptions-file=docs/erd-descriptions.yml` 로 경로 구성. **filesystem 만 지원**(classpath fallback 은 이번 범위 밖). 파일 미존재 시 경고 로그 + ERD 는 설명 없이 정상 동작(FR-1). 운영 배포 환경에서 경로가 다르면 JVM 옵션 `-Dapp.erd.descriptions-file=/opt/.../erd-descriptions.yml` 로 override. |
| FR-11 | YAML 파일은 **UTF-8 인코딩** 강제. BOM 있어도 허용 (Snake YAML 기본 파서 동작). |
| FR-12 | 설명 갱신은 **서버 재기동**으로만 반영됨(기존 .mmd 캐시와 동일 정책). 문서/CLAUDE.md 에 명시 불필요(개발계획서에서 커밋 메시지로 남김). |

### YAML 스키마 (FR-2)

```yaml
# docs/erd-descriptions.yml
# ERD 상세 패널에 표시할 한글 설명.
# 빠진 테이블·컬럼은 상세 패널에 설명 없이 표시됨 (오류 아님).

tables:
  tb_contract_participant:
    desc: 계약 참여자
    columns:
      participant_id: 참여자 식별자
      contract_id: 참여 계약 식별자
      user_id: 참여 사용자
      role_type: 역할 구분
      is_site_rep: 현장 대표 여부

  users:
    desc: 시스템 사용자 (가입자 및 관리자)
    columns:
      user_id: 사용자 식별자
      userid: 로그인 ID
      username: 사용자 이름
      password: 비밀번호 해시
      # (모든 컬럼에 설명을 넣을 필요 없음 — 필요한 것만)
```

### 초안 생성 방식 (하이브리드)
- 개발팀이 구현과 함께 초안 YAML 을 생성 (33개 테이블 + 주요 컬럼). 근거는 테이블명·컬럼명·관계선 의미 추론.
- **사용자 검수·수정 단계**를 의사결정 항목(7-3)에 명시.

---

## 5. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | YAML 파싱은 기존 ERD 파싱과 함께 1초 이내 완료. |
| NFR-2 | 설명 추가로 API 응답 크기가 크게 증가하지 않음(한 테이블당 수백 바이트 수준, 전체 50KB 이내 예상). |
| NFR-3 | 설명 누락이 서버/프론트 오류로 이어지지 않음 (FR-5, FR-7 보장). |
| NFR-4 | DB 접근·변경 없음. YAML 파일만 읽기. |
| NFR-5 | 신규 의존성: Snake YAML (Spring Boot starter 에 이미 포함되어 추가 의존성 불필요). 확인은 개발계획서에서 진행. |
| NFR-6 | 권한: 기존 ADMIN 권한 유지 (NFR 별도 추가 없음). |
| NFR-7 | 읽기 전용: 화면/API 에서 설명 편집 기능은 이번 범위 밖. YAML 직접 수정만 허용. |

---

## 6. DB팀 자문 결과

### 영향받는 테이블/컬럼
**없음** — YAML 파일만 추가, DB 비접근.

### 마이그레이션/인덱스/제약
**모두 불필요.**

### 데이터 정합성 리스크
- YAML 과 실제 DB 스키마가 어긋나 설명이 낡은 채 남을 수 있음 (알려진 한계).
- 완화책: YAML 주석으로 갱신일 기록 권장. 그 외 자동 검출은 이번 범위 밖.

**DB팀 의견: 승인 (DB 무관).**

---

## 7. 의사결정 / 우려사항

### 7-1. 파일 위치 — ✅ 확정 (v2: filesystem-only)
- 파일시스템 기본 경로: `docs/erd-descriptions.yml`
- 프로퍼티: `app.erd.descriptions-file` (미지정 시 기본값 사용)
- classpath fallback: **미지원** (v1 에서 약속했으나 v2 에서 제외 — 실제 패키징 범위 밖이고, 운영 현실상 `mvnw spring-boot:run` 방식이라 filesystem 으로 충분). 향후 JAR 배포 시 별도 Phase 에서 재검토.

### 7-2. 설명이 없는 케이스 처리 — ✅ 확정
- 개별 테이블/컬럼 누락: 해당 항목만 설명 미노출, 오류 아님 (FR-5, FR-6, FR-7)
- YAML 파일 자체 부재: 경고 로그 + ERD 는 설명 없이 기존처럼 표시 (FR-1)

### 7-3. 초안 검수 — **결정 필요**
- 개발팀이 생성한 초안 YAML 을 사용자가 PR 리뷰 방식으로 검수할지, 구두 검토 후 바로 커밋할지 선택.
- **권고**: 개발 단계에서 초안 커밋 전 사용자에게 YAML 내용을 요약 제시 → 피드백 받고 수정 후 최종 커밋. (기존 codex 검토 + 사용자 최종승인 흐름과 비슷).

### 7-4. 설명 언어 — ✅ 확정
- 한국어 1개 언어만 지원. 다국어/i18n 은 범위 밖.

### 7-5. 향후 확장
- 어드민이 웹 UI 로 설명을 편집·저장하는 기능: 별도 Phase (권한 관리 + 동시성 고려 필요).
- 설명을 DB `COMMENT ON COLUMN` 과 자동 동기화: 별도 Phase.

---

## 8. 영향 범위

| 계층 | 파일 | 변경 유형 |
|------|------|-----------|
| Config | `src/main/resources/application.properties` | `app.erd.descriptions-file` 1줄 추가 |
| Data | `docs/erd-descriptions.yml` (신규) | 초안 YAML 파일 (33 테이블, 주요 컬럼) |
| Backend (수정) | `src/main/java/com/swmanager/system/dto/ErdGraphDTO.java` | Node/Column record 에 `desc` 필드 추가 |
| Backend (수정) | `src/main/java/com/swmanager/system/service/ErdGraphService.java` | YAML 로드 + Node/Column 생성 시 desc 채움 |
| Frontend (수정) | `src/main/resources/templates/admin-system-graph.html` | 상세 패널/호버 툴팁에 desc 표시 |
| DB | **없음** | — |

**신규 1개 (YAML) + 수정 4개. DB 변경 없음.**

---

## 9. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| YAML 포맷 오류로 파싱 실패 → ERD 자체 로딩 중단 | 중간 | FR-1: YAML 파싱 실패해도 ERD 로딩은 계속 (try-catch 분리) |
| DTO 스키마 변경으로 프론트 기존 코드 깨짐 | 낮음 | `desc` 는 선택 필드로만 추가, 기존 필드 불변 |
| 설명이 낡아 잘못된 안내가 UI 에 표시 | 중간 | 운영 가이드에 "스키마 변경 시 YAML 도 갱신" 원칙 명시 (README 혹은 주석) |
| Snake YAML 이 Spring Boot starter 에 없을 경우 | 낮음 | 개발계획서에서 `./mvnw dependency:tree` 로 확인 (NFR-5) |
| 일부 컬럼만 설명 → 들쭉날쭉해 보임 | 낮음 | UX 상 허용. 설명 없으면 깔끔하게 숨김 (FR-7) |
| YAML 내 악성 페이로드로 XSS (관리자 페이지) | 중간 | FR-7/8/9 에 `escapeHtml()`/`escapeAttr()` 필수 처리 명시 + 개발계획서에서 전용 테스트 케이스(예: `<script>` 문자열을 desc 에 넣고 그대로 표시되는지 확인) |
| JAR 배포 전환 시 설명 파일 누락 가능 | 낮음 | FR-10 주석에 JVM 옵션 override 방법 명시, 향후 classpath 지원은 별도 Phase |

---

## 10. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
