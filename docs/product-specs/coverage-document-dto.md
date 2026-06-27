# [기획서] DocumentDTO 커버리지 보강 (B)

- **작성팀**: 기획팀 / **작성일**: 2026-06-27
- **트랙**: beyond-A 커버리지 증분 B. 순수 DTO 변환 로직. 기존 `DocumentDTOTest` 확장.
- **상태**: ✅ 구현 완료(2026-06-27). DocumentDTOTest +3(6→9), DocumentDTO **68→92.8% LINE**(116/125), 전역 LINE 76.7→77.07%·INSTR 62.7→63.06%, floor 유지. mvnw -o clean verify 1394 green. 구현 후 codex 검증 PASS(+exact 필드 단언 보강).

---

## 1. 배경 / 목표

`DocumentDTO`(218줄, **LINE 68% / miss 40**)는 fromEntity 매핑 + DetailSectionDTO·buildTargetDisplay·buildOrgUnitPath·getEnvironmentLabel 등 순수 로직. 기존 테스트(6)는 environmentLabel/orgUnitPath/targetDisplay(regionCode·project·empty)/label 만 덮어, **fromEntity 의 infra/project full/workPlan/author/approver/approvedAt/details→sections·DetailSectionDTO·hasSignedScan·targetDisplay infra 분기**가 미커버였다.

## 2. 변경 — DocumentDTOTest 케이스 3 추가
- fromEntity_fullMapping: infra(우선)+workPlan+project+author+approver+approvedAt+createdAt/updatedAt+signedScanPath(hasSignedScan)+details→sections(DetailSectionDTO 필드). 스칼라/매핑 필드 exact 단언.
- fromEntity_projectFallback_whenNoInfra: infra=null → project cityNm/distNm/sysNm fallback.
- fromEntity_targetDisplay_infraBranch: regionCode/project null → infra 분기.

## 3. 요건
- FR: 위 분기 박제(응답 계약 필드값 단언). NFR: production 0, verify SUCCESS, 68→92.8%(잔여=@Data boilerplate), floor 유지, 구현 후 codex PASS → 듀얼푸시.

## 4. 영향/리스크
- `dto/DocumentDTOTest.java` 케이스 추가. production 0. 잔여 9줄=@Data getter/setter boilerplate(저가치).
