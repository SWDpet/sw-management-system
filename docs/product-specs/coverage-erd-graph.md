# [기획서] ErdGraphService 커버리지 보강 (B)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A 커버리지 증분 B. 순수 파싱 로직(repo 의존 0).
- **상태**: ✅ **구현 완료(2026-06-27)**. ErdGraphServiceTest 5개(2-A 실데이터 smoke·2-B synthetic tiebreaker+FK 2단계·2-C 빈DTO/없는dir). **ErdGraphService 85.3→93.8%**(miss 31→13). 전역 LINE 73.29%(소폭). floor 상향 생략(버퍼 충분). 1334 green. codex NEEDS-FIX(실질검토) 반영. dual-review→듀얼푸시.

---

## 1. 배경 / 목표

`ErdGraphService`(372줄, LINE 85.3%, miss 31, 기존 테스트 0)는 .mmd(Mermaid ERD) 파서 + 도메인 병합 + FK 휴리스틱. **repo 의존 0의 순수 로직**이라 mock 불요. 미커버 = `load`/`parseFile` 정규식 분기·`pickCanonical` tiebreaker·FK 2단계 매핑. **신규 단위테스트로 86→95%+** 목표. 동작 변경 0(테스트-only).

---

## 2. 변경 설계 — ErdGraphServiceTest 신설 (codex 반영: smoke + synthetic 분리)

⚠**codex 지적**: 실데이터 구조 단언만으로는 ① `pickCanonical` **동률 tiebreaker**(실 mmd 중복 테이블은 컬럼 수가 달라 안 탐) ② **FK 2단계 휴리스틱**(`fk()&&fkRef!=null`은 1단계만으로 만족)을 보장 못함. → **실데이터 smoke + @TempDir synthetic fixture 분리**.

### 2-A 실데이터 smoke (회귀 안전망)
- `new ErdGraphService()` + `ReflectionTestUtils.setField(mmdDir="docs", descriptionsFile="docs/erd-descriptions.yml")` → `init()` → `getGraph()` 구조 단언: nodes>0·각 Node id/label/domain non-null·PK Column 존재·FK+fkRef 존재·edges from/to 가 실제 Node id·cardinality non-null·domain∈DOMAINS.
- ⚠"repo 의존 0"이 아니라 "DB 의존 0 + docs fixture 의존"(codex 정정).

### 2-B @TempDir synthetic fixture (핵심 분기 정밀 검증)
- `@TempDir Path dir` 에 erd-*.mmd 5개(core/infra/contract/document/quotation) 최소 생성 → `mmdDir=dir.toString()` → init().
- **pickCanonical 동률 tiebreaker**: `core` 와 `infra` 에 **동일 컬럼 수** 중복 테이블(예 `dup { bigint id PK; text name }`) → 정본 domain 이 우선순위 높은 `core` 로 선택됨 검증(Node.domain=="core").
- **FK 2단계 유일 쌍 매핑**: `parent { bigint id PK }` + `child { bigint child_id PK; bigint parent_ref FK }` + 관계 `parent ||--o{ child : "has"`. parent_ref 이름이 parent PK(id)와 **불일치** → 1단계 실패 → 잔여 관계 1·잔여 FK 1 → **2단계 fkRef=parent** 직접 검증(child 의 parent_ref Column.fkRef=="parent").

### 2-C 엣지 케이스
- **getGraph() init 전 빈 DTO**: `new ErdGraphService().getGraph()` → nodes/edges empty(캐시 null 폴백).
- **존재하지 않는 mmdDir**: mmdDir="nonexistent_dir" → init() → 빈 DTO(readFile null 분기 + load warn-only).

---

## 3. 기능/비기능 요건

| ID | 내용 |
|----|------|
| FR-1 | init() 가 실제 mmd 파싱 후 getGraph() 가 nodes/edges 채운 DTO 반환. |
| NFR-1 | `./mvnw -o clean verify` BUILD SUCCESS. |
| NFR-2 | ErdGraphServiceTest 신설, ErdGraphService 86→95%+ LINE. |
| NFR-3 | floor 실측 후 상향 검토(전역 영향 작으면 생략). ratchet/PIT 불변. |
| NFR-4 | dual-review → 듀얼푸시. |

---

## 4. 영향 / 리스크

| 파일 | 유형 |
|------|------|
| `service/ErdGraphServiceTest.java` | 신규 통합 단위테스트 |

production 변경 0.

| 리스크 | 수준 | 완화 |
|---|---|---|
| docs/erd-*.mmd 변경 시 테스트 brittle | 중 | 구조적 단언(개수>0, PK/FK 존재)으로 특정 값 하드코딩 회피 |
| init() 가 @Value 미주입 시 NPE | 낮음 | ReflectionTestUtils 로 mmdDir/descriptionsFile 주입 |
| 작업 디렉토리 의존(FileSystemResource "docs/...") | 낮음 | Maven test cwd=프로젝트 루트, docs/ 상대경로 유효 |

---

## 5. 승인 요청

본 기획서(ErdGraphService 커버리지)에 대한 codex 검토 및 사용자 최종승인을 요청합니다. 특히 **2(구조적 단언으로 brittle 회피 충분한가)**, **엣지 케이스 범위(private parseFile 직접 호출 여부)** 의견 요청.
