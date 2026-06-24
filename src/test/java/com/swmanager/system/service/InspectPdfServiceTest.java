package com.swmanager.system.service;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.dto.InspectCheckResultDTO;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.SwProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * InspectPdfService.renderToHtml (v1 레거시 표) 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 필드 주입이라 ReflectionTestUtils 로 mock 주입. 점검결과 섹션 분리·프로젝트/인프라 컨텍스트·
 * sysType 기본값·null 프로젝트/결과 분기 커버. (v2/generatePdf 의 차트·외부브라우저는 별도)
 */
class InspectPdfServiceTest {

    private final InspectReportService inspectReportService = mock(InspectReportService.class);
    private final SwProjectRepository swProjectRepository = mock(SwProjectRepository.class);
    private final InfraRepository infraRepository = mock(InfraRepository.class);
    private final TemplateEngine templateEngine = mock(TemplateEngine.class);

    private InspectPdfService service;

    @BeforeEach
    void setUp() {
        service = new InspectPdfService();
        ReflectionTestUtils.setField(service, "inspectReportService", inspectReportService);
        ReflectionTestUtils.setField(service, "swProjectRepository", swProjectRepository);
        ReflectionTestUtils.setField(service, "infraRepository", infraRepository);
        ReflectionTestUtils.setField(service, "templateEngine", templateEngine);
        // plain mock() (MockitoExtension 미사용) → 미사용 stub 도 무해, lenient 불필요.
        when(inspectReportService.getTemplateItems(anyString())).thenReturn(List.of());
        when(infraRepository.findByDistNmAndSysNmEn(any(), any())).thenReturn(List.of());
    }

    private InspectCheckResultDTO cr(String section, String code) {
        InspectCheckResultDTO c = new InspectCheckResultDTO();
        c.setSection(section);
        c.setResultCode(code);
        return c;
    }

    private InspectReportDTO report(Long pjtId, String sysType, List<InspectCheckResultDTO> results) {
        InspectReportDTO r = new InspectReportDTO();
        r.setPjtId(pjtId);
        r.setSysType(sysType);
        r.setInspectMonth("2026-06");
        r.setCheckResults(results);
        return r;
    }

    @Test
    void renderToHtml_withProjectAndResults_processesV1Template() {
        InspectReportDTO rpt = report(10L, "UPIS", List.of(
                cr("DB", "NORMAL"), cr("AP", "ABNORMAL"), cr("GIS", "PARTIAL"),
                cr("DBMS", "NORMAL"), cr("APP", "NORMAL"), cr("DB_USAGE", "NORMAL")));
        when(inspectReportService.findById(1L)).thenReturn(rpt);
        SwProject p = new SwProject();
        p.setDistNm("강릉시"); p.setSysNmEn("UPIS");
        when(swProjectRepository.findById(10L)).thenReturn(Optional.of(p));
        when(templateEngine.process(eq("pdf/pdf-inspect-report"), any(Context.class))).thenReturn("html-v1");

        assertThat(service.renderToHtml(1L)).isEqualTo("html-v1");
        verify(infraRepository).findByDistNmAndSysNmEn("강릉시", "UPIS");   // 프로젝트 있음 → 인프라 조회
        verify(inspectReportService).getTemplateItems("UPIS");
    }

    @Test
    void renderToHtml_nullProject_skipsInfraLookup() {
        when(inspectReportService.findById(2L)).thenReturn(report(11L, "KRAS", List.of(cr("DB", "NORMAL"))));
        when(swProjectRepository.findById(11L)).thenReturn(Optional.empty());
        when(templateEngine.process(eq("pdf/pdf-inspect-report"), any(Context.class))).thenReturn("html-noproj");

        assertThat(service.renderToHtml(2L)).isEqualTo("html-noproj");
        verify(infraRepository, never()).findByDistNmAndSysNmEn(any(), any());   // 프로젝트 없음
        verify(inspectReportService).getTemplateItems("KRAS");
    }

    @Test
    void renderToHtml_nullCheckResults_omitsSectionVars() {
        when(inspectReportService.findById(3L)).thenReturn(report(12L, "UPIS", null));   // checkResults null
        when(swProjectRepository.findById(12L)).thenReturn(Optional.empty());
        when(templateEngine.process(eq("pdf/pdf-inspect-report"), any(Context.class))).thenReturn("html-nores");

        assertThat(service.renderToHtml(3L)).isEqualTo("html-nores");
        // checkResults null → 섹션 변수(dbItems 등) 미설정 확인
        ArgumentCaptor<Context> ctx = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("pdf/pdf-inspect-report"), ctx.capture());
        assertThat(ctx.getValue().getVariable("dbItems")).isNull();
    }

    @Test
    void renderToHtml_nullSysType_defaultsToUpis() {
        when(inspectReportService.findById(4L)).thenReturn(report(13L, null, List.of()));   // sysType null
        when(swProjectRepository.findById(13L)).thenReturn(Optional.empty());
        when(templateEngine.process(eq("pdf/pdf-inspect-report"), any(Context.class))).thenReturn("html");

        service.renderToHtml(4L);
        verify(inspectReportService).getTemplateItems("UPIS");                       // null → 기본 UPIS
        verify(templateEngine).process(eq("pdf/pdf-inspect-report"), any(Context.class));  // v1 템플릿 선택
    }
}
