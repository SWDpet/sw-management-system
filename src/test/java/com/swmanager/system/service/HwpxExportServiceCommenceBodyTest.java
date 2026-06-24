package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.PjtManpowerPlan;
import com.swmanager.system.domain.workplan.PjtSchedule;
import com.swmanager.system.domain.workplan.PjtTarget;
import com.swmanager.system.i18n.MessageResolver;
import com.swmanager.system.repository.PjtManpowerPlanRepository;
import com.swmanager.system.repository.PjtScheduleRepository;
import com.swmanager.system.repository.PjtTargetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
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
        lenient().when(messages.get(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(messages.get(anyString(), any())).thenAnswer(i -> i.getArgument(0));
        // 기본: 빈 리스트(repo 블록의 !isEmpty() false 경로)
        lenient().when(pjtScheduleRepository.findByProjIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of());
        lenient().when(pjtManpowerPlanRepository.findByProjIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of());
        lenient().when(pjtTargetRepository.findByProjIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of());
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
}
