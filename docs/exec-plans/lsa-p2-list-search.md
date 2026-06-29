# [개발계획] LSA P2 — 목록 + 검색

- **기획서**: `docs/product-specs/lsa-license-ledger.md` / **선행**: P1(cba27a0·d77af77, 토대 완료)
- **단계**: P2/4. **상태**: 개발계획(codex 검토 대기). 머신=회사 PC.

---

## P2 범위 (lsa_license 실데이터 목록 + 키워드 검색)

### 1. 엔티티/리포지토리
- **`Lsa`**(`com.swmanager.system.lsa.domain`): `@Table("lsa_license")` 매핑. id(`@GeneratedValue IDENTITY` — DEFAULT nextval 호환, OpsDocument 패턴)·cityNm·distNm·deptNm·teamNm·userNm·tel·email·version·issuer·psInfoId·createdBy·createdAt·updatedBy·updatedAt. Lombok @Getter/@Setter.
- **`LsaRepository`**(extends JpaRepository<Lsa,Long>): 검색 `@Query`
  ```
  SELECT l FROM Lsa l WHERE (:kw IS NULL OR l.cityNm LIKE %:kw% OR l.distNm LIKE %:kw%
    OR l.userNm LIKE %:kw% OR l.version LIKE %:kw% OR l.issuer LIKE %:kw%) ORDER BY l.createdAt DESC
  ```
  (AccessLog/Infra 검색 관례 동일).

### 2. DTO/서비스
- **`LsaDTO`**(record 또는 @Getter projection): 목록 행(id·지자체(city+dist)·부서·팀·이름·연락처(tel/email)·버전·발급자·등록일). `fromEntity`.
- **`LsaService`**: `list(String keyword)` → `List<LsaDTO>`(빈 키워드 null 정규화). MapDebt 금지(타입화 DTO).

### 3. 컨트롤러
- **`LsaController.list`** 확장: `@RequestParam(required=false) String keyword` + checkViewAuth + model(`lsaList`·`keyword`·`canEdit`=EDIT|admin). 기존 `/lsa/list` 시그니처 교체.

### 4. 화면 (⚠ 디자인 자문 — 구현 전 정식)
- **`lsa-list.html`** 실 구현: 검색 폼(keyword input + 검색 버튼, GET `/lsa/list`) + 작성 버튼(EDIT|admin, P3 까지 disabled) + 테이블(지자체·부서·팀·이름·연락처·버전·발급자·등록일). 빈 결과 행. 디자인시스템 표준(.btn 로컬정의·테이블 클래스 quotation-list 관례 재사용).

### 5. 검증 (S 품질)
- **`LsaServiceTest`**: 검색 키워드 null/값 분기·DTO 매핑(mock repo). 
- **`LsaControllerMvcTest`** 확장: list VIEW → 200·model(lsaList/keyword/canEdit)·검색 param 바인딩·service 호출 검증. 기존 4케이스 유지.
- `mvnw -o clean verify` green(JaCoCo·**MapDebt ratchet**·거대클래스·ArchUnit) + PIT(해당시) + CI 3 job. endpoint-inventory 변화 없음(/lsa/list 동일 경로, param만 추가). 
- 회사 PC: 앱 재기동 → 브라우저 QA(빈 목록·검색 폼 렌더·검색 동작). 데이터는 P3 입력 후 검증.

## 5-1. codex 반영(APPROVE-WITH-FIX)
- 정렬 `ORDER BY l.createdAt DESC, l.id DESC`(동률/NULL 안정).
- `Lsa` 엔티티는 dto/service/controller import 금지(ArchUnit 도메인순수성).
- **`LsaControllerMvcTest`: `new LsaController()` → mock `LsaService` 생성자 주입**으로 갱신 + 케이스(keyword param 전달·lsaList/keyword/canEdit model·VIEW canEdit=false·EDIT/admin canEdit=true).
- 컨트롤러는 `LsaRepository` 직접 참조 금지(service 경유, ControllerRepositoryRatchet).
- blank keyword → null 서비스 테스트. endpoint golden 변경 불요(경로 동일).

## 6. 리스크
- Lsa IDENTITY + DEFAULT nextval 호환: 빈 테이블이라 insert 는 P3. P2 는 조회만 → 무부하.
- 검색 LIKE 인덱스 없음(소량 데이터 → 무시, 추후 필요시 인덱스).
- canEdit 모델: getAuthLsa EDIT 또는 admin. (작성 버튼은 P3 까지 비활성.)
- MapDebt: LsaDTO 타입화로 신규 Map 0.
