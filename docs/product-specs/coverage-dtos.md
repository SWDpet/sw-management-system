# [기획서] DTO 커버리지 상향 — InfraDTO·SwProjectDTO·DocumentDTO (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-25
- **트랙**: beyond-A 커버리지 스프린트. 코어 서비스 5종 소진 후 DTO 변환 로직 타깃. 선행 `coverage-quotation-service`(`a1c533e`).
- **상태**: ✅ 완료 (2026-06-25). codex APPROVE + 구현검증. 신규 14테스트. InfraDTO 0%→91.1%, SwProjectDTO 34%→71.3%, DocumentDTO 37.6%→65.6%. 전역 LINE 49.34%→51.56%(50% 돌파).

---

## 1. 배경 / 목표

코어 서비스 커버리지 소진 후, 남은 큰 미커버 중 **DTO 변환 로직**이 순수(운영DB·mock 무관)·고가치 타깃이다.

| DTO | 현재 LINE | 미커버 | 로직 |
|---|---|---|---|
| InfraDTO | **0%** | 112 | fromEntity(mask 여부) + convertServer/Upis/Api(중첩·마스킹) + maskKey |
| SwProjectDTO | 34% | 99 | fromEntity(~45필드) + toEntity(~43필드) |
| DocumentDTO | 37.6% | 78 | fromEntity + getEnvironmentLabel/buildOrgUnitPath/buildTargetDisplay + 라벨 static 3 |

**목표**: 세 DTO 변환 로직에 순수 단위테스트 추가 → 커버리지 대폭 상향. 프로덕션 코드 변경 0(테스트만).

---

## 2. 테스트 대상 (DTO별)

### 2-1. InfraDTO
- `fromEntity(null)` → null
- `fromEntity(entity, mask=true)` → accPw/swAccPw/gpkiPw/minwonKey = "********", API 키는 maskKey(앞4+****)
- `fromEntity(entity, mask=false)` → 원본 값 그대로
- servers/upisLinks/apiLinks null 분기 (null 유지) vs 존재(변환)
- softwares null vs 존재(swAccPw mask)
- API expDt null vs 존재(toString)
- `maskKey`(private, convertApi 경유): null/길이≤4 → "****", 길이>4 → 앞4+"****"+"****"

### 2-2. SwProjectDTO
- `fromEntity(null)` → null
- `fromEntity(populated)` → 대표 필드(projId/year/projNm/contAmt/maintType 등) 매핑
- `toEntity()` → 대표 필드 매핑 + fromEntity→toEntity 라운드트립 일관
- ⚠ **단언 제외(codex 지적)**: `statNm`/`maintTypeNm`/`personNm` 은 fromEntity/toEntity 미매핑(조인 전용 필드), `toEntity()` 는 `regDt` 미세팅 → 라운드트립 단언에서 이들 제외(매핑되는 대표 필드만 검증).

### 2-3. DocumentDTO
- `getEnvironmentLabel`(fromEntity environment 경유): PROD→"운영", TEST→"테스트", 기타→원본, null→null(environmentLabel)
- `buildOrgUnitPath`(orgUnit 경유): 루트→리프 " > " 연결, null→null
- `buildTargetDisplay`: regionCode 분기 "[code] sysType" / project 분기 "city dist - sysNm"(dist==city 중복제거) / infra 분기 / 빈 문자
- 라벨 static: `getDocTypeLabel`/`getStatusLabel`/`getStatusColor` null·정상

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 신규 테스트 클래스 `InfraDTOTest`·`SwProjectDTOTest`·`DocumentDTOTest`(또는 기존 dto 패키지 규약 따름) 추가. 순수(mock 불요, entity Lombok 빌더/세터로 구성). |
| FR-2 | InfraDTO 마스킹 mask=true/false 두 경로 + maskKey 경계(null/≤4/>4) 단언. |
| FR-3 | DocumentDTO private 헬퍼는 public `fromEntity` 경유 간접 검증(라벨 static 3종은 직접). |
| FR-4 | 프로덕션 코드(DTO 등) 무변경. 테스트만. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 신규 테스트 전건 green. |
| NFR-2 | 전체 `./mvnw test` green. |
| NFR-3 | JaCoCo 게이트 통과 + 세 DTO·전역 커버리지 상승. floor 상향은 구현 후 실측 판단. |

---

## 5. 의사결정

- **5-1 entity 구성 방식** ✅: Infra/InfraServer/InfraSoftware/InfraLinkUpis/InfraLinkApi/SwProject/Document/OrgUnit 모두 Lombok @Data/@Setter → 세터로 직접 구성(mock 불요).
- **5-2 private 헬퍼** ✅: convertServer/Upis/Api·maskKey·getEnvironmentLabel·buildOrgUnitPath·buildTargetDisplay 는 public fromEntity 경유로 커버(리플렉션 불요).
- **5-3 마스킹 보안 가치** ✅: InfraDTO mask 경로는 민감정보(비번/키) 노출 방지 로직 → 회귀 위험 큼, 테스트 가치 높음.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Test(신규) | `dto/InfraDTOTest`·`dto/SwProjectDTOTest`·`dto/DocumentDTOTest` | 신규 |
| Docs | 본 기획서 + 개발계획서 | 신규 |

프로덕션/DB/API/UI 변경 0. UI 키워드 0 → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 중첩 entity 세터 명 불일치 | 낮음 | 컴파일/실행 게이트로 즉시 검출 |
| fromEntity 분기 누락 | 낮음 | mask 두 경로 + null/존재 분기 명시 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
