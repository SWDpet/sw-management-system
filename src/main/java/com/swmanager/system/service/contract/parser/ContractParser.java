package com.swmanager.system.service.contract.parser;

import com.swmanager.system.dto.ContractParseResultDTO;

/**
 * 전자계약서 PDF 양식별 파서 인터페이스.
 *
 * Sprint: pdf-contract-autofill (PCA-1, 2026-05-04)
 * 근거: docs/exec-plans/pdf-contract-autofill.md §1 Step 5 (FR-13)
 *
 * 1차 구현체: {@link LocalGovContractParser} (지방자치단체 표준 양식, iText 4.2.0).
 * 향후 추가 가능: LhContractParser, MoisContractParser 등 (sprint v2).
 */
public interface ContractParser {

    /**
     * 본 파서가 주어진 PDF 양식을 처리할 수 있는지 판단.
     * 1차는 1개 구현체뿐이라 항상 true 반환. 다중 구현체 시 양식 식별 로직 추가.
     */
    boolean supports(byte[] pdfBytes);

    /**
     * PDF 를 파싱해 폼 자동 채움용 DTO 반환.
     *
     * @param pdfBytes PDF 파일 본문 (영구 저장 X — 호출 후 GC)
     * @return ContractParseResultDTO. 매핑 실패 필드는 null + failedFields 에 포함.
     * @throws ContractParseException 파싱 자체가 불가능한 경우 (손상·암호 등)
     */
    ContractParseResultDTO parse(byte[] pdfBytes);

    /** 양식 식별자 (로깅·디버그용) */
    String formatId();

    /** 파싱 실패 예외 */
    class ContractParseException extends RuntimeException {
        public ContractParseException(String msg) { super(msg); }
        public ContractParseException(String msg, Throwable cause) { super(msg, cause); }
    }
}
