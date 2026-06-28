package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.domain.workplan.PjtManpowerPlan;
import com.swmanager.system.domain.workplan.PjtSchedule;
import com.swmanager.system.domain.workplan.PjtTarget;
import com.swmanager.system.i18n.MessageResolver;
import com.swmanager.system.repository.PjtManpowerPlanRepository;
import com.swmanager.system.repository.PjtScheduleRepository;
import com.swmanager.system.repository.PjtTargetRepository;
import com.swmanager.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HwpxExportService commence_body(착수계 본문) 단위테스트 (커버리지 상향 beyond-A, mock 기반).
 * 착수계 본문은 예정공정표 월별 마스크·P13 투입인력·제품명을 pjt_* 테이블/계약기간에서 조립.
 * 두 경로 커버: ① repo 빈값(계약기간 기반 sequential 마스크·P13 계약일자) ② repo 데이터
 * (PjtSchedule 월마스크·PjtManpowerPlan 등급·PjtTarget 제품명 블록).
 */
class HwpxExportServiceCommenceBodyTest {

    private final DocumentService documentService = mock(DocumentService.class);
    private final MessageResolver messages = mock(MessageResolver.class);
    private final PjtScheduleRepository pjtScheduleRepository = mock(PjtScheduleRepository.class);
    private final PjtManpowerPlanRepository pjtManpowerPlanRepository = mock(PjtManpowerPlanRepository.class);
    private final PjtTargetRepository pjtTargetRepository = mock(PjtTargetRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    private HwpxExportService service;

    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};

    @BeforeEach
    void setUp() {
        service = new HwpxExportService();
        ReflectionTestUtils.setField(service, "documentService", documentService);
        ReflectionTestUtils.setField(service, "messages", messages);
        ReflectionTestUtils.setField(service, "pjtScheduleRepository", pjtScheduleRepository);
        ReflectionTestUtils.setField(service, "pjtManpowerPlanRepository", pjtManpowerPlanRepository);
        ReflectionTestUtils.setField(service, "pjtTargetRepository", pjtTargetRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        lenient().when(messages.get(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(messages.get(anyString(), any())).thenAnswer(i -> i.getArgument(0));
        // 기본: 빈 리스트(repo 블록의 !isEmpty() false 경로)
        lenient().when(pjtScheduleRepository.findByProjIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of());
        lenient().when(pjtManpowerPlanRepository.findByProjIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of());
        lenient().when(pjtTargetRepository.findByProjIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of());
        lenient().when(userRepository.findFirstByUsername(anyString())).thenReturn(Optional.empty());
    }

    // ===== 섹션/리스트 픽스처 헬퍼 =====

    private void addSection(Document d, String key, Map<String, Object> data) {
        DocumentDetail det = new DocumentDetail();
        det.setSectionKey(key);
        det.setSectionData(data);
        d.getDetails().add(det);
    }

    /** {"k1":v1,...} 빌더. 홀수 인자는 즉시 실패(픽스처 오타로 인한 부분 누락 방지). */
    private Map<String, Object> sec(Object... kv) {
        if ((kv.length & 1) != 0) throw new IllegalArgumentException("sec(): 키/값 쌍이 맞지 않음(홀수 인자)");
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
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

    private Document commenceDoc(Long projId) {
        SwProject p = new SwProject();
        p.setProjId(projId);
        p.setProjNm("강릉시 GIS 유지보수");
        p.setCityNm("강원도");
        p.setDistNm("강릉시");
        p.setOrgNm("강릉시청");
        p.setSysNm("UPIS");
        p.setStartDt(LocalDate.of(2026, 1, 1));
        p.setEndDt(LocalDate.of(2026, 6, 30));   // months = 6
        Document d = new Document();
        d.setProject(p);
        d.setDocType(DocumentType.COMMENCE);
        d.setDocNo("D-2026-001");
        return d;
    }

    // ===== ① repo 빈값: 계약기간 기반 sequential 마스크 + P13 계약일자 =====

    @Test
    void commenceBody_emptyRepos_usesContractPeriod() {
        when(documentService.getDocumentById(1)).thenReturn(commenceDoc(100L));
        byte[] out = service.generateHwpx(1, "commence_body");
        assertThat(out).isNotEmpty().startsWith(ZIP_MAGIC);
    }

    // ===== ② repo 데이터: 월마스크·등급·제품명 블록 =====

    @Test
    void commenceBody_withRepoData_fillsScheduleManpowerProduct() throws IOException {
        Document doc = commenceDoc(200L);

        PjtSchedule sch = new PjtSchedule();
        sch.setM01(true); sch.setM02(true); sch.setM03(false);
        sch.setRemark("월별 공정 비고");
        when(pjtScheduleRepository.findByProjIdOrderBySortOrderAsc(200L)).thenReturn(List.of(sch));

        PjtManpowerPlan mp = new PjtManpowerPlan();
        mp.setStepName("유지보수");
        mp.setStartDt(LocalDate.of(2026, 1, 1));
        mp.setEndDt(LocalDate.of(2026, 6, 30));
        mp.setGradeSpecial(1); mp.setGradeHigh(2); mp.setGradeMid(3); mp.setGradeLow(0);
        mp.setFuncHigh(1); mp.setFuncMid(0); mp.setFuncLow(0);
        when(pjtManpowerPlanRepository.findByProjIdOrderBySortOrderAsc(200L)).thenReturn(List.of(mp));

        PjtTarget tgt = new PjtTarget();
        tgt.setProductName("UPIS시스템");
        when(pjtTargetRepository.findByProjIdOrderBySortOrderAsc(200L)).thenReturn(List.of(tgt));

        when(documentService.getDocumentById(2)).thenReturn(doc);
        byte[] out = service.generateHwpx(2, "commence_body");

        assertThat(out).isNotEmpty().startsWith(ZIP_MAGIC);
        // repo 블록이 실제 호출됐는지(월마스크/등급/제품명 조립 경로 진입) 확인.
        // 공정비고+월마스크로 schedule 은 2회 조회되므로 atLeastOnce.
        verify(pjtScheduleRepository, atLeastOnce()).findByProjIdOrderBySortOrderAsc(200L);
        verify(pjtManpowerPlanRepository, atLeastOnce()).findByProjIdOrderBySortOrderAsc(200L);
        verify(pjtTargetRepository, atLeastOnce()).findByProjIdOrderBySortOrderAsc(200L);
        // 렌더 내용 검증: PjtTarget 제품명이 실제 section0.xml 치환에 반영(무치환 회귀 차단)
        assertThat(readSection0(out)).contains("UPIS시스템");
    }

    // ===== 계약기간 미입력(P13 "(미입력)" 경로) =====

    @Test
    void commenceBody_noContractDates_p13Blank() {
        Document doc = commenceDoc(300L);
        doc.getProject().setStartDt(null);
        doc.getProject().setEndDt(null);
        when(documentService.getDocumentById(3)).thenReturn(doc);
        byte[] out = service.generateHwpx(3, "commence_body");
        assertThat(out).isNotEmpty().startsWith(ZIP_MAGIC);
    }

    // ===== C1: site_rep SSN century 20 + plan_personnel U+0002 셀 줄바꿈 =====

    @Test
    void commenceBody_siteRep20_planPersonnelLineBreak() throws IOException {
        Document doc = commenceDoc(401L);
        // ssn 대시포함 14자, index7='3' → century "20", yy "90" → 생년 "2090"
        addSection(doc, "site_rep", sec("addr", "강릉시 ...", "name", "박욱진", "ssn", "900615-3000000"));
        // 인력 1명: name "이사 박욱진"(공백→마지막토큰), tasks 콤마 → U+0002 분리
        addSection(doc, "plan_personnel", sec("list", List.of(
                sec("name", "이사 박욱진", "position", "이사", "techGrade", "특급",
                        "career", "5년", "dept", "SW팀", "tasks", "분석, 설계, 개발", "mobile", "010-1111-2222"))));
        when(documentService.getDocumentById(401)).thenReturn(doc);

        String xml = readSection0(service.generateHwpx(401, "commence_body"));
        assertThat(xml)
                .contains("2090")        // 현대_생년 = century"20" + yy"90"
                .contains("박욱진")        // 인력1_성명(공백 split 마지막 토큰)
                .contains("분석").contains("설계").contains("개발")   // U+0002 셀 분리 결과
                .doesNotContain("분석, 설계, 개발");                  // 원본 콤마 결합문자열은 분리·치환됨
    }

    // ===== C2: site_rep SSN century 19 + 6자 미만(else 빈값) =====

    @Test
    void commenceBody_siteRep19_andShortSsn() throws IOException {
        // (a) century 19: index7='1'
        Document a = commenceDoc(402L);
        addSection(a, "site_rep", sec("name", "홍길동", "ssn", "900615-1000000"));
        when(documentService.getDocumentById(402)).thenReturn(a);
        assertThat(readSection0(service.generateHwpx(402, "commence_body")))
                .contains("1990");   // 생년 = century"19" + yy"90"

        // (b) 6자 미만 → 생년/생월/생일 빈값(else). 양성+음성 단언으로 else 분기 박제:
        //   주민번호 "123" 은 렌더(섹션 처리됨) BUT 생년 파생값(if 분기였다면 "1912")은 미주입.
        Document b = commenceDoc(403L);
        addSection(b, "site_rep", sec("name", "김철수", "ssn", "123"));
        when(documentService.getDocumentById(403)).thenReturn(b);
        String shortXml = readSection0(service.generateHwpx(403, "commence_body"));
        assertThat(shortXml)
                .contains("123")          // 현대_주민번호 렌더 = site_rep 섹션이 처리됨(양성)
                .doesNotContain("1912");  // else 분기: 생년 칸에 ssn 파생값 미주입(음성)
    }

    // ===== C3: participants + userRepository present(fieldRole/certificate 보강) =====

    @Test
    void commenceBody_participants_userRepoEnriches() throws IOException {
        Document doc = commenceDoc(404L);
        addSection(doc, "participants", sec("list", List.of(
                sec("name", "이사 박욱진", "position", "이사", "ssn", "800101-1000000",
                        "techGrade", "특급", "tasks", "PM", "cert", ""))));
        User u = new User();
        u.setUsername("박욱진");
        u.setFieldRole("유지보수책임기술자");
        u.setCertificate("정보처리기사");
        when(userRepository.findFirstByUsername("박욱진")).thenReturn(Optional.of(u));
        when(documentService.getDocumentById(404)).thenReturn(doc);

        String xml = readSection0(service.generateHwpx(404, "commence_body"));
        assertThat(xml)
                .contains("박욱진")          // 보안1_성명
                .contains("정보처리기사");     // 참여1_자격: cert 빈→User.certificate 보강
        verify(userRepository).findFirstByUsername("박욱진");
    }

    // ===== C4: participants absent(폴백) 다인 — i==0 책임 vs i>0 참여 =====

    @Test
    void commenceBody_participants_fallbackRoles() throws IOException {
        Document doc = commenceDoc(405L);
        addSection(doc, "participants", sec("list", List.of(
                sec("name", "김철수", "position", "부장", "ssn", "700101-1000000", "techGrade", "고급", "tasks", "개발"),
                sec("name", "이영희", "position", "과장", "ssn", "750202-2000000", "techGrade", "중급", "tasks", "지원"))));
        // userRepository empty(default) → fieldRole 폴백
        when(documentService.getDocumentById(405)).thenReturn(doc);

        String xml = readSection0(service.generateHwpx(405, "commence_body"));
        assertThat(xml)
                .contains("유지보수책임기술자")    // 참여1_분야(i==0 폴백)
                .contains("유지보수참여기술자");    // 참여2_분야(i>0 폴백)
    }

    // ===== C5: 다중 PjtTarget → 제품명 U+0001 다중행 표 확장 =====

    @Test
    void commenceBody_multiTarget_rowExpansion() throws IOException {
        Document doc = commenceDoc(406L);
        PjtTarget t1 = new PjtTarget(); t1.setProductName("KRAS시스템");
        PjtTarget t2 = new PjtTarget(); t2.setProductName("UPIS시스템");
        when(pjtTargetRepository.findByProjIdOrderBySortOrderAsc(406L)).thenReturn(List.of(t1, t2));
        when(documentService.getDocumentById(406)).thenReturn(doc);

        String xml = readSection0(service.generateHwpx(406, "commence_body"));
        // join(U+0001) → tbl 다중행 복제: 두 제품명 모두 존재 + sentinel 잔존 없음
        assertThat(xml).contains("KRAS시스템").contains("UPIS시스템");
        assertThat(xml).doesNotContain("\u0001");   // 다중행 sentinel 은 확장 후 소거
    }

    // ===== C6: scopeText 멀티라인 + processName projNm 정규식 파생 =====

    @Test
    void commenceBody_scopeMultiline_processNameFromProjNm() throws IOException {
        Document doc = commenceDoc(407L);
        doc.getProject().setProjNm("2026년 강릉시 KRAS 유지관리 용역");
        doc.getProject().setScopeText("범위A\n범위B\n범위C");
        when(documentService.getDocumentById(407)).thenReturn(doc);

        String xml = readSection0(service.generateHwpx(407, "commence_body"));
        assertThat(xml)
                .contains("범위A").contains("범위B").contains("범위C")   // 용역범위1/2/3 분할
                .contains("KRAS 유지관리");   // 공정명: "2026년 강릉시 " 제거 + " 용역" 제거
    }

    // ===== C7: processName schedule 섹션 경유 =====

    @Test
    void commenceBody_processNameFromScheduleSection() throws IOException {
        Document doc = commenceDoc(408L);
        addSection(doc, "schedule", sec("processName", "커스텀공정"));
        when(documentService.getDocumentById(408)).thenReturn(doc);

        String xml = readSection0(service.generateHwpx(408, "commence_body"));
        assertThat(xml).contains("커스텀공정");   // schedule 섹션 processName 우선
    }

    // ===== C8: commence 섹션 date present(제출일자 섹션값 우선, startDt 파생 skip) =====

    @Test
    void commenceBody_commenceDatePresent_usesSectionValue() throws IOException {
        Document doc = commenceDoc(409L);
        addSection(doc, "commence", sec("date", "2026년 03월 절차"));
        when(documentService.getDocumentById(409)).thenReturn(doc);

        String xml = readSection0(service.generateHwpx(409, "commence_body"));
        assertThat(xml).contains("2026년 03월 절차");   // 섹션 date 우선(startDt 파생 분기 skip)
    }
}
