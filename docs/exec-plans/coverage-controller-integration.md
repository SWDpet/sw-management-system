# [개발계획서] 컨트롤러 통합영역 커버리지 상향 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-26
- **기획서**: `docs/product-specs/coverage-controller-integration.md`
- **상태**: ⚠ **정식 절차 소급 적용**. 구현 완료(15컨트롤러, 커밋 `6c6a307`~`8ef732f`). 본 문서로 codex 검토 + 사용자 최종승인 게이트 소급 보강. → ✅ **완료**: codex APPROVE + **사용자 최종승인(2026-06-26)**.

---

## 1. 작업 개요

비-license 컨트롤러 15종에 직접호출 mock 단위테스트를 추가하고 컨트롤러별/전역 커버리지 상승에 맞춰 JaCoCo floor 를 점진 ratchet. 신규 테스트 15클래스 + 픽스처 1 + pom floor.

---

## 2. 컨트롤러별 작업 (T-n)

각 컨트롤러는 1커밋 = 1테스트클래스 + (해당 시) pom floor. 매 커밋 dual-review→듀얼푸시.

| ID | 컨트롤러 | 테스트수 | 커밋 | 컨트롤러 INSTR | 핵심 커버 |
|----|---|---|---|---|---|
| T-1 | DocumentController | 64 | 6c6a307 | 1.5→38.7% | 가드 전엔드포인트·정적헬퍼·createForm 분기 |
| T-2 | QuotationController | 53 | 9ab5788 | 1.2→85.1% | checkView/EditAuth throw·미리보기 금액분기 |
| T-3 | OpsDocController | 46 | 931c77a | 10.4→73.3% | list 지역해석·CRUD·지원파일·검색 |
| T-4 | InspectReportController | 29 | 878420a | 2.5→86.1% | save/조회/삭제 가드·extractSnapshotSpecs |
| T-5 | WorkPlanController | 25 | 3b1fbb7 | 1.0→91.7% | 캘린더/칸반·저장 검증실패 redirect |
| T-6 | OpsKbController | 37 | fa68b47 | 1.3→89.4% | KB 상태전이·승인반려·소유 스코프 |
| T-7 | PerformanceController | 13 | 107d609 | 0.9→95.7% | 개인/부서 대시보드·집계·엑셀 |
| T-8 | MainController | 6 | 649cff0 | 1.8→95.1% | 대시보드 집계 루프(projection mock) |
| T-9 | InfraController | 14 | 2423126 | 1.9→90.6% | 인프라 CRUD·type 디폴트 |
| T-10 | PersonController | 15 | d290843 | 2.4→95.3% | 담당자 CRUD·notFound |
| T-11 | PartnerController | 19 | 764c681 | 2.4→99.2% | 업체/담당자 CRUD·소프트삭제 |
| T-12 | DocumentParticipantController | 8 | 396aead | 3.5→87.2% | 과업참여자 EDIT가드·lenient 파싱 |
| T-13 | OrgUnitController | 19 | 0bab819 | 2.4→100% | 조직/직원 CRUD |
| T-14 | MyPageController | 11 | 5f16954 | 1.6→82.4% | 마이페이지·마스킹가드·비번변경 |
| T-15 | InspectAgentController | 5 | 8ef732f | 3.8→62.3% | 수집모듈 페이지/다운로드·meta 파싱 |

---

## 3. 구현 순서 (컨트롤러당 반복)

| ID | 단계 |
|----|------|
| S-1 | 대상 컨트롤러 분석(인증방식·의존성·엔드포인트·폼/DTO 타입). |
| S-2 | 직접호출 테스트 작성(가드+해피패스+예외 분기, ArgumentCaptor 매핑). |
| S-3 | `./mvnw -o test -Dtest=<Test>` 전건 green. |
| S-4 | `./mvnw -o clean verify`(전체 + JaCoCo 게이트) green + csv 로 컨트롤러/전역 상승 측정. |
| S-5 | floor 상향 판단(실측−~3pp, 게인 작으면 유지) → pom 갱신 후 재 verify. |
| S-6 | dual-review(codex+Opus4.8) → 합의 반영·분쟁 refute. |
| S-7 | 커밋 + 듀얼푸시 + 메모리 갱신. |

---

## 4. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-3 compile |
| NFR-2 | S-3 테스트클래스 green |
| NFR-3 | S-4 clean verify green |
| NFR-4 | S-4/S-5 상승 + 게이트 |
| NFR-5 | S-6 dual-review + S-7 듀얼푸시 |

**누적 효과**: 전역 LINE 52.20→69.00%(+16.8pp), INSTR 43.66→56.32%(+12.66pp). floor LINE 0.51→0.66, INSTR 0.42→0.53.

---

## 5. 롤백

각 컨트롤러 1커밋. 문제 시 해당 커밋 `git revert`. 테스트-only라 프로덕션 영향 없음.

---

## 6. 커밋

- 컨트롤러별 `test(coverage): <Controller> 단위테스트 N + JaCoCo floor 상향 (beyond-A)`.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`.
- 본 정식문서(기획/개발계획)는 별도 커밋으로 소급 추가.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
