package com.swmanager.system.util;

import com.swmanager.system.domain.SysMst;
import com.swmanager.system.repository.SysMstRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 사업명 → 시스템명·SW사업형태 매핑 유틸.
 *
 * Sprint: pdf-contract-autofill (PCA-1, 2026-05-04)
 * 근거: docs/product-specs/pdf-contract-autofill.md §4-2 + §3 (FR-5, FR-6)
 *
 * 매핑 규칙:
 *  • 시스템 영문명: 사업명에서 괄호 안 영문 약어 추출
 *      - 정규식: \(([A-Z][A-Z0-9]+)\)   예: "(UPIS)" → "UPIS"
 *      - sys_mst.cd 와 정확 매칭 시 sysNm = nm, sysNmEn = cd
 *      - 매칭 실패 시 두 필드 모두 null
 *  • SW사업형태 (cont_frm_mst.cd):
 *      - 사업명에 "유지보수" 포함 → "4" (유상)
 *      - 그 외 키워드는 자동 매핑 없음 (사용자 확정 §4-2)
 *
 * 단위 테스트: ProjectNameParserTest (T10~T13).
 */
@Component
public class ProjectNameParser {

    private final SysMstRepository sysMstRepository;

    /** 괄호 안 영문 약어 패턴 — 첫 글자 대문자 + 이후 대문자/숫자 1자 이상 */
    private static final Pattern ENG_PATTERN = Pattern.compile("\\(([A-Z][A-Z0-9]+)\\)");

    /** "유지보수" → cont_frm_mst cd "4" (유상) */
    private static final String MAINTENANCE_KEYWORD = "유지보수";
    private static final String BIZ_TYPE_PAID = "4";

    public ProjectNameParser(SysMstRepository sysMstRepository) {
        this.sysMstRepository = sysMstRepository;
    }

    /** 파싱 결과: (시스템 한글명, 시스템 영문명, SW사업형태 cd). 미매칭 시 각 필드 null. */
    public record ParseResult(String sysNm, String sysNmEn, String bizTypeCd) {}

    /**
     * 사업명 파싱.
     *
     * @param projNm 사업명 (예: "2026년 양양군 도시계획정보체계(UPIS) 유지보수 용역")
     * @return ParseResult — 미매칭 필드는 null. 입력 자체가 null/blank 면 모두 null.
     */
    public ParseResult parse(String projNm) {
        if (projNm == null || projNm.isBlank()) {
            return new ParseResult(null, null, null);
        }

        // [1] 시스템 영문 약어 추출 + sys_mst 매칭
        String sysNm = null;
        String sysNmEn = null;
        Matcher m = ENG_PATTERN.matcher(projNm);
        if (m.find()) {
            String code = m.group(1);
            Optional<SysMst> match = sysMstRepository.findById(code);
            if (match.isPresent()) {
                sysNm = match.get().getNm();
                sysNmEn = code;
            }
            // 매칭 실패 시 두 필드 모두 null (사용자 수동 입력 유도)
        }

        // [2] SW사업형태 키워드 매핑 — "유지보수" 만
        String bizTypeCd = projNm.contains(MAINTENANCE_KEYWORD) ? BIZ_TYPE_PAID : null;

        return new ParseResult(sysNm, sysNmEn, bizTypeCd);
    }
}
