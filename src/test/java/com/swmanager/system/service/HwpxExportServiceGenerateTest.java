package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.i18n.MessageResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HwpxExportService.generateHwpx 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * documentService 만 mock + classpath 실 HWPX 템플릿으로 생성→결과가 zip(PK 시그니처)인지 +
 * 템플릿 경로 분기(resolveTemplatePath)·치환맵(buildReplacements) 전 templateType + 예외 커버.
 */
class HwpxExportServiceGenerateTest {

    private final DocumentService documentService = mock(DocumentService.class);
    private final MessageResolver messages = mock(MessageResolver.class);

    private HwpxExportService service;

    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};   // "PK\x03\x04"

    @BeforeEach
    void setUp() {
        service = new HwpxExportService();
        ReflectionTestUtils.setField(service, "documentService", documentService);
        ReflectionTestUtils.setField(service, "messages", messages);
        lenient().when(messages.get(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(messages.get(anyString(), any())).thenAnswer(i -> i.getArgument(0));
    }

    private Document doc(DocumentType docType, String... sectionKeys) {
        SwProject p = new SwProject();
        p.setProjNm("강릉시 GIS 유지보수");
        p.setCityNm("강원도");
        p.setDistNm("강릉시");
        p.setOrgNm("강릉시청");
        p.setOrgLghNm("강릉시장");
        Document d = new Document();
        d.setProject(p);
        d.setDocType(docType);
        d.setDocNo("D-2026-001");
        for (String key : sectionKeys) {
            DocumentDetail det = new DocumentDetail();
            det.setSectionKey(key);
            det.setSectionData(new HashMap<>(Map.of("manager", "홍길동", "tel", "010-0000-0000", "date", "2026-06-01")));
            d.getDetails().add(det);
        }
        return d;
    }

    private void assertHwpxZip(byte[] out) {
        assertThat(out).isNotEmpty().startsWith(ZIP_MAGIC);   // HWPX = OOXML zip
    }

    /** HWPX(zip) 내 Contents/section0.xml 본문 추출 — 치환 반영 검증용. */
    private String readSection0(byte[] hwpx) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(hwpx))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                if ("Contents/section0.xml".equals(e.getName())) {
                    return new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        return "";
    }

    // ===== 데이터분기 픽스처 헬퍼 (beyond-A: 날짜/금액/섹션 present 분기 탐) =====

    /** 날짜·금액이 채워진 project (계약일자/착수년/준공년 등 present 분기 탐). */
    private SwProject richProject() {
        SwProject p = new SwProject();
        p.setProjNm("강릉시 GIS 유지보수");
        p.setCityNm("강원도");
        p.setDistNm("강릉시");
        p.setOrgNm("강릉시청");
        p.setOrgLghNm("강릉시장");
        p.setSysNm("UPIS");
        p.setSysNmEn("KRAS");
        p.setContDt(LocalDate.of(2026, 3, 10));
        p.setStartDt(LocalDate.of(2026, 1, 1));
        p.setEndDt(LocalDate.of(2026, 6, 30));
        p.setContAmt(100000000L);          // 금100,000,000원 = 금일억원
        p.setYear(2025);
        return p;
    }

    private Document docOf(DocumentType type, SwProject p) {
        Document d = new Document();
        d.setProject(p);
        d.setDocType(type);
        d.setDocNo("D-2026-001");
        return d;
    }

    private void addSection(Document d, String key, Map<String, Object> data) {
        DocumentDetail det = new DocumentDetail();
        det.setSectionKey(key);
        det.setSectionData(data);
        d.getDetails().add(det);
    }

    /** {"k1":v1,"k2":v2,...} 빌더. 홀수 인자는 즉시 실패(픽스처 오타로 인한 부분 누락 방지). */
    private Map<String, Object> sec(Object... kv) {
        if ((kv.length & 1) != 0) throw new IllegalArgumentException("sec(): 키/값 쌍이 맞지 않음(홀수 인자)");
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    // ===== G1: letter contDt present (계약일자 formatDate 분기) =====

    @Test
    void generate_letter_contDtPresent_substitutesContractDate() throws IOException {
        Document d = docOf(DocumentType.COMMENCE, richProject());
        addSection(d, "letter", sec("manager", "홍길동", "tel", "010-0000-0000", "date", "2026-06-01"));
        when(documentService.getDocumentById(11)).thenReturn(d);
        String xml = readSection0(service.generateHwpx(11, "letter"));
        // contDt=2026-03-10 → formatDate "2026. 3. 10." (null 이면 빈값 else 였음)
        assertThat(xml).contains("2026. 3. 10.");
    }

    // ===== G2: inspector 리치 — 분리일자 + 유효 기성금액 + 날짜 present 분기 =====

    @Test
    void generate_inspector_richSeparatedDate_substitutesAmounts() throws IOException {
        Document d = docOf(DocumentType.INTERIM, richProject());
        addSection(d, "inspector", sec(
                "paymentAmount", "85000000", "paymentRate", "50",
                "interimYear", "2026", "interimMonth", "6", "interimDay", "15"));
        when(documentService.getDocumentById(12)).thenReturn(d);
        String xml = readSection0(service.generateHwpx(12, "inspector"));
        assertThat(xml)
                .contains("금85,000,000원")     // 기성금액(유효 paymentAmount)
                .contains("금팔천오백만원")        // 기성금액한글(convertToKoreanAmount)
                .contains("2026년")              // 계약년도(contDt present 분기)
                // 기성일자 분리입력 경로: "2026년    06월    15일" (월/일 0패딩)
                .contains("06월").contains("15일")
                .contains("금100,000,000원");     // 계약금액(contAmt)
        // 기성율 50 치환
        assertThat(xml).contains("50");
    }

    // ===== G3: inspector interimDate 정규식 경로(find 성공) =====

    @Test
    void generate_inspector_interimDateRegex_parsed() throws IOException {
        Document d = docOf(DocumentType.INTERIM, richProject());
        addSection(d, "inspector", sec("interimDate", "2026-06-15", "paymentRate", "30"));
        when(documentService.getDocumentById(13)).thenReturn(d);
        String xml = readSection0(service.generateHwpx(13, "inspector"));
        // interimDate 정규식 분기: 06월/15일 0패딩 결합
        assertThat(xml).contains("2026년").contains("06월").contains("15일");
    }

    // ===== G4: inspector 비숫자 금액 → NumberFormatException 무시 → 계약금액 fallback =====

    @Test
    void generate_inspector_nonNumericAmount_fallsBackToContract() throws IOException {
        Document d = docOf(DocumentType.INTERIM, richProject());
        addSection(d, "inspector", sec("paymentAmount", "abc"));
        when(documentService.getDocumentById(14)).thenReturn(d);
        String xml = readSection0(service.generateHwpx(14, "inspector"));
        // "abc"→숫자 0개→parseLong("") NFE→interimAmt 0→계약금액(100,000,000) 사용
        assertThat(xml).contains("금100,000,000원");
    }

    // ===== G5: inspector interimDate find 실패 → raw 유지 =====

    @Test
    void generate_inspector_interimDateNoMatch_keepsRaw() throws IOException {
        Document d = docOf(DocumentType.INTERIM, richProject());
        addSection(d, "inspector", sec("interimDate", "미정"));
        when(documentService.getDocumentById(15)).thenReturn(d);
        String xml = readSection0(service.generateHwpx(15, "inspector"));
        assertThat(xml).contains("미정");   // 정규식 미매치 else: raw 그대로
    }

    // ===== G6: completion_body(KRAS) 리치 — 날짜 present + actualDate =====

    @Test
    void generate_completionBody_rich_substitutesDates() throws IOException {
        Document d = docOf(DocumentType.COMPLETION, richProject());
        addSection(d, "completion", sec("actualDate", "2026-05"));
        when(documentService.getDocumentById(16)).thenReturn(d);
        String xml = readSection0(service.generateHwpx(16, "completion_body"));
        assertThat(xml)
                .contains("2026년 05월")   // 준공일 formatYearMonthBlankDay(actualDate)
                .contains("2025");        // 점검년도(year=2025, contDt/start/end 는 2026)
    }

    // ===== G7: completion_body_upis 리치 — 날짜 present + actualDate =====

    @Test
    void generate_completionBodyUpis_rich_substitutesDates() throws IOException {
        Document d = docOf(DocumentType.COMPLETION, richProject());
        addSection(d, "completion", sec("actualDate", "2026-05"));
        when(documentService.getDocumentById(17)).thenReturn(d);
        String xml = readSection0(service.generateHwpx(17, "completion_body_upis"));
        assertThat(xml)
                .contains("2026년 1월 1일 ~")   // 계약기간(start~end formatDateKorean)
                .contains("2026년 05월");        // 준공일(actualDate)
    }

    // ===== G8: completion_full 리치 + 행마커(target/inspect_summary 복제) =====

    @Test
    void generate_completionFull_rowMarkers_replicateRows() throws IOException {
        Document d = docOf(DocumentType.COMPLETION, richProject());
        addSection(d, "completion", sec("actualDate", "2026-05", "completionDate", "2026-06"));
        // 유지보수 대상 2행: category SW / 빈값(→SW fallback)
        List<Map<String, Object>> targets = new ArrayList<>();
        targets.add(sec("category", "SW", "productName", "KRAS시스템", "specification", "v1.0", "quantity", "1"));
        targets.add(sec("category", "", "productName", "UPIS시스템", "specification", "v2", "quantity", "2"));
        addSection(d, "target", sec("targets", targets));
        // 정기점검: resultCode 3분기 + datetime 파싱(성공/len<10 skip/invalid catch)
        List<Map<String, Object>> insps = new ArrayList<>();
        insps.add(sec("datetime", "2026-06-15T09:00", "targetList", "서버A", "result", "NORMAL"));
        insps.add(sec("datetime", "2026-07-01", "targetList", "서버B", "result", "CHECK"));
        insps.add(sec("datetime", "2026-08-01", "targetList", "서버C", "result", "OTHER"));
        insps.add(sec("datetime", "bad", "targetList", "서버D", "result", "NORMAL"));        // len<10 → skip
        insps.add(sec("datetime", "9999-99-99XX", "targetList", "서버E", "result", "CHECK")); // len>=10 invalid → catch
        addSection(d, "inspect_summary", sec("inspections", insps));
        when(documentService.getDocumentById(18)).thenReturn(d);

        String xml = readSection0(service.generateHwpx(18, "completion_full"));
        assertThat(xml)
                .contains("KRAS시스템").contains("UPIS시스템")   // 대상 2행 복제(replication loop)
                .contains("정상 (이상 없음)")                     // NORMAL 라벨
                .contains("점검")                                // CHECK 라벨
                .contains("OTHER")                              // 그외 resultCode passthrough
                .contains("2026년 6월 15일")                      // datetime 파싱 성공
                .contains("2026년 06월");                         // 제출일(completionDate present)
    }

    // ===== G9: completion_full 제출일 fallback (completionDate 미입력→actualDate) =====

    @Test
    void generate_completionFull_submitDateFallback() throws IOException {
        Document d = docOf(DocumentType.COMPLETION, richProject());
        addSection(d, "completion", sec("actualDate", "2026-05"));   // completionDate 없음
        when(documentService.getDocumentById(19)).thenReturn(d);
        String xml = readSection0(service.generateHwpx(19, "completion_full"));
        // submitDateK 빈→actualDateK 로 대체(line 848): 제출일/검사_제출일 도 실제준공일과 같은 "2026년 05월"로 렌더.
        // 템플릿 placeholder 4종(실제준공일·제출일·검사_실제준공일·검사_제출일) 모두 actualDate 파생 → 4회.
        // fallback 미동작 시 제출일/검사_제출일 빈값 → 2회. ≥3 으로 fallback 동작을 판별(위장통과 차단).
        int cnt = xml.split(java.util.regex.Pattern.quote("2026년 05월"), -1).length - 1;
        assertThat(cnt).as("제출일 fallback 이 actualDate 로 채워짐(미동작=2, 동작=4)").isGreaterThanOrEqualTo(3);
    }

    // ===== letter (docType 분기) =====

    @Test
    void generate_letter_commence_substitutesProjName() throws IOException {
        when(documentService.getDocumentById(1)).thenReturn(doc(DocumentType.COMMENCE, "letter"));
        byte[] out = service.generateHwpx(1, "letter");
        assertHwpxZip(out);
        // 치환 반영 검증: {{용역명}} → projNm 이 실제 section0.xml 에 들어갔는지(빈 zip/무치환 회귀 차단)
        assertThat(readSection0(out)).contains("강릉시 GIS 유지보수");
    }

    @Test
    void generate_letter_interim() {
        when(documentService.getDocumentById(2)).thenReturn(doc(DocumentType.INTERIM, "letter"));
        assertHwpxZip(service.generateHwpx(2, "letter"));
    }

    @Test
    void generate_letter_completion() {
        when(documentService.getDocumentById(3)).thenReturn(doc(DocumentType.COMPLETION, "letter"));
        assertHwpxZip(service.generateHwpx(3, "letter"));
    }

    // ===== 기타 templateType =====

    @Test
    void generate_inspector() {
        when(documentService.getDocumentById(4)).thenReturn(doc(DocumentType.INTERIM, "inspector"));
        assertHwpxZip(service.generateHwpx(4, "inspector"));
    }

    @Test
    void generate_completionBody_kras() {
        when(documentService.getDocumentById(6)).thenReturn(doc(DocumentType.COMPLETION, "completion"));
        assertHwpxZip(service.generateHwpx(6, "completion_body"));
    }

    @Test
    void generate_completionBody_upis() {
        when(documentService.getDocumentById(7)).thenReturn(doc(DocumentType.COMPLETION, "completion"));
        assertHwpxZip(service.generateHwpx(7, "completion_body_upis"));
    }

    @Test
    void generate_completionFull() {
        when(documentService.getDocumentById(8)).thenReturn(doc(DocumentType.COMPLETION, "completion"));
        assertHwpxZip(service.generateHwpx(8, "completion_full"));
    }

    // ===== 예외 경로 (resolveTemplatePath) =====

    @Test
    void generate_letter_nullDocType_throws() {
        when(documentService.getDocumentById(9)).thenReturn(doc(null, "letter"));
        assertThatThrownBy(() -> service.generateHwpx(9, "letter"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generate_unknownTemplateType_throws() {
        when(documentService.getDocumentById(10)).thenReturn(doc(DocumentType.COMMENCE, "letter"));
        assertThatThrownBy(() -> service.generateHwpx(10, "no_such_type"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
