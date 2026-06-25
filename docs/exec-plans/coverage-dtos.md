# [개발계획서] DTO 커버리지 상향 — InfraDTO·SwProjectDTO·DocumentDTO (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-25
- **기획서**: `docs/product-specs/coverage-dtos.md` (codex APPROVE, 사용자 최종승인 완료)
- **상태**: ✅ 완료 (2026-06-25). codex 기획/개발계획 APPROVE + 구현검증. 신규 14테스트(Infra 5/SwProject 3/Document 6). InfraDTO 0%→91.1%, SwProjectDTO 34%→71.3%, DocumentDTO 37.6%→65.6%. 전역 LINE 49.34%→51.56%(50% 돌파). floor 0.47→0.49/0.39→0.41.

---

## 1. 작업 개요

3개 DTO 변환 로직에 순수 단위테스트 추가. **프로덕션 코드 변경 0**, 신규 테스트 3파일.

---

## 2. 신규 테스트 (T-n)

### `InfraDTOTest` (com.swmanager.system.dto)
| ID | 테스트 | 검증 |
|----|--------|------|
| T-I1 | `fromEntity_null_returnsNull` | null entity → null |
| T-I2 | `fromEntity_mask_masksSecrets` | mask=true → server.accPw/software.swAccPw/upis.gpkiPw/upis.minwonKey = "********", api 키 = maskKey 결과 |
| T-I3 | `fromEntity_noMask_keepsRaw` | mask=false → 원본 비번/키 유지 |
| T-I4 | `fromEntity_nullCollections_keepNull` | servers/upisLinks/apiLinks null → DTO 해당 필드 null |
| T-I5 | `maskKey_boundaries` (convertApi 경유) | null/길이≤4 → "****"; 길이>4 → 앞4글자+"****"+"****". API expDt null/존재(toString) 포함 |

### `SwProjectDTOTest`
| ID | 테스트 | 검증 |
|----|--------|------|
| T-S1 | `fromEntity_null_returnsNull` | null → null |
| T-S2 | `fromEntity_mapsRepresentativeFields` | projId/year/projNm/contAmt/maintType/stat 등 매핑 |
| T-S3 | `toEntity_mapsBack_roundTripExcludingUnmapped` | toEntity 대표 필드 + fromEntity→toEntity 라운드트립(⚠ statNm/maintTypeNm/personNm/regDt 제외 — 미매핑) |

### `DocumentDTOTest`
| ID | 테스트 | 검증 |
|----|--------|------|
| T-D1 | `fromEntity_environmentLabel` | environment PROD→"운영", TEST→"테스트", 기타→원본, null→null(environmentLabel) |
| T-D2 | `fromEntity_orgUnitPath` | orgUnit 루트→리프 체인 → "a > b > c"(buildOrgUnitPath, getParent 체인) |
| T-D3 | `fromEntity_targetDisplay_regionCode/project/infra` | regionCode 있으면 "[code] sysType"; 없고 project 있으면 "city dist - sysNm"(dist==city 중복제거); infra 분기; 모두 없으면 "" |
| T-D4 | `staticLabels_nullSafe` | getDocTypeLabel/getStatusLabel/getStatusColor: null → "-"/"-"/"#858796", 정상값 → label/color |

- 전부 순수: 엔티티(Infra/InfraServer/InfraSoftware/InfraLinkUpis/InfraLinkApi/SwProject/Document/OrgUnit) Lombok 세터로 구성, mock 불요.
- private 헬퍼(convert*/maskKey/getEnvironmentLabel/buildOrgUnitPath/buildTargetDisplay)는 public fromEntity 경유 간접 검증.

---

## 3. 구현 순서 (T)

| ID | 단계 |
|----|------|
| S-1 | `InfraDTOTest`·`SwProjectDTOTest`·`DocumentDTOTest` 작성. |
| S-2 | `./mvnw test -Dtest=InfraDTOTest,SwProjectDTOTest,DocumentDTOTest` → 전건 green. |
| S-3 | `./mvnw verify`(전체 + JaCoCo 게이트) green. |
| S-4 | JaCoCo csv 로 3 DTO·전역 커버리지 상승 확인 → floor 상향 판단(실측−2~2.5pp, 게인 작으면 유지). |

---

## 4. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-2 신규 테스트 green |
| NFR-2 | S-3 전체 test green |
| NFR-3 | S-3 게이트 + S-4 상승 |

---

## 5. 롤백

신규 테스트 파일만 추가 → 회귀 위험 극소. 문제 시 파일 제거. 프로덕션 영향 0.

---

## 6. 커밋 (작업완료 후)

- 메시지: `test(coverage): InfraDTO·SwProjectDTO·DocumentDTO 변환 로직 단위테스트 추가 (beyond-A)` (+ floor 상향 시 동봉)
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획서·개발계획서 ✅ 완료 갱신 동봉.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
