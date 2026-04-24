---
tags: [plan, sprint, logging, enum, normalization]
sprint: "access-log-action-and-menu-sync"
priority: P2
wave: 3
status: draft
created: "2026-04-21"
---

# [기획서] access_logs action_type Enum + MenuName 상수 동기화

- **작성팀**: 기획팀
- **작성일**: 2026-04-21
- **근거 로드맵**: [[data-architecture-roadmap]] v2 §S9 (Wave 3 / P2)
- **상태**: v2 (codex v1 ⚠수정필요 4건 반영)
- **v1→v2 변경점**:
  1. CI 게이트 강화 — ArchUnit 테스트 게이트 신설로 String 오버로드 신규 호출 차단 (CI 단계)
  2. FR 수치 고정 — `MenuName 총 16개(기존 11 + 신규 5)`, `AccessActionType 13종`
  3. 로드맵 §S9 정합성 — 본 기획서에서 "4→5종 정정" 역링크 + 로드맵 갱신 조치항목 추가
  4. NFR-7 label Freeze 규칙 신설 + Enum `fromKoLabel` 정규화 규칙 명문화

---

## 1. 배경 / 문제

### 1-1. 감사 발견 (data-architecture-utilization-audit)

1. **`access_logs.action_type` 자유 텍스트 20+종 하드코딩**
   - 컨트롤러 10+곳에서 `logService.log("메뉴", "조회", ...)` 형태로 리터럴 사용
   - DB 상 분포: 조회/수정/등록/삭제/다운로드/승인/... 20+ 종 (공백/유사어 오염 우려)
   - Enum·마스터 부재 → 오타·변형값 허용 (예: "조회" vs "목록조회" vs "상세조회")

2. **`MenuName` 상수 누락 5종**
   - 현재 존재 (11개): DASHBOARD, PROJECT, PERSON, INFRA, USER, LOG, MYPAGE, CONTRACT, WORK_PLAN, DOCUMENT, PERFORMANCE
   - **누락 5종**: QR라이선스 / 라이선스대장 / GeoNURIS라이선스 / 견적서 / 회원가입
   - 컨트롤러에서 각자 리터럴로 `"견적서"`, `"GeoNURIS라이선스"` 등 하드코딩

### 1-2. 로드맵 기록 vs 실측 차이

로드맵 §S9는 "누락 4종" 으로 기록되었으나 본 스프린트 착수 시 실측 결과 **5종** 확인. 기획서에서 정정.

**정정 이력 (추적성)**:
- 로드맵 근거 문서: `docs/design-docs/data-architecture-roadmap.md` §2 S9 (line ~173) — `MenuName 누락 4종`
- 본 기획서 실측: §1-1, §4-2 — `MenuName 누락 5종 (QR_LICENSE/LICENSE_REGISTRY/GEONURIS_LICENSE/QUOTATION/SIGNUP)`
- **조치**: 개발계획서 Step 최종 단계에서 로드맵 §2 S9 본문을 "4종 → 5종" 으로 정정 + 본 기획서 링크 추가 (T-LINK)

---

## 2. 목적

§5 FR과 번호 1:1 매칭 (v2 정렬):
- **목적-1** → FR-1: `AccessActionType` Enum 도입 — 유효값 집합 고정 + 표시라벨 분리
- **목적-2** → FR-2: `MenuName` 상수 누락 5종 추가 → **총 16개 고정**
- **목적-3** → FR-3: LogService Enum 오버로드 신설 + 모든 호출부 치환 (리터럴·변수 양쪽)
- **목적-4** → FR-4: `@JsonCreator` 양방향 바인딩 + `fromKoLabel` 정규화 (§4-1-A)
- **목적-5** → FR-5: ArchUnit 규칙으로 String 오버로드 신규 호출 **게이트 차단**

> 주: 기존 DB 로그 데이터 정제는 §3-2 범위 제외. 감사성 보존을 위해 과거 레코드는 읽기 전용.

---

## 3. 범위

### 3-1. 포함
- `AccessActionType` Enum 파일 신설 (`constant/enums/AccessActionType.java`)
- `MenuName` 상수 5개 추가
- `LogService.log(...)` 오버로드 (Enum 수용)
- 컨트롤러 호출부 **전체 치환** (리터럴 → 상수 + Enum)
- 단위 테스트 + 문서 갱신

### 3-2. 제외
- access_logs 테이블 스키마 변경 없음 (action_type VARCHAR 유지)
- 기존 로그 데이터 마이그레이션 **수행하지 않음** (Enum 도입은 신규 기록에만 적용, 과거 데이터는 읽기 전용)
- S5 범위(userid 정제)와 독립

---

## 4. 제안 설계

### 4-1. AccessActionType Enum (1차 후보)

| 상수 | label(ko) | 의미 | 기존 리터럴 통합 후보 |
|------|-----------|------|--------------------|
| VIEW | 조회 | 목록/상세 조회 일반 | 조회, 목록조회, 상세조회, 접근, 발급폼접근, 수정폼접근 |
| CREATE | 등록 | 신규 저장 | 등록, 생성, 발급, 신청 |
| UPDATE | 수정 | 기존 수정 | 수정, 정보수정, 비번변경, 상태변경 |
| DELETE | 삭제 | 삭제 | 삭제 |
| DOWNLOAD | 다운로드 | 파일 다운로드 | 다운로드, CSV다운로드 |
| UPLOAD | 업로드 | 파일 업로드 | (추후) |
| APPROVE | 승인 | 결재/승인 | 승인, 승인요청 |
| SIGN | 서명 | 전자서명 | 서명 |
| PREVIEW | 미리보기 | 미리보기/PDF | 미리보기 |
| BATCH | 일괄처리 | 집계/일괄생성/재계산 | 일괄생성, 집계, 금액재계산, 패턴복사, 패턴초기화 |
| PATTERN_CRUD | 패턴관리 | 품명/비고 패턴 CRUD | 패턴등록, 패턴수정, 패턴삭제, 비고패턴등록, 비고패턴수정, 비고패턴삭제 |
| WAGE_CRUD | 노임관리 | 노임단가 CRUD | 노임단가등록, 노임단가수정, 노임단가삭제 |
| SENSITIVE_VIEW | 민감정보조회 | 마스킹 해제 조회 | 민감정보조회 |

**정책**:
- `@JsonCreator`로 label/name 양방향 바인딩
- Enum 값은 DB에는 **한글 label** 저장 (기존 데이터와 호환). 향후 action_type VARCHAR → action_code(ENUM name) 전환은 별도 스프린트(S9-B)
- **label Freeze 규칙** (v2 신설, NFR-7과 연계): 일단 본 스프린트에서 확정된 한글 label은 **불변(immutable)**. 변경 시 과거 DB 로그와 신규 로그가 분기되어 조회 깨짐 → label 수정은 **마이그레이션 스프린트로만 허용**

#### 4-1-A. fromKoLabel 정규화 규칙 (v2)

```java
public static AccessActionType fromKoLabel(String raw) {
    if (raw == null) return null;
    String norm = raw.trim();                     // (1) 공백 trim
    // (2) 정확 매칭 (공식 label)
    for (var v : values()) if (v.label.equals(norm)) return v;
    // (3) 동의어 매핑 (4-1 표 "기존 리터럴 통합 후보" 열과 일치)
    return SYNONYM_MAP.getOrDefault(norm, null);  // null → unknown
}
```

**unknown 처리 정책**:
- `LogService`에서 `fromKoLabel(raw) == null` 인 경우 → `log.warn("ACCESS_LOG_ACTION_UNKNOWN: raw='{}'", raw)` + 원본 문자열 그대로 저장 (fail-soft, 로그 유실 방지)
- 운영 중 WARN 누적 시 동의어 보강 or Enum 추가 (후속 보정)

### 4-2. MenuName 추가 5종

```java
public static final String QR_LICENSE = "QR라이선스";
public static final String LICENSE_REGISTRY = "라이선스대장";
public static final String GEONURIS_LICENSE = "GeoNURIS라이선스";
public static final String QUOTATION = "견적서";
public static final String SIGNUP = "회원가입";
```

### 4-3. LogService 오버로드

```java
// 신규 — 타입 안전
public void log(String menuNm, AccessActionType action, String detail) {
    log(menuNm, action.getLabel(), detail);
}

// 기존 — @Deprecated(since="S9", forRemoval=false) 예고만, 실제 제거는 후속 스프린트
public void log(String menuNm, String actionType, String detail) { ... }
```

**치환 규칙**:
- `logService.log(MenuName.QUOTATION, AccessActionType.VIEW, "...")` 형태로 전면 교체
- 전역 `rg -n 'logService\.log\("[^"]+", "[^"]+",' src/main/java` 0 hits 목표

---

## 5. FR / NFR

### FR
- **FR-1**: AccessActionType Enum 파일 존재 + **정확히 13개 상수** + label 필드 + `fromKoLabel(String)` 정규화 메서드 (§4-1-A)
- **FR-2**: MenuName 상수 **정확히 16개(기존 11 + 신규 5)** — QR_LICENSE, LICENSE_REGISTRY, GEONURIS_LICENSE, QUOTATION, SIGNUP 추가
- **FR-3**: LogService Enum 오버로드 존재 + 기존 호출부 **전부 치환** (리터럴 + 변수 전달 호출 양쪽)
- **FR-4**: `JsonCreator` 양방향 바인딩 테스트 통과 (label, name)
- **FR-5** (v2 신설): **ArchUnit 테스트 게이트** — `LogService.log(String,String,String)` 는 `@Deprecated` 유지. ArchUnit 규칙 `no classes should call method LogService.log(String,String,String)` 을 `./mvnw test` 단계에서 실행해 **신규 호출 지점 0** 강제 (CI 레벨 게이트, 엄밀히는 컴파일러 차단이 아닌 테스트 실패로 빌드 중단)

### NFR
- **NFR-1**: 기존 access_logs 데이터 손상 0건 (RW 없음)
- **NFR-2**: 서버 재기동 후 로그 새 레코드가 정상 label로 저장됨 (스모크)
- **NFR-3**: 회귀 테스트 신규 ≥ 5건 (Enum 변환 / fromKoLabel 동의어 / unknown fail-soft / 로그 저장 / MenuName 상수 개수)
- **NFR-4** (v2 강화): 리터럴 + 변수 전달 양쪽 재오염 방지 (다층 방어):
  - (a) 한글 리터럴 잔존 검증: `rg -n 'logService\.log\("[^"]+", "[가-힣]+", ' src/main/java` = 0 hits
  - (b) **전체 리터럴 잔존 검증** (v2 보강): `rg -n 'logService\.log\("[^"]+", "[^"]+", ' src/main/java` = 0 hits (한글/영문/기타 모두 포함)
  - (c) ArchUnit 테스트: `log(String,String,String)` 호출 지점 0 (신규·기존 모두)
  - (d) `InfraController`/`PersonController`/`WorkPlanController` 의 `action` 변수도 `AccessActionType` 타입으로 전환 검증
- **NFR-5**: 컴파일 + 서버 기동 성공
- **NFR-6**: 기존 VIEW 컨트롤러 스모크 5건 이상 정상
- **NFR-7** (v2 신설): **label Freeze** — AccessActionType / MenuName 의 한글 label 문자열은 본 스프린트 확정 후 불변. 변경 시 반드시 마이그레이션 스프린트 선행

---

## 6. 리스크 / 완화

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | Enum 매핑 정책(어느 리터럴을 어느 Enum으로?) 오해 | 중 | 4-1 표를 사용자·codex 승인 사항에 포함. 수동 매핑표를 개발계획서에서 FR-2.1 로 재확정 |
| R-2 | DB 과거 값과 Enum label 불일치로 조회 필터 깨짐 | 낮음 | label은 **한글 유지** → 기존 값과 호환. 관리자 로그뷰어 필터 영향 0 |
| R-3 | 호출부 치환 누락 (리터럴) | 중 | NFR-4 (a) `rg` 게이트 |
| R-4 | `@Deprecated` String 오버로드 남김으로 재오염 (변수 전달) | **중→낮음 (v2)** | **ArchUnit 규칙 (FR-5)** 로 신규 호출을 **테스트/CI 게이트(테스트 실패로 빌드 중단)** 로 차단. 기존 호출은 전부 치환 후 ArchUnit 게이트가 회귀 방지 |
| R-5 (v2) | Enum unknown 리터럴 유입 시 로그 유실 | 낮음 | §4-1-A `fromKoLabel` unknown → WARN + 원본 저장 (fail-soft) |
| R-6 (v2) | label 문자열 변경 시 과거/신규 데이터 분기 | 낮음 | NFR-7 label Freeze 규칙 명문화 |

---

## 7. 대안 / 비채택

| 대안 | 설명 | 비채택 사유 |
|------|------|-------------|
| A1. DB 마스터 테이블 신설 (access_action_mst) | 테이블 + FK | 과잉 설계. 20~25종 유한 집합은 Enum이 적절 |
| A2. action_type VARCHAR → action_code ENUM 컬럼 전환 | DB 스키마 변경 | 본 스프린트 범위 초과. 후속 S9-B로 분리 |
| A3. 기존 DB 값 일괄 정제 UPDATE | 과거 로그 label 정규화 | 로그는 **역사적 기록**. 수정하면 감사성 훼손. 신규 로그만 Enum 적용 |

---

## 8. 사용자 승인 요청 사항

1. **Enum 매핑 표 (4-1)** — 13개 범주 + 리터럴 통합 방향 승인
2. **MenuName 추가 5종** 네이밍 (QR_LICENSE / LICENSE_REGISTRY / GEONURIS_LICENSE / QUOTATION / SIGNUP)
3. **Deprecated 제거 시점** — 후속 스프린트로 분리할지 or 이번에 제거할지 (기획은 분리 권장)
4. **과거 DB 데이터 정제 여부** — 본 기획은 **수행하지 않음** 권장

### 승인 확인 사항
- [ ] §4-1 AccessActionType 13종 매핑표 + §4-1-A fromKoLabel 정규화 규칙
- [ ] §4-2 MenuName 추가 5종 (총 16개 고정)
- [ ] §3-2 스키마 변경·과거 데이터 정제 제외
- [ ] §5 FR-1 ~ FR-5, NFR-1 ~ NFR-7 (ArchUnit / label Freeze 포함)
- [ ] §6 리스크 완화 (R-1 ~ R-6)
- [ ] §1-2 로드맵 §S9 정정 이력 (T-LINK 조치)
