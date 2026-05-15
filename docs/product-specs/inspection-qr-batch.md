---
tags: [plan, sprint, inspection, api, qr, pwa]
sprint: "inspection-qr-batch"
status: draft-v1
created: "2026-05-13"
---

# [기획서] /api/inspection/qr-batch — 점검 PoC → SW Manager 업로드 API

- **작성팀**: 기획팀
- **작성일**: 2026-05-13
- **선행 산출물**: `inspection-poc/` PoC (commit `fa6fe24`) — encoder, decoder, PWA scanner, Windows AP 점검 에이전트, 점검내역서 docx 시안 3종
- **상태**: 초안 v1 — codex 1차 검토 대기
- **UI 키워드**: 없음 (백엔드 API 전용 — 디자인팀 자문 skip). 단, 업로드 결과 inspect_report DRAFT 자동 생성 → 기존 점검내역서 상세 화면(`/document/inspect/{id}`)에 자동수집 표지 1개 추가 필요 — 디자인팀 자문은 후속 sprint.
- **선행 결정 (사용자 확정 2026-05-13)**:
  1. **저장 모델**: (A) 원본 JSON 보존(`inspect_qr_batch.payload_json` JSONB) + `inspect_report`(status=DRAFT) 자동생성
  2. **요청 컨트랙트**: (A) PWA가 디코드한 payload JSON 을 그대로 POST. 서버 재조립 없음, payload header(`hash`, `gz_bytes`, `raw_bytes`)는 보조 검증용으로만 사용
  3. **result 매핑**: `ok→NORMAL` / `warn→PARTIAL` / `err→ABNORMAL` / `M→NOT_APPLICABLE` (육안항목은 UI 보정 대기)

---

## 1. 배경 / 목표

### 배경

폐쇄망 운영장비(UPIS AP/DB/GIS) 의 점검 결과를 사외망 SW Manager 로 가져오는 파이프라인이 PoC 까지 완료된 상태:

```
[폐쇄망 서버] 점검 에이전트 → JSON payload
        ↓ (encode.py)
        gzip + base45 + QR PNG (모니터 표시)
        ↓ (광학 스캔)
[갤럭시탭 PWA] BarcodeDetector/jsQR → decoder.mjs
        ↓ CRC + 누락 + SHA-1 검증 → decoded payload JSON
        ???   ← 본 sprint
[SW Manager] inspect_report DRAFT + inspect_check_result rows
```

PWA decoder.mjs 까지가 PoC 의 검증 범위. SW Manager 로 보내는 마지막 한 단계가 미구현. **본 sprint 는 그 한 단계의 API + 도메인 매핑 + 저장 모델만** 구현한다.

### 목표

1. **PWA → SW Manager 업로드 1엔드포인트 (`POST /api/inspection/qr-batch`)** — 디코드된 payload JSON 을 받아 (a) 원본 보존, (b) `inspect_report` DRAFT 자동생성, (c) tier별 metric 행을 `inspect_check_result` 로 INSERT, (d) 생성된 `report_id` 반환.
2. **멱등성** — 동일 `payload.id` (예: `dyg-2026-05`) 재업로드 시 신규 row 생성 X, 기존 batch + 기존 report_id 반환 (HTTP 200, body 에 `idempotent=true`).
3. **원본 audit** — payload 전체 JSONB 보존 (`inspect_qr_batch.payload_json`) + 페이로드 header 의 `hash`/`raw_bytes`/`gz_bytes` 보존 → 사후 PoC frame 과 대조 가능.
4. **육안 점검 항목 가시화** — payload `"M"` 결과는 `NOT_APPLICABLE` 로 저장하되 `inspect_check_result.remarks` 에 `"육안 점검 필요 (자동수집 불가)"` 표지 텍스트 → 점검내역서 상세 화면에서 UI 보정 대상으로 식별 가능.

### 비목표

- PWA 스캐너 UI 변경 — PoC 그대로 사용 (후속 sprint 에서 업로드 버튼/세션 처리).
- 점검내역서 상세 화면의 자동수집 표지 디자인 — UI 키워드 없음, 후속 sprint.
- 점검 에이전트 (PowerShell/ksh) 수정 — 본 sprint scope 외.
- inspect_template 자동 매칭 — payload metric 키(`ap.perf.cpu_pct`)와 inspect_template item_name 한글명(`CPU 사용률`) 의 매칭 로직은 후속 sprint. **본 sprint 에서는 payload 의 metric 키를 그대로 `inspect_check_result.item_name` 으로 저장.**
- 보안 — pen-test, rate limiting 강화 등은 후속.

---

## 2. 시나리오

### 정상 흐름

1. 단양군 현장 점검자(박욱진)가 갤럭시탭 PWA scanner 로 폐쇄망 모니터 QR 광학 스캔 완료. PWA 가 SHA-1 일치 확인 + decoded payload 보유.
2. PWA 가 SW Manager 세션 (사외망 PC에서 로그인된 세션을 갤럭시탭으로 인계 — 후속 sprint 에서 OAuth/일회용 토큰. 본 sprint 에서는 **세션 쿠키 기반 (기존 SW Manager 인증)**) 으로 `POST /api/inspection/qr-batch` 호출.
3. 서버:
   - Step A: payload schema 검증 (필수 필드 `id`, `site`, `round`, `tiers`).
   - Step B: `inspect_qr_batch.payload_id = payload.id` 로 중복 조회. 있으면 멱등 응답.
   - Step C: `payload.site` → `sw_pjt.proj_id` 매핑 (sw_pjt 의 어떤 컬럼?). 매핑 실패 시 422.
   - Step D: `inspect_report` INSERT (status=DRAFT, pjt_id, inspect_month=payload.round, batch_id=payload.id, sys_type 매핑).
   - Step E: payload 각 tier (ap/db/gis) 의 metric 배열을 순회 → `inspect_check_result` row INSERT (section=tier, item_name=metric key, result_code=매핑, result_text=값, remarks=육안 표지 or 빈값).
   - Step F: `inspect_qr_batch` row INSERT (report_id, payload_id, payload_json, header_hash, raw_bytes, gz_bytes, source_inspector, uploaded_by, uploaded_at).
4. 서버가 `{ report_id, batch_id, idempotent: false, mapped_items: N, manual_items: M }` 응답.
5. PWA 가 응답을 받고 사용자에게 "업로드 완료 — 보고서 #123 생성됨" 토스트 + 점검내역서 상세 링크 노출.

### 시나리오 — 중복 업로드 (멱등)

- 동일 payload.id 재업로드 → Step B 에서 기존 batch 발견 → DB 변경 0, `{ report_id, batch_id, idempotent: true }` 200 응답.

### 시나리오 — 사이트 코드 매핑 실패

- payload.site 가 sw_pjt 에 매칭되지 않음 → 422 `{ error: "site_not_mapped", site: "dyg" }`. DB 변경 0.
- 운영자가 SW Manager 사업관리 화면에서 site_code 컬럼 보정 후 재시도.

### 시나리오 — 페이로드 header hash 불일치

- 서버에서 payload(`tiers` 등) 를 다시 직렬화·SHA-1 → header.hash 와 비교. 불일치 시 400 `{ error: "hash_mismatch" }`. **단** PWA decoder.mjs 가 SHA-1 검증을 이미 통과한 후 보낸 것이므로 정상 흐름에서는 발생하지 않아야 한다. 발생 시 PWA ↔ 서버 직렬화 차이 의심 (key order, whitespace 등) — NFR-3 참조.

---

## 3. 기능 요구사항 (FR)

### FR-1 — 엔드포인트
- `POST /api/inspection/qr-batch` (Content-Type: `application/json`)
- 인증: 기존 SW Manager 세션 (Spring Security 기존 설정). 비로그인 → 401.
- 권한: 모든 로그인 사용자 — 본 sprint 에서는 ROLE 분기 없음 (후속).

### FR-2 — 요청 바디
PoC `sample_payload.json` 과 동일한 구조를 받음 (PWA decoder.mjs 가 디코드해서 얻은 payload). 추가로 header 보조 필드.

```json
{
  "payload": {
    "s": "snapshot/qr1",
    "id": "dyg-2026-05",
    "site": "dyg",
    "round": "2026-05",
    "ts": 1778461321,
    "inspector": "박욱진",
    "tiers": { "ap": {...}, "db": {...}, "gis": {...} }
  },
  "header": {
    "hash": "abc123",
    "raw_bytes": 1597,
    "gz_bytes": 681,
    "b45_chars": 1022,
    "total": 1
  }
}
```

- `payload.id`, `payload.site`, `payload.round`, `payload.tiers` 는 필수. 결여 시 400.
- `header.hash` 필수 — 보조 검증용.

### FR-3 — 응답
정상 (200):
```json
{
  "report_id": 123,
  "batch_id": "dyg-2026-05",
  "idempotent": false,
  "pjt_id": 17,
  "tier_count": 3,
  "item_count": 47,
  "manual_items": 8,
  "warn_items": 2,
  "report_url": "/document/inspect/123"
}
```

오류:
- 400 `bad_request` — schema 위반 (필수 필드 결여, hash 불일치)
- 401 — 미로그인
- 422 — 비즈니스 검증 실패 (site 매핑 X 등)
- 500 — 서버 오류

### FR-4 — 도메인 매핑 (payload → inspect_*)

#### FR-4-A `inspect_report` INSERT
| inspect_report 컬럼 | 값 |
|---|---|
| pjt_id | site → sw_pjt.proj_id (매핑 §4 참조) |
| inspect_month | payload.round (예: "2026-05") |
| sys_type | sw_pjt.sys_nm_en (사업의 시스템 타입을 따름. 예: "UPIS") |
| doc_title | `"[자동수집] {sys_type} {round} 점검내역서"` |
| status | DRAFT |
| insp_user_id | uploaded_by 의 user_id (세션) — 단, payload.inspector 가 별도 user 인 경우 후속 sprint 에서 매핑. 본 sprint 는 uploaded_by 동일. |
| conf_ps_info_id | NULL (사용자 UI 에서 보정) |
| created_by / updated_by | 세션 사용자 id |

#### FR-4-B `inspect_check_result` INSERT (1 row per metric)
payload.tiers 의 각 tier 의 `i` 배열을 순회 — `[key, status, value]` 3-tuple.

| inspect_check_result 컬럼 | 값 |
|---|---|
| report_id | FR-4-A 에서 생성된 id |
| section | tier 이름 대문자 (ap→"AP", db→"DB", gis→"GIS") |
| category | NULL (본 sprint MVP — inspect_template 매칭 후속) |
| item_name | metric key (예: `"ap.perf.cpu_pct"`) |
| item_method | NULL |
| result_code | status 매핑 — `ok→NORMAL`, `warn→PARTIAL`, `err→ABNORMAL`, `M→NOT_APPLICABLE` (정의되지 않은 값은 NORMAL fallback + remarks 로그) |
| result_text | value 의 문자열 표현 (`true→"true"`, `null→""`, 숫자→문자열) — 단 길이 500 제한 |
| remarks | `M` 인 경우 `"육안 점검 필요 (자동수집 불가)"`, 그 외 NULL |
| sort_order | payload tier 내 index (0-based) |

#### FR-4-C `inspect_qr_batch` INSERT (1 row per upload)
신규 테이블 §4 참조.

### FR-5 — 멱등성
- `inspect_qr_batch.payload_id` UNIQUE.
- 동일 `payload_id` 재업로드 시: 기존 batch 의 `report_id` 조회 → 200 + `idempotent: true` 응답. **DB 변경 0**. inspect_report / inspect_check_result 갱신 안 함 (이미 사용자가 UI에서 수정했을 수 있음).

### FR-6 — 서버측 보조 검증
- payload JSON 을 `Map<String,Object>` 로 파싱 후 PoC encode.py 와 동일 직렬화 (`ensure_ascii=False, separators=(",", ":")`) → gzip → SHA-1 hex[:6] 계산 → header.hash 와 비교. 불일치 시 400.
- **불일치 위험** — Jackson 의 키 순서가 Python `json.dumps` 와 다를 수 있음. NFR-3 참조.

---

## 4. 도메인·DB 변경

### 4-1. 새 테이블 `inspect_qr_batch`

```sql
CREATE TABLE IF NOT EXISTS inspect_qr_batch (
    id                BIGSERIAL PRIMARY KEY,
    payload_id        VARCHAR(64) NOT NULL UNIQUE,    -- payload.id (예: "dyg-2026-05")
    report_id         BIGINT REFERENCES inspect_report(id) ON DELETE SET NULL,
    site_code         VARCHAR(32) NOT NULL,           -- payload.site
    inspect_round     VARCHAR(7),                     -- payload.round
    payload_ts        BIGINT,                         -- payload.ts (unix seconds)
    source_inspector  VARCHAR(50),                    -- payload.inspector
    header_hash       VARCHAR(16),                    -- header.hash (sha1 hex[:6])
    raw_bytes         INT,
    gz_bytes          INT,
    payload_json      JSONB NOT NULL,                 -- 원본 페이로드 전체 보존
    uploaded_by       BIGINT REFERENCES users(user_id),
    uploaded_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qr_batch_site_round ON inspect_qr_batch(site_code, inspect_round);
CREATE INDEX IF NOT EXISTS idx_qr_batch_report ON inspect_qr_batch(report_id);
```

배치 위치: **`db_init_phase2.sql` 의 점검내역서 섹션 끝 (line ~210 inspect_template 정의 직후)** — phase1/V*.sql 의존 없음, 멱등.

### 4-2. `inspect_report` 컬럼 추가
```sql
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS source VARCHAR(20) DEFAULT 'manual';
-- 'manual' = 사용자가 UI에서 직접 작성
-- 'auto-qr' = QR batch 로부터 자동생성
```

### 4-3. sw_pjt site_code 매핑
- sw_pjt 에 `site_code` 컬럼 존재 여부 확인 → 없으면 추가:
```sql
ALTER TABLE sw_pjt ADD COLUMN IF NOT EXISTS site_code VARCHAR(32);
CREATE UNIQUE INDEX IF NOT EXISTS uq_sw_pjt_site_code ON sw_pjt(site_code) WHERE site_code IS NOT NULL;
```
- 운영자가 SW Manager 사업관리 화면에서 site_code 입력 — 본 sprint 에서는 **DDL 추가만**, UI 변경 미포함 (UI 키워드 0건 유지). 임시 운용은 DB 에서 직접 UPDATE.
- 단양군 사업 → site_code='dyg' 수동 매핑 (sprint 검증용).

**대안 검토**:
- sw_pjt 에 별도 컬럼 추가 없이 inspect_qr_batch.site_code 만으로 보관하고, 사용자가 UI에서 사업을 선택하는 방식 → idempotent 이 깨짐 (재업로드 시 사용자가 다시 선택해야). **기각** — 자동 매핑이 필요.
- sw_pjt.sys_nm_en 으로 site_code 대체 → "UPIS"는 사업이 여러 개일 수 있어 1:1 보장 X. **기각**.
- 별도 매핑 테이블 `inspect_site_map(site_code, pjt_id)` → 1:N 확장 가능. **본 sprint 에서는 1:1 단순 컬럼 + 후속에서 분리 가능**.

### 4-4. JPA Entity
- 신규 `InspectQrBatch` entity
- `InspectReport` 에 `source` 필드 추가 (값: `"manual" | "auto-qr"`, getter/setter)

---

## 5. 비기능 요구사항 (NFR)

### NFR-1 — 트랜잭션
- API 전체를 단일 `@Transactional` 로 묶음. inspect_report INSERT 와 inspect_check_result + inspect_qr_batch INSERT 사이 실패 시 전체 롤백.
- 멱등 응답 시 readonly tx.

### NFR-2 — 검증 순서 (fail fast)
1. schema 필수 필드 → 400
2. payload.id 중복 → 200 (멱등)
3. site_code 매핑 → 422 (DB 변경 0)
4. header hash 검증 → 400 (DB 변경 0)
5. 그 이후 INSERT

### NFR-3 — header hash 검증 정책
- 서버측 재계산이 PWA decoder 와 다를 위험 (JSON key 순서, whitespace, encoding).
- **본 sprint 정책**: NFR-3-x **재계산 검증은 best-effort warn-only**. 불일치 시 400 으로 reject 하지 않고, `inspect_qr_batch.header_hash` 에 header 값 + `payload_json` 에 원본 보존 + 서버 로그 WARN. 응답에 `hash_check: "warn"` 필드 포함.
- 이유: PoC encode.py 는 Python 의 `json.dumps(ensure_ascii=False, separators=(",", ":"))` 으로 직렬화 후 gzip → SHA-1. Java Jackson 의 직렬화 결과가 동일하지 않을 수 있음. PWA decoder.mjs 가 이미 SHA-1 검증을 통과한 후의 페이로드이므로 서버 재검증은 보조 audit 용.
- **후속 sprint** 에서 endpoint 에 `raw_b45[]` 받아 서버측 b45 decode + gunzip + SHA 검증으로 강화 가능 (요청 컨트랙트 (B) 로 회귀).

### NFR-4 — 페이로드 크기 제한
- 요청 body 최대 256KB (server.servlet.max-request-size 또는 컨트롤러 검증). PoC stress payload 가 32KB 이므로 충분.
- payload.tiers 각 tier 의 `i` 배열 최대 500 row (DoS 가드).

### NFR-5 — 응답 시간
- p95 < 500ms (47 row INSERT + JSONB 1건). 로컬 PG 기준 여유.

### NFR-6 — 로깅
- 정상 INSERT 시 `INFO inspection-qr-batch ok payload_id={} report_id={} items={}`
- 멱등 시 `INFO inspection-qr-batch idempotent payload_id={} report_id={}`
- 실패 시 `WARN inspection-qr-batch fail reason={code} payload_id={?}`

### NFR-7 — 보안
- 인증: 세션 쿠키 기반 (Spring Security 기존 정책 — `SecurityConfig.filterChain` line 92-98 의 스프린트 5 v2 정책: `/api/**` 는 CSRF 면제 + 세션 인증).
- 본 endpoint `/api/inspection/qr-batch` 도 동일 정책 — CSRF 토큰 불필요, 세션 쿠키만 검증.
- 입력 검증: payload.id `^[a-z0-9_\-:]{1,64}$`, site `^[a-z0-9_\-]{1,32}$`, round `^[0-9]{4}-[0-9]{2}$`.
- JSONB 저장 시 SQL injection 위험 없음 (parameter binding).
- 응답에 payload 전체 포함 X (report_url 만).

---

## 6. 결정 사항 / 갈래

### D-1. 저장 모델 — (A) 원본 + 자동생성 [확정]
- 사용자 결정 (2026-05-13).
- **이유**: 원본 audit + UI 즉시 검토.
- **트레이드오프**: JSONB 와 정규화 테이블이 동시에 존재 → 정합성 책임. 멱등 정책 (FR-5) 으로 갱신 안 함 → 사용자 UI 수정이 원본과 디버지면 inspect_qr_batch.payload_json 으로 원본 추적 가능.

### D-2. 요청 컨트랙트 — (A) decoded payload JSON [확정]
- 사용자 결정.
- **이유**: PWA decoder.mjs 가 SHA-1 검증까지 완료 → 서버 재조립 중복.
- **트레이드오프**: 서버측 hash 재검증이 직렬화 차이로 fail 위험 → NFR-3 warn-only 정책.

### D-3. result 매핑 — `ok→NORMAL / warn→PARTIAL / err→ABNORMAL / M→NOT_APPLICABLE` [확정]
- 사용자 결정.
- **육안 항목 표지**: remarks 에 `"육안 점검 필요 (자동수집 불가)"` 텍스트.

### D-4. site_code 컬럼 위치 — sw_pjt 직접 컬럼 [잠정]
- **이유**: 단순. 1 사이트 = 1 사업 가정.
- **위험**: 동일 사이트에 사업이 분리되는 경우 (UPIS 본사업 + 차세대 사업) 1:1 깨짐. 후속 sprint 에서 `inspect_site_map` 테이블로 분리 가능.

### D-5. 인증 — 기존 세션 (CSRF 면제) [확정 — 정정]
- **이유**: SW Manager 기존 정책 (스프린트 5 v2) 이 `/api/**` 를 CSRF 면제로 운영 중. 본 endpoint 도 동일 정책 적용.
- **NFR-7 정정**: 기획서 v1 초기 작성 시 "CSRF 토큰" 명시했으나, `SecurityConfig.filterChain` line 92-98 확인 결과 `/api/**` 는 CSRF 면제. 본 endpoint 도 면제 — PWA 가 별도 CSRF 토큰 전송 불필요.
- **PWA 측 변경 필요**: 후속 sprint 에서 PWA → SW Manager 세션 인계 흐름 설계.

---

## 7. 리스크

### R-1 — header hash 직렬화 불일치 (NFR-3 mitigated)
- 발생 시 warn-only, payload_json 보존으로 사후 비교 가능.

### R-2 — 멱등 정책의 사용자 의도 충돌
- 사용자가 UI 에서 inspect_report 수정 → 동일 batch 재업로드 → 사용자 수정이 덮어쓰이지 않음 (정책 보존).
- 단 사용자가 "원본으로 reset" 을 원할 가능성 → 본 sprint 비목표, 후속 UI.

### R-3 — site_code 매핑 누락 시 사용자 차단
- 운영자가 사업관리 UI 에서 site_code 입력해야 함. 본 sprint 는 DDL 만 추가 (UI 변경 X).
- 첫 사이트 (단양군) 는 DB 직접 UPDATE 로 임시 운용 → sprint 검증 가능.
- **후속 sprint** 에서 사업관리 UI 에 site_code 필드 추가.

### R-4 — 동일 payload 의 다른 round 가능성 없음 (멱등 키 = payload.id, payload.id = `{site}-{round}` convention)
- encode.py 의 build_frames 가 payload.id 를 header 에 그대로 사용 → site+round 가 다르면 id 도 다름.
- 단 점검자가 payload.id 를 수동으로 같게 만들고 다른 결과를 업로드하는 시나리오는 운영 정책 위반.

### R-5 — DB 변경의 phase2.sql 위치 위험
- 4-1, 4-2, 4-3 모두 phase2.sql 에 ALTER/CREATE IF NOT EXISTS 로 추가 — 멱등.
- phase2-V018-init-ordering / phase2-tb_ops_doc-forward-ref 진행 중 → 본 sprint DDL 은 그 sprint 의 forward-reference 와 무관 (inspect_* 테이블은 이미 phase2 초반에 존재).
- **선행 sprint 미완료 영향**: phase2.sql full fresh-init rc=0 은 후속 sprint 후. 본 sprint DDL 추가 자체는 운영DB ALTER 멱등이므로 무영향.

---

## 8. 산출물

### 8-1. 코드
- `src/main/java/.../controller/InspectionQrBatchController.java` (POST `/api/inspection/qr-batch`)
- `src/main/java/.../service/InspectionQrBatchService.java` (검증·매핑·저장)
- `src/main/java/.../dto/InspectionQrBatchRequest.java` / `Response.java`
- `src/main/java/.../domain/InspectQrBatch.java` (신규 entity)
- `src/main/java/.../domain/InspectReport.java` (source 필드 추가)
- `src/main/java/.../repository/InspectQrBatchRepository.java`
- `src/main/java/.../constant/enums/InspectResultCode.java` 의 fromPoCStatus(`"ok"|"warn"|"err"|"M"`) 매퍼 추가

### 8-2. DB
- `src/main/resources/db_init_phase2.sql` — §4-1, 4-2, 4-3 DDL 추가 (멱등)

### 8-3. 테스트
- `InspectionQrBatchServiceTest` — payload → inspect_report + inspect_check_result 매핑 단위테스트 (10+ 케이스: 정상, 멱등, site 매핑실패, header hash warn, M 항목, warn 항목, 누락 필드, 빈 tier, 잘못된 status 값, 긴 result_text 절단)
- `InspectionQrBatchControllerIT` — 통합테스트 1건 (sample_payload.json 전체 흐름 + 멱등 재호출)

### 8-4. 문서
- 본 기획서 (`docs/product-specs/inspection-qr-batch.md`)
- 개발계획서 (`docs/exec-plans/inspection-qr-batch.md`) — 기획 승인 후 작성
- `inspection-poc/README.md` 의 "다음 단계" 체크리스트 갱신 (`/api/inspection/qr-batch` 항목 완료 표시는 sprint 종료 시)

---

## 9. 다음 절차

1. **codex 1차 검토** — 본 기획서 v1
2. 검토 결과 반영 → v2 (필요 시)
3. **사용자 최종 승인**
4. 개발계획서 작성 → codex 2차 검토 → 사용자 승인
5. 구현 (Step 1: DDL + entity / Step 2: service + controller / Step 3: 테스트 / Step 4: 통합 / Step 5: PoC sample 로 end-to-end 검증)
6. codex 검증 → 작업완료 commit
