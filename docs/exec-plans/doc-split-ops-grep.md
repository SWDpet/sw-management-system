---
tags: [grep-snapshot, sprint]
sprint: "doc-split-ops"
created: "2026-04-29"
---

# doc-split-ops — Step 1 사전 grep 스냅샷

스프린트 시작 직전 (2026-04-29) 기준 영향 범위 매핑. Step 6/7/8 정리 시 누락 0 검증 baseline.

---

## 1. `DocumentType.{INSPECT,FAULT,SUPPORT,INSTALL,PATCH}` 참조 (Step 8 제거 대상)

**총 13 hit / 4 파일**:

| 파일 | 라인 | 케이스 |
|------|------|--------|
| `src/main/java/com/swmanager/system/dto/DocumentDTO.java` | 181 | SUPPORT 분기 (INTERNAL → orgUnit 라벨) |
| `src/main/java/com/swmanager/system/controller/DocumentController.java` | 314, 320, 335, 344-345, 391-392, 410, 422 | 4 종 (FAULT/INSTALL/PATCH/SUPPORT) 분기 (총 9 hit) |
| `src/main/java/com/swmanager/system/service/InspectReportService.java` | 123 | INSPECT — **Step 6 에서 `OpsDocType.INSPECT` 로 이동** |
| `src/main/java/com/swmanager/system/service/PerformanceService.java` | 53-56 | INSTALL/PATCH/FAULT/SUPPORT 카운트 (4 hit) |

**Step 8 제거 후 기대 결과**: `Grep "DocumentType\.(INSPECT|FAULT|SUPPORT|INSTALL|PATCH)" src/main/java` → 0 hit.

---

## 2. `DocumentType.{COMMENCE,INTERIM,COMPLETION}` 참조 (사업측 — 보존 대상)

**총 20 hit / 4 파일** (Step 8 후에도 동일 유지 — 회귀 baseline):

| 파일 | hit 수 |
|------|--------|
| `src/main/java/com/swmanager/system/controller/DocumentController.java` | 16 |
| `src/main/java/com/swmanager/system/service/DocumentService.java` | 1 |
| `src/main/java/com/swmanager/system/service/HwpxExportService.java` | 1 |
| `src/main/java/com/swmanager/system/service/PerformanceService.java` | 2 |

**Step 8 후 기대 결과**: 동일 20 hit / 4 파일 유지. 다른 결과면 사업측 회귀 의심 — 즉시 검토.

---

## 3. `tb_inspect_(checklist|issue)` 참조 (Step 2 DROP 안전성)

**실행 코드 0 hit / 코멘트 3 hit**:

| 파일 | 라인 | 종류 |
|------|------|------|
| `src/main/java/com/swmanager/system/service/DocumentService.java` | 285 | 코멘트 (deprecated 안내) |
| `src/main/java/com/swmanager/system/domain/workplan/Document.java` | 105-106 | 코멘트 (S1 통합 안내) |

**결론**: 실행 코드 잔존 0 → `DROP TABLE IF EXISTS` 안전. 코멘트는 Step 8 에서 정리.

---

## 4. `linkToDocument` (Step 6 커스터마이징 대상)

**총 2 hit / 1 파일**:

| 파일 | 라인 | 종류 |
|------|------|------|
| `src/main/java/com/swmanager/system/service/InspectReportService.java` | 96 | 호출 (`save()` 안에서) |
| `src/main/java/com/swmanager/system/service/InspectReportService.java` | 103 | private 메서드 정의 |

**Step 6 후 기대 결과**: 0 hit (메서드 삭제 + 호출은 `opsDocLinkService.linkInspectReport(saved)` 로 교체).

---

## 5. 제거 대상 템플릿 (Step 8 삭제 대상 8 개)

| 템플릿 폼 | PDF 짝 |
|-----------|--------|
| `templates/document/doc-fault.html` | `templates/pdf/pdf-fault.html` |
| `templates/document/doc-support.html` | `templates/pdf/pdf-support.html` |
| `templates/document/doc-install.html` | `templates/pdf/pdf-install.html` |
| `templates/document/doc-patch.html` | `templates/pdf/pdf-patch.html` |

**Step 8 후 기대 결과**: 8 파일 모두 삭제. `Glob` 으로 0 hit.

---

## 6. 5 종 컨트롤러 URL 패턴 (Step 8 정리 대상)

`/document/api/(fault|support|install|patch)` 또는 `/document/(fault|support|install|patch)` 라우트 직접 매칭 → **0 hit**.

**해석**: DocumentController 가 `/document/api/save` 등 단일 라우트에 `doc_type` 파라미터로 분기하는 구조. URL 별도 제거 불필요. doc_type 분기 로직만 Step 8 에서 정리.

---

## 7. Step 8 정리 작업 체크리스트 (위 1·5·6 통합)

- [ ] `DocumentType.java` enum 5 종 (INSPECT/FAULT/SUPPORT/INSTALL/PATCH) 제거
- [ ] `DocumentDTO.java:181` SUPPORT 분기 제거
- [ ] `DocumentController.java` 4 종 doc_type 분기 9 hit 정리 (314/320/335/344-345/391-392/410/422)
- [ ] `PerformanceService.java:53-56` 4 종 카운트 제거 (또는 `OpsDocService` 위임)
- [ ] `InspectDocNoMigrationRunner.java` deprecate 또는 ops 측 이전
- [ ] `templates/document/doc-{fault,support,install,patch}.html` 4 개 삭제
- [ ] `templates/pdf/pdf-{fault,support,install,patch}.html` 4 개 삭제
- [ ] `DocumentService.java:285` deprecated 코멘트 갱신 또는 제거
- [ ] `Document.java:105-106` deprecated 코멘트 갱신 또는 제거

---

## 8. 검증용 baseline 명령

스프린트 진행 중 어느 시점에서든 다음 명령으로 진행도 측정:

```bash
# 5 종 enum 제거 진행도
Grep "DocumentType\.(INSPECT|FAULT|SUPPORT|INSTALL|PATCH)" src/main/java | wc -l
# Step 1: 13 → Step 8 후: 0 기대

# 사업측 보존 검증
Grep "DocumentType\.(COMMENCE|INTERIM|COMPLETION)" src/main/java | wc -l
# Step 1: 20 → Step 8 후: 20 (변동 없음 기대)

# linkToDocument 정리 검증
Grep "linkToDocument" src/main/java | wc -l
# Step 1: 2 → Step 6 후: 0 기대

# 제거 대상 템플릿
ls src/main/resources/templates/document/doc-{fault,support,install,patch}.html 2>/dev/null | wc -l
# Step 1: 4 → Step 8 후: 0 기대
```
