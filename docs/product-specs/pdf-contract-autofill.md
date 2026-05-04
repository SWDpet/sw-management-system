---
tags: [plan, sprint, feature, pdf, autofill, ocr]
sprint: "pdf-contract-autofill"
short_alias: "PCA-1"
status: draft-v1
created: "2026-05-04"
---

# [기획서] 전자계약서 PDF 업로드 → SW 사업 등록 폼 자동 채움

- **작성팀**: 기획팀
- **작성일**: 2026-05-04
- **선행**: `36f1b6b` (doc-split-ops 후속), `team-monitor-dashboard` 와 코드 영역 비충돌
- **상태**: 초안 v1 (codex 검토 대기)
- **약칭**: PCA-1 (PDF Contract Autofill v1)

---

## 1. 배경 / 목표

### 배경

SW 사업 정보 등록(`/projects/new`) 시 사용자는 전자계약서 PDF (지방자치단체 발주, iText 4.2.0 표준 양식) 의 내용을 폼에 **수기로 입력**하고 있음. 사업당 약 17개 필드 입력 + 마스터(시도/시군구/시스템명/SW사업형태) 매칭 — 시간 소요 + 입력 오타 발생.

본 sprint 는 PDF 업로드 1회로 폼 17개 필드 중 12개 이상을 **AI 자동 채움 + 사용자 검토** 후 저장하게 한다.

### 사전조사 결과 (2026-05-04)

샘플 PDF 1건 분석 (양양군 UPIS 유지보수 용역):
- iText 4.2.0 PDF 의 ToUnicode CMap 누락 → `pdfplumber`/`PyMuPDF` 텍스트 추출 시 한글 깨짐 (재현됨).
- **PyMuPDF 의 `page.get_pixmap()` 으로 PNG 변환 후 비전 모델로 읽기**: 100% 정확한 한글·숫자·날짜 추출 검증 완료.
- 폼 필드 17개 중 16개가 PDF 에서 추출 가능 (1개는 사용자 마스터 데이터 — `personId`).
- 필요 마스터 모두 기존: `cont_frm_mst`(5), `prj_types`(3), `sys_mst`(37), `sigungu_code`.
- 기존 폼 (project-form.html) 은 변경 없이 **자동 채움만 추가**하면 됨.

### 목표

- (G1) 양양군 양식 PDF 업로드 시 폼 12개 이상 필드 자동 채움 (1차 양식 한정).
- (G2) 행정구역 → 기관명/장 변환 함수 정확도 100% (9 케이스 단위 테스트 PASS).
- (G3) 사업명 → 시스템명 매칭 정확도 95% 이상 (sys_mst 37개 cd 정확 매칭).
- (G4) 사용자 검토 단계 강제 — 자동 저장 금지, 검토 후 저장.
- (G5) 매핑 실패 필드는 빈칸 + 토스트 안내, 회귀 없음.
- (G6) 사업 담당자 선택 시 부서 자동 채움 (PDF 무관 부가 기능).

---

## 2. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | `/projects/new` 폼 상단에 **"계약서 PDF 업로드"** 버튼 추가. 클릭 → 파일 선택 모달 (`.pdf` 한정). |
| FR-2 | 업로드 시 백엔드 `POST /projects/parse-contract-pdf` 엔드포인트로 multipart 전송. 응답: 구조화된 DTO (`ContractParseResultDTO`). |
| FR-3 | 백엔드: PDF → PNG 변환 (PyMuPDF 또는 Java 대응) → 비전 추출 → 정규식 파싱 → DTO 반환. |
| FR-4 | 행정구역 변환 함수 `OrgNameResolver.resolve(cityNm, distNm)` 구현. 9 케이스 모두 단위 테스트로 보장 (§ 4-1). |
| FR-5 | 사업명 파서: 괄호 영문 약어 추출 → `sys_mst.cd` 매칭. 매칭 성공 시 `sysNm`/`sysNmEn` 자동 채움. |
| FR-6 | 사업명에 "유지보수" 포함 시 `bizType` = `cont_frm_mst.cd='4'` (유상) 자동 선택. **그 외 키워드 매핑 없음** (사용자 수동). |
| FR-7 | 발주처(`client`) = 변환된 `orgNm`. (예: 양양군 → 양양군청) |
| FR-8 | 추출된 필드는 폼에 채워진 후 **시각적 마크 (배지 "AI 추출")** 표시. 사용자가 수정하면 마크 자동 제거. |
| FR-9 | 매핑 실패 필드는 빈칸 + 토스트 알림: "X개 필드는 자동 매핑이 안 되었습니다. 수동 입력해주세요." |
| FR-10 | **저장은 사용자가 명시적으로 [저장] 버튼 클릭 시에만**. 자동 저장 금지. |
| FR-11 | 사업 담당자(`personId`) select 변경 시 → 해당 user 의 `team_nm` → `contDept` 자동 채움 (이미 `personList` 가 `teamNm` 보유, JS 한 줄 추가). |
| FR-12 | PDF 파싱 실패 (손상·암호·지원 외 양식) 시 명확한 에러 토스트 + 폼은 변경 없음. |
| FR-13 | `ContractParser` 인터페이스로 양식별 파서 분리. 1차 구현체: `LocalGovContractParser` (지방자치단체 표준 양식). 향후 `LhContractParser`, `MoisContractParser` 등 추가 가능 구조. |
| FR-14 | PDF 자동 채움 직전 사용자가 입력한 값이 있으면 **덮어쓰기 전 사용자 확인** (confirm 모달). |

---

## 3. 추출-매핑 표 (FR-3 상세)

| # | PDF 추출값 | 폼 필드 | 변환 |
|---|-----------|--------|------|
| 1 | 발주처 기관명 (예: "강원특별자치도 양양군") | `cityNm` | "강원특별자치도" 분리 |
| 2 |  | `distNm` | "양양군" 분리 |
| 3 | (자동) | `orgCd` | `sigungu_code.instt_c` |
| 4 | (자동) | `distCd` | `sigungu_code.adm_sect_c` |
| 5 | (자동, FR-4) | `orgNm` | OrgNameResolver |
| 6 | (자동, FR-4) | `orgLghNm` | OrgNameResolver |
| 7 | (자동, FR-7) | `client` | = orgNm |
| 8 | 계약건명 | `projNm` | 그대로 |
| 9 | (자동, FR-6) | `bizType` | "유지보수" → 4(유상) |
| 10 | (자동, FR-5) | `sysNm` | sys_mst.nm 매칭 |
| 11 | (자동, FR-5) | `sysNmEn` | sys_mst.cd 매칭 |
| 12 | 계약상대자 상호 | `contEnt` | 그대로 |
| 13 | 계약일자 | `contDt` | YYYY-MM-DD |
| 14 | 착수일자 | `startDt` | YYYY-MM-DD |
| 15 | 총완수일자 | `endDt` | YYYY-MM-DD |
| 16 | 계약금액 | `contAmt` | 숫자만 (₩, 콤마 제거) |
| 17 | (FR-11, PDF 무관) | `contDept` | personId → users.team_nm |

매핑 안 되는 필드 (사용자 수동): `year`, `pmsCd`, `personId`, `bizCat`, `instDt`, `budgAmt`, `contRt`, `swRt`, `contType`, 금액 분할 항목들.

---

## 4. 의사결정 / 우려사항

### 4-1. 행정구역 → 기관명/장 변환 규칙 (사용자 확정 2026-05-04)

```
[기관명]
  - 시군구 없음:                  cityNm + "청"
  - 도/자치도 산하 시/군:          distNm + "청" (시도명 안 붙음)
  - 특별시/광역시 산하 구/군:      cityNm + " " + distNm + "청"

[기관장]
  - 도/자치도 자체:               cityNm + "지사" (예: 강원특별자치도지사)
  - 특별자치시/특별시/광역시 자체: cityNm + "장"
  - 도 산하 시:                   distNm + "장"
  - 도 산하 군:                   distNm + "수"
  - 특별시/광역시 산하 구:        cityNm + " " + distNm + "청장"
  - 광역시 산하 군:               cityNm + " " + distNm + "수"
```

9 케이스 단위 테스트로 회귀 보장 (§5 OrgNameResolverTest).

### 4-2. 사업명 키워드 → bizType (사용자 확정)

- "유지보수" → 유상 (cd=4)
- 그 외 키워드 자동 매핑 **없음** (구축/개발/고도화 등은 사례가 적어 자동화 ROI 낮음 — 사용자 수동)

### 4-3. PDF 파싱 기술 스택 — Python 마이크로서비스 vs Java 통합

**결정**: **Java 통합** (Spring Boot 안에서 처리)
- 근거: Python 의존성 (PyMuPDF) 추가 시 배포·운영 복잡도 ↑. 동등 기능 Java 라이브러리 존재:
  - PDF 텍스트 추출: **Apache PDFBox** (이미 한국어 PDF 처리 검증됨)
  - PDF → 이미지: PDFBox `PDFRenderer`
  - 비전 추출: **Anthropic Claude API** (또는 Naver Clova OCR) HTTP 호출
- Python 의존성·별도 프로세스 없이 Spring Boot 단일 실행 유지.

### 4-4. 비전 모델 호출 방식

**결정**: **Anthropic Claude API (Claude 3.5 Haiku 또는 Sonnet)** 직접 호출
- 근거: 본 영상 자막 분석에서 한글 PDF 100% 정확. Naver Clova OCR 도 가능하나 API 키·과금 추가 필요. Claude API 는 이미 사내 사용 중.
- 비용: 1 PDF (2 페이지 PNG) 호출당 약 $0.001~0.005 — 사업당 1회 사용이라 무시할 수준.
- API 키는 `ANTHROPIC_API_KEY` 환경변수로 관리 (DB_PASSWORD 와 동일 패턴).

### 4-5. 사용자 검토 단계 (FR-10) — 강제

PDF 자동 채움 후 **저장은 사용자 명시 클릭 시에만**. 자동 저장 X.
- 근거: 데이터 품질·회귀 방지. AI 추출이 100% 정확하지 않을 수 있음 (특히 신규 양식).
- "AI 추출" 배지 (FR-8) 로 사용자가 검토 우선순위 시각적 인지.

### 4-6. 1차 양식 한정 (FR-13)

**결정**: 1차는 **지방자치단체 표준 양식 (iText 4.2.0)** 만 지원
- 양양군 PDF 가 대표 — 강릉시·울산광역시 북구 등 다른 자치단체도 동일 포맷 가능성 높음.
- 다른 발주처 (LH·국토부·민간) 양식은 향후 sprint (`pdf-contract-autofill-v2`) 분리.

### 4-7. .hwp / .zip 파싱 — 본 sprint 제외

- 과업지시서(`.hwp`) 파싱: hwplib/Pyhwp 라이브러리 의존성 + 복잡도 → 별도 sprint.
- 계약서일체(`.zip`) 자동 풀기: zip 안 파일별 다양성 → 별도 sprint.

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller (신규) | `src/main/java/com/swmanager/system/controller/ContractPdfController.java` | 신규 |
| Service (신규) | `src/main/java/com/swmanager/system/service/contract/ContractPdfService.java` | 신규 |
| Service (신규) | `src/main/java/com/swmanager/system/service/contract/parser/ContractParser.java` (interface) | 신규 |
| Service (신규) | `src/main/java/com/swmanager/system/service/contract/parser/LocalGovContractParser.java` | 신규 |
| Util (신규) | `src/main/java/com/swmanager/system/util/OrgNameResolver.java` | 신규 |
| Util (신규) | `src/main/java/com/swmanager/system/util/ProjectNameParser.java` | 신규 |
| Vision (신규) | `src/main/java/com/swmanager/system/integration/AnthropicVisionClient.java` | 신규 |
| DTO (신규) | `src/main/java/com/swmanager/system/dto/ContractParseResultDTO.java` | 신규 |
| Config | `src/main/resources/application.properties` (env: `ANTHROPIC_API_KEY`) | 수정 (1줄) |
| Template | `src/main/resources/templates/project-form.html` | 수정 (PDF 업로드 버튼 + JS) |
| pom.xml | Apache PDFBox 의존성 추가 | 수정 |
| Test | 단위 테스트 6종 (OrgNameResolver / ProjectNameParser / Service mock / Controller MVC / Parser Integration / FR-11 JS) | 신규 |
| Docs (수정) | `docs/generated/audit/2026-04-18-system-audit.md` 해당 항목 체크 X (감사와 무관, 신규 기능) | — |

**수정 2 파일. 신규 약 12 파일. DB 변경 없음. API 신규 1개 (`POST /projects/parse-contract-pdf`).**

---

## 6. 테스트 시나리오 (T)

### 6-1. 단위 (OrgNameResolver — 9 케이스)

| ID | 입력 | 기대 orgNm | 기대 orgLghNm |
|----|------|-----------|--------------|
| T1 | 강원특별자치도, null | 강원특별자치도청 | 강원특별자치도지사 |
| T2 | 세종특별자치시, null | 세종특별자치시청 | 세종특별자치시장 |
| T3 | 서울특별시, null | 서울특별시청 | 서울특별시장 |
| T4 | 울산광역시, null | 울산광역시청 | 울산광역시장 |
| T5 | 강원특별자치도, 강릉시 | 강릉시청 | 강릉시장 |
| T6 | 강원특별자치도, 양양군 | 양양군청 | 양양군수 |
| T7 | 서울특별시, 강남구 | 서울특별시 강남구청 | 서울특별시 강남구청장 |
| T8 | 울산광역시, 북구 | 울산광역시 북구청 | 울산광역시 북구청장 |
| T9 | 인천광역시, 강화군 | 인천광역시 강화군청 | 인천광역시 강화군수 |

### 6-2. 단위 (ProjectNameParser)

| ID | 입력 | 기대 sysNmEn | 기대 sysNm | 기대 bizType |
|----|------|-------------|-----------|-------------|
| T10 | "2026년 양양군 도시계획정보체계(UPIS) 유지보수 용역" | UPIS | 도시계획정보체계 | 4(유상) |
| T11 | "OO시 부동산종합공부시스템(KRAS) 유지보수" | KRAS | 부동산종합공부시스템 | 4(유상) |
| T12 | "OO 신규 구축 용역" (괄호 영문 없음) | null | null | null |
| T13 | "OO (XYZ) 유지보수" — XYZ 가 sys_mst 에 없음 | null | null | 4(유상) |

### 6-3. 통합 (실제 PDF)

| ID | 시나리오 | 기대 |
|----|---------|------|
| T14 | 양양군 PDF 업로드 → 16개 필드 자동 채움 | 위 §3 표 16개 모두 정확 채움 |
| T15 | 채움 후 사용자 수정 → 저장 | 정상 저장, "AI 추출" 배지 사용자 수정 필드만 제거 |
| T16 | 손상 PDF 업로드 | 에러 토스트, 폼 변경 없음 |
| T17 | 사용자 입력 후 PDF 업로드 (FR-14) | confirm 모달 → 확인 시 덮어씀 |

### 6-4. 회귀 (FR-11 personId → contDept)

| ID | 시나리오 | 기대 |
|----|---------|------|
| T18 | personId 변경 → contDept 자동 채움 | users.team_nm 즉시 반영 |
| T19 | personId 빈 값으로 변경 | contDept 빈 값 |

---

## 7. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | Maven compile 성공. PDFBox 3.x 호환. |
| NFR-2 | 서버 재기동 후 `/projects/new` 정상 + `POST /projects/parse-contract-pdf` 200/4xx 응답. |
| NFR-3 | PDF 업로드 size 제한 5MB (현재 spring multipart 100MB 보다 좁게). 파일 type 체크 (.pdf). |
| NFR-4 | 비전 API 호출 timeout 30초. 실패 시 사용자 친화적 에러. |
| NFR-5 | API 키 (`ANTHROPIC_API_KEY`) 미설정 시 PDF 업로드 버튼 비활성화 (또는 명확한 에러). 운영 부팅 실패 X. |
| NFR-6 | OrgNameResolver / ProjectNameParser 단위 테스트 100% PASS. |
| NFR-7 | 통합 테스트: 양양군 PDF 1건 + (사용자 제공) 추가 자치단체 PDF 1~2건 PASS. |
| NFR-8 | PDF 본문은 서버에 영구 저장하지 않음 (메모리·임시파일만, 처리 후 즉시 삭제). 보안 감사 NFR-S1 준수. |
| NFR-9 | 비전 API 에 PDF 페이지 이미지가 외부로 전송되는 점 → 사용자에게 명시적 동의 필요한지? (개인정보 정책 검토 필요 — codex 검토 항목) |

---

## 8. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 양양군 외 자치단체 양식 호환성 미지수 | 중 | 1차는 양양군 한정 명시. 회귀 테스트 추가 샘플 1~2건. 2차 sprint 로 일반화 |
| 비전 API 호출 비용 | 낮음 | Haiku 사용 시 사업당 $0.001 수준. 모니터링 권장 |
| 비전 API 응답 정확도 (특히 숫자/날짜 OCR 오인) | 중 | 사용자 검토 단계 강제 (FR-10). "AI 추출" 배지로 검토 유도 |
| 외부 API 의존 (Anthropic 장애 시) | 낮음 | 사용자 친화적 에러 + 수동 입력 fallback (기존 워크플로우와 동일) |
| 개인정보 외부 전송 우려 | **중** | 사용자 첫 사용 시 동의 안내 (또는 기획서 검토 시 사용자 확정). 향후 사내 OCR 으로 대체 가능 구조 (`VisionClient` 인터페이스) |
| sys_mst 에 신규 시스템 없을 때 매칭 실패 | 낮음 | 빈칸 + 사용자 수동 (정상 동작) |
| 향후 폼 필드 추가 시 매핑 누락 | 낮음 | 매핑 표 (§3) 가 단일 진실원. DTO 리뷰로 갱신 |
| **모니터링 sprint 와 코드 충돌** | 낮음 | 영역 비충돌 (TeamMonitor* vs Contract*). 단, 동일 폴더 작업 시 git pull 자주 |

---

## 9. 결정 사항 요약 (사용자 확정)

| 항목 | 결정 |
|------|------|
| 행정구역 → 기관명/장 변환 9 케이스 | 사용자 확정 (§4-1) |
| 사업명 키워드 → bizType | "유지보수" → 유상만. 나머지 패스 (§4-2) |
| `bizCat` 자동 매핑 | 없음 (사용자 수동) |
| 시스템명 매칭 | 괄호 영문 → sys_mst.cd 정확 매칭 |
| PDF 처리 위치 | Java 통합 (Spring Boot, PDFBox + Anthropic API) |
| 비전 모델 | Anthropic Claude API |
| 사용자 검토 단계 | 강제 (자동 저장 X) |
| 1차 양식 | 지방자치단체 표준 (iText 4.2.0) |
| `.hwp` / `.zip` 파싱 | 본 sprint 제외 (별도 sprint) |
| Sprint 명 / 약칭 | `pdf-contract-autofill` / PCA-1 |

---

## 10. 릴리스 체크리스트 (개발 단계 진입 시)

- [ ] codex 기획서 검토 통과
- [ ] 사용자 최종승인 (기획서)
- [ ] 개발계획서 v1 작성
- [ ] codex 개발계획서 검토
- [ ] 사용자 최종승인 (개발계획서)
- [ ] 환경변수 `ANTHROPIC_API_KEY` 설정 및 모델 (Haiku/Sonnet) 결정
- [ ] PDFBox 의존성 추가
- [ ] OrgNameResolver / ProjectNameParser / Service / Parser / VisionClient 구현
- [ ] FR-11 (personId → contDept) JS 한 줄 추가
- [ ] 단위 + 통합 테스트 6종 PASS
- [ ] 양양군 PDF 1건 회귀 + 사용자 추가 샘플 1~2건
- [ ] codex 구현 검증 (FR/NFR/T 항목별 충족 판정)
- [ ] 사용자 확인
- [ ] git commit + push + tag `pca-1-v1`

---

## 11. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

**검토 포인트 (codex 가 우선 보았으면 하는 항목)**:
1. NFR-9 (개인정보 외부 전송) 정책 적정성 — 사내 OCR 대체 필요성 판단
2. §4-3 Java 통합 결정 (Python 마이크로서비스 대비 trade-off) 적정성
3. FR/NFR 누락 여부, 특히 보안·로깅 측면
4. T1~T19 회귀 커버리지 충분성
5. `team-monitor-dashboard` sprint 와의 영역 비충돌 검증
