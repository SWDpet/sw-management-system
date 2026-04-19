package com.swmanager.system.util;

import com.swmanager.system.domain.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 사용자 권한 요약 뱃지 빌더.
 *
 * 스프린트 6 (2026-04-19) — admin-user-list.html 목록 "권한 요약" 컬럼 용.
 *
 * Thymeleaf 3.1 의 T(...) 정적 호출 금지에 따라 Spring Bean 으로 등록.
 * Template 에서 ${@authSummary.summarize(u, 4)} 로 호출.
 *
 * 10 개 권한 필드 중 NONE 이 아닌 것만 뱃지 표시.
 * 약어 매핑은 FR-4-C 참조.
 */
@Component("authSummary")
public class AuthSummary {

    public static class Badge {
        public final String label;
        public final String cssClass; // "view" | "edit"

        public Badge(String label, String cssClass) {
            this.label = label;
            this.cssClass = cssClass;
        }

        // Thymeleaf 접근용
        public String getLabel() { return label; }
        public String getCssClass() { return cssClass; }
    }

    public static class Result {
        public final List<Badge> list;
        public final int moreCount;

        public Result(List<Badge> list, int moreCount) {
            this.list = list;
            this.moreCount = moreCount;
        }

        public List<Badge> getList() { return list; }
        public int getMoreCount() { return moreCount; }
    }

    /**
     * 각 필드와 짧은 라벨 매핑.
     * (필드 접근자는 getter 로, 약어 라벨은 FR-4-C 에 맞춤)
     */
    private static final String[][] FIELDS = {
        {"authDashboard",   "대시"},
        {"authProject",     "사업"},
        {"authPerson",      "담당"},
        {"authInfra",       "서버"},
        {"authLicense",     "라이"},
        {"authQuotation",   "견적"},
        {"authWorkPlan",    "업무"},
        {"authDocument",    "문서"},
        {"authContract",    "계약"},
        {"authPerformance", "성과"}
    };

    /**
     * 10개 권한을 스캔하여 NONE 이 아닌 것만 뱃지 생성. max 초과분은 moreCount.
     */
    public Result summarize(User u, int max) {
        if (u == null) return new Result(Collections.emptyList(), 0);
        List<Badge> all = new ArrayList<>();
        for (String[] pair : FIELDS) {
            String field = pair[0];
            String label = pair[1];
            String value = readAuth(u, field);
            if (value == null || "NONE".equals(value) || value.isEmpty()) continue;
            String suffix;
            String cssClass;
            if ("EDIT".equals(value)) {
                suffix = "E"; cssClass = "edit";
            } else if ("VIEW".equals(value)) {
                suffix = "V"; cssClass = "view";
            } else {
                continue; // 알 수 없는 값 무시
            }
            all.add(new Badge(label + suffix, cssClass));
        }
        if (max <= 0 || all.size() <= max) {
            return new Result(all, 0);
        }
        List<Badge> trimmed = new ArrayList<>(all.subList(0, max));
        return new Result(trimmed, all.size() - max);
    }

    private String readAuth(User u, String field) {
        // 필드별 직접 getter 호출 (리플렉션 회피)
        switch (field) {
            case "authDashboard":   return u.getAuthDashboard();
            case "authProject":     return u.getAuthProject();
            case "authPerson":      return u.getAuthPerson();
            case "authInfra":       return u.getAuthInfra();
            case "authLicense":     return u.getAuthLicense();
            case "authQuotation":   return u.getAuthQuotation();
            case "authWorkPlan":    return u.getAuthWorkPlan();
            case "authDocument":    return u.getAuthDocument();
            case "authContract":    return u.getAuthContract();
            case "authPerformance": return u.getAuthPerformance();
            default: return null;
        }
    }
}
