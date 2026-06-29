package com.swmanager.system.controller;

import com.swmanager.system.exception.GlobalExceptionHandler;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.OrgUnitRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.DocumentAccessSupport;
import com.swmanager.system.service.DocumentService;
import com.swmanager.system.service.LogService;
import com.swmanager.system.service.OrgUnitService;
import com.swmanager.system.service.PdfExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * [beyond-A] DocumentController HTTP 표면 net — standalone MockMvc (codex 컨트롤러 net 순서 3번째·마지막).
 * 라우팅(/document/*)·status·redirect·view명·@ResponseBody JSON·권한분기 박제.
 * 인증=DocumentAccessSupport.getAuth() 위임 → mock+stub 으로 제어(SecurityContext 불요). NONE→redirect:/.
 * DB/컨텍스트 불요 → CI gates(verify) 실행. PIT 미편입(컨트롤러 부적합).
 */
class DocumentControllerMvcTest {

    private MockMvc mockMvc;
    private DocumentService documentService;
    private UserRepository userRepository;
    private LogService logService;
    private PdfExportService pdfExportService;
    private DocumentAccessSupport access;

    @BeforeEach
    void setUp() throws Exception {
        DocumentController controller = new DocumentController();
        documentService = mock(DocumentService.class);
        userRepository = mock(UserRepository.class);
        logService = mock(LogService.class);
        pdfExportService = mock(PdfExportService.class);
        access = mock(DocumentAccessSupport.class);
        inject(controller, "documentService", documentService);
        inject(controller, "infraRepository", mock(InfraRepository.class));
        inject(controller, "swProjectRepository", mock(SwProjectRepository.class));
        inject(controller, "orgUnitRepository", mock(OrgUnitRepository.class));
        inject(controller, "orgUnitService", mock(OrgUnitService.class));
        inject(controller, "userRepository", userRepository);
        inject(controller, "logService", logService);
        inject(controller, "pdfExportService", pdfExportService);
        inject(controller, "access", access);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())  // /list 의 @PageableDefault
                .build();
    }

    private void inject(DocumentController c, String field, Object value) throws Exception {
        Field f = DocumentController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(c, value);
    }

    // ── 1. /list NONE → redirect:/ ───────────────────────────────────────────
    @Test
    void list_none_redirectsHome() throws Exception {
        when(access.getAuth()).thenReturn("NONE");
        mockMvc.perform(get("/document/list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(documentService, never()).searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ── 2. /list VIEW → 200 + view document-list + model ─────────────────────
    @Test
    void list_view_rendersList() throws Exception {
        when(access.getAuth()).thenReturn("VIEW");
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        when(documentService.getCityNames()).thenReturn(java.util.List.of());
        mockMvc.perform(get("/document/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("document/document-list"))
                .andExpect(model().attributeExists("documents", "cityList", "userAuth"));
        verify(documentService).searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ── 3. /detail/{id} NONE → redirect:/ ────────────────────────────────────
    @Test
    void detail_none_redirectsHome() throws Exception {
        when(access.getAuth()).thenReturn("NONE");
        mockMvc.perform(get("/document/detail/5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(documentService, never()).getDocumentById(anyInt());
    }

    // ── 4. /api/dist-list → @ResponseBody JSON ───────────────────────────────
    @Test
    void distList_api_returnsJson() throws Exception {
        when(documentService.getDistNamesByCity("서울특별시")).thenReturn(java.util.List.of("종로구"));
        mockMvc.perform(get("/document/api/dist-list").param("cityNm", "서울특별시"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0]").value("종로구"));
    }

    // ── 5. /preview/{id} → view document-preview + model ─────────────────────
    @Test
    void preview_rendersPreview() throws Exception {
        when(pdfExportService.renderDocumentToHtml(7)).thenReturn("<html>x</html>");
        mockMvc.perform(get("/document/preview/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("document/document-preview"))
                .andExpect(model().attribute("docId", 7))
                .andExpect(model().attributeExists("htmlContent"));
    }

    // ── 6. POST /delete/{id} EDIT → redirect:/document/list + 삭제 호출 ────────
    @Test
    void delete_edit_redirectsAndDeletes() throws Exception {
        when(access.getAuth()).thenReturn("EDIT");
        mockMvc.perform(post("/document/delete/9"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/document/list"));
        verify(documentService).deleteDocument(9);
    }

    // ── 7. POST /delete/{id} 비-EDIT → redirect(권한없음) + 삭제 안 함 ─────────
    @Test
    void delete_notEdit_redirectsWithoutDeleting() throws Exception {
        when(access.getAuth()).thenReturn("VIEW");
        mockMvc.perform(post("/document/delete/9"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/document/list"));
        verify(documentService, never()).deleteDocument(anyInt());
    }
}
