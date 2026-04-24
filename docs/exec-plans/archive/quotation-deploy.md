# 견적서 모듈 배포 가이드

## 1단계: PostgreSQL에 테이블 생성

### pgAdmin 또는 DBeaver에서 실행
```
접속 정보: 192.168.10.194:5880 / DB: SW_Dept
```

파일 위치: `swdept/sql/V002_quotation_tables.sql`

**실행 내용:**
- `users` 테이블에 `auth_quotation` 컬럼 추가 (기존 데이터 영향 없음)
- `qt_quotation` — 견적서 메인 테이블
- `qt_quotation_item` — 견적 품목 테이블
- `qt_quotation_ledger` — 견적 대장 테이블
- `qt_product_pattern` — 품명 패턴 테이블 (137개 시드)
- `qt_quote_number_seq` — 견적번호 시퀀스 테이블
- 인덱스 생성 + 137개 분석 기반 패턴 INSERT

### 실행 방법
```sql
-- pgAdmin에서: 쿼리 도구 열기 → V002_quotation_tables.sql 전체 붙여넣기 → 실행(F5)
```

### 실행 후 확인
```sql
SELECT count(*) FROM qt_product_pattern;  -- 137
SELECT auth_quotation FROM users WHERE user_role = 'ROLE_ADMIN';  -- EDIT
SELECT * FROM information_schema.tables WHERE table_name LIKE 'qt_%';  -- 5개 테이블
```

---

## 1-1단계: 패턴 하위항목 데이터 추가 (신규)

파일 위치: `swdept/sql/V003_pattern_subitems.sql`

**실행 내용:**
- 주요 패턴 13개에 `sub_items` (하위항목 세트) 데이터 추가
- 견적서 작성 시 패턴 선택하면 하위 상세항목이 자동 채워짐
- 모든 하위항목은 사용자가 수동으로 편집 가능

### 실행 방법
```sql
-- pgAdmin에서: 쿼리 도구 열기 → V003_pattern_subitems.sql 전체 붙여넣기 → 실행(F5)
```

### 실행 후 확인
```sql
SELECT product_name, sub_items FROM qt_product_pattern
WHERE sub_items IS NOT NULL ORDER BY usage_count DESC;
-- 13건의 하위항목 데이터 확인
```

---

## 2단계: Eclipse에서 빌드

### Maven 빌드
1. Eclipse에서 프로젝트 우클릭 → **Maven** → **Update Project** (Force Update 체크)
2. 빌드: `Run As` → `Maven Build...` → Goals: `clean compile` → Run
3. 에러 없으면 서버 실행: `Run As` → `Spring Boot App` 또는 Tomcat

### 컴파일 확인 포인트
만약 빌드 에러 발생 시 확인:

| 에러 | 원인 | 해결 |
|------|------|------|
| `InsufficientPermissionException` 못 찾음 | import 경로 | `com.swmanager.system.exception.InsufficientPermissionException` 확인 |
| `LogService` 못 찾음 | import 경로 | `com.swmanager.system.service.LogService` 확인 |
| `CustomUserDetails` 못 찾음 | import 경로 | `com.swmanager.system.config.CustomUserDetails` 확인 |
| thymeleaf-extras-springsecurity6 | pom.xml 의존성 | thymeleaf-extras-springsecurity6 추가 필요 여부 확인 |

---

## 3단계: 기능 테스트

### 3-1. 로그인 및 메뉴 확인
1. `http://localhost:8080` 접속 → 로그인
2. 메인 대시보드 GNB에 `📝 견적서` 메뉴 표시 확인
3. 관리자(ROLE_ADMIN)이면 `+` 아이콘도 표시

### 3-2. 견적서 목록
- `http://localhost:8080/quotation/list` 접속
- 연도/카테고리/상태 필터 동작 확인

### 3-3. 견적서 작성
1. `📝 견적서` → `+` 또는 `견적서 작성`
2. 분류 선택 (용역/제품/유지보수) → 예상 견적번호 표시 확인
3. 건명, 수신, 참조 입력
4. `패턴 불러오기` → 품명 패턴 선택 → **하위항목 미리보기 확인**
5. 패턴 적용 후 **내용 칸에 하위항목이 자동 채워지는지 확인** (편집 가능)
6. 수량, 단가 입력 → 금액 자동 계산 → **단가 헤더의 VAT포함 체크박스 확인**
7. `미리보기` 클릭 → **PDF와 동일한 레이아웃 확인** (단위 컬럼 없음, 한글 날짜, 서울 주소, 하위항목 표시)
8. `발행` 클릭 → 목록으로 이동

### 3-4. 미리보기 확인 (신규)
1. 발행된 견적서 상세 → `미리보기` 클릭
2. 확인 포인트:
   - 주소: **서울특별시 강남구 테헤란로 38길 29 이원빌딩 5F**
   - 견적일자: **2026년 2월 9일** (한글 형식)
   - 품목 테이블: **No / 품명 / 수량 / 단가 / 금액 / 비고** (단위 컬럼 없음)
   - 품명 아래 하위항목 표시 (- 제품명 :, - Desktop 1식, 등)
   - 합계 테이블에 비고 컬럼 존재
3. `인쇄하기` 클릭 → 인쇄 미리보기에서 A4 형태 확인

### 3-5. 견적 대장
- `http://localhost:8080/quotation/ledger` → 자동 등록 확인

### 3-6. 패턴 관리
- `http://localhost:8080/quotation/patterns` → 137개 패턴 확인
- 패턴 추가 테스트

### 3-7. 관리자 권한 설정
1. 관리자 페이지 → 사용자 관리
2. 각 사용자의 `견적서` 권한 (X/조회/편집) 변경
3. 해당 사용자로 접근 시 권한 동작 확인

---

## 수정된 파일 목록

### 1차 검증에서 수정된 파일 (버그 수정)

| 파일 | 수정 내용 |
|------|----------|
| `QuotationController.java` | InsufficientPermissionException 생성자 호출 수정 ("로그인이 필요합니다." → "로그인") |
| `quotation-form.html` | 카테고리 enum 값 (SERVICE→용역), MODE 감지, 편집 모드 items 로딩, 폼 제출 URL/메서드, 날짜 포맷 |
| `quotation-ledger.html` | 날짜 포맷 #calendars → #temporals (LocalDate/LocalDateTime 호환) |
| `pattern-list.html` | 통화 포맷 COMMA 파라미터 추가, 그룹 헤더/데이터 행 단일 루프로 통합 |
| `QuoteNumberSeq.java` | @Column(name = "seq_id") 추가 (DB 컬럼명 매핑) |

### 2차 수정 (미리보기 + 하위항목 기능)

| 파일 | 수정 내용 |
|------|----------|
| `quotation-preview.html` | PDF와 동일한 레이아웃으로 전면 재작성: 서울 주소, 한글 날짜, 단위 컬럼 제거, 하위항목(specification) 표시, 합계 비고 컬럼 |
| `quotation-form.html` | 패턴 카테고리 탭 한글화(용역/제품/유지보수), 패턴에 sub_items 미리보기, 패턴 적용 시 specification 자동 채움, 규격 입력을 textarea로 변경, 실시간 미리보기 레이아웃 동기화(단위 컬럼 제거, 한글 날짜, 서울 주소, 하위항목 표시) |
| `V003_pattern_subitems.sql` | 주요 패턴 13개에 sub_items 데이터 추가 (신규 SQL 파일) |

### 3차 수정 (VAT 체크박스 이동 + UI 개선)

| 파일 | 수정 내용 |
|------|----------|
| `quotation-form.html` | VAT 체크박스를 기본정보에서 품목 테이블 단가 컬럼 헤더로 이동, "규격" 컬럼명을 "내용"으로 변경, 내용 textarea 크기 확대(min-height:50px), 견적금액 항상 "VAT 포함" 표시 |
| `quotation-preview.html` | 건명 영역 위 표 윗선 누락 수정(border-top 복원), 견적금액 항상 "(VAT 포함)" 고정 표시 |

### 4차 수정 (헤더 TABLE 레이아웃 변경)

| 파일 | 수정 내용 |
|------|----------|
| `quotation-preview.html` | 상단 정보 영역을 flexbox에서 TABLE 기반 레이아웃으로 전면 변경: 좌측 5행(견적번호/견적일자/빈행/수신/참조), 우측 5행(사업자번호/상호/대표자/주소/업태·업종), 라벨에 letter-spacing 적용, 직인 rowspan=5 세로 병합, 업태/업종 행 신규 추가 |
| `quotation-form.html` | handlePreview() JS 팝업 미리보기도 동일한 TABLE 기반 헤더 레이아웃으로 변경, CSS 클래스 .ht/.hl/.hs/.hv/.hseal 신규, 업태/업종 행 추가 |

### 신규 파일

| 파일 | 설명 |
|------|------|
| `swdept/sql/V003_pattern_subitems.sql` | 패턴 하위항목 시드 데이터 |
