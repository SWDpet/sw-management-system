package com.swmanager.system.quotation.service;

import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 비고 패턴 컨텐츠의 placeholder 치환 유틸 (S3 qt-remarks-users-link).
 *
 * 정의된 placeholder:
 *  - {username}        → users.username (실명)
 *  - {dept_nm}         → users.deptNm   (부서명)
 *  - {position_title}  → users.positionTitle (직급)
 *
 * 치환 규칙:
 *  - userId == null   → content 원문 반환 (placeholder 미치환, fallback)
 *  - users 미존재     → content 원문 반환
 *  - users 존재       → 정의된 placeholder 만 치환, NULL 컬럼은 빈 문자열
 *  - 미정의 placeholder ({phone} 등) → 자동 보존 (Pattern 미매칭)
 *
 * 본 스프린트 스코프: 이름·부서·직급만. 전화/이메일은 후속 S3-B 에서 다룸.
 */
@Component
public class RemarksRenderer {

    private static final Pattern PLACEHOLDER =
            Pattern.compile("\\{(username|dept_nm|position_title)\\}");

    private final UserRepository userRepository;

    public RemarksRenderer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String render(String content, Long userId) {
        if (content == null || content.isEmpty()) return content;
        if (userId == null) return content;

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return content;

        User u = userOpt.get();
        Matcher m = PLACEHOLDER.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String token = m.group(1);
            String value = switch (token) {
                case "username"        -> nullToEmpty(u.getUsername());
                case "dept_nm"         -> nullToEmpty(u.getDeptNm());
                case "position_title"  -> nullToEmpty(u.getPositionTitle());
                default                -> m.group();
            };
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }
}
