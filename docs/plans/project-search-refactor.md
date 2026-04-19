# 🧭 기획서 — SW 사업 추진 현황 검색 기능 개편

- 작성일: 2026-04-19 (r2)
- 작성팀: 기획팀 (Claude)
- 대상 화면: `/projects/status` (project-list.html)
- 연관 파일: `SwController`, `SwService`, `SwProjectRepository`, `project-list.html`
- 개정: r1 codex 검토 반영 (FR-9 재정의, API 계약 보강, district 엄격화, DB 분석 세분화, 경계 테스트 추가)

---

## 1. 배경 및 문제 정의

### 현재 상태
- 단일 키워드 입력창 `kw` + **`&` 구분 AND 토큰** 방식
  - 예시: `2024&UPIS&진행중` → 3개 토큰 AND 검색
- 힌트 태그(진행중/완료/연도 2020~2026) 클릭 시 `&연도` 추가
- 서버: `SwService.search(kw)` → 토큰 분리 후 Specification AND

### 문제
1. **학습 곡선**: 사용자가 `&` 규칙을 알아야 사용 가능
2. **오타/공백 민감**: `2024 &UPIS` 같은 공백 혼란
3. **필터 가시성 낮음**: 현재 어떤 조건으로 검색 중인지 UI에 명확히 표시 안 됨
4. **반복 작업 번거로움**: 매번 같은 `&시도&시스템` 조합 재입력

---

## 2. 요구사항

### 2-1. 기능 요구사항 (FR)

| ID | 요구사항 | 우선순위 |
|----|---------|---------|
| **FR-1** | `&` AND 토큰 검색 방식 **제거** | 필수 |
| **FR-2** | 자유 텍스트 키워드 검색 `kw` **유지** (사업명·발주처·PMS코드) | 필수 |
| **FR-3** | **년도** 드롭다운 (distinct `year`) | 필수 |
| **FR-4** | **시도명** 드롭다운 (distinct `cityNm`) | 필수 |
| **FR-5** | **시군구명** 드롭다운 (선택된 시도에 **엄격 종속**) | 필수 |
| **FR-6** | **시스템명(영문)** 드롭다운 (distinct `sysNmEn`) | 필수 |
| **FR-7** | 각 필터에 **📌 고정(Pin)** 기능 — LocalStorage 지속 | 필수 |
| **FR-8** | "초기화" 버튼 — 핀 켜진 조건은 유지, 나머지만 리셋 | 필수 |
| **FR-9** | **부분 호환**: 기존 `?kw=xxx` 북마크는 동일 동작. `?kw=2024&UPIS` 같은 구 AND 토큰 URL은 **전체 문자열을 자유 검색** 으로 해석 (결과 달라질 수 있음 — 의도된 회귀 방식). 별도 리다이렉트/파서 없음. | 필수 |
| **FR-10** | `district` 단독 필터 **불허**: `city` 미선택 상태로 `district` 파라미터가 들어오면 서버가 무시 (400 아님, silent ignore) | 필수 |

### 2-2. 비기능 요구사항 (NFR)

| ID | 요구사항 |
|----|---------|
| NFR-1 | 시도 드롭다운 변경 시 시군구 드롭다운 AJAX 갱신 — 신규 엔드포인트 `GET /projects/api/dist-options?city=xxx` 사용 (기존 `/projects/api/districts` 와 별개로 sw_pjt 기반 distinct 반환) |
| NFR-2 | 시도 미선택 시 시군구 드롭다운 비활성화(`disabled` 속성) + 값 `null` |
| NFR-3 | 핀 상태 LocalStorage 키: `swProjSearchPins` = `{year, city, district, sysNmEn}` (boolean) |
| NFR-4 | 페이지네이션 이동 시 현재 필터 조건 유지 (URL 파라미터 재전달) |
| NFR-5 | 빈 문자열 파라미터는 서버에서 `null` 로 정규화 (trim 후 `isEmpty` → null) |
| NFR-6 | 기존 Deep Teal 디자인 토큰 유지 |
| NFR-7 | `year` 파라미터 숫자 검증: 파싱 실패 시 무시 (null 처리) |
| NFR-8 | AJAX 시군구 호출 실패 시 fallback: 기존 옵션 유지 + 비활성화 대신 "로드 실패" placeholder |
| NFR-9 | distinct 드롭다운 조회는 매 요청 수행 (캐시 도입은 Phase 2 로 연기) |

### 2-3. UI/UX 플로우

```
┌──────────────────────────────────────────────────────────────────────┐
│ SW 사업 추진 현황                                       [+ 신규 사업 등록] │
├──────────────────────────────────────────────────────────────────────┤
│ 🔍 [사업명/발주처/PMS코드 검색________________________________]           │
│                                                                      │
│  📌    📌     📌      📌                                              │
│ [년도 ▾][시도 ▾][시군구 ▾][시스템(EN) ▾]            [검색] [초기화]     │
│                                                                      │
│ 🔖 고정됨: 시스템(EN)=UPIS                                            │
└──────────────────────────────────────────────────────────────────────┘
```

**핀 상호작용**
- 드롭다운 위 작은 📌 아이콘 버튼 (회색 = OFF, teal = ON)
- ON: 필드 배경 `--primary-lt` + 🔖 뱃지 노출
- 초기화 버튼: 핀 OFF인 필드만 값 null로 리셋

**자동 검색 방지**
- 드롭다운 변경만으로는 자동 submit 하지 않음
- "검색" 버튼 클릭 시에만 `GET /projects/status?...` 발송
- (선택 변경 시 자동 검색이 편할 수도 있지만, 4개 필드를 차례로 고르는 시나리오에서 비효율)

---

## 3. 데이터 흐름 / API 계약

### 3-1. URL 파라미터 계약

| 파라미터 | 타입 | 필수 | 기본값 | 정규화 | 검증 |
|---------|------|------|--------|--------|------|
| `page`  | int  | N    | 0      | 음수→0 | `< totalPages` |
| `size`  | int  | N    | 10     | 고정 10 사용 (Client 변경 불허) | — |
| `kw`    | String | N  | `""`   | trim → `""` 이면 null | 길이 ≤ 100 |
| `year`  | Integer | N | null   | 숫자 파싱 실패 시 null | 2000 ≤ year ≤ 2099 (위반 시 null) |
| `city`  | String | N  | null   | trim → 빈값 null | — |
| `district` | String | N | null | trim → 빈값 null; **`city` 없으면 무시 (null)** | — |
| `sysNmEn` | String | N | null | trim → 빈값 null + uppercase | — |

**예시:**
```
GET /projects/status?page=0&kw=유지보수&year=2026&city=경기도&district=광명시&sysNmEn=UPIS
```

**정렬:** 서버 고정 (year DESC, cityNm ASC, distNm ASC, sysNm ASC) — 클라이언트 지정 불허

### 3-2. 서버 계약 변경

**Controller**
```java
@GetMapping("/status")
public String projectList(
    Model model,
    @RequestParam(defaultValue="0") int page,
    @RequestParam(defaultValue="") String kw,
    @RequestParam(required=false) String year,        // String 으로 받아 서비스에서 parseYearSafe (NFR-7)
    @RequestParam(required=false) String city,
    @RequestParam(required=false) String district,
    @RequestParam(required=false) String sysNmEn
) { ... }
```

**Service**
```java
Page<SwProject> search(
    String kw, String year, String city, String district, String sysNmEn,  // year String → parseYearSafe 로 Integer 변환
    Pageable pageable
);
```
- Specification AND 조합:
  - kw → 기존 다중 컬럼 OR 검색 (**& 토큰 제거**, 단일 문자열 전체 매칭)
  - year → `year = ?`
  - city → `cityNm = ?`
  - district → `distNm = ?`
  - sysNmEn → `sysNmEn = ?`

**Repository (기존 쿼리 활용)**
- `findDistinctYears()` ✅
- `findDistinctCityNms()` ✅
- `findDistinctSysNmEns()` ✅
- `findDistinctDistNmsByCity(city)` — 확인 필요 (없으면 추가)

### 3-3. Model 주입 (뷰)
```
kw, year, city, district, sysNmEn  — 현재 선택값
yearOptions: List<Integer>          — distinct 연도 desc
cityOptions: List<String>            — distinct 시도
sysNmEnOptions: List<String>         — distinct 시스템 EN
districtOptions: List<String>        — city 선택 시 서버 렌더 OR 빈 배열
paging: Page<SwProject>
```

---

## 4. DB 영향

### 4-1. 스키마 변경
- **새 테이블/컬럼 추가 없음**
- 기존 `sw_project` 테이블 그대로 사용

### 4-2. 데이터 규모 — **실측 (2026-04-19, live DB `sw_pjt`)**

| 지표 | SQL | 실측값 |
|------|-----|-------|
| 전체 레코드 수 | `SELECT COUNT(*) FROM sw_pjt` | **596** |
| 연도 개수 | `SELECT COUNT(DISTINCT year) ...` | **5** |
| 시도 개수 | `SELECT COUNT(DISTINCT city_nm) ...` | **17** |
| 시군구 개수 | `SELECT COUNT(DISTINCT dist_nm) ...` | **136** |
| 시스템(EN) 개수 | `SELECT COUNT(DISTINCT sys_nm_en) ...` | **8** |

### 4-3. 성능 판단 — **실측 EXPLAIN ANALYZE**

**A. 4-필터 AND 쿼리 (year=2026 AND city_nm='경기도')**
```
Limit (cost=0.00..16.13 rows=10) (actual time=0.015..0.076 rows=10)
  Seq Scan on sw_pjt (rows=26, removed=368)
Planning: 0.509 ms · Execution: 0.105 ms
```
→ **0.105ms** (목표 50ms 대비 475배 여유)

**B. distinct city_nm (시도 드롭다운)**
```
HashAggregate (rows=17)
  Seq Scan on sw_pjt (rows=596)
Planning: 0.052 ms · Execution: 0.303 ms
```
→ **0.303ms**

**C. distinct dist_nm WHERE city_nm='경기도' (시군구 종속)**
```
HashAggregate (rows=22)
  Seq Scan on sw_pjt (rows=108, removed=488)
Planning: 0.068 ms · Execution: 0.197 ms
```
→ **0.197ms**

**결론**: 모든 쿼리 < **1ms** — Sequential Scan으로 충분, **인덱스 추가 불필요**.

### 4-3. 쿼리 패턴
1. **메인 목록 쿼리** (Specification AND)
   - `WHERE year=? AND cityNm=? AND distNm=? AND sysNmEn=?` + OR kw 다중 컬럼 매칭
   - 현재 규모에서 full scan 비용 ≪ 인덱스 lookup 비용 → **인덱스 불필요**
2. **distinct 드롭다운 쿼리** (4종)
   - `year`, `cityNm`, `sysNmEn`: 전체 스캔이지만 레코드 수 2,000건 이하 → 수 ms 이내
   - `distNm`: `WHERE cityNm=?` 필터 후 distinct → cityNm 인덱스 있으면 유리

### 4-4. 인덱스 후보 (지금 즉시 추가 불필요, 성장 시 검토)
| 후보 | 컬럼 | 용도 | 도입 기준 |
|------|-----|------|----------|
| IX-1 | `(cityNm)` | 시군구 드롭다운 로드 | 레코드 > 5,000 |
| IX-2 | `(year, sysNmEn)` | 연도+시스템 조합 최다 예상 | 레코드 > 10,000 |
| IX-3 | `(cityNm, distNm, sysNmEn, year)` covering | 4필터 동시 | 레코드 > 50,000 |

**결론**: 현 단계(~수백 건)에서는 **인덱스 추가 없이도 충분**. Phase 2(데이터 10배 이상 성장)에서 IX-1 우선 고려.

### 4-5. EXPLAIN 측정 계획

**구현 직후 개발팀이 **반드시 수행**하고 결과를 본 문서 Appendix A 에 첨부:**

```sql
-- A. 최악 케이스 (kw + 4필터 AND)
EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM sw_project
WHERE year=2026 AND city_nm='경기도' AND dist_nm='광명시' AND sys_nm_en='UPIS'
  AND (LOWER(proj_nm) LIKE '%유지%' OR LOWER(client) LIKE '%유지%')
ORDER BY year DESC, city_nm ASC LIMIT 10;

-- B. 드롭다운 distinct 3종
EXPLAIN ANALYZE SELECT DISTINCT year FROM sw_project ORDER BY year DESC;
EXPLAIN ANALYZE SELECT DISTINCT city_nm FROM sw_project;
EXPLAIN ANALYZE SELECT DISTINCT sys_nm_en FROM sw_project WHERE sys_nm_en IS NOT NULL;

-- C. 시군구 종속 쿼리
EXPLAIN ANALYZE SELECT DISTINCT dist_nm FROM sw_project WHERE city_nm='경기도';
```

**게이트:**
- A·C의 Execution Time > **50ms** 이면 구현 배포 **중단** + IX-1 인덱스 즉시 추가
- B의 Execution Time > **20ms** 이면 distinct 캐시 도입 검토
- 초과 없으면 ✅ 성능 합격으로 문서 체크

---

## 5. 기존 기능 제거

```
project-list.html
├─ [제거] `.search-hint` 영역 (힌트 태그 바)
├─ [제거] `.hint-tag` 클래스
├─ [제거] `appendHint()` JS 함수
├─ [제거] `.search-input.multi-mode` 강조 스타일
└─ [제거] placeholder 의 `& 구분` 안내 문구
```

---

## 6. LocalStorage 스키마

```json
// key: swProjSearchPins
{
  "year": false,
  "city": false,
  "district": false,
  "sysNmEn": true    // UPIS 고정 중
}

// key: swProjSearchValues (핀 켜진 필드의 마지막 값)
{
  "sysNmEn": "UPIS"
}
```

- 페이지 로드 시: URL 파라미터 > LocalStorage(핀) > 빈 값 순 우선순위
- 핀 토글 OFF → 해당 필드 LocalStorage에서 제거

---

## 7. 리스크 / 엣지 케이스

| ID | 리스크 | 완화 |
|----|-------|------|
| R1 | 기존 북마크 `?kw=2024&UPIS&진행중` 링크 | `kw`를 그대로 자유 검색으로 처리. AND 토큰 파싱 없어서 원래 결과와 달라질 수 있음. 공지 불필요 (점진적 사용자 학습) |
| R2 | 시도 바꾸면 시군구 값이 해당 시도에 없는 경우 | JS에서 시도 변경 시 district 값 clear |
| R3 | 핀 켜진 필드 값이 DB에서 삭제된 경우 | 드롭다운에 옵션 없음 → select 가 빈 값으로 렌더. 서버는 WHERE 절에서 0 행 반환 (자연스러움) |
| R4 | AND 조합으로 결과 0건 | 엠프티 스테이트로 이미 대응 완료 (이전 개편에서) |
| R5 | 동시에 kw + 4개 드롭다운 모두 채울 경우 쿼리 복잡도 | 소규모 데이터셋(<만건) 가정, Specification 조합이므로 문제 없음 |

---

## 8. 테스트 시나리오 (T)

### 8-1. 기능
| ID | 시나리오 | 기대 결과 |
|----|---------|----------|
| T1 | kw만 입력 후 검색 | 기존과 동일하게 다중 컬럼 OR 매칭 |
| T2 | 시도 "경기도" 선택 | 시군구 드롭다운에 경기도 하위 시군구만 표시 |
| T3 | 년도 2026 + 시도 강원 + 시스템 UPIS 선택 후 검색 | AND 매칭 결과만 노출 |
| T4 | 시스템 UPIS 핀 ON → 초기화 | UPIS 만 유지되고 나머지 리셋 |
| T5 | 핀 3개 ON 상태에서 새 탭으로 `/projects/status` 접근 | 핀 켜진 필드 값 자동 복원 |
| T6 | 직접 URL `?year=2024&city=강원특별자치도` 접근 | 두 필터만 적용된 결과 |
| T7 | 페이지 2 이동 | 현재 필터 파라미터 URL 에 포함되어 유지 |

### 8-2. 하위 호환 & 회귀
| ID | 시나리오 | 기대 결과 |
|----|---------|----------|
| T8  | `?kw=UPIS` 접근 | 정상 동작 (기존과 결과 동일) |
| T9  | 구 AND 토큰 URL `?kw=2024%26UPIS` 접근 | 결과 달라짐 (전체 문자열 자유 검색) — 의도된 동작, 오류 아님 |
| T10 | 구 힌트 태그 `?kw=UPIS%26진행중` 북마크 | "UPIS&진행중" 문자열 전체로 검색 → 아마 0건. 엠프티 스테이트 표시 |

### 8-3. 경계/예외
| ID | 시나리오 | 기대 결과 |
|----|---------|----------|
| T11 | `?year=abc` (숫자 아님) | year 무시 (null 처리) + 정상 응답 |
| T12 | `?year=1800` (범위 밖) | year 무시 + 정상 응답 |
| T13 | `?kw=` (빈 문자열) | null 처리 + 전체 목록 |
| T14 | `?kw=%20%20%20` (공백만) | trim → null → 전체 목록 |
| T15 | `?district=광명시` (city 없음) | district **무시** + 전체 목록 (silent ignore) |
| T16 | kw 대소문자 `upis` vs `UPIS` | 기존 LOWER 비교 로직 유지로 동일 결과 |
| T17 | 시도 드롭다운 AJAX 실패 (`/projects/api/dist-options` 5xx) | 기존 시군구 옵션 보존 + 상단 "⚠ 로드 실패" placeholder + 다른 필터는 사용 가능 (NFR-8) |
| T18 | kw 길이 > 100자 | 100자까지만 잘림 (또는 validation 경고) |

### 8-4. LocalStorage
| ID | 시나리오 | 기대 결과 |
|----|---------|----------|
| T19 | LocalStorage 파싱 실패 (JSON 깨짐) | try-catch 후 초기값으로 복원, 에러 콘솔에만 로그 |
| T20 | 핀 4개 모두 ON 후 초기화 | 모두 그대로 유지 |
| T21 | 핀 OFF 상태에서 검색한 값이 다음 진입 시 복원되는가? | **아니오** (핀만 복원, 비핀 필드는 URL 의존) |

---

## 9. 결정 사항 (사용자 승인 완료)

- **OQ-1** (자동 submit?): ❌ **검색 버튼 클릭 방식** (사용자 확정)
- **OQ-2** (비핀 필드 기억?): ❌ **핀만 기억** (사용자 확정)
- **OQ-3** (URL vs LocalStorage 충돌?): ✅ **URL 우선** (사용자 확정)
- **OQ-4** (FR-9 호환 방식?): ✅ **부분 호환 (A안)** — 결과 달라질 수 있음 감수 (사용자 확정)
- **OQ-5** (district 종속?): ✅ **엄격 종속 (A안)** — city 없으면 무시 (사용자 확정)

---

## 10. 릴리스 체크리스트

- [ ] codex 기획서 검토 ✅
- [ ] 사용자 최종승인 ✅
- [ ] 개발계획서 작성
- [ ] codex 개발계획 검토
- [ ] 사용자 최종승인
- [ ] 구현
- [ ] T1~T8 codex 검증
- [ ] 사용자 확인
- [ ] git commit + push
