package com.swmanager.system.controller;

import com.swmanager.system.dto.ContractParseResultDTO;
import com.swmanager.system.integration.AnthropicVisionClient;
import com.swmanager.system.service.contract.ContractPdfService;
import com.swmanager.system.service.contract.parser.ContractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 전자계약서 PDF 업로드 → 자동 채움 결과 반환 컨트롤러.
 *
 * Sprint: pdf-contract-autofill (PCA-1, 2026-05-04)
 * 근거: docs/exec-plans/pdf-contract-autofill.md §1 Step 7
 *
 * 엔드포인트: POST /projects/parse-contract-pdf (multipart/form-data, file=PDF)
 *
 * NFR 충족:
 *  • NFR-3: 5MB size 체크 + .pdf type 체크
 *  • NFR-5: API 키 미설정 시 503 반환
 *  • NFR-8: 파일 본문 영구 저장 X (MultipartFile.getBytes() 만 사용)
 */
@RestController
@RequestMapping("/projects")
public class ContractPdfController {

    private static final Logger log = LoggerFactory.getLogger(ContractPdfController.class);

    private final ContractPdfService service;
    private final AnthropicVisionClient visionClient;
    private final long maxSizeBytes;

    public ContractPdfController(ContractPdfService service,
                                 AnthropicVisionClient visionClient,
                                 @Value("${pdf.contract.max-size-mb:5}") long maxSizeMb) {
        this.service = service;
        this.visionClient = visionClient;
        this.maxSizeBytes = maxSizeMb * 1024L * 1024L;
    }

    @PostMapping("/parse-contract-pdf")
    public ResponseEntity<?> parsePdf(@RequestParam("file") MultipartFile file) {
        // [NFR-5] API 키 미설정
        if (!visionClient.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    Map.of("error", "PDF 자동 입력 기능이 비활성화 상태입니다 (관리자 API 키 설정 필요)")
            );
        }

        // [NFR-3] 빈 파일
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "파일이 비어있습니다")
            );
        }

        // [NFR-3] size 체크
        if (file.getSize() > maxSizeBytes) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                    Map.of("error", "파일 크기가 " + (maxSizeBytes / 1024 / 1024) + "MB 를 초과합니다")
            );
        }

        // [NFR-3] type 체크 — content-type + 파일명 확장자
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        boolean isPdf = (filename != null && filename.toLowerCase().endsWith(".pdf"))
                || "application/pdf".equalsIgnoreCase(contentType);
        if (!isPdf) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "PDF 파일만 업로드 가능합니다")
            );
        }

        try {
            byte[] bytes = file.getBytes();
            ContractParseResultDTO result = service.parse(bytes);
            log.info("PDF parsed: filename={}, failedFields={}",
                    filename, result.failedFields().size());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.warn("PDF read 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "파일 읽기 실패: " + e.getMessage())
            );
        } catch (ContractParser.ContractParseException e) {
            log.warn("PDF 파싱 실패: {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(
                    Map.of("error", "PDF 파싱 실패: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("PDF 자동 입력 처리 중 알 수 없는 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "처리 중 오류 발생: " + e.getMessage())
            );
        }
    }
}
