package com.swmanager.system.service.contract.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.dto.ContractParseResultDTO;
import com.swmanager.system.integration.AnthropicVisionClient;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.util.OrgNameResolver;
import com.swmanager.system.util.ProjectNameParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 지방자치단체 표준 양식 (iText 4.2.0) 전자계약서 파서.
 *
 * Sprint: pdf-contract-autofill (PCA-1, 2026-05-04)
 * 근거: docs/exec-plans/pdf-contract-autofill.md §1 Step 5
 *
 * 처리 흐름:
 *  1. PDFBox 로 페이지 → BufferedImage → PNG byte[] 변환 (DPI 144)
 *  2. AnthropicVisionClient 호출 (이미지 + 추출 프롬프트)
 *  3. JSON 응답 파싱 → 라벨별 값 추출
 *  4. OrgNameResolver, ProjectNameParser 활용한 후처리
 *  5. SigunguCode 매칭 → orgCd, distCd
 *  6. ContractParseResultDTO 반환
 *
 * 추출 프롬프트는 JSON 형식 응답을 강제. 응답이 JSON 이 아니면 ContractParseException.
 */
@Component
public class LocalGovContractParser implements ContractParser {

    private static final Logger log = LoggerFactory.getLogger(LocalGovContractParser.class);
    private static final float RENDER_DPI = 144f;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final List<String> ALL_FIELDS = Arrays.asList(
            "cityNm", "distNm", "orgCd", "distCd", "orgNm", "orgLghNm",
            "bizTypeCd", "sysNm", "sysNmEn",
            "projNm", "client", "contEnt",
            "contDt", "startDt", "endDt", "contAmt"
    );

    /** 추출 프롬프트 — JSON 형식 응답 강제 */
    private static final String EXTRACTION_PROMPT = """
        이 이미지는 한국 지방자치단체의 전자 용역계약서입니다.
        다음 필드를 추출해 JSON 객체로 반환하세요. 다른 설명·마크다운 없이 JSON만 출력.

        - sido          : 발주처(계약자) 기관명에서 시도부분 (예: "강원특별자치도", "서울특별시", "울산광역시")
        - sigungu       : 발주처 기관명에서 시군구부분 (예: "양양군", "강남구", "북구"). 광역 자체발주면 null.
        - projName      : 사업명/계약건명 (예: "2026년 양양군 도시계획정보체계(UPIS) 유지보수 용역")
        - contractor    : 계약상대자(수급인) 상호 (예: "(주)유오인터")
        - contractDate  : 계약일자 (yyyy-MM-dd 형식)
        - startDate     : 착수일자 (yyyy-MM-dd 형식)
        - endDate       : 총완수일자/종료일 (yyyy-MM-dd 형식)
        - contractAmount: 계약금액 (숫자만, 원 단위. 예: 18000000)

        값을 찾을 수 없는 필드는 null. JSON 외 텍스트 일체 금지.
        """;

    private final AnthropicVisionClient visionClient;
    private final SigunguCodeRepository sigunguCodeRepository;
    private final OrgNameResolver orgNameResolver;
    private final ProjectNameParser projectNameParser;

    public LocalGovContractParser(AnthropicVisionClient visionClient,
                                  SigunguCodeRepository sigunguCodeRepository,
                                  OrgNameResolver orgNameResolver,
                                  ProjectNameParser projectNameParser) {
        this.visionClient = visionClient;
        this.sigunguCodeRepository = sigunguCodeRepository;
        this.orgNameResolver = orgNameResolver;
        this.projectNameParser = projectNameParser;
    }

    @Override
    public boolean supports(byte[] pdfBytes) {
        return pdfBytes != null && pdfBytes.length > 0;
    }

    @Override
    public String formatId() {
        return "local-gov-itext-4.2";
    }

    @Override
    public ContractParseResultDTO parse(byte[] pdfBytes) {
        if (!supports(pdfBytes)) {
            throw new ContractParseException("PDF 본문이 비어있습니다");
        }

        // [1] PDF → PNG 페이지 이미지
        List<byte[]> pageImages = renderPagesToPng(pdfBytes);
        log.info("PDF rendered to {} page image(s)", pageImages.size());

        // [2] Anthropic Vision 호출
        String visionResponse = visionClient.extractFromImages(pageImages, EXTRACTION_PROMPT);
        log.debug("Vision response: {}", visionResponse);

        // [3] JSON 응답 파싱
        JsonNode root = parseVisionJson(visionResponse);

        // [4] 1차 추출
        String sido = textOrNull(root, "sido");
        String sigungu = textOrNull(root, "sigungu");
        String projName = textOrNull(root, "projName");
        String contractor = textOrNull(root, "contractor");
        LocalDate contractDate = parseDate(textOrNull(root, "contractDate"));
        LocalDate startDate = parseDate(textOrNull(root, "startDate"));
        LocalDate endDate = parseDate(textOrNull(root, "endDate"));
        Long contractAmount = parseAmount(root.path("contractAmount"));

        // [5] 후처리: 행정구역 → 기관명·기관장
        OrgNameResolver.OrgInfo orgInfo = orgNameResolver.resolve(sido, sigungu);
        String orgNm = orgInfo != null ? orgInfo.orgNm() : null;
        String orgLghNm = orgInfo != null ? orgInfo.orgLghNm() : null;
        String client = orgNm; // 발주처 = 기관명 (FR-7)

        // [6] 후처리: SigunguCode 매칭 → orgCd, distCd
        String orgCd = null;
        String distCd = null;
        if (sido != null) {
            List<SigunguCode> sidoMatches = sigunguCodeRepository.findBySidoNm(sido);
            Optional<SigunguCode> best = pickBest(sidoMatches, sigungu);
            if (best.isPresent()) {
                orgCd = best.get().getInsttC();
                distCd = best.get().getAdmSectC();
            }
        }

        // [7] 후처리: 사업명 파싱 → sysNm/sysNmEn/bizTypeCd
        ProjectNameParser.ParseResult pr = projectNameParser.parse(projName);

        // [8] 실패 필드 집계
        ContractParseResultDTO dto = new ContractParseResultDTO(
                sido, sigungu, orgCd, distCd, orgNm, orgLghNm,
                pr.bizTypeCd(), pr.sysNm(), pr.sysNmEn(),
                projName, client, contractor,
                contractDate, startDate, endDate, contractAmount,
                List.of() // 임시
        );
        List<String> failedFields = computeFailedFields(dto);

        return new ContractParseResultDTO(
                sido, sigungu, orgCd, distCd, orgNm, orgLghNm,
                pr.bizTypeCd(), pr.sysNm(), pr.sysNmEn(),
                projName, client, contractor,
                contractDate, startDate, endDate, contractAmount,
                failedFields
        );
    }

    // === Rendering ===

    private List<byte[]> renderPagesToPng(byte[] pdfBytes) {
        try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            List<byte[]> images = new ArrayList<>(pageCount);
            for (int i = 0; i < pageCount; i++) {
                BufferedImage img = renderer.renderImageWithDPI(i, RENDER_DPI);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "PNG", baos);
                images.add(baos.toByteArray());
            }
            return images;
        } catch (IOException e) {
            throw new ContractParseException("PDF 로드/렌더링 실패: " + e.getMessage(), e);
        }
    }

    // === Response parsing ===

    private JsonNode parseVisionJson(String response) {
        if (response == null || response.isBlank()) {
            throw new ContractParseException("Vision 응답이 비어있습니다");
        }
        // 모델이 ```json ... ``` 코드블록으로 감쌀 수 있으므로 stripping
        String cleaned = response.trim();
        if (cleaned.startsWith("```")) {
            int firstNl = cleaned.indexOf('\n');
            if (firstNl > 0) cleaned = cleaned.substring(firstNl + 1);
            if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.trim();
        }
        try {
            return MAPPER.readTree(cleaned);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new ContractParseException("Vision 응답이 JSON 형식이 아닙니다: " + cleaned.substring(0, Math.min(cleaned.length(), 200)), e);
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode n = node.path(field);
        if (n.isMissingNode() || n.isNull()) return null;
        String s = n.asText().trim();
        return s.isEmpty() ? null : s;
    }

    private static LocalDate parseDate(String s) {
        if (s == null) return null;
        // 다양한 포맷 허용
        String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd"};
        for (String p : patterns) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern(p));
            } catch (DateTimeParseException ignored) {}
        }
        log.warn("날짜 파싱 실패: {}", s);
        return null;
    }

    private static Long parseAmount(JsonNode n) {
        if (n == null || n.isMissingNode() || n.isNull()) return null;
        if (n.isNumber()) return n.asLong();
        String s = n.asText().replaceAll("[₩,\\s원]", "");
        if (s.isEmpty()) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            log.warn("금액 파싱 실패: {}", s);
            return null;
        }
    }

    /** sigungu (e.g. "양양군") 가 sgg_name 과 일치하는 row 우선. 광역 자체발주면 null sgg 의 row. */
    private static Optional<SigunguCode> pickBest(List<SigunguCode> rows, String sigunguName) {
        if (rows == null || rows.isEmpty()) return Optional.empty();
        if (sigunguName == null || sigunguName.isBlank()) {
            // 광역 자체발주: sgg_name 이 null/blank/광역명자체인 row 찾기
            return rows.stream()
                    .filter(r -> r.getSggNm() == null || r.getSggNm().isBlank())
                    .findFirst()
                    .or(() -> rows.stream().findFirst());
        }
        return rows.stream()
                .filter(r -> sigunguName.equals(r.getSggNm()))
                .findFirst();
    }

    /** 매핑 실패 필드명 집계 (DTO 에서 null 인 필드 — failedFields 자체 제외) */
    private static List<String> computeFailedFields(ContractParseResultDTO dto) {
        List<String> failed = new ArrayList<>();
        if (dto.cityNm() == null) failed.add("cityNm");
        if (dto.distNm() == null && dto.cityNm() != null && !isSelfArea(dto.cityNm())) failed.add("distNm");
        if (dto.orgCd() == null) failed.add("orgCd");
        if (dto.distCd() == null) failed.add("distCd");
        if (dto.orgNm() == null) failed.add("orgNm");
        if (dto.orgLghNm() == null) failed.add("orgLghNm");
        if (dto.bizTypeCd() == null) failed.add("bizType");
        if (dto.sysNm() == null) failed.add("sysNm");
        if (dto.sysNmEn() == null) failed.add("sysNmEn");
        if (dto.projNm() == null) failed.add("projNm");
        if (dto.client() == null) failed.add("client");
        if (dto.contEnt() == null) failed.add("contEnt");
        if (dto.contDt() == null) failed.add("contDt");
        if (dto.startDt() == null) failed.add("startDt");
        if (dto.endDt() == null) failed.add("endDt");
        if (dto.contAmt() == null) failed.add("contAmt");
        return failed;
    }

    /** 광역 자체발주 (시군구가 없는 게 정상) 여부 — distNm 누락을 실패로 잡지 않기 위함 */
    private static boolean isSelfArea(String cityNm) {
        // 단순 휴리스틱: 도/특별시/광역시/특별자치시 자체 발주 시 distNm 없을 수 있음
        return cityNm.endsWith("도") || cityNm.endsWith("특별시") || cityNm.endsWith("광역시");
    }
}
