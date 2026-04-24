# [개발계획서] 감사 후속조치 스프린트 2b — P2 Dead code 3건

- **작성팀**: 개발팀
- **작성일**: 2026-04-19
- **근거 기획서**: [docs/product-specs/audit-fix-p2-deadcode.md](../product-specs/audit-fix-p2-deadcode.md) (v1, 사용자 승인)
- **상태**: 초안 (codex 검토 대기)

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 삭제 전 최종 스캔 (재현성 확보)
1. `rg -n "ProjectService" src/` → 본인 파일 외 0건 확인
2. `rg -n "swService\.(getAllProjects|saveProject|getProjectById|deleteProject|getAllUsers|getDistinctYears|getSystemStats|isDuplicate|getProjectDTOById|getProjectListAsDTO|saveProjectFromDTO|resolveStatCodes)" src/` → 0건 확인
3. `rg -n "customer-list|registry-new-list" src/` → 0건 확인
4. 하나라도 hit 이 있으면 **즉시 중단** 후 기획 팀과 재검토.

### Step 2 — `ProjectService` 삭제
- `src/main/java/com/swmanager/system/service/ProjectService.java` 파일 삭제 (`rm`).
- `mvn compile` → **컴파일 성공** 확인.

### Step 3 — Orphan 템플릿 2개 삭제
- `src/main/resources/templates/customer-list.html` 삭제
- `src/main/resources/templates/license/registry-new-list.html` 삭제
- 디렉터리 `templates/license/` 가 비지 않았는지 확인 (다른 파일 존재 확인).

### Step 4 — `SwService` 메서드 11 + private 1 삭제
4-1. 제거 대상 블록 (위에서 아래 순서):
   - `getAllProjects()` (43-45)
   - `saveProject(SwProject)` (47-49)
   - `getProjectById(Long)` (51-55)
   - `deleteProject(Long)` (57-59)
   - `getAllUsers()` (61-64)
   - `getDistinctYears()` (68-71)
   - `getSystemStats(Integer)` (73-76)
   - `isDuplicate(...)` (78-81)
   - `resolveStatCodes(String)` (119-125, private)
   - `getProjectDTOById(Long)` (190-195)
   - `getProjectListAsDTO(...)` (197-203)
   - `saveProjectFromDTO(...)` (205-214)
   - 관련 섹션 주석(`// ========== 대시보드 ==========`, `// ========== DTO 지원 ==========`) 및 `// ========== 기존 메서드 (유지) ==========` 도 의미 상실 → 정리.

4-2. **유지** 메서드 확정:
   - `getList(Pageable)` (85-88)
   - `search(String,Pageable)` (95-108)
   - `buildAndSpec(List<String>)` (127-169, private)
   - `getProject(Long)` (173-176)
   - `save(SwProject)` (178-181)
   - `delete(Long)` (183-186)

4-3. Import/필드 정리:
   - `userRepository` 필드 — 유지 메서드에서 참조 없음 → 제거
   - `UserRepository` import — 제거
   - `User` import — 제거 (getAllUsers 전용이었음)
   - `SwProjectDTO` import — 제거 (DTO 메서드 3개 제거됨)
   - `DuplicateResourceException`, `ErrorCode` import — 제거 (saveProjectFromDTO 전용)
   - `ResourceNotFoundException` import — 제거 (getProjectById/getProjectDTOById 전용)
   - `ContStatMst`, `ContStatMstRepository` — buildAndSpec 에서 여전히 사용 → **유지**
   - 나머지 import (`Page`, `Pageable`, `Specification`, `Predicate`, `List`, `ArrayList`, `Arrays`, `Map`, `Collectors`) → 사용처 확인 후 유지. `Map` 은 `getSystemStats` 제거 후 참조 사라지면 제거.

4-4. `mvn compile` → **컴파일 성공** 확인. 실패 시 stderr 를 보고 추가 import/필드만 보정 (로직 변경 금지).

### Step 5 — 감사 보고서 갱신
- `docs/generated/audit/2026-04-18-system-audit.md` 의 4-1, 4-2, 4-3 체크박스 `☑ 조치함` + 커밋 요약 한 줄씩.

### Step 6 — 서버 재기동 + 스모크
- `bash server-restart.sh`
- `server.log` 에서 `ERROR`/`Exception`/`Failed` 0건 + `Started SwmanagerApplication` 성공 라인 확인.
- 스모크 URL 5개(기획서 NFR-2) 에 대해 codex 위임 방식으로 HTTP 200/302/401(인증 리디렉션) 정상 확인.

### Step 7 — codex Specs 검증
- 기획서·개발계획서 각 FR/NFR/T# 항목별 충족/미충족 판정.

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | `ProjectService.java` 삭제 | `test -f src/main/java/com/swmanager/system/service/ProjectService.java` | exit 1 (파일 없음) |
| T2 | `SwService` 메서드 삭제 | `rg -n "public .+ (getAllProjects|saveProject\(SwProject|getProjectById|deleteProject|getAllUsers|getDistinctYears|getSystemStats|isDuplicate|getProjectDTOById|getProjectListAsDTO|saveProjectFromDTO)" src/main/java/com/swmanager/system/service/SwService.java` | 0 hits |
| T3 | `SwService.resolveStatCodes` 삭제 | `rg -n "resolveStatCodes" src/` | 0 hits |
| T4 | 유지 메서드 존재 | `rg -n "public .+ (getList|search|getProject|save|delete)\(" src/main/java/com/swmanager/system/service/SwService.java` | ≥ 5 hits |
| T5 | 템플릿 2개 삭제 | `test ! -f src/main/resources/templates/customer-list.html && test ! -f src/main/resources/templates/license/registry-new-list.html` (프로젝트 루트에서 실행) | 두 절 모두 exit 0 (= 파일 둘 다 없음) |
| T6 | Maven compile | `./mvnw -q -pl . compile` (또는 `./mvnw -q clean compile`) | BUILD SUCCESS |
| T7 | 서버 기동 성공 | `bash server-restart.sh` 후 `server.log` tail | `Started SwmanagerApplication` 포함, ERROR/Exception 0건 |
| T8 | 스모크 경로 | 기획서 NFR-2 의 5 URL 에 대한 HTTP 상태 확인 (codex 위임) | 200 / 302(리디렉션) / 401(인증 요구) 중 하나. 500 및 여타 5xx 금지. (Step 6 과 기준 일치) |
| T9 | 감사 체크박스 | `grep "☑ 조치함" docs/generated/audit/2026-04-18-system-audit.md` 의 4-1/4-2/4-3 라인 확인 | 3 lines hit |

---

## 3. 롤백 전략

**원칙**: 본 변경은 **비가역적 파일/메서드 삭제**. 롤백은 git revert 로만.

| 상황 | 조치 |
|------|------|
| Step 2~4 중 `mvn compile` 실패 | 해당 Step 의 Edit 를 역순으로 되돌림 (staged 상태면 `git restore`). 근본 원인(누락된 import/필드) 분석 후 재시도 |
| Step 6 재기동 실패 | `server.log` 로 원인 파악. 만약 삭제로 인한 것이면 `git revert HEAD` 로 이번 커밋 철회 후 재분석 |
| 배포 후 회귀 발견 | `git revert <sprint-2b-commit>` → 재배포. 파일/메서드 모두 완전 복원됨 |

---

## 4. 리스크·완화책 재확인 (기획서 외 추가)

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| `Map` import 제거 누락 → 경고(Warning) 수준 컴파일 | 낮음 | `mvn compile` 의 warning 까지 확인 |
| `git rm` 대신 Edit 로 처리할 경우 빈 파일 잔존 | 낮음 | 파일 삭제는 반드시 `rm` 또는 `git rm` 사용 |
| 향후 대시보드에서 `getSystemStats` 재사용 | 낮음 | 현재 `MainController` 는 `swProjectRepository.getSystemStats` 를 직접 호출하므로 영향 없음. 재필요 시 controller 에서 repo 직접 호출 패턴 유지 |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
