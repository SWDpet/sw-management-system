# [기획서] 감사 후속조치 스프린트 2b — P2 Dead code 3건

- **작성팀**: 기획팀
- **작성일**: 2026-04-19
- **선행**: `b93e8bc` (스프린트 2a P2 스키마·문서)
- **상태**: ✅ 완료 (commit `914fa5c`, 2026-04-19) — Dead code 3건 모두 ☑ 조치함.

---

## 1. 배경 / 목표

감사 P2 중 **Dead code 그룹 3건** 처리:

| ID | 내용 | 권고 |
|----|------|------|
| 4-1 | 미사용 서비스 `ProjectService` — 주입·호출 없음 | 삭제 |
| 4-2 | `SwService` 미호출 public 메서드 11개 | 삭제 |
| 4-3 | Orphan 템플릿 2 — `customer-list.html`, `license/registry-new-list.html` | 삭제 |

**재검증 결과(2026-04-19)**:
- `ProjectService`: 전체 소스에서 해당 클래스가 주입·호출되는 지점 0건.
- `SwService.{getAllProjects/saveProject/getProjectById/deleteProject/getAllUsers/getDistinctYears/getSystemStats/isDuplicate/getProjectDTOById/getProjectListAsDTO/saveProjectFromDTO}`: 각 메서드의 외부 호출 0건. (`SwController` 는 `search/getList/getProject/save/delete` 5개만 사용)
- 템플릿 2개: 어떤 `.java`/`.html` 에도 참조 없음.

**목표**: 런타임·빌드에 영향 없이 **죽은 코드·리소스 삭제** → 유지보수 부담 경감, 오독 가능성 제거.

---

## 2. 기능 요건 (FR)

### 4-1. ProjectService 삭제

| ID | 내용 |
|----|------|
| FR-4-1-A | `src/main/java/com/swmanager/system/service/ProjectService.java` 파일 삭제. |
| FR-4-1-B | 삭제 전 최종 스캔: `rg -n "ProjectService" src/` 결과가 본인 파일 외 0건이어야 함. 만약 예기치 못한 참조 발견 시 삭제 중단 후 별도 판단. |

### 4-2. SwService 미사용 메서드 11개 삭제

| ID | 내용 |
|----|------|
| FR-4-2-A | `SwService` 에서 다음 11개 public 메서드 및 관련 주석/섹션 구분자 삭제: `getAllProjects`, `saveProject(SwProject)`, `getProjectById`, `deleteProject`, `getAllUsers`, `getDistinctYears`, `getSystemStats`, `isDuplicate`, `getProjectDTOById`, `getProjectListAsDTO`, `saveProjectFromDTO`. **추가**(codex 지적 반영): private 헬퍼 `resolveStatCodes(String)` 도 호출자 0건이므로 함께 삭제. |
| FR-4-2-B | **유지 메서드**: `getList(Pageable)`, `search(String,Pageable)`, `buildAndSpec`(private), `getProject(Long)`, `save(SwProject)`, `delete(Long)`. |
| FR-4-2-A-검증 | 삭제 전 각 메서드의 호출자 0건을 재확인. 기준 커맨드: `rg -n "swService\.(getAllProjects\|saveProject\|getProjectById\|deleteProject\|getAllUsers\|getDistinctYears\|getSystemStats\|isDuplicate\|getProjectDTOById\|getProjectListAsDTO\|saveProjectFromDTO\|resolveStatCodes)" src/` 결과 0건. |
| FR-4-2-C | 삭제 후 사용하지 않게 되는 import 정리: `ContStatMst`/`ContStatMstRepository`/`User`/`UserRepository`/`SwProjectDTO`/`DuplicateResourceException`/`ErrorCode`/`ResourceNotFoundException` 중 **실제로 나머지 코드에서 참조되지 않는 것만** 제거 (`search/buildAndSpec` 가 `ContStatMst`/`ContStatMstRepository` 사용, `search/save` 가 log 사용 등 확인 필요). |
| FR-4-2-D | 의존성 필드(`userRepository`, `contStatMstRepository`) 중 남은 코드가 쓰지 않는 것은 제거. `contStatMstRepository` 는 `buildAndSpec/resolveStatCodes` 가 사용하므로 **유지**. `userRepository` 는 `getAllUsers` 전용이었다면 제거. |

### 4-3. Orphan 템플릿 삭제

| ID | 내용 |
|----|------|
| FR-4-3-A | `src/main/resources/templates/customer-list.html` 삭제. |
| FR-4-3-B | `src/main/resources/templates/license/registry-new-list.html` 삭제. |
| FR-4-3-C | 삭제 전 최종 스캔: `rg -n "customer-list|registry-new-list" src/` (rg 는 `|` 를 바로 OR 로 해석) 결과 0건 확인. |

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 삭제 후 빌드 성공 — Maven 컴파일 오류 0건. |
| NFR-2 | 런타임 회귀 없음 — 서버 재기동 후 다음 경로 스모크 테스트 정상: `/projects/status`, `/projects/detail/{id}`, `/projects/form`, `/license/registry/list`, `/admin/system-graph`. (실제 라우트는 `SwController @RequestMapping("/projects")`, `LicenseRegistryController "/license/registry"` 기준) |
| NFR-3 | 관련 JPA 쿼리/Hibernate SQL 로그에 예기치 못한 오류 없음. |
| NFR-4 | 삭제는 **비가역** — 롤백은 git revert 로만 수행. 본 문서 및 감사 보고서에 삭제 내역 명시. |

---

## 4. 의사결정 / 우려사항

### 4-1. `SwService` 메서드 중 일부는 외부(템플릿)에서 Thymeleaf 로 쓰일 가능성? — ✅ 배제
- Thymeleaf 에서 `@swService.xxx()` 패턴 또는 JavaScript `fetch('/api/...')` 경로로 간접 사용될 수 있으나, 재검증 시 `grep` 결과 해당 메서드명 모두 0건 → 서비스 레이어 직접 호출 없음.

### 4-2. `ProjectService` vs `SwService.save` 중복 — ✅ SwService 쪽 유지 확정
- `ProjectService.saveProject` 의 중복체크 로직은 `SwService.saveProjectFromDTO` 에도 구현 (둘 다 삭제 대상이지만 향후 중복체크가 다시 필요해지면 `SwController` 레벨 or `SwService.save` 내 옵션 파라미터로 재구현 검토).

### 4-3. 템플릿 삭제 시 사이트맵/문서 영향 — ✅ 없음
- 두 파일 모두 컨트롤러 매핑 없어 URL 로 도달 불가 → 사용자 UI 에서 접근 경로 없음.

### 4-4. 삭제 방식 — ✅ 확정
- 파일 단위 삭제(`rm`), Java 메서드는 파일 내 Edit 로 제거. `@Deprecated` 경유 단계는 **스킵** — 이미 호출자 0건이므로 단계적 폐기 의미 없음.

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Service (삭제) | `src/main/java/com/swmanager/system/service/ProjectService.java` | 파일 삭제 |
| Service (수정) | `src/main/java/com/swmanager/system/service/SwService.java` | 메서드 11개 + 관련 import/필드 제거 |
| Template (삭제) | `src/main/resources/templates/customer-list.html` | 파일 삭제 |
| Template (삭제) | `src/main/resources/templates/license/registry-new-list.html` | 파일 삭제 |
| Docs (수정) | `docs/generated/audit/2026-04-18-system-audit.md` | 4-1, 4-2, 4-3 체크박스 ☑ 조치함 |

**삭제 3 파일 + 수정 2 파일. 엔티티/DB/API 계약 변경 0.**

---

## 6. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| `SwService` 에서 사용하는 import·필드를 오판해 제거하여 컴파일 깨짐 | 중간 | 단계별 진행: 메서드 삭제 → `mvn compile` → import 정리 → `mvn compile` 재실행 |
| 런타임 Thymeleaf 뷰에서 간접 참조 누락 발견 | 낮음 | 서버 재기동 후 `/admin/sw`, `/admin/license`, `/admin/system-graph` 등 주요 화면 1회 스모크 테스트 (codex 위임) |
| 템플릿 삭제로 과거 개발 레퍼런스 소실 | 낮음 | git history 에 내용 보존됨 — 필요 시 `git log --diff-filter=D` 로 복구 가능 |
| 향후 "중복체크 saveProjectFromDTO" 로직 재필요 | 낮음 | 본 문서 "4-2 의사결정" 에 재구현 위치 가이드 명시 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
