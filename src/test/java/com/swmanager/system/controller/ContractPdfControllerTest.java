package com.swmanager.system.controller;

import com.swmanager.system.dto.ContractParseResultDTO;
import com.swmanager.system.integration.AnthropicVisionClient;
import com.swmanager.system.service.contract.ContractPdfService;
import com.swmanager.system.service.contract.parser.ContractParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ContractPdfController 통합 테스트 — T14, T16, T22 + size/type 회귀.
 *
 * Sprint: pdf-contract-autofill (PCA-1)
 * 근거: docs/exec-plans/pdf-contract-autofill.md §6
 *
 * Vision 호출은 MockBean 으로 격리. 실제 Anthropic API 호출 X.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "anthropic.api-key=test-key",
        "pdf.contract.max-size-mb=5"
})
class ContractPdfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContractPdfService contractPdfService;

    @MockBean
    private AnthropicVisionClient anthropicVisionClient;

    @BeforeEach
    void setUp() {
        when(anthropicVisionClient.isConfigured()).thenReturn(true);
    }

    @Test
    @DisplayName("T14 (mock): 정상 PDF 업로드 → 200 + DTO 응답")
    @WithMockUser(roles = "ADMIN")
    void t14_normal_upload() throws Exception {
        ContractParseResultDTO mockDto = new ContractParseResultDTO(
                "강원특별자치도", "양양군",                                      // cityNm, distNm
                "4830000", "51830",                                            // orgCd, distCd
                "양양군청", "양양군수",                                          // orgNm, orgLghNm
                "4", "도시계획정보체계", "UPIS",                                  // bizTypeCd, sysNm, sysNmEn
                "2026년 양양군 도시계획정보체계(UPIS) 유지보수 용역",              // projNm
                "양양군청", "(주)유오인터",                                       // client, contEnt
                LocalDate.of(2026, 2, 13),                                      // contDt
                LocalDate.of(2026, 2, 19),                                      // startDt
                LocalDate.of(2026, 12, 31),                                     // endDt
                18000000L,                                                      // contAmt
                List.<String>of()                                               // failedFields
        );
        when(contractPdfService.parse(any())).thenReturn(mockDto);

        MockMultipartFile file = new MockMultipartFile(
                "file", "yangyang.pdf", "application/pdf", new byte[]{37, 80, 68, 70}  // %PDF
        );

        mockMvc.perform(multipart("/projects/parse-contract-pdf").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityNm").value("강원특별자치도"))
                .andExpect(jsonPath("$.distNm").value("양양군"))
                .andExpect(jsonPath("$.orgNm").value("양양군청"))
                .andExpect(jsonPath("$.orgLghNm").value("양양군수"))
                .andExpect(jsonPath("$.bizTypeCd").value("4"))
                .andExpect(jsonPath("$.sysNmEn").value("UPIS"))
                .andExpect(jsonPath("$.contAmt").value(18000000));
    }

    @Test
    @DisplayName("T22: API 키 미설정 → 503")
    @WithMockUser(roles = "ADMIN")
    void t22_apikey_missing() throws Exception {
        when(anthropicVisionClient.isConfigured()).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile(
                "file", "x.pdf", "application/pdf", new byte[]{37, 80, 68, 70}
        );

        mockMvc.perform(multipart("/projects/parse-contract-pdf").file(file).with(csrf()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("회귀-1: 빈 파일 → 400")
    @WithMockUser(roles = "ADMIN")
    void empty_file() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[]{}
        );

        mockMvc.perform(multipart("/projects/parse-contract-pdf").file(file).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회귀-2: PDF 가 아닌 type → 400")
    @WithMockUser(roles = "ADMIN")
    void wrong_type() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.txt", "text/plain", "hello".getBytes()
        );

        mockMvc.perform(multipart("/projects/parse-contract-pdf").file(file).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("T16 (mock): 손상 PDF (Service 가 ContractParseException) → 422")
    @WithMockUser(roles = "ADMIN")
    void t16_corrupted_pdf() throws Exception {
        when(contractPdfService.parse(any()))
                .thenThrow(new ContractParser.ContractParseException("PDF 로드 실패"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "broken.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/projects/parse-contract-pdf").file(file).with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

    /** CSRF 토큰 추가 (Spring Security 활성 환경) */
    private static org.springframework.test.web.servlet.request.RequestPostProcessor csrf() {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf();
    }
}
