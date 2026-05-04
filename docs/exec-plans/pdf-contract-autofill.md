---
tags: [dev-plan, sprint, feature, pdf, autofill, ocr]
sprint: "pdf-contract-autofill"
short_alias: "PCA-1"
status: draft-v1
created: "2026-05-04"
---

# [개발계획서] 전자계약서 PDF 업로드 → SW 사업 등록 폼 자동 채움

- **작성팀**: 개발팀
- **작성일**: 2026-05-04
- **근거 기획서**: [../product-specs/pdf-contract-autofill.md](../product-specs/pdf-contract-autofill.md) (사용자 승인 v1, codex quota 초과로 자동 패스)
- **선행 커밋**: `36f1b6b` (doc-split-ops 후속)
- **상태**: 초안 v1 (codex 검토 대기)
- **예상 작업 시간**: 4~5일

---

## 0. 사전조사 결과 (2026-05-04)

### 0-1. 현재 폼 구조

`src/main/resources/templates/project-form.html` (372줄) — 기존 6개 fieldset:

| Fieldset | 핵심 필드 | 비고 |
|----------|----------|------|
| ① 기본 및 담당자 | `year`, `pmsCd`, `personId` | personList 가 `username + teamNm` 보유 → FR-11 즉시 적용 가능 |
| ② 행정 및 기관 | `cityNm`, `distNm`, `orgCd`, `distCd`, `orgNm`, `orgLghNm` | sigungu_code 마스터, JS `loadDistricts()` `autoFillCodes()` 기존 |
| ③ 사업 식별 | `bizType`, `bizCat`, `sysNm`, `sysNmEn` | cont_frm_mst, prj_types, sys_mst 마스터 |
| ④ 계약정보 | `projNm`, `client`, `contEnt`, `contDept`, `contType` | |
| ⑤ 일정정보 | `contDt`, `startDt`, `endDt`, `instDt` | |
| ⑥ 제품 및 금액 | `contAmt` 외 금액 분할, `payProgYn`, `interimYn`, `completionYn`, `maintType`, `stat` | |

### 0-2. 마스터 데이터 확인 (DB snapshot)

| 마스터 | 옵션 수 | 본 sprint 사용 |
|--------|---------|--------------|
| `cont_frm_mst` | 5 (도입/교체/무상/유상/종료) | "유지보수" → 4(유상) 매핑 |
| `prj_types` | 3 (GISSW/PKSW/TS) | 본 sprint 자동 매핑 X |
| `sys_mst` | 37 (UPIS/KRAS/BSIS 등) | 사업명 괄호 영문 매칭 |
| `sigungu_code` | 시도/시군구 + instt_c + adm_sect_c | 기존 JS 로직 재활용 |

### 0-3. PDF 파싱 검증 결과

샘플: 양양군 UPIS 계약서 (`H:\공유 드라이브\.../양양군_UPIS/.../260213.pdf`, iText 4.2.0, 2페이지, 297KB)

- pdfplumber/pypdf/PyMuPDF 텍스트 추출: **한글 깨짐** (ToUnicode 누락)
- PyMuPDF PNG 변환 → Claude 비전 모델: **한글 100% 정확**
- 폼 17 필드 중 16 필드 추출 가능 (1 필드 = personId 는 사용자 마스터)

### 0-4. 코드 영역 비충돌 검증 (`team-monitor-dashboard` sprint)

```
team-monitor-dashboard:  src/main/java/.../monitor/, controller/TeamMonitorController.java,
                         resources/static/admin/team-monitor*, application.properties §7
pdf-contract-autofill:   src/main/java/.../service/contract/, controller/ContractPdfController.java,
                         integration/AnthropicVisionClient.java, util/OrgNameResolver.java,
                         resources/templates/project-form.html (§7 추가, 기존 JS 보존)
```

→ **코드 영역 0% 겹침**. 단, `application.properties` 와 `pom.xml` 은 동일 파일 수정 가능 → git pull 빈번 권장.

### 0-5. 환경변수 사전 결정

| 변수 | 용도 | 기본값 |
|------|------|-------|
| `ANTHROPIC_API_KEY` | Claude Vision API 호출 | 미설정 시 PDF 업로드 비활성화 (NFR-5) |
| `ANTHROPIC_MODEL` | 모델 선택 | `claude-haiku-4-5` (저비용 기본) |
| `pdf.contract.max-size-mb` | 업로드 크기 제한 | 5 |
| `pdf.contract.api-timeout-ms` | API timeout | 30000 |

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 의존성 추가 (`pom.xml`)

```xml
<!-- PDF 처리 (Apache PDFBox 3.x) -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.3</version>
</dependency>

<!-- HTTP client for Anthropic API (Java 17 native HttpClient 사용 — 추가 의존성 없음) -->
```

검증: `./mvnw -q compile` → BUILD SUCCESS

### Step 2 — `OrgNameResolver` 유틸 + 단위 테스트

**파일 (신규)**:
- `src/main/java/com/swmanager/system/util/OrgNameResolver.java`
- `src/test/java/com/swmanager/system/util/OrgNameResolverTest.java`

```java
@Component
public class OrgNameResolver {
    public record OrgInfo(String orgNm, String orgLghNm) {}
    
    public OrgInfo resolve(String cityNm, String distNm) {
        boolean isDo = cityNm != null && cityNm.endsWith("도");
        boolean isCityArea = cityNm != null 
            && (cityNm.endsWith("특별시") || cityNm.endsWith("광역시"));
        boolean hasDist = distNm != null && !distNm.isBlank();
        
        if (cityNm == null || cityNm.isBlank()) return null;
        
        if (!hasDist) {
            if (isDo) return new OrgInfo(cityNm + "청", cityNm + "지사");
            return new OrgInfo(cityNm + "청", cityNm + "장");
        }
        if (isDo) {
            String suffix = distNm.endsWith("군") ? "수" : "장";
            return new OrgInfo(distNm + "청", distNm + suffix);
        }
        if (isCityArea) {
            String prefix = cityNm + " " + distNm;
            String head = distNm.endsWith("군") ? "수" : "청장";
            return new OrgInfo(prefix + "청", prefix + head);
        }
        return null;  // 매핑 실패
    }
}
```

테스트 (T1~T9, 9 케이스 — 기획서 §6-1 표 그대로).

### Step 3 — `ProjectNameParser` 유틸 + 단위 테스트

**파일 (신규)**:
- `src/main/java/com/swmanager/system/util/ProjectNameParser.java`
- `src/test/java/com/swmanager/system/util/ProjectNameParserTest.java`

```java
@Component
@RequiredArgsConstructor
public class ProjectNameParser {
    private final SysMstRepository sysMstRepository;
    
    public record ParseResult(String sysNm, String sysNmEn, String bizTypeCd) {}
    
    private static final Pattern ENG_PATTERN = Pattern.compile("\\(([A-Z][A-Z0-9]+)\\)");
    
    public ParseResult parse(String projNm) {
        if (projNm == null || projNm.isBlank()) return new ParseResult(null, null, null);
        
        // 시스템 영문/한글
        String sysNm = null, sysNmEn = null;
        Matcher m = ENG_PATTERN.matcher(projNm);
        if (m.find()) {
            String code = m.group(1);
            sysMstRepository.findByCd(code).ifPresent(sm -> {
                /* sysNm = sm.getNm(); sysNmEn = code; */
            });
            // (record 라 setter 없음 — 위 의사코드. 실제 구현은 var 사용)
        }
        
        // bizType: "유지보수" 키워드만
        String bizTypeCd = projNm.contains("유지보수") ? "4" : null;
        
        return new ParseResult(sysNm, sysNmEn, bizTypeCd);
    }
}
```

테스트 (T10~T13, 4 케이스 — 기획서 §6-2):
- T10: 양양군 UPIS 유지보수 → UPIS / 도시계획정보체계 / 4
- T11: KRAS 유지보수 → KRAS / 부동산종합공부시스템 / 4
- T12: 괄호 없음 → null / null / null
- T13: 괄호 있지만 sys_mst 미매칭 + 유지보수 → null / null / 4

### Step 4 — `AnthropicVisionClient` 통합

**파일 (신규)**:
- `src/main/java/com/swmanager/system/integration/AnthropicVisionClient.java`

Java 17 native `HttpClient` 사용. POST `https://api.anthropic.com/v1/messages` (model + base64 image + prompt). 응답 JSON 에서 `content[0].text` 추출.

```java
@Component
public class AnthropicVisionClient {
    @Value("${anthropic.api-key:}") private String apiKey;
    @Value("${anthropic.model:claude-haiku-4-5}") private String model;
    @Value("${pdf.contract.api-timeout-ms:30000}") private long timeoutMs;
    
    public boolean isConfigured() { return apiKey != null && !apiKey.isBlank(); }
    
    public String extractFromImages(List<byte[]> pngImages, String extractionPrompt) {
        if (!isConfigured()) throw new IllegalStateException("ANTHROPIC_API_KEY not set");
        // ... HttpClient 호출 + JSON 파싱
    }
}
```

NFR-5: API 키 미설정 시 폼 PDF 버튼 비활성화 (Thymeleaf `th:disabled`).

### Step 5 — `ContractParser` 인터페이스 + `LocalGovContractParser` 구현

**파일 (신규)**:
- `src/main/java/com/swmanager/system/service/contract/parser/ContractParser.java` (interface)
- `src/main/java/com/swmanager/system/service/contract/parser/LocalGovContractParser.java`

```java
public interface ContractParser {
    boolean supports(byte[] pdfBytes);  // 양식 감지 (1차는 항상 true)
    ContractParseResultDTO parse(byte[] pdfBytes);
}
```

LocalGovContractParser 흐름:
1. PDFBox 로 텍스트 추출 시도 → 한글 깨짐 감지 (`?` 또는 깨진 문자 비율) → fallback
2. PDFBox `PDFRenderer` 로 페이지 PNG 변환 (DPI 144)
3. AnthropicVisionClient 로 추출 프롬프트 + 이미지 전송
4. 응답 텍스트에서 정규식으로 라벨별 값 추출
5. OrgNameResolver / ProjectNameParser 로 변환
6. DTO 반환

### Step 6 — DTO + Service

**파일 (신규)**:
- `src/main/java/com/swmanager/system/dto/ContractParseResultDTO.java`
- `src/main/java/com/swmanager/system/service/contract/ContractPdfService.java`

DTO 구조 (기획서 §3 매핑 표 그대로):
```java
public record ContractParseResultDTO(
    String cityNm, String distNm, String orgCd, String distCd,
    String orgNm, String orgLghNm, String client,
    String projNm, String bizTypeCd, String sysNm, String sysNmEn,
    String contEnt,
    LocalDate contDt, LocalDate startDt, LocalDate endDt,
    Long contAmt,
    List<String> failedFields  // 매핑 실패 필드 목록 (FR-9 토스트용)
) {}
```

### Step 7 — Controller

**파일 (신규)**:
- `src/main/java/com/swmanager/system/controller/ContractPdfController.java`

```java
@RestController
@RequestMapping("/projects")
public class ContractPdfController {
    @PostMapping("/parse-contract-pdf")
    public ResponseEntity<ContractParseResultDTO> parsePdf(
            @RequestParam("file") MultipartFile file) {
        // size 체크 (NFR-3, 5MB)
        // type 체크 (NFR-3, .pdf)
        // service 호출
        return ResponseEntity.ok(service.parse(file.getBytes()));
    }
}
```

NFR-8: 파일 본문 영구 저장 X. service 처리 후 byte[] GC 대상.

### Step 8 — UI: PDF 업로드 버튼 + JS

**파일 (수정)**:
- `src/main/resources/templates/project-form.html`

추가 위치: 폼 상단 (page-header 직후).

```html
<!-- PDF 업로드 영역 (PCA-1) -->
<div class="pdf-upload-row" th:if="${@anthropicVisionClient.isConfigured()}">
  <label class="btn-pdf-upload">
    <i class="fas fa-file-pdf"></i> 계약서 PDF 자동 입력
    <input type="file" id="pdfUploadInput" accept=".pdf" hidden>
  </label>
  <span class="pdf-upload-hint">PDF 업로드 시 폼이 자동 채워집니다 (검토 후 저장)</span>
</div>
```

JS:
1. `pdfUploadInput` change → 사용자 입력 값 있으면 confirm (FR-14)
2. FormData 로 `POST /projects/parse-contract-pdf`
3. 응답 DTO 로 폼 필드 채움 (`document.querySelector('[name="..."]').value = ...`)
4. 채워진 필드에 "AI 추출" 배지 추가 (FR-8)
5. `failedFields` 배열 → 토스트 (FR-9)
6. 사용자가 채워진 필드 수정 시 배지 자동 제거

### Step 9 — FR-11: personId → contDept 자동 (PDF 무관)

**파일 (수정)**:
- `project-form.html` ① fieldset:

```html
<!-- option 에 data-team-nm 속성 추가 -->
<option th:each="p : ${personList}"
        th:value="${p.userSeq}"
        th:attr="data-team-nm=${p.teamNm}"
        th:text="${p.username + ' (' + p.teamNm + ')'}"
        th:selected="${p.userSeq == project.personId}">
</option>
```

JS (페이지 하단):
```javascript
document.querySelector('select[name="personId"]').addEventListener('change', function() {
    const teamNm = this.options[this.selectedIndex].dataset.teamNm || '';
    document.querySelector('input[name="contDept"]').value = teamNm;
});
```

### Step 10 — application.properties 추가

```properties
# ========================================
# 8. PDF 계약서 자동 입력 (sprint pdf-contract-autofill)
# ========================================
anthropic.api-key=${ANTHROPIC_API_KEY:}
anthropic.model=${ANTHROPIC_MODEL:claude-haiku-4-5}
pdf.contract.max-size-mb=5
pdf.contract.api-timeout-ms=30000
```

### Step 11 — 통합 테스트 + 회귀 테스트

**테스트 파일 (신규)**:
- `src/test/java/com/swmanager/system/integration/ContractPdfIntegrationTest.java`
- `src/test/java/com/swmanager/system/controller/ContractPdfControllerTest.java`

T14~T17 (기획서 §6-3) — `@MockBean AnthropicVisionClient` 로 mock 응답 주입.

### Step 12 — 빌드 / 재기동 / 스모크

```bash
./mvnw -q test    # 단위 + 통합 테스트 모두 PASS
./mvnw -q compile # BUILD SUCCESS
bash server-restart.sh  # Started + ERROR 0
```

스모크:
1. `/projects/new` 진입 → "계약서 PDF 자동 입력" 버튼 표시
2. 양양군 PDF 업로드 → 16개 필드 자동 채움
3. "AI 추출" 배지 시각 확인
4. personId 변경 → contDept 즉시 채움
5. 저장 → projects/status 에 새 row 등장

---

## 2. 테스트 매트릭스 (T1~T19)

| ID | 종류 | 내용 | 검증 방법 | Pass 기준 |
|----|------|------|----------|-----------|
| T1~T9 | 단위 | OrgNameResolver 9 케이스 | `mvn test -Dtest=OrgNameResolverTest` | 9 GREEN |
| T10~T13 | 단위 | ProjectNameParser 4 케이스 | `mvn test -Dtest=ProjectNameParserTest` | 4 GREEN |
| T14 | 통합 | 양양군 PDF → 16 필드 | `mvn test -Dtest=ContractPdfIntegrationTest#yangyang` | 16 필드 정확 |
| T15 | 통합 | 채움 후 사용자 수정 → 저장 | 수동 스모크 | "AI 추출" 배지 사용자 수정 필드만 제거 |
| T16 | 통합 | 손상 PDF 업로드 | 수동 스모크 | 에러 토스트, 폼 변경 없음 |
| T17 | 통합 | 사용자 입력 후 PDF 업로드 | 수동 스모크 | confirm 모달 → 확인 시 덮어씀 |
| T18 | 회귀 | personId → contDept | 수동 스모크 | users.team_nm 즉시 반영 |
| T19 | 회귀 | personId 빈 값 | 수동 스모크 | contDept 빈 값 |
| T20 | 빌드 | Maven compile | `./mvnw -q compile` | BUILD SUCCESS |
| T21 | 부팅 | 서버 재기동 | `bash server-restart.sh` | Started + ERROR 0 |
| T22 | 보안 | API 키 미설정 시 버튼 비활성화 | env unset 후 `/projects/new` | 버튼 disabled |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| compile 실패 (Step 1 PDFBox 의존성) | pom.xml revert 후 PDFBox 버전 호환 확인 |
| Anthropic API 응답 형식 불일치 | mock 응답으로 대체 + 사용자 안내 (수동 입력) |
| 양양군 양식 외 PDF 업로드 시 오작동 | 1차는 양양군만 명시. ContractParser.supports() false 반환 |
| 배포 후 회귀 (기존 폼 동작 깨짐) | `git revert <commit>` → 재배포. project-form.html 변경은 추가만이라 영향 적음 |
| API 키 노출 사고 | 즉시 환경변수 rotate, git history 검사 (.gitignore 에 application-local.properties 확인) |

---

## 4. 리스크·완화 재확인 (기획서 §8 대비)

| 리스크 | 수준 | 완화 (개발계획 단계 보강) |
|--------|------|-------------------------|
| 양양군 외 양식 호환성 | 중 | 1차 양양군 한정. ContractParser.supports() 가 false 반환 시 명확한 메시지 |
| 비전 API 호출 비용 | 낮음 | Haiku 기본. Sonnet 으로 자동 fallback X (사용자 수동 모델 변경 가능) |
| API 응답 정확도 | 중 | "AI 추출" 배지 + 사용자 검토 강제 (FR-10) |
| Anthropic 장애 | 낮음 | 사용자 친화적 에러 + 수동 입력 가능 |
| 개인정보 외부 전송 | 중 | 1차 사용 시 동의 안내 모달 (Step 8 JS 추가) |
| sys_mst 매칭 실패 | 낮음 | 빈칸 + 사용자 수동 (정상) |
| **모니터링 sprint 와 git 충돌** | 낮음 | application.properties / pom.xml 만 동일 — 작업 시작 전 git pull, 작업 끝 시 git pull --rebase |

---

## 5. NFR 충족 매트릭스 (기획서 §7 1:1)

| NFR | 충족 위치 |
|-----|----------|
| NFR-1 Maven compile | Step 12 T20 |
| NFR-2 서버 재기동 | Step 12 T21 |
| NFR-3 size/type 체크 | Step 7 Controller |
| NFR-4 timeout 30s | Step 4 + properties |
| NFR-5 API 키 미설정 비활성화 | Step 8 (`th:if="${@anthropicVisionClient.isConfigured()}"`) + T22 |
| NFR-6 단위 100% PASS | T1~T13 |
| NFR-7 통합 PASS | T14 (양양군). 추가 샘플은 사용자 제공 시 후속 |
| NFR-8 영구 저장 X | Step 7 byte[] 만 사용, 파일 시스템 미사용 |
| NFR-9 외부 전송 동의 | Step 8 첫 사용 동의 모달 |

---

## 6. 작업 시간 추정 (재계산)

| Step | 시간 |
|------|------|
| 1 (의존성) | 10분 |
| 2 (OrgNameResolver + 9 테스트) | 1시간 |
| 3 (ProjectNameParser + 4 테스트) | 1시간 |
| 4 (AnthropicVisionClient) | 3시간 |
| 5 (ContractParser + LocalGov) | 4시간 |
| 6 (DTO + Service) | 2시간 |
| 7 (Controller + size/type 체크) | 1시간 |
| 8 (UI: 버튼 + JS + 배지) | 4시간 |
| 9 (FR-11 personId → contDept) | 30분 |
| 10 (properties) | 5분 |
| 11 (통합 테스트 + 회귀) | 3시간 |
| 12 (빌드·재기동·스모크) | 1시간 |
| **합계** | **약 21시간 = 3일 (집중) ~ 5일 (다른 일과 병행)** |

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

**검토 포인트 (codex)**:
1. Step 4 AnthropicVisionClient 의 Java 17 HttpClient 직접 사용 (vs Spring WebClient) 적정성
2. Step 5 LocalGovContractParser 의 한글 깨짐 감지 알고리즘 — `?` 비율 임계값 결정 필요
3. Step 8 "AI 추출" 배지 UX 패턴 — 디자인 시스템(`docs/DESIGN.md`) 일관성
4. Step 9 personId 빈 값 처리 (T19) 누락 시나리오 없는지
5. NFR-8 ↔ Step 7 byte[] 메모리 보장 — multipart temp file (`spring.servlet.multipart.location`) 자동 삭제 확인 필요
6. T17 "사용자 입력 후 PDF 업로드 confirm" — 어디까지 입력했을 때 confirm 띄울지 (예: 1 글자도 입력했으면? 핵심 필드만?)
