package com.swmanager.system.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

/**
 * Anthropic Claude Vision API 호출 클라이언트.
 *
 * Sprint: pdf-contract-autofill (PCA-1, 2026-05-04)
 * 근거: docs/exec-plans/pdf-contract-autofill.md §1 Step 4
 *
 * 책임:
 *  • 다중 PNG 이미지 + 추출 프롬프트 → Anthropic Messages API 호출
 *  • 응답 텍스트 추출 (content[0].text)
 *  • API 키 미설정 시 isConfigured()=false → 호출 측에서 PDF 업로드 비활성화 (NFR-5)
 *
 * 보안:
 *  • API 키는 환경변수 ANTHROPIC_API_KEY (NFR-9 외부 전송 동의)
 *  • 이미지는 base64 인코딩, HTTPS 전송. 호출 후 byte[] 즉시 GC 대상 (NFR-8)
 */
@Component
public class AnthropicVisionClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicVisionClient.class);
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";

    private final String apiKey;
    private final String model;
    private final long timeoutMs;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicVisionClient(
            @Value("${anthropic.api-key:}") String apiKey,
            @Value("${anthropic.model:claude-haiku-4-5}") String model,
            @Value("${pdf.contract.api-timeout-ms:30000}") long timeoutMs) {
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutMs = timeoutMs;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /** API 키가 설정되어 있는지 (Thymeleaf th:if 에서 PDF 버튼 표시 여부 결정용) */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * 다중 PNG 이미지를 비전 모델에 전송하여 텍스트 추출.
     *
     * @param pngImages 페이지별 PNG byte[]
     * @param extractionPrompt 추출 지시 프롬프트
     * @return 모델 응답 텍스트
     * @throws IllegalStateException API 키 미설정 시
     * @throws AnthropicApiException API 호출 실패 시
     */
    public String extractFromImages(List<byte[]> pngImages, String extractionPrompt) {
        if (!isConfigured()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY 환경변수가 설정되지 않았습니다");
        }
        if (pngImages == null || pngImages.isEmpty()) {
            throw new IllegalArgumentException("이미지가 비어있습니다");
        }

        String requestBody = buildRequestBody(pngImages, extractionPrompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("x-api-key", apiKey)
                .header("anthropic-version", API_VERSION)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body();

            if (status < 200 || status >= 300) {
                log.warn("Anthropic API 비정상 응답: status={} body={}", status, truncate(body, 500));
                throw new AnthropicApiException("Anthropic API HTTP " + status + ": " + truncate(body, 200));
            }

            return parseResponseText(body);
        } catch (java.io.IOException e) {
            log.warn("Anthropic API IO 오류", e);
            throw new AnthropicApiException("Anthropic API 호출 실패: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnthropicApiException("Anthropic API 호출 중단", e);
        }
    }

    /** Messages API 요청 body 생성 (text + image 콘텐츠 배열) */
    private String buildRequestBody(List<byte[]> pngImages, String prompt) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", model);
            root.put("max_tokens", 4096);

            ArrayNode messages = root.putArray("messages");
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");

            ArrayNode contentArr = userMsg.putArray("content");

            // 이미지 먼저 (모델이 이미지를 본 후 텍스트 지시 따르도록)
            for (byte[] img : pngImages) {
                ObjectNode imgNode = contentArr.addObject();
                imgNode.put("type", "image");
                ObjectNode source = imgNode.putObject("source");
                source.put("type", "base64");
                source.put("media_type", "image/png");
                source.put("data", Base64.getEncoder().encodeToString(img));
            }

            // 텍스트 프롬프트
            ObjectNode textNode = contentArr.addObject();
            textNode.put("type", "text");
            textNode.put("text", prompt);

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new AnthropicApiException("요청 body 생성 실패", e);
        }
    }

    /** 응답 JSON 에서 content[0].text 추출 */
    private String parseResponseText(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode content = root.path("content");
            if (!content.isArray() || content.isEmpty()) {
                throw new AnthropicApiException("응답에 content 배열이 비어있음: " + truncate(body, 200));
            }
            JsonNode text = content.get(0).path("text");
            if (text.isMissingNode() || !text.isTextual()) {
                throw new AnthropicApiException("응답 content[0].text 누락: " + truncate(body, 200));
            }
            return text.asText();
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new AnthropicApiException("응답 JSON 파싱 실패", e);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    /** Anthropic API 호출 실패 예외 */
    public static class AnthropicApiException extends RuntimeException {
        public AnthropicApiException(String msg) { super(msg); }
        public AnthropicApiException(String msg, Throwable cause) { super(msg, cause); }
    }
}
