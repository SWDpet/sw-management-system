---
tags: [plan, sprint, quotation, master, normalization, wave-3]
sprint: "qt-quotation-domain-normalize"
priority: P2
wave: 3
status: draft
created: "2026-04-22"
---

# [기획서] qt_quotation category 마스터 신설 (S8, 옵션 B)

- **작성팀**: 기획팀
- **작성일**: 2026-04-22
- **근거 로드맵**: [[data-architecture-roadmap]] v2 §S8 (Wave 3 / P2)
- **사용자 결정 (2026-04-22)**:
  - v1 검토 중 견적서 도메인 확인 → **옵션 A (prj_types 통일) 부적합 확정**
  - **옵션 B 채택**: 견적서 전용 `qt_category_mst` 신설 (한글 3종 유지)
- **상태**: v2 (codex 검토 대기)

---

## 0. 사전 조사 결과 (2026-04-22 실측)

### 0-1. 견적서 도메인 (사용자 확인)
**견적서는 유지보수 / 용역 / 제품 3종으로 나뉘며, prj_types 와는 별도 도메인.**

### 0-2. `qt_quotation.category` 현재 분포 (61건)
| category | count |
|----------|------:|
| 유지보수 | 44 |
| 용역     | 12 |
| 제품     |  5 |

### 0-3. `qt_quotation` 스키마 관련
- `category` VARCHAR(10) **NOT NULL**
- `status` VARCHAR(10), 전부 "발행완료" 61건 (정규화 의미 낮음)
- `template_type` INTEGER, 1(58) / 2(3) (매직넘버)

### 0-4. 로드맵 vs 본 스프린트 범위
- 로드맵 §S8 은 **3 조치 결합**이었음 (category + status + template_type)
- 본 스프린트는 **category 만** 우선 처리 (옵션 B). status / template_type 은 별도 스프린트로 분리

---

## 1. 목적

- **FR-1**: `qt_category_mst` 마스터 테이블 신설 (한글 3종)
- **FR-2**: `qt_quotation.category` 에 FK 제약 추가 (→ `qt_category_mst.category_code`)
- **FR-3**: `qt_quotation.category` **NOT NULL 유지** (기존 정책 확인)
- **FR-4**: Java 코드의 리터럴 `"유지보수"` / `"용역"` / `"제품"` 사용처 조사 + **상수화** (`QtCategory` 상수 클래스 or Enum 도입)
- **FR-5**: `db_init_phase2.sql` 에 마스터 3행 시드 추가

---

## 2. 범위

### 2-1. 포함
- 신규 테이블: `qt_category_mst` (3행 시드)
- V024 마이그레이션 SQL (CREATE + 시드 + FK)
- `QtCategory` 상수 또는 Enum 신설 (FR-4)
- Java 코드 리터럴 치환 (Step 1 에서 grep 결과 기반 전수)
- `db_init_phase2.sql` 시드 추가
- 회귀 테스트 ≥ 3건

### 2-2. 제외
- **`qt_quotation.status` 정규화** — 전부 "발행완료" 단일값. 별도 스프린트
- **`qt_quotation.template_type` Enum 화** — 매직넘버 1/2 처리. 별도 스프린트
- 기존 61건 데이터 변경 **없음** (값 그대로 유지 + FK 만 추가)
- UI 드롭다운 변경 — 이미 한글 3종 드롭다운이 있으면 마스터 참조로 치환, 없으면 범위 외

---

## 3. 사용자 결정 사항

### 3-A. 상수화 방식 (택 1)

| 옵션 | 방식 | 장점 | 단점 |
|------|------|------|------|
| **S-opt1 (권장)** | **Enum** `QtCategory { MAINTENANCE("유지보수"), SERVICE("용역"), PRODUCT("제품") }` | 타입 안전. 기존 AccessActionType/DocumentStatus 패턴 재사용. ArchUnit 게이트 가능 | DB는 한글 저장이라 Java 쪽에서 label 변환 필요 |
| S-opt2 | **상수 클래스** `QtCategory { public static final String MAINTENANCE = "유지보수"; ... }` | 리터럴 치환 단순 | 타입 안전 X |
| S-opt3 | 상수화 없이 마스터만 | DB 무결성만 확보 | Java 리터럴 오타 위험 |

**기획 권장**: **S-opt1 Enum** — S9 AccessActionType 패턴 검증됨. `@JsonValue`/`@JsonCreator` + `fromKoLabel` 재사용.

### 3-B. DB 저장값

| 옵션 | DB 값 | 영향 |
|------|-------|------|
| **V-opt1 (권장)** | **한글 유지** (유지보수/용역/제품) | 기존 61건 변경 0. UI/리포트 호환 |
| V-opt2 | 영문 코드로 변경 (MAINTENANCE/SERVICE/PRODUCT) | 기존 61건 UPDATE 필요. NFR-5 영향 |

**기획 권장**: **V-opt1 한글 유지** — 견적서는 한글 UI/PDF 출력이 주 사용처라 한글 값이 자연스러움.

---

## 4. 제안 설계 (S-opt1 + V-opt1)

### 4-1. `qt_category_mst` 스키마

```sql
CREATE TABLE qt_category_mst (
  category_code  VARCHAR(10)  PRIMARY KEY,   -- 한글 (유지보수/용역/제품)
  category_label VARCHAR(50)  NOT NULL,      -- UI 라벨 (동일 한글)
  display_order  INT          NOT NULL DEFAULT 0
);

INSERT INTO qt_category_mst (category_code, category_label, display_order) VALUES
  ('유지보수', '유지보수', 1),
  ('용역',     '용역',     2),
  ('제품',     '제품',     3);
```

### 4-2. `QtCategory` Enum (S-opt1)

```java
public enum QtCategory {
    MAINTENANCE("유지보수"),
    SERVICE("용역"),
    PRODUCT("제품");

    private final String label;
    // @JsonValue/@JsonCreator + fromKoLabel + values().length=3
    // S9 AccessActionType 패턴 복제
}
```

### 4-3. FK 추가

```sql
ALTER TABLE qt_quotation
  ADD CONSTRAINT fk_qt_category
  FOREIGN KEY (category) REFERENCES qt_category_mst(category_code);
```

### 4-4. V024 마이그 순서

1. **Phase 0**: 사전검증 (category distinct 3종 확인, 예상 외 값 HALT)
2. **Phase 1**: `qt_category_mst` CREATE IF NOT EXISTS + 3행 시드 (ON CONFLICT DO NOTHING)
3. **Phase 2**: Exit Gate — `qt_quotation.category` 전부 마스터 존재 확인
4. **Phase 3**: FK ADD (**명시적 멱등 SQL**):
   ```sql
   DO $$ BEGIN
     IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_qt_category') THEN
       ALTER TABLE qt_quotation
         ADD CONSTRAINT fk_qt_category
         FOREIGN KEY (category) REFERENCES qt_category_mst(category_code);
     END IF;
   END $$ LANGUAGE plpgsql;
   ```
5. **Phase 4**: 최종 검증 (mst=3, FK 1개 존재)

---

## 5. FR / NFR

### FR
- FR-1 ~ FR-5 (§1)

### NFR
- **NFR-1**: `qt_category_mst` **정확히 3행**
- **NFR-2**: FK `fk_qt_category` 존재
- **NFR-3**: `qt_quotation.category` 기존 61건 **값 변경 0** (V-opt1)
- **NFR-4**: Java 코드 리터럴 잔존 0 hits
  - `rg '"유지보수"|"용역"|"제품"' src/main/java --glob '!**/enums/**' --glob '!**/constant/**'` = 0
- **NFR-5**: 회귀 테스트 ≥ 3건 (마스터 행수 / FK / Enum 3종 + fromKoLabel)
- **NFR-6**: 견적서 목록·상세·PDF 스모크 정상 (3 화면)
- **NFR-7**: 컴파일 + 전체 테스트 green + 서버 기동

---

## 6. 리스크 / 완화

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | 한글을 category_code (PK) 로 저장 — 이식성 | 낮음 | V-opt1 은 기존 정책 유지. 향후 영문코드 전환은 별도 스프린트 |
| R-2 | Java 리터럴 치환 누락 | 중 | NFR-4 `rg` 게이트 + ArchUnit (S9 패턴) |
| R-3 | UI 드롭다운 옵션 값 하드코딩 잔존 | 낮음 | Step 1 grep으로 템플릿 포함 전수 검사. 있으면 마스터 참조로 치환 |
| R-4 | 신규 category 추가 절차 부재 | 낮음 | 본 문서에 "추가 절차: V*** migration + INSERT" 명시 |

---

## 7. 대안 / 비채택

| 대안 | 설명 | 비채택 사유 |
|------|------|-------------|
| A. prj_types 통일 | 영문코드 GISSW/PKSW/TS 로 UPDATE | 사용자 확정 (2026-04-22): 견적서는 별도 도메인, 부적합 |
| S8 결합 스프린트 | category + status + template_type 일괄 | 범위 팽창 리스크. 별도 스프린트로 분리 |
| 마스터 없이 Enum 만 | Java 만 정리 | DB 무결성 없음. 오타 재발 가능 |

---

## 8. 사용자 승인 요청 사항

### 결정 필요 항목
1. **§3-A 상수화** — S-opt1 Enum 권장
2. **§3-B DB 저장값** — V-opt1 한글 유지 권장
3. §2-2 범위 축소 (status / template_type 제외)

### 승인 확인 사항
- [ ] §3 A/B 결정
- [ ] §4 V024 마이그 순서
- [ ] §5 FR-1~FR-5, NFR-1~NFR-7
- [ ] §6 리스크 완화
