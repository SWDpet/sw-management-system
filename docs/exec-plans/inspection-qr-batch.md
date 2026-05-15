---
tags: [dev-plan, sprint, inspection, api, qr]
sprint: "inspection-qr-batch"
status: draft-v1
created: "2026-05-13"
---

# [개발계획서] /api/inspection/qr-batch — PWA → SW Manager 업로드 API

- **작성팀**: 개발팀
- **작성일**: 2026-05-13
- **근거 기획서**: `docs/product-specs/inspection-qr-batch.md` v1 (속도 모드 — codex 1차 검토 생략, 사용자 주도)
- **모드**: 속도 모드 (사용자 명시 2026-05-13) — codex 중간검토 생략, 구현 완료 후 전체 검증만
- **상태**: 초안 v1

---

## 0. 진입 조건 (Entry Criteria)

| EC | 충족 여부 | 검증 |
|----|----------|------|
| EC-1 기획서 v1 작성 완료 | ✅ | `docs/product-specs/inspection-qr-batch.md` |
| EC-2 PoC sample_payload.json 존재 | ✅ | `inspection-poc/sample_payload.json` |
| EC-3 기존 inspect_* 도메인 정착 | ✅ | `domain/InspectReport.java`, `domain/InspectCheckResult.java`, `db_init_phase2.sql:135-210` |
| EC-4 SecurityConfig 의 `/api/**` CSRF 면제 정책 | ✅ | `SecurityConfig.java:92-98` |

---

## 1. 작업 순서 (Stepwise)

### Step 1 — DB 스키마 (db_init_phase2.sql DDL 추가)

**파일**: `src/main/resources/db_init_phase2.sql`

**1-1. 신규 테이블 `inspect_qr_batch`** — `inspect_template` 정의 직후 (line ~210 다음).
```sql
-- ============================================================
-- 점검 QR 배치 (PoC 자동수집 원본 보존 — inspection-qr-batch sprint)
-- ============================================================
CREATE TABLE IF NOT EXISTS inspect_qr_batch (
    id                BIGSERIAL PRIMARY KEY,
    payload_id        VARCHAR(64) NOT NULL UNIQUE,
    report_id         BIGINT REFERENCES inspect_report(id) ON DELETE SET NULL,
    site_code         VARCHAR(32) NOT NULL,
    inspect_round     VARCHAR(7),
    payload_ts        BIGINT,
    source_inspector  VARCHAR(50),
    header_hash       VARCHAR(16),
    raw_bytes         INT,
    gz_bytes          INT,
    payload_json      JSONB NOT NULL,
    hash_check        VARCHAR(10) DEFAULT 'skip',     -- 'ok'|'warn'|'skip'
    uploaded_by       BIGINT REFERENCES users(user_id),
    uploaded_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qr_batch_site_round ON inspect_qr_batch(site_code, inspect_round);
CREATE INDEX IF NOT EXISTS idx_qr_batch_report ON inspect_qr_batch(report_id);
```

**1-2. `inspect_report.source` 컬럼 추가** (멱등):
```sql
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS source VARCHAR(20) DEFAULT 'manual';
```

**1-3. `inspect_report.batch_id` 추가** — 멱등 응답 조회용 (UNIQUE 는 NULL 허용 partial 인덱스):
```sql
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS batch_id VARCHAR(64);
CREATE UNIQUE INDEX IF NOT EXISTS uq_inspect_report_batch_id
    ON inspect_report(batch_id) WHERE batch_id IS NOT NULL;
```

**1-4. `sw_pjt.site_code` 컬럼 추가** (멱등):
```sql
ALTER TABLE sw_pjt ADD COLUMN IF NOT EXISTS site_code VARCHAR(32);
CREATE UNIQUE INDEX IF NOT EXISTS uq_sw_pjt_site_code
    ON sw_pjt(site_code) WHERE site_code IS NOT NULL;
```

**검증**:
```bash
./mvnw spring-boot:run
# 또는 bash server-restart.sh
# 로그에서 phase2.sql 실행 성공 + new columns/tables 생성 확인
```

PG 셸 검증:
```sql
\d inspect_qr_batch
\d inspect_report
SELECT column_name FROM information_schema.columns WHERE table_name='sw_pjt' AND column_name='site_code';
```

---

### Step 2 — JPA Entity

**2-1. 신규** `src/main/java/com/swmanager/system/domain/InspectQrBatch.java`
- 컬럼 매핑 (Step 1-1 과 동일)
- `payload_json` 은 `@JdbcTypeCode(SqlTypes.JSON)` + `String` (JSONB → String 매핑, jackson 으로 변환은 service 레이어)
- `@PrePersist` 로 uploaded_at 기본값

**2-2. `InspectReport.java` 변경**:
- `source` 필드 추가 (`@Column(name="source", length=20)`, getter/setter)
- `batchId` 필드 추가 (`@Column(name="batch_id", length=64)`)
- `@PrePersist` 의 `if (source == null) source = "manual";` 추가

**2-3. `SwProject.java` 변경**:
- `siteCode` 필드 추가 (`@Column(name="site_code", length=32)`)

---

### Step 3 — Repository

**3-1. 신규** `src/main/java/com/swmanager/system/repository/InspectQrBatchRepository.java`
```java
public interface InspectQrBatchRepository extends JpaRepository<InspectQrBatch, Long> {
    Optional<InspectQrBatch> findByPayloadId(String payloadId);
}
```

**3-2. 기존** `SwProjectRepository` (또는 동등 repository) 에 메서드 추가:
```java
Optional<SwProject> findBySiteCode(String siteCode);
```

**3-3. 기존** `InspectReportRepository` (없으면 신규) 에 메서드:
```java
Optional<InspectReport> findByBatchId(String batchId);
```

확인 — repository 가 있는지 확인하고 없으면 신규 생성.

---

### Step 4 — DTO

**4-1. 신규** `src/main/java/com/swmanager/system/dto/inspection/InspectionQrBatchRequest.java`
- `payload` (Map<String,Object> 또는 강타입 클래스 — 강타입 권장)
  - PayloadDto: `s`, `id`, `site`, `round`, `ts`, `inspector`, `tiers` (Map<String, TierDto>)
  - TierDto: `h`, `os`, `i` (List<List<Object>> — `[key, status, value]`)
- `header` (HeaderDto: `hash`, `raw_bytes`, `gz_bytes`, `b45_chars`, `total`)
- jakarta.validation 사용 — `@NotBlank id`, `@Pattern` site/round, `@NotEmpty tiers`

**4-2. 신규** `InspectionQrBatchResponse.java`
- `reportId`, `batchId`, `idempotent` (boolean), `pjtId`, `tierCount`, `itemCount`, `manualItems`, `warnItems`, `hashCheck` (`ok|warn|skip`), `reportUrl`

---

### Step 5 — Service

**5-1. 신규** `src/main/java/com/swmanager/system/service/inspection/InspectionQrBatchService.java`

핵심 메서드 흐름:
```java
@Transactional
public InspectionQrBatchResponse upload(InspectionQrBatchRequest req, Long userId) {
    // A. 멱등 조회
    Optional<InspectQrBatch> existing = batchRepo.findByPayloadId(req.payload.id);
    if (existing.isPresent()) {
        return Response.idempotent(existing.get());
    }
    // B. site → pjt 매핑
    SwProject pjt = pjtRepo.findBySiteCode(req.payload.site)
        .orElseThrow(() -> new SiteNotMappedException(req.payload.site));
    // C. inspect_report INSERT (source='auto-qr', batch_id=payload.id)
    InspectReport report = buildReport(req, pjt, userId);
    reportRepo.save(report);
    // D. inspect_check_result rows
    int items = 0, manual = 0, warn = 0;
    for (Tier tier : req.payload.tiers) {
        for (Metric m : tier.i) {
            InspectCheckResult row = mapMetric(report.id, tier.key, m);
            checkResultRepo.save(row);
            items++;
            if ("M".equals(m.status)) manual++;
            else if ("warn".equals(m.status)) warn++;
        }
    }
    // E. hash 보조검증 (warn-only)
    String hashCheck = verifyHash(req); // "ok" | "warn" | "skip"
    // F. inspect_qr_batch INSERT
    InspectQrBatch batch = buildBatch(req, report, userId, hashCheck);
    batchRepo.save(batch);
    return Response.created(report, batch, items, manual, warn, hashCheck);
}
```

**5-2. result code 매퍼** — `InspectResultCode` enum 에 추가:
```java
public static InspectResultCode fromPoCStatus(String status) {
    if (status == null) return NORMAL;
    return switch (status.toLowerCase()) {
        case "ok" -> NORMAL;
        case "warn" -> PARTIAL;
        case "err", "error" -> ABNORMAL;
        case "m" -> NOT_APPLICABLE;
        default -> NORMAL;
    };
}
```
**주의**: 기존 `fromJson` 의 toLowerCase 흐름과 충돌 없는지 확인. PoC status 는 항상 소문자 (`ok`/`warn`/`err`/`M`). `M` 만 대소문자 양쪽 받음.

**5-3. hash 보조검증** (`verifyHash`) — best-effort:
```java
try {
    String canonical = jacksonCompactSerialize(req.payload);     // ensureAscii=false, no whitespace
    byte[] gz = gzip(canonical.getBytes(UTF_8));
    String sha = sha1Hex(gz).substring(0, 6);
    return sha.equals(req.header.hash) ? "ok" : "warn";
} catch (Exception e) {
    log.warn("hash verify failed", e);
    return "skip";
}
```
Jackson 의 ObjectMapper 직렬화가 Python 과 다를 위험 — 본 sprint 에서는 warn-only 정책 (NFR-3). hash_check 컬럼에 결과 저장.

**5-4. 예외 매핑** — `@RestControllerAdvice` 또는 컨트롤러 내부 ExceptionHandler:
- `SiteNotMappedException` → 422 `{ "error": "site_not_mapped", "site": "..." }`
- `MethodArgumentNotValidException` → 400 `{ "error": "bad_request", "fields": {...} }`

---

### Step 6 — Controller

**6-1. 신규** `src/main/java/com/swmanager/system/controller/inspection/InspectionQrBatchController.java`

```java
@Slf4j
@RestController
@RequestMapping("/api/inspection")
@RequiredArgsConstructor
public class InspectionQrBatchController {

    private final InspectionQrBatchService service;

    @PostMapping("/qr-batch")
    public ResponseEntity<InspectionQrBatchResponse> upload(
            @Valid @RequestBody InspectionQrBatchRequest req,
            @AuthenticationPrincipal CustomUserDetails me) {
        InspectionQrBatchResponse res = service.upload(req, me.getUserId());
        log.info("inspection-qr-batch {} payload_id={} report_id={} items={}",
                res.isIdempotent() ? "idempotent" : "ok", res.getBatchId(),
                res.getReportId(), res.getItemCount());
        return ResponseEntity.ok(res);
    }
}
```

**경로 확정**: `/api/inspection/qr-batch` — SecurityConfig `/api/**` 매칭으로 CSRF 면제 자동 적용.

---

### Step 7 — 단위 테스트

**7-1. 신규** `src/test/java/com/swmanager/system/service/inspection/InspectionQrBatchServiceTest.java`

테스트 케이스 (mockito 기반):
1. ✅ 정상 payload → report + 14 (ap) + 26 (db) + 6 (gis) = 46 items INSERT
2. ✅ 멱등 — 동일 payload_id 두 번 호출 → 첫 번째 created, 두 번째 idempotent (DB 변경 0)
3. ✅ site_not_mapped — sw_pjt 에 site_code 없음 → SiteNotMappedException, 어떤 INSERT 도 발생 X
4. ✅ status 매핑 — ok/warn/err/M 각각 NORMAL/PARTIAL/ABNORMAL/NOT_APPLICABLE
5. ✅ M 항목의 remarks 표지 — "육안 점검 필요 (자동수집 불가)"
6. ✅ 알 수 없는 status → NORMAL fallback + remarks log
7. ✅ result_text 길이 500 초과 → 절단 + remarks 에 "(truncated)" 표지
8. ✅ tier 가 비어있는 tiers — 0 items
9. ✅ payload.ts null 처리
10. ✅ inspector null 처리
11. ✅ hash 매칭 OK → hash_check="ok"
12. ✅ hash 불일치 → hash_check="warn", 그래도 정상 INSERT (warn-only)

**7-2. 신규** `InspectionQrBatchControllerIT.java` — `@SpringBootTest` + `@AutoConfigureMockMvc`
- sample_payload.json 을 그대로 POST → 200, body 검증
- 멱등 재호출 → 200 + `idempotent: true`
- site 미매핑 payload → 422
- 미로그인 → 401

---

### Step 8 — 운영 데이터 매핑 (단양군 사이트)

본 sprint 의 sw_pjt.site_code 컬럼 추가는 DDL만 — 운영 데이터는 사용자가 수동 매핑.

**8-1. 단양군 사업 확인**:
```sql
SELECT proj_id, proj_nm, sys_nm_en FROM sw_pjt WHERE proj_nm LIKE '%단양%';
```

**8-2. 단양군 사이트 코드 부여**:
```sql
UPDATE sw_pjt SET site_code = 'dyg' WHERE proj_id = <proj_id>;
```

**8-3. 검증**:
```sql
SELECT proj_id, proj_nm, site_code FROM sw_pjt WHERE site_code IS NOT NULL;
```

본 단계는 운영 DB 갱신이 필요한 단계 — 본 sprint 의 자동 마이그레이션은 아니다. **사용자가 직접 실행하거나 sprint 종료 시 별도 안내.**

---

### Step 9 — End-to-End Smoke Test

**9-1. PWA 없이 cURL 로 검증**:
```bash
# 1. 로그인 → 세션 쿠키 획득
curl -c cookies.txt -X POST http://localhost:8080/login \
  -d "username=USER&password=PASS"

# 2. sample payload + header 로 업로드
curl -b cookies.txt -X POST http://localhost:8080/api/inspection/qr-batch \
  -H "Content-Type: application/json" \
  -d '{"payload": '"$(cat inspection-poc/sample_payload.json)"', "header": {"hash":"abc123","raw_bytes":1597,"gz_bytes":681,"b45_chars":1022,"total":1}}'

# 기대: 200 + {"report_id": N, "batch_id":"dyg-2026-05", "idempotent": false, ...}

# 3. 멱등 재호출 — 동일 payload
curl -b cookies.txt -X POST http://localhost:8080/api/inspection/qr-batch ...
# 기대: 200 + {"idempotent": true}
```

**9-2. UI 확인**:
- `/document/inspect/{report_id}` 페이지 접속 → 자동수집 보고서가 표시되는지 확인.
- 본 sprint 는 UI 표지 (source='auto-qr') 변경 없음 — 정상 표시되면 OK.

**9-3. DB 검증**:
```sql
SELECT * FROM inspect_qr_batch WHERE payload_id='dyg-2026-05';
SELECT id, doc_title, source, batch_id FROM inspect_report WHERE batch_id='dyg-2026-05';
SELECT section, item_name, result_code, remarks
  FROM inspect_check_result
  WHERE report_id=<above id>
  ORDER BY section, sort_order;
```

---

### Step 10 — 작업완료 / Codex 전체 검증

속도 모드 — codex 중간검토 없음. 본 sprint 구현 + 테스트 통과 후:
1. 사용자에게 "구현 완료, smoke 통과" 보고
2. 사용자가 codex 전체 검증 의뢰 (또는 본인 확인 후 직접 작업완료 선언)
3. `git commit` (작업완료 자동 commit 룰)

---

## 2. 산출물 체크리스트

### 신규 파일 (8개)
- [ ] `src/main/java/com/swmanager/system/domain/InspectQrBatch.java`
- [ ] `src/main/java/com/swmanager/system/repository/InspectQrBatchRepository.java`
- [ ] `src/main/java/com/swmanager/system/dto/inspection/InspectionQrBatchRequest.java`
- [ ] `src/main/java/com/swmanager/system/dto/inspection/InspectionQrBatchResponse.java`
- [ ] `src/main/java/com/swmanager/system/service/inspection/InspectionQrBatchService.java`
- [ ] `src/main/java/com/swmanager/system/controller/inspection/InspectionQrBatchController.java`
- [ ] `src/test/java/com/swmanager/system/service/inspection/InspectionQrBatchServiceTest.java`
- [ ] `src/test/java/com/swmanager/system/controller/inspection/InspectionQrBatchControllerIT.java`

### 수정 파일 (5개)
- [ ] `src/main/resources/db_init_phase2.sql` (Step 1)
- [ ] `src/main/java/com/swmanager/system/domain/InspectReport.java` (source, batchId 추가)
- [ ] `src/main/java/com/swmanager/system/domain/SwProject.java` (siteCode 추가)
- [ ] `src/main/java/com/swmanager/system/constant/enums/InspectResultCode.java` (fromPoCStatus 추가)
- [ ] `inspection-poc/README.md` — "다음 단계" 의 `/api/inspection/qr-batch` 항목 ✓ 표시

### 신규 디렉토리
- `src/main/java/.../controller/inspection/`
- `src/main/java/.../service/inspection/`
- `src/main/java/.../dto/inspection/`

---

## 3. 리스크 / 롤백

### 롤백 매트릭스

| 단계 | 롤백 방법 |
|------|----------|
| Step 1 (DDL) | `DROP TABLE inspect_qr_batch; ALTER TABLE inspect_report DROP COLUMN source, DROP COLUMN batch_id; ALTER TABLE sw_pjt DROP COLUMN site_code;` — 단 운영DB 에 inspect_report.source = 'auto-qr' row 가 있으면 데이터 손실. 운영 적용 전 백업. |
| Step 2-6 (코드) | `git revert` |
| Step 7 (테스트) | 코드 revert 와 동일 |
| Step 8 (운영 데이터) | `UPDATE sw_pjt SET site_code=NULL WHERE site_code='dyg';` |

### 리스크

- **R-1 (기획서 R-1 hash 직렬화)** — Jackson 직렬화가 PoC encode.py 와 다를 가능성 높음. 본 sprint 는 NFR-3 warn-only 정책으로 회피. hash_check 컬럼 도입.
- **R-2 (멱등 키 충돌)** — 운영자가 동일 payload.id 를 의도적으로 두 번 다른 데이터로 만들 시 두 번째는 무시됨. 운영 가이드로 보완 (후속).
- **R-3 (운영 데이터 단양군 매핑)** — Step 8 누락 시 첫 업로드가 422. Sprint 종료 시 사용자에게 명시 안내 필요.
- **R-4 (db_init_phase2.sql 길이)** — 본 sprint DDL 추가가 phase2.sql 끝부분에 위치 → 다른 phase2 sprint (V018-init-ordering, tb_ops_doc-forward-ref) 의 line 번호와 충돌 없음 (phase2 끝).

---

## 4. 진행 상태

- [ ] Step 1 — DDL
- [ ] Step 2 — Entity
- [ ] Step 3 — Repository
- [ ] Step 4 — DTO
- [ ] Step 5 — Service
- [ ] Step 6 — Controller
- [ ] Step 7 — 테스트
- [ ] Step 8 — 운영 매핑 (사용자 직접)
- [ ] Step 9 — Smoke
- [ ] Step 10 — 사용자 보고 + codex 전체 검증
