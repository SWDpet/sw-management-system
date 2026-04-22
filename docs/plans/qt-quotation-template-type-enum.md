---
tags: [plan, sprint, quotation, enum, magic-number, s8-c]
sprint: "qt-quotation-template-type-enum"
priority: P3
wave: 4
status: draft
created: "2026-04-22"
parent: "qt-quotation-domain-normalize"
---

# [통합 기획+개발계획서] qt_quotation.template_type 매직넘버 → Enum (S8-C)

- **작성팀**: 기획팀 + 개발팀 통합
- **작성일**: 2026-04-22
- **근거**: S8 본 스프린트(`qt-quotation-domain-normalize` v2.1) 에서 후속 분리된 항목
- **배경**: 규모가 작은 리팩터 (Enum 신설 + 3곳 치환) — CLAUDE.md 워크플로우 단축 옵션 적용
- **상태**: v2 (codex v1 ⚠ 수정필요 4건 반영)
- **v1→v2 변경점**:
  1. NFR-2 정규식 확장 — `?: 1`, `== 2` 패턴까지 잡도록 보강
  2. FR-3 단일 완료조건 고정 — **"주석만"** (이번 스프린트는 Enum 주입 X)
  3. Controller preview 분기(L225, L228, L229) 치환 대상 **추가 명시**
  4. `fromCode(unknown)` WARN 로그 추가 (이상값 은닉 방지)

---

## 0. 문제 (기존 S8 분석 요약)

### 0-1. 매직넘버 현황
| 위치 | 코드 | 의미 |
|---|---|---|
| `Quotation.java:75` | `private Integer templateType = 1;` | 기본 = 1 |
| `QuotationDTO.java:131` | `q.setTemplateType(this.templateType != null ? this.templateType : 1);` | 기본 |
| `QuotationController.java:225` | `model.addAttribute("templateType", ... : 1);` | 기본 |
| `quotation-form.html:847` | `<option value="1">기본양식</option><option value="2">인건비 통합양식</option>` | 유일한 의미 정의 |
| `quotation-form.html:2924, 3029` | `if (tplType === 2) { ... }` | 분기 |
| DB | 1 (58건) / 2 (3건) | 실제 값 |

### 0-2. 문제 요약
- Java 코드에서 `1` / `2` 만 봐선 의미 불명 (HTML select option 만 유일한 의미 정의처)
- 매직넘버 안티패턴, 새 양식 추가 시 모든 위치 추적 필요
- **오작동은 없음** — 현재 1/2 두 값 범위에서 정상 동작

### 0-3. DB 값은 유지
**DB 저장은 Integer (1/2) 유지** — 스키마·데이터 변경 없음. Java 측에만 Enum 도입.

---

## 1. 목적 / 범위

### 1-1. 목적
- **FR-1**: `QuoteTemplateType` Enum 신설 (2 values: BASIC=1, LABOR_COST_INTEGRATED=2)
- **FR-2**: Java 3곳(Quotation, QuotationDTO, QuotationController) 매직넘버 `1` → `QuoteTemplateType.BASIC.getCode()` 치환
- **FR-3** (v2 단일 완료조건): Thymeleaf `quotation-form.html` 의 `<option value="1/2">` 위에 **주석만** 추가 — "S8-C: value 는 QuoteTemplateType Enum code 와 대응 (1=BASIC, 2=LABOR_COST_INTEGRATED)". Enum 주입(model attribute)은 **본 스프린트 범위 외** (향후 새 양식 추가 시 처리)

### 1-2. 범위 제외
- DB 스키마 변경 없음 (Integer column 유지, FK 안 함)
- HTML의 JS 분기 `tplType === 2` 는 **유지** (클라이언트 JS 에 Enum 반영은 복잡도 대비 이득 낮음, 주석으로 의미 명시)
- template_type 기본값 정책 변경 없음 (현재 `1 = BASIC` 유지)

---

## 2. 설계

### 2-1. Enum 파일

`src/main/java/com/swmanager/system/constant/enums/QuoteTemplateType.java`

```java
public enum QuoteTemplateType {
    BASIC(1, "기본양식"),
    LABOR_COST_INTEGRATED(2, "인건비 통합양식");

    private final int code;
    private final String label;

    QuoteTemplateType(int code, String label) { this.code = code; this.label = label; }

    @JsonValue
    public int getCode() { return code; }

    public String getLabel() { return label; }

    public static QuoteTemplateType fromCode(Integer code) {
        if (code == null) return BASIC;  // 기본값 정책
        for (QuoteTemplateType t : values()) if (t.code == code) return t;
        // v2: unknown 은 BASIC fallback + WARN 로그 (이상값 은닉 방지)
        LoggerFactory.getLogger(QuoteTemplateType.class).warn(
            "QT_TEMPLATE_TYPE_UNKNOWN: code={} → BASIC 로 fallback", code);
        return BASIC;
    }
}
```

### 2-2. Java 치환 (v2: Controller preview 분기 포함)

| 파일 | line | 변경 |
|------|-----:|------|
| `Quotation.java` | 75 | `private Integer templateType = QuoteTemplateType.BASIC.getCode();` |
| `QuotationDTO.java` | 131 | `this.templateType != null ? this.templateType : QuoteTemplateType.BASIC.getCode()` |
| `QuotationController.java` | 225 | 동일 패턴 (`: 1` → `: QuoteTemplateType.BASIC.getCode()`) |
| `QuotationController.java` | 228 | `int tplType = dto.getTemplateType() != null ? dto.getTemplateType() : QuoteTemplateType.BASIC.getCode();` |
| `QuotationController.java` | 229 | `return tplType == QuoteTemplateType.LABOR_COST_INTEGRATED.getCode() ? "quotation/quotation-preview2" : "quotation/quotation-preview";` |

### 2-3. Thymeleaf 개선

- `quotation-form.html:847` 영역의 `<option value="1/2">` 는 **유지** (static option 으로 충분)
- `<option>` 위에 주석 추가: `<!-- S8-C: value 는 QuoteTemplateType Enum code 와 대응 (1=BASIC, 2=LABOR_COST_INTEGRATED) -->`
- (범위 외) Controller `model.addAttribute("quoteTemplateTypes", ...)` 동적 주입은 **본 스프린트 미포함** — 향후 새 양식 추가 스프린트에서 처리

---

## 3. FR / NFR

### FR
- FR-1~FR-3 (§1-1)

### NFR
- **NFR-1**: `QuoteTemplateType` = 정확 2 values
- **NFR-2** (v2 보강): templateType 관련 Java 매직넘버 잔존 0 — 모든 숫자 패턴 커버:
  - `rg 'templateType[^=\n]*=\s*[12]\b' src/main/java/com/swmanager/system/quotation` = 0 hits
  - `rg 'setTemplateType\(\s*[12]\s*\)' src/main/java` = 0 hits
  - `rg ':\s*1\b|\?\s*[12]\s*:' src/main/java/com/swmanager/system/quotation/controller/QuotationController.java --line-number` 의 결과 라인이 templateType 문맥이면 0 hits 확인 (수동 검토)
  - `rg '==\s*2\b' src/main/java/com/swmanager/system/quotation` 결과가 templateType 문맥 아님을 확인 (0 hits)
  - 단, JS 내 `tplType === 2` 는 허용 (클라이언트 분기, §1-2 범위 제외)
- **NFR-3**: 기존 61건 DB 값 변경 0 (1=58, 2=3 그대로)
- **NFR-4**: 회귀 테스트 ≥ 2건 (Enum 값·fromCode 동작)
- **NFR-5**: 컴파일 + 전체 테스트 green + 서버 기동 정상
- **NFR-6**: 견적서 작성·목록·미리보기 스모크 정상

---

## 4. 작업 순서

### Step 0 — 기준 SHA
```bash
BASE_SHA=$(git rev-parse HEAD)
```

### Step 1 — QuoteTemplateType Enum 신설

### Step 2 — Java 3곳 치환 (Entity, DTO, Controller)

### Step 3 — Thymeleaf 주석 추가

### Step 4 — 단위 테스트

`src/test/java/com/swmanager/system/constant/enums/QuoteTemplateTypeTest.java`
- 2 values 정확
- BASIC=1, LABOR=2 매핑
- `fromCode(null)` → BASIC
- `fromCode(99)` → BASIC (unknown fallback)
- `@JsonValue` = int code

### Step 5 — 빌드 / 재기동 / 스모크

```bash
./mvnw -q clean compile
./mvnw -q test
bash server-restart.sh
```
- `/quotation/list` 정상
- `/quotation/form` 양식 드롭다운 정상

### Step 6 — `작업완료` 발화 후 커밋/푸시 (CLAUDE.md)

---

## 5. 테스트 / 검증

| T# | 항목 | 검증 | Pass 기준 |
|----|------|------|-----------|
| T1 | Enum 2 values | JUnit | PASS |
| T2 | code 매핑 (BASIC=1, LABOR=2) | JUnit | PASS |
| T3 | fromCode null / unknown → BASIC | JUnit | PASS |
| T4 | @JsonValue int | JUnit | 직렬화 = 1 or 2 |
| T5 | 리터럴 잔존 | rg | 0 hits (JS 제외) |
| T6 | 전체 테스트 | mvnw test | green |
| T7 | 서버 기동 | restart | HTTP 응답 정상 |
| T8 | 견적서 목록 / 작성 / 미리보기 | 수동 | 정상 |

---

## 6. 롤백

| 상황 | 조치 |
|------|------|
| 컴파일 실패 | `git revert` (단일 commit 형태) |
| 런타임 회귀 | 동일 |

**DB 변경 없음 → SQL 롤백 불필요**

---

## 7. 리스크

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | `fromCode(unknown)` fallback 정책 | 낮음 | BASIC 으로 fallback (기존 HTML select 기본값과 일치) |
| R-2 | JS 클라이언트 매직넘버 `=== 2` 잔존 | 낮음 | 명시적 주석 + NFR-2 가 서버 측만 검사 |

---

## 8. 승인 요청

### 승인 확인
- [ ] §2 Enum 구조 (BASIC=1, LABOR_COST_INTEGRATED=2, fromCode null/unknown→BASIC)
- [ ] §1-2 범위 제외 (DB·JS 변경 없음)
- [ ] §3 FR-1~FR-3, NFR-1~NFR-6
- [ ] §4 Step 0~6
