# 개발계획서 — 검색목록 산출물 일괄 ZIP 다운로드 (doc-bulk-export)

- **상태**: DRAFT v1 (codex 검토 대기)
- **기획서**: `docs/product-specs/doc-bulk-export.md` (v0.2, 승인 2026-06-10)
- **작성일**: 2026-06-10

---

## 0. 구현 원칙
- **신규 생성 로직 0**: 기존 docId 기반 메서드(`generateHwpx`, `generateInterimReport`, `generateDesignEstimate`)와 단건 ZIP 패턴(`DocumentController.downloadZip` 584-621, `addZipEntry` 623-627) **재사용·조립만**.
- 검색은 `searchDocuments`(필터·핀·작성자 그대로) 재사용.
- 정책(FR-7~11) 서버에서 강제.

## 1. 구현 묶음

### A. 백엔드 — bulk-zip 엔드포인트 (`DocumentController`)
1. **`GET /document/api/bulk-zip`** 신규. 파라미터: `docType, cityNm, distNm, keyword, authorName`(excel-list 와 동일) + `type`(letter|all|inspector|interim|commence_body|design|completion). **type 화이트리스트 검증** — 허용값 외 → `400`(codex).
2. **권한**(FR-7): `if ("NONE".equals(getAuth())) return 403`.
3. **조회·상한**(FR-9/10): `searchDocuments(docType,null,cityNm,distNm,null,null,null,keyword,authorName, PageRequest.of(0, 201))`.
   - `totalElements == 0` → `400` ("대상 문서 없음").
   - `totalElements > 200` → `413` ("검색 결과 N건 — 최대 200건, 필터를 좁히세요").
4. **ZIP 생성**(`ByteArrayOutputStream`+`ZipOutputStream`): 각 `DocumentDTO` 루프, `type` 에 따라 산출물 생성:
   - `letter` → `generateHwpx(docId,"letter")` (전 유형 공통)
   - `inspector` → INTERIM 만 `generateHwpx(docId,"inspector")`
   - `interim` → INTERIM 만 `generateInterimReport(docId)`
   - `commence_body` → COMMENCE 만 `generateHwpx(docId,"commence_body")`
   - `design` → COMMENCE 만 `generateDesignEstimate(docId)`
   - `completion` → COMPLETION 만. **KRAS·UPIS 각각** 생성: `generateHwpx(docId,"completion_body")`→`준공계_KRAS.hwpx`, `generateHwpx(docId,"completion_body_upis")`→`준공계_UPIS.hwpx`. **둘 다 개별 try-catch**, 실패 시 `_실패목록.txt` 기록(codex — 기존 단건 ZIP 은 조용히 무시하나 벌크는 기록).
   - `all` → 각 문서 유형의 **전체 산출물**(downloadZip 584-608 로직: 공문+유형별, 준공은 KRAS+UPIS 둘 다)을 **문서별 하위폴더**(`{시군구}_{사업명}_{문서번호}/공문.hwpx`, `/준공계_KRAS.hwpx` 등)로.
   - **단일유형**: 평면 `{시군구}_{사업명}_{문서번호}_{산출물}.{ext}`.
   - 유형 불일치 문서는 **건너뜀**(FR-4), `all` 이외 단일유형에서.
5. **건별 try-catch**(FR-8): 생성 실패/건너뜀을 `List<String> fails` 누적 → 마지막에 ZIP 에 `_실패목록.txt`(UTF-8) 엔트리 추가(준공 KRAS/UPIS 조용 실패도 기록).
6. **엔트리명 sanitize**(FR-11): 폴더/파일명 경로 금지문자 제거 — 컨트롤러 `private static String zipSafe(String)` 헬퍼(`DocumentSignedScanService.sanitize` 규칙: `[\\/:*?"<>|]`·제어문자 제거·길이컷). 동일명 충돌 시 Set 추적 `_n`.
7. **메모리**(codex): **개별 산출물 byte[] 는 즉시 `addZipEntry` write 후 참조 폐기(루프 밖 누적 금지)**. 단 최종 응답은 `ByteArrayOutputStream` 전체 ZIP 을 메모리 보유(완전 스트리밍 아님 — 200건 상한으로 관리. 필요 시 후속 스트리밍 응답). 응답 `_일괄_{날짜}.zip`, UTF-8 파일명 인코딩(기존 패턴).

### B. 프론트 — 드롭다운 UI (`document-list.html`)
8. 필터바 `엑셀 다운로드` 옆 **"일괄 산출물 ▾"** 드롭다운 — 기존 `.dropdown`/`.dropdown-content` 컴포넌트 재사용(문서작성 버튼과 동일).
9. **옵션 동적**(현재 선택 `docType` 기준, Thymeleaf `th:if`):
   - INTERIM: 공문 일괄 / 기성검사원 일괄 / 기성내역서 일괄 / 전체 일괄
   - COMMENCE: 공문 / 착수계본문 / 설계내역서 / 전체
   - COMPLETION: 공문 / 준공계본문 / 전체
   - 미선택(혼합): 공문 일괄 / 전체 일괄
10. 각 항목 → `bulkDownload(type)`: `filterForm` 의 FormData → querystring + `type` → `window.location = '/document/api/bulk-zip?'+params`. 대량 대비 클릭 시 안내(생성 수십 초 가능) + 드롭다운 일시 비활성.
11. 접근성(codex — hover만으론 부족, **JS 구현 필요**): 드롭다운 버튼 `aria-haspopup="true"`/`aria-expanded` 토글, **키보드 열기/닫기**(Enter/Space/Esc·포커스 이동), 다운로드 시작 시 **disabled + "생성 중"** 표시. 기존 hover 동작과 병행. 토큰 색만.

### C. 테스트
12. **단위**: `zipSafe` 엣지(금지문자/제어/길이/충돌 _n).
13. **컨트롤러 테스트 4종**(codex, MockMvc): `NONE→403`, `0건→400`, `201건→413`, 일부 산출물 실패 시 ZIP 에 정상 파일 + `_실패목록.txt` 포함. (서비스 mock 으로 건수·실패 시뮬레이트.)
14. **수동(회사 PC)**: 소량 letter/all, 혼합목록(유형 스킵+_실패목록), 0건·201건 경계, 부분실패(섹션없는 문서) 시 나머지 ZIP 정상, 파일명 한글, 드롭다운 키보드/disabled.

## 2. 검증 게이트
| 게이트 | 비고 |
|---|---|
| 컴파일 `mvnw -q -DskipTests compile` | 신규 엔드포인트·헬퍼 |
| 단위(zipSafe) | sanitize |
| 수동 다운로드 | 소량/대량차단/혼합/부분실패/빈결과 |
| UI 육안(디자인) | 드롭다운 토큰·다크모드·접근성·진행피드백 |

## 3. 리스크 / 롤백
- 성능: 상한 200 + 건별 try-catch 로 부분실패·중단 방지. 대량 시 동기 응답 지연 → 상한·안내로 관리(nginx 300s 내).
- 메모리: 즉시 write 로 누적 최소화(전체 ZIP 은 baos 에 누적되나 200건 상한으로 관리). 필요 시 후속 스트리밍 응답.
- 롤백: 순수 신규 엔드포인트+UI 추가 → 제거만으로 롤백, 기존 기능 무영향.

## 4. 진행 순서
A(엔드포인트+헬퍼+생성 루프 ZIP) → 컴파일 → B(드롭다운 UI+접근성 JS) → C(테스트) → codex 구현검토 → 회사 PC 육안.

## 5. codex 1차 검토 (2026-06-10)
- **판정**: 수정필요 → 방향 승인 가능. FR-7~11·재사용·DTO 필드 충족 확인.
- **반영**: type 화이트리스트 검증(400), 준공 KRAS/UPIS 각 파일명·실패 기록, 메모리 표현 명확화, 드롭다운 접근성 JS 명시, 컨트롤러 테스트 4종(403/400/413/부분실패).
