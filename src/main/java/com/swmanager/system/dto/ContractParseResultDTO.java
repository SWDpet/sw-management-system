package com.swmanager.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * 전자계약서 PDF 파싱 결과 DTO.
 *
 * Sprint: pdf-contract-autofill (PCA-1, 2026-05-04)
 * 근거: docs/exec-plans/pdf-contract-autofill.md §1 Step 6
 *
 * 매핑 표 (기획서 §3, 17 필드 중 16개):
 *  • 행정 ②: cityNm, distNm, orgCd, distCd, orgNm, orgLghNm
 *  • 사업 식별 ③: bizTypeCd, sysNm, sysNmEn  (bizCat 자동 매핑 X — 사용자 수동)
 *  • 계약정보 ④: projNm, client, contEnt
 *  • 일정 ⑤: contDt, startDt, endDt
 *  • 금액 ⑥: contAmt
 *  • personId → contDept 는 PDF 무관 (FR-11 별도 JS)
 *
 * failedFields: 매핑 실패한 필드명 목록 → 클라이언트 토스트 알림 (FR-9).
 */
public record ContractParseResultDTO(
        // 행정 (②)
        String cityNm,
        String distNm,
        String orgCd,
        String distCd,
        String orgNm,
        String orgLghNm,

        // 사업 식별 (③)
        String bizTypeCd,    // cont_frm_mst.cd ("4" = 유상)
        String sysNm,        // sys_mst.nm
        String sysNmEn,      // sys_mst.cd

        // 계약 정보 (④)
        String projNm,
        String client,
        String contEnt,

        // 일정 (⑤) — ISO-8601 직렬화
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate contDt,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDt,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDt,

        // 금액 (⑥)
        Long contAmt,

        // 매핑 실패 필드명 (FR-9)
        List<String> failedFields
) {
}
