# [기획서] PIT 뮤테이션 게이트 CI 통합 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A (CI 트랙 연장). 이미 만든 PIT 게이트를 CI 에서 매 push 자동 강제.
- **상태**: ✅ 구현 완료(2026-06-29). ci.yml `mutation` job 추가(gates·fresh-init 와 독립, `-Ppit ...mutationCoverage`, timeout 20m) + pom PIT `<jvmArgs>` ko 로케일 + RELIABILITY §8. 로컬 PIT 180/188=96% BUILD SUCCESS. **로케일 jvmArgs 실증**: 바깥 JVM `-Duser.language=en` 강제에도 PIT 통과(fork 가 jvmArgs 로 ko → MessageResolver 정상) = CI(en) 안전. codex APPROVE-WITH-FIX(PIT 로케일 jvmArgs 채택). dual-review 합의2(ko 전역·blocking=의도)·분쟁5 refute(jvmArgs 요소명 실증·threshold 기존재). ⚠첫 CI mutation job 검증은 push 후.

---

## 1. 배경 / 목표
PIT 뮤테이션 게이트(`-Ppit` 프로파일, 11클래스, threshold 93, 실측 180/188=95.7%)는 **로컬/수동 실행에만** 존재. CI 트랙(`ci-fresh-init-gate-v1`)이 JaCoCo·거대클래스·ArchUnit·Map부채·골든·Enum 은 `mvnw verify`로 자동 강제하지만 **PIT 는 `verify`에 미바운드(라이프사이클 분리)라 CI 에서 안 돈다.** → 뮤테이션 게이트만 "로컬/수동" 구멍.

**목표**: ci.yml 에 PIT 전용 job 추가 → 매 push/PR 마다 뮤테이션 게이트 자동 강제(다른 게이트와 동일 수준).

## 2. 범위
### D1 — `.github/workflows/ci.yml` 에 `mutation` job 추가
- ubuntu-latest, setup-java 17(cache maven), `chmod +x mvnw`.
- `./mvnw -B -ntp -Ppit test-compile org.pitest:pitest-maven:mutationCoverage` 실행.
- PIT 게이트(KILLED/TOTAL ≥ 93) 미달 시 job 실패 → 워크플로 red(뮤테이션 회귀 검출).
- 기존 `gates`·`fresh-init-smoke` 와 병렬(독립 job). timeout 20m.
- PIT 의 targetTests 는 순수 단위테스트(util/service)라 DB/Spring 컨텍스트 불요 → DB_PASSWORD 등 불필요.

### D2 — 문서
- `docs/RELIABILITY.md` §8(CI) 에 mutation job 항목 추가.

## 3. 요건
- **FR-1**: push/PR(master) 시 mutation job 이 PIT 게이트 강제(미달=red).
- **NFR**: 기존 gates·fresh-init job 무영향(독립 job), 로컬 `mvnw -Ppit ...` 무영향, CI green 확인(push 후), codex + dual-review. 헤드룸 2.7pt(95.7% vs 93)라 정상 변경엔 안정.

## 4. 영향 / 리스크
- 변경: ci.yml 에 job 1개 추가 + 문서. production/테스트 코드 0.
- **R1 PIT 실행시간**: 11클래스 ~1~2분 + 의존성 다운로드 → job ~3~5분(gates 와 병렬이라 wall-clock 무증가). timeout 20m 여유.
- **R2 헤드룸**: 95.7% vs 93 = 2.7pt. 게이트 클래스(util/service 핫스팟)는 자주 안 변해 PR 노이즈 낮음. 미달 시 = 실제 뮤테이션 회귀(의도된 차단).
- **R3 첫 CI 검증**: push 후 Actions 에서 mutation job green 확인(로컬 PIT 는 이미 green=180/188).
