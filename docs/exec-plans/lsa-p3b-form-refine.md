# [개발계획] LSA P3-b — 입력폼 개선(부서/팀 prefill·발급자 선택·버전 기본값)

- **선행**: P1~P4 완료. 본 건은 P3 입력폼 후속 개선(사용자 요청 3건). 머신=회사 PC.

## 요구사항(사용자)
1. **부서·팀도 ps_info에서 불러오기** + 수정 가능.
2. **발급자**: 관리자만 수정, 타이핑 대신 **users 이름 선택(드롭다운)**. 비관리자=수정 불가(로그인 실명).
3. **버전 기본값 "3.0"** + 수정 가능.

## 구현
### 1. 부서/팀 prefill
- **`PersonRow`** record +`deptNm`,`teamNm`(fromEntity 갱신). `/lsa/api/persons` 응답에 포함.
- **`담당자 불러오기`** 쿼리는 city+dist 기준(dept/team 선택적 narrowing — findCandidates 이미 null-guard). 선택 시 applyPerson 이 부서/팀/이름/전화/이메일 모두 채움. **부서/팀 input 유지(수정 가능)**.
- JS `applyPerson(p)`: deptNm/teamNm 도 set.

### 2. 발급자 선택(users 드롭다운)
- **`LsaService.issuerCandidates()`** → enabled users 실명 distinct(`UserRepository.findByEnabledTrue().map(getUsername)`). UserRepository 생성자 주입(controller→repo 직접참조 회피).
- **`LsaController`** newForm/editForm: `model.userList=issuerCandidates()`(관리자만 필요하나 항상 추가 무해).
- **`lsa-form.html`** 발급자:
  - 관리자: `<select name="issuer">`(userList, 기존값/로그인실명 selected).
  - 비관리자: readonly input(로그인 실명) — 기존 유지.
- 서버 위조방지 **불변**: 비관리자 폼값 무시(로그인 실명 강제), 수정 시 발급자 보존(issuerOverride). 관리자 select 값=폼값 채택.

### 3. 버전 기본값
- `lsa-form.html` version input: `th:value="${lsa != null ? lsa.version() : '3.0'}"`(신규 3.0, 수정=기존값). required 유지.

## 검증
- **LsaServiceTest**: issuerCandidates(enabled users 실명 매핑) + PersonRow deptNm/teamNm 매핑(persons API).
- **LsaControllerMvcTest**: newForm model userList 존재. 발급자 위조방지 기존 테스트 유지(비관리자 무시).
- mvnw -o clean verify green(MapDebt·도메인순수성·ControllerRepoRatchet). endpoint golden 불변(신규 endpoint 없음).
- ⚠**디자인 자문**(발급자 select 추가 — 폼 일관성). 회사 PC 브라우저 QA(prefill 부서/팀·발급자 select·버전 3.0).

## 리스크
- 발급자 select: 관리자가 임의 이름 선택 가능(users 목록 내). 위조방지는 비관리자만 강제(관리자는 원래 변경 권한).
- prefill 부서/팀 덮어쓰기: 사용자가 수정 가능하므로 OK.
- UserRepository 주입: ControllerRepoRatchet 회피 위해 service 경유(controller 직접참조 X).
