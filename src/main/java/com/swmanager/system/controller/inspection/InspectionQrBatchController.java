package com.swmanager.system.controller.inspection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.dto.inspection.InspectionQrBatchRequest;
import com.swmanager.system.dto.inspection.InspectionQrBatchResponse;
import com.swmanager.system.service.inspection.InspectionQrBatchService;
import com.swmanager.system.service.inspection.QrBatchPayloadAdapter;
import com.swmanager.system.service.inspection.SiteNotMappedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 점검 QR batch 업로드 — PWA decoder.js 가 디코드한 payload 를 받아
 * inspect_report DRAFT + inspect_check_result rows + inspect_qr_batch 원본 저장.
 *
 * 기획서: docs/product-specs/inspection-qr-batch.md v1
 * 개발계획서: docs/exec-plans/inspection-qr-batch.md v1 (Step 6)
 *
 * 인증: 세션 쿠키 (SecurityConfig.filterChain 의 /api/** CSRF 면제 정책 자동 적용).
 *
 * Schema adapter: agent-windows 의 per-tier 페이로드(`payload.tier` 단수 + 상위 `i`) 도
 *   받아서 multi-tier 형태로 normalize 후 검증. {@link QrBatchPayloadAdapter} 참조.
 */
@Slf4j
@RestController
@RequestMapping("/api/inspection")
@RequiredArgsConstructor
public class InspectionQrBatchController {

    private final InspectionQrBatchService service;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping("/qr-batch")
    public ResponseEntity<InspectionQrBatchResponse> upload(
            @RequestBody JsonNode rawBody,
            @AuthenticationPrincipal CustomUserDetails me) {

        QrBatchPayloadAdapter.normalize(rawBody);

        InspectionQrBatchRequest req;
        try {
            req = objectMapper.treeToValue(rawBody, InspectionQrBatchRequest.class);
        } catch (Exception e) {
            log.warn("inspection-qr-batch malformed body reason={}", e.toString());
            throw new IllegalArgumentException("Malformed QR batch body");
        }

        Set<ConstraintViolation<InspectionQrBatchRequest>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Long userId = (me != null && me.getUser() != null) ? me.getUser().getUserSeq() : null;
        InspectionQrBatchResponse res = service.upload(req, userId);
        return ResponseEntity.ok(res);
    }

    // ── 예외 매핑 ────────────────────────────────────────────────────────────

    @ExceptionHandler(SiteNotMappedException.class)
    public ResponseEntity<Map<String, Object>> handleSiteNotMapped(SiteNotMappedException e) {
        log.warn("inspection-qr-batch fail reason=site_not_mapped site={}", e.getSiteCode());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "site_not_mapped");
        body.put("site", e.getSiteCode());
        body.put("hint", "사업관리 화면에서 해당 사이트의 site_code 를 설정하세요");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }
}
