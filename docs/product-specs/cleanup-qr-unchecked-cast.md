# [기획서] InspectQrMetricSupport unchecked cast 정리 (§8-5)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: §8 QR 버그픽스 dual-review 잔여(§8-5). 작은 cleanup.
- **상태**: ✅ **구현 완료(2026-06-27)**. formatValueWithContext unchecked cast 2곳 → `instanceof Map<?,?>` 패턴, @SuppressWarnings 2개 제거, `String.valueOf(de.getKey())`. ⚠**부수: `m.getOrDefault("status","-")`가 Map<?,?>에서 capture 충돌** → `m.get("status")` null삼항으로 전환(동작 동일). 1329 green(동작 보증). codex 11번째 오판(교정코드는 기획과 일치). 미세작업이라 §2가 개발계획 겸함. dual-review→듀얼푸시.

---

## 1. 배경 / 목표

`InspectQrMetricSupport.formatValueWithContext` 의 `(Map<String,Object>)value` unchecked cast(키타입 미검증, dual-review §8-5 low/low). Jackson 파싱 맵은 통상 String 키라 실위험 낮으나 컴파일 경고+가정 잔존. **`instanceof Map<?,?>` 패턴 전환으로 unchecked 제거**(동작 동일). ROI 낮음(경고 제거·방어), 사용자 명시 진행.

| 위치 | 현재 | 교정 |
|---|---|---|
| L193 메서드 | `@SuppressWarnings("unchecked")` | 제거(불요) |
| L198-199 | `value instanceof Map` + `(Map<String,Object>)value` | `value instanceof Map<?,?> m` 패턴 |
| L205-207 | `de.getValue() instanceof Map` + `(Map<String,Object>)de.getValue()` | `de.getValue() instanceof Map<?,?> dd` 패턴 |
| L208 | `de.getKey().toUpperCase()` | `String.valueOf(de.getKey()).toUpperCase()` |

---

## 2. 변경 설계

- `Map<?,?> m`: `m.get(String)` → `Object`(기존과 동일, switch yield 가 Object 취급). `m.entrySet()` → `Entry<?,?>`, `e.getKey()`/`e.getValue()` → `Object`. fallback 루프 `skip.contains(e.getKey())`(Set<String>.contains(Object) OK)·`UNIT_LABELS.getOrDefault(key,...)`(key=String 파라미터) 불변.
- `Map<?,?> dd`: `dd.get("p")` → `Object`(동일).
- `de.getKey()` 가 Object 가 되므로 `.toUpperCase()` 직접 호출 불가 → `String.valueOf(...)` 경유(키가 실제 String 이므로 출력 동일).
- @SuppressWarnings 2곳 제거(Map<?,?>는 unchecked 아님).

> 동작 100% 동일: 키가 String 인 실데이터에서 String.valueOf(key)=key, m.get/dd.get 반환 동일. 컴파일 경고만 소거.

---

## 3. 기능/비기능 요건

| ID | 내용 |
|----|------|
| FR-1 | formatValueWithContext 출력 이동 전후 동일. |
| NFR-1 | `./mvnw -o clean verify` BUILD SUCCESS. unchecked 경고 0(해당 2곳). |
| NFR-2 | 기존 FormatTest(40 케이스)·ServiceTest 통과 — 동작 보증. 신규 테스트 불요. |
| NFR-3 | 커버리지·ratchet·floor 불변. |
| NFR-4 | dual-review → 듀얼푸시. |

---

## 4. 영향 / 리스크

| 파일 | 유형 |
|------|------|
| `service/inspection/InspectQrMetricSupport.java` | unchecked cast 2곳 패턴 전환 |

| 리스크 | 수준 | 완화 |
|---|---|---|
| 키 비-String 시 String.valueOf 출력 변화 | 낮음 | 실데이터 String 키, String.valueOf(String)=원본. FormatTest 보증 |
| m 광범위 사용처 타입 변경 | 낮음 | get/entrySet 반환 Object 동일, 컴파일 검출 |

---

## 5. 승인 요청

본 기획서(§8-5 unchecked cast 정리)에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
