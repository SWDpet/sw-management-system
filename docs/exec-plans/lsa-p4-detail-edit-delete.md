# [개발계획] LSA P4 — 상세 / 수정 / 삭제 (마무리 CRUD)

- **기획서**: `docs/product-specs/lsa-license-ledger.md` / **선행**: P1(cba27a0)·P2(ff01a99)·P3(487f1c2)
- **단계**: P4/4 (마무리). **상태**: 개발계획(codex 검토 대기). 머신=회사 PC.

---

## P4 범위 (기존 LSA 레코드 상세·수정·삭제)

### 1. LsaService 확장
- `getById(Long id)` → `LsaDTO`(없으면 ResourceNotFoundException). 상세·수정 prefill 공용.
- `update(Long id, LsaForm form, String issuer)` @Transactional: 기존 Lsa 로드(없으면 NotFound) → 필수값 검증 → ps_info upsert(create 와 동일 dedup) → 필드 갱신 + updatedBy/updatedAt + psInfoId 재연결. (create 로직 재사용 헬퍼화.)
- `delete(Long id)` @Transactional: lsa_license 행만 삭제(ps_info 보존 — 담당자 이력 유지).

### 2. LsaController 확장
- `GET /lsa/{id}` → 상세 `lsa/lsa-detail`(checkViewAuth, model lsa·canEdit).
- `GET /lsa/{id}/edit` → 수정 폼(checkEditAuth, lsa-form 재사용 + 기존값 prefill + hidden id).
- `POST /lsa/save` 확장: `LsaForm.id` nullable — **id 있으면 update, 없으면 create**. 발급자 위조방지 동일(비관리자 실명 강제).
- `POST /lsa/{id}/delete` → 삭제(checkEditAuth) → redirect:/lsa/list.
- ⚠경로 충돌 주의: `/{id}`(GET) vs `/list`·`/new`·`/api/*` — Spring 은 구체경로 우선이나 `{id}` 는 Long 바인딩이라 'list' 등과 미충돌(타입 불일치). 단 매핑 순서·정규 확인.

### 3. 화면 (⚠ 구현 전 디자인 자문)
- **lsa-detail.html**(신규): 읽기전용 카드(지자체·부서·팀·이름·연락처·버전·발급자·등록/수정일) + 수정·삭제 버튼(canEdit) + 목록 버튼. 디자인시스템 토큰·카드 패턴.
- **lsa-form.html** 수정: 제목 동적(작성/수정), hidden `id`, 기존값 th:value prefill(lsa 모델). 시도/시군구 prefill(edit 시 select 초기값).
- **lsa-list.html** 수정: 행 클릭→상세 링크 + (canEdit) 수정/삭제 액션. 삭제는 POST form(_csrf) + confirm.

### 4. 검증 (S 품질)
- **LsaServiceTest 확장**: getById(존재/NotFound)·update(필드갱신·ps_info 재upsert·updatedBy)·delete(lsa만 삭제·ps_info 보존)·update 필수값 검증.
- **LsaControllerMvcTest 확장**: 상세 VIEW 200/NONE 403·수정폼 EDIT 200/VIEW 403·save update(id 분기)·delete EDIT 3xx/VIEW 403.
- `mvnw -o clean verify` green(MapDebt·도메인순수성·ControllerRepoRatchet) + **endpoint golden +3**(/lsa/{id}·/lsa/{id}/edit·/lsa/{id}/delete) GOLDEN_RECORD.
- 회사 PC: 앱 재기동 → 브라우저 QA(작성→상세→수정→삭제 라이프사이클·운영DB 반영·삭제 시 ps_info 보존 확인) + QA행 정리.

## 4-1. codex 반영(APPROVE-WITH-FIX 4)
- `update(Long id, LsaForm form, String issuer, String updatedBy)` — updatedBy=cu.getUsername() 컨트롤러 전달.
- **LsaDTO 에 `updatedAt`(+`updatedBy`) 추가**(상세 수정일 표시). fromEntity 갱신.
- `delete` 도 먼저 조회→없으면 ResourceNotFoundException(deleteById 직접 호출 금지, 예외 일관).
- 경로 `@GetMapping("/{id:\\d+}")`·`/{id:\\d+}/edit`·`@PostMapping("/{id:\\d+}/delete")` 정규 제약(list/new/api 충돌 원천 차단).

## 5. 리스크
- update ps_info 재upsert: 담당자 변경 시 새 ps_info 생성/기존 재사용(create 와 동일, 일관). 기존 연결 person 은 보존(삭제 안 함).
- delete 는 lsa_license 만 — ps_info 미삭제(다른 LSA/이력 참조 가능). FK ON DELETE 없음.
- 경로 매핑 `/{id}` Long — list/new/api 와 타입 분기. endpoint golden 갱신 필수.
- create/update 공통 로직 헬퍼화로 중복 0(코드품질).
