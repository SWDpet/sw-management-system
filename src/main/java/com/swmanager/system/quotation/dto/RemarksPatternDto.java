package com.swmanager.system.quotation.dto;

import com.swmanager.system.quotation.domain.RemarksPattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 비고 패턴 응답 DTO (S3 qt-remarks-users-link).
 *
 * 추가 필드:
 *  - userId            : 담당자 user_id (편집 모달에서 드롭다운 선택값)
 *  - renderedContent   : RemarksRenderer 가 placeholder 치환한 결과 (textarea 자동 입력용)
 *
 * JS는 renderedContent 우선 사용, 미존재 시 content fallback.
 */
@Getter @Setter
public class RemarksPatternDto {

    private Long patternId;
    private String patternName;
    private String content;          // 원문 (편집/저장용 — placeholder 포함)
    private String renderedContent;  // 치환 결과 (사용자에게 보여줄 최종 텍스트)
    private Integer sortOrder;
    private Long userId;             // 담당자 user_id (NULL 가능)

    public static RemarksPatternDto from(RemarksPattern entity) {
        RemarksPatternDto dto = new RemarksPatternDto();
        dto.patternId = entity.getPatternId();
        dto.patternName = entity.getPatternName();
        dto.content = entity.getContent();
        dto.sortOrder = entity.getSortOrder();
        dto.userId = entity.getUserId();
        return dto;
    }
}
