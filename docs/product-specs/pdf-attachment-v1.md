---
status: draft
sprint: pdf-attachment-v1
created: 2026-05-06
last_updated: 2026-05-06
---

# pdf-attachment-v1 — 첨부파일 통합 스프린트 (초안)

> **상태: DRAFT — 본격 작성 보류 (2026-05-06)**
> 다음 세션에서 본문 풀 작성 → DB팀/디자인팀 자문 → codex 검토 → 사용자 최종 승인 순으로 재개.

---

## 0. 확정된 결정 사항 (2026-05-06)

### 0-1. 저장소 백엔드

**PostgreSQL bytea + 메타/blob 분리 테이블**.

```sql
tb_attachment        -- 메타 (목록 조회 시 가벼움)
  attach_id, parent_type, parent_id, blob_id (FK),
  original_filename, file_size, mime_type, sha256, uploaded_by, uploaded_at

tb_attachment_blob   -- 실제 바이트 (TOAST 자동 압축)
  blob_id, file_data BYTEA
```

**필수 패턴:**
- `@Lob @Basic(fetch = FetchType.LAZY)` 로 blob 컬럼 lazy 로딩
- 메타 ↔ blob `@ManyToOne(fetch = FetchType.LAZY)` 분리
- 다운로드는 `StreamingResponseBody` + JDBC stream — byte[] 통째 메모리 로딩 금지
- DB CHECK 제약: `file_size <= 20*1024*1024`, `mime_type = 'application/pdf'`

**향후 마이그레이션 트리거:**
- `pg_dump` 소요 시간이 야간 백업 윈도우(3시간) 초과 시점
- 추정 누적 크기 100~200GB 부근
- 그 시점이면 개발도 내부망 한정 (외부 IP 미사용) → NAS 마이그레이션 자연스러움
- 5년 안엔 트리거 안 올 가능성 높음

### 0-2. 권한
- 업로드/교체/삭제: **작성자 + ROLE_ADMIN**
- 조회: 사업/문서 권한 모델 따름

### 0-3. 사업 첨부 (FR-2 — 전자계약서)
- 사업당 **단일 PDF**
- 최대 **20MB**
- mime: `application/pdf`

### 0-4. 문서 첨부 (FR-3 — 착수계/기성계/준공계 최종 스캔)
- 문서당 **단일 PDF** (공문+착수계+설계내역서 등 합쳐 스캔한 단일 파일)
- 최대 **20MB**
- mime: `application/pdf`

### 0-5. 운영 환경 컨텍스트
- **prod**: Win11 별도 PC, Tomcat 11에 war 배포, 24/7 가동
- **DB**: PostgreSQL @ `${DB_HOST}:${DB_PORT}/${DB_NAME}` (사내, 환경변수 또는 1Password 조회)
- **dev**: 회사/집/출장 멀티 PC, 각자 mvn spring-boot:run, prod DB 직접 연결 패턴
- **사용**: 우리 부서 + 다른 2개 부서 (총 3개)

---

## 1. 배경 / 목적

(작성 보류 — 다음 세션)

---

## 2. 기능 요건 (FR)

### FR-1. SW사업 추진현황 — 날짜 컬럼 추가
- 목록에 **계약일 / 착수일 / 준공일** 컬럼 추가 (yyyy-MM-dd)
- 데이터: `SwProject.contDt` / `startDt` / `endDt` (이미 엔티티 존재, UI 매핑만 필요)
- DB 스키마 변경 **0**

(상세 작성 보류)

### FR-2. 신규 사업 등록 시 전자계약서 PDF 업로드
- 사업 등록/수정 폼에 PDF 업로드 위젯
- 사업당 1개. 교체 시 이전 첨부는 soft-delete 또는 hard-delete (정책 결정 보류)

(상세 작성 보류)

### FR-3. 문서관리 (착수계/기성계/준공계) 최종 스캔 PDF
- 문서 상세 화면에 "최종 스캔본" 슬롯 추가
- 기존 `tb_document_attachment` 와의 관계: 신규 `tb_attachment` 로 통합 vs 기존 분리 — 결정 보류

(상세 작성 보류)

---

## 3. 비기능 요건 (NFR)

- **NFR-1**. 저장소 = DB bytea 분리 테이블 (위 0-1 참조)
- **NFR-2**. Hibernate `@Lob @Basic(FetchType.LAZY)` 필수
- **NFR-3**. `StreamingResponseBody` 다운로드 패턴 필수
- **NFR-4**. SHA-256 무결성 (저장 시 계산, 다운로드 시 검증 옵션)
- **NFR-5**. 권한 = 작성자 + ROLE_ADMIN
- **NFR-6**. DB CHECK 제약 (file_size ≤ 20MB, mime = application/pdf)
- **NFR-7**. 감사 로그 — 업로드/교체/삭제 이벤트를 기존 `access_log` 에 기록
- **NFR-8**. 백업 모니터링 — `pg_dump` 소요 시간 추적, 임계 알람 (운영)

(상세 작성 보류)

---

## 4. 데이터 모델

(작성 보류 — DB팀 자문 필요)

---

## 5. UI / UX

(작성 보류 — 디자인팀 자문 필요)

---

## 6. 마이그레이션

- 기존 사업: `contract_attach_id` NULL 허용 (점진 채움)
- 기존 `tb_document_attachment`: 흡수 vs 분리 결정 보류 (옵션 2 권장 — 기존 그대로 두고 follow-up sprint)

(상세 작성 보류)

---

## 7. 리스크 / 운영

(작성 보류)

---

## 8. 테스트 계획

(작성 보류)

---

## 9. 배포 / 롤백

(작성 보류)

---

## 10. 다음 세션 재개 체크리스트

- [ ] 본문 풀 작성 (1, 2 상세, 4, 5, 6 상세, 7, 8, 9)
- [ ] **DB팀 자문** (subagent — 스키마 검증, FK 모델, CHECK 제약, 마이그레이션 SQL 초안)
- [ ] **디자인팀 자문** (subagent — A+D 정책, UI 변경 있어 트리거됨. 목록 컬럼·업로드 위젯 패턴)
- [ ] **codex 검토** (gpt-5.5, 5축: 요건/설계/DB/리스크/권고)
- [ ] **사용자 최종 승인**
- [ ] 개발계획서 단계 진입 (`docs/exec-plans/pdf-attachment-v1.md`)

---

## 부록 A. 검토 과정 노트 (2026-05-06)

### A-1. 거부된 저장 옵션과 사유

| 옵션 | 거부 사유 |
|---|---|
| Google Drive (컨슈머) | 정부 사업 데이터 해외 클라우드 반출 우려, CSAP 미인증 |
| 회사 Google Workspace | Workspace도 한국 region pinning 없음, CSAP 미인증. **부서장 개인 Drive 채택 시** 계정 정지 시 시스템 데이터 손실, 권한 모델 충돌 |
| 사내 파일서버 + SMB share | 출장지 VPN/방화벽 의존, NTFS ACL·Tomcat service 계정 권한 셋업 부담. dev 환경에서 prod 디스크 직접 쓰기 위험 |
| 운영 서버 로컬 디스크 (UNC 미공유) | 절대 경로 mismatch, multi-PC 분산 저장 사고 명백 |
| 외부 객체 스토리지 (S3/NCP/AWS) | 정부 사업 데이터 외부 반출 정책 충돌 우려 |

### A-2. DB bytea 채택의 결정 변수

- prod DB를 회사/집/출장 dev PC가 이미 공유 사용 → 일관성 자동 확보 (가장 큰 이유)
- multi-PC 환경에서 SMB/UNC/VPN/방화벽 인프라 셋업 부담 0
- 트랜잭션 무결성 (계약서·준공계는 법적 효력 문서)
- 단일 백업 단위 (pg_dump)
- 5년 추정 100~200GB는 PostgreSQL TOAST + 압축으로 충분 처리

### A-3. 보류된 결정 (다음 세션에서 다룸)

- 기존 `tb_document_attachment` 데이터 흡수 vs 분리
- 기존 `DocumentAttachmentService` (절대 경로 + 로컬 디스크) 처리 — 별도 follow-up sprint 권장
- 첨부 교체 시 이전 첨부 soft-delete vs hard-delete
- 첨부 다운로드 권한 모델 (사업/문서 ACL 따름 vs 별도)
- 기획서 풀 작성 시 데이터 모델 SQL 초안 + ERD diff
