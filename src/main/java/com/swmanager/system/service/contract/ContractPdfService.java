package com.swmanager.system.service.contract;

import com.swmanager.system.dto.ContractParseResultDTO;
import com.swmanager.system.service.contract.parser.ContractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 전자계약서 PDF 자동 채움 서비스 (FR-3 진입점).
 *
 * Sprint: pdf-contract-autofill (PCA-1, 2026-05-04)
 * 근거: docs/exec-plans/pdf-contract-autofill.md §1 Step 6
 *
 * 책임:
 *  • 등록된 ContractParser 들 중 supports() 가 true 인 첫 번째에게 위임
 *  • 1차는 LocalGovContractParser 1개만 등록됨
 *  • 향후 다중 파서 시 supports() 우선순위는 등록 순서 (Spring @Order 추가 권장)
 *
 * 보안 (NFR-8): pdfBytes 는 메서드 파라미터로만 흐르고 영구 저장 X.
 */
@Service
public class ContractPdfService {

    private static final Logger log = LoggerFactory.getLogger(ContractPdfService.class);

    private final List<ContractParser> parsers;

    public ContractPdfService(List<ContractParser> parsers) {
        this.parsers = parsers;
    }

    /**
     * PDF 파싱 진입점.
     *
     * @param pdfBytes PDF 본문 (Controller 에서 multipart 로 받음, 영구 저장 X)
     * @return ContractParseResultDTO (실패 필드는 null + failedFields 집계)
     * @throws ContractParser.ContractParseException 매칭 파서 없거나 파싱 실패
     */
    public ContractParseResultDTO parse(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new ContractParser.ContractParseException("PDF 본문이 비어있습니다");
        }

        for (ContractParser parser : parsers) {
            if (parser.supports(pdfBytes)) {
                log.info("Using parser: {}", parser.formatId());
                return parser.parse(pdfBytes);
            }
        }
        throw new ContractParser.ContractParseException(
                "지원하지 않는 PDF 양식입니다 (1차는 지방자치단체 표준 양식만 지원)"
        );
    }
}
