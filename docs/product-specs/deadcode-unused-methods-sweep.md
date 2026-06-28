# [기획서] 미사용 메서드 2종 제거 (sysList·InspectMaintProfile.includes) (무손실)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: production 무손실 정리(beyond-A 헌장). [[deadcode-unused-repo-queries]] 후속 — private/public 메서드 dead 스윕.
- **상태**: ✅ 구현 완료(2026-06-28). sysList(OpsKbController)·includes(InspectMaintProfile) 2종 삭제. **`mvnw -o clean verify` BUILD SUCCESS**(컴파일+테스트=잔여 호출처 0 증명), 전역 LINE 81.176→81.203%(미커버 includes 제거로 분모 축소)·INSTR 66.498%, floor 0.78/0.64 유지. codex 기획 APPROVE(독립 grep 재검증: 0 호출·live 메서드[sysOptions/sections/badgeLabel] 보존·recordHistory 내부호출 제외). dual-review(codex1/Opus3) 합의0·분쟁4 전건 오탐(diff-blindness "삭제가 호출처 깸"·"import 미사용"→build green+sysOptions가 import 사용 반증). private/public 메서드 dead 스윕 완료(코드베이스 클린).

---

## 1. 배경 / 목표

repo 쿼리 30개 제거 후, **미사용 private 메서드 + public service 메서드 전수 스캔**(src/**/*.java, `(?:\.|::|bare)name(` 호출 카운트) 결과 호출처 0인 메서드는 **단 2종**(license·false-positive 제외)으로 코드베이스는 이미 깨끗하다. 그 2종만 무손실 삭제한다.

| 메서드 | 위치 | 판정 근거 |
|---|---|---|
| `sysList()` (private) | `OpsKbController` L72-76 | 호출처 0. 형제 `sysOptions()`(SysMstOption 변환)가 모델 `"systems"` 채움(L88/103/116)에 사용 — **raw 코드 List 변형 sysList 는 대체돼 미사용**. |
| `includes(String,boolean,String)` (public static) | `InspectMaintProfile` L64-67 | Java 호출처 0(production·test 모두). InspectMaintProfile 은 sections/badgeLabel/badgeTone 등으로 쓰이나 `includes` 만 미호출(테스트도 안 함). |

> 스캔 false-positive 제외: `DocumentService.recordHistory`(내부 bare 호출 L165/201 — 사용 중, 유지), license 계열 4종(영구 패스).

## 2. 범위 (production 무손실 삭제)
- **D1** `OpsKbController.sysList()` private 메서드 삭제.
- **D2** `InspectMaintProfile.includes(...)` public static 메서드 + javadoc 삭제.
- 삭제로 미사용이 되는 import 정리(없으면 무변경 — 두 메서드 모두 클래스 내 다수 공유 타입 사용이라 잔존 예상).

## 3. 요건
- **FR-1**: dead 2종 삭제. 형제/유사 live 메서드(sysOptions·sections·badgeLabel 등) 무변경.
- **NFR**: `mvnw -o clean verify` SUCCESS(컴파일=잔여 호출처 0 증명, 전체 green), 커버리지 유지/소폭(분모 축소), floor 유지, 구현 후 codex PASS + dual-review → 듀얼푸시.

## 4. 영향 / 리스크
- 변경: `controller/ops/OpsKbController.java`·`service/inspection/InspectMaintProfile.java`.
- **R1 false-negative**: bare 내부호출(`name(` without `.`)은 컴파일이 최종 안전망 — 잔존 호출 시 BUILD FAIL. recordHistory 처럼 내부호출 있는 건 이미 제외 검증.
- **R2 includes 커버리지**: InspectMaintProfile.includes 는 테스트도 안 하므로 삭제로 잃는 커버리지 0(미커버 라인이었음).
- production 회귀 0(미사용 제거).
