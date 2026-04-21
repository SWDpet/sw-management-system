package com.swmanager.system.service;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.domain.AccessLog;
import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.repository.AccessLogRepository;
import com.swmanager.system.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

/**
 * 접근 로그 서비스.
 *
 * S5 access-log-userid-fix (2026-04-20):
 *  - 익명 식별자 단일 상수화: ANONYMOUS_USERID = "anonymousUser"
 *  - Orphan Guard: users 테이블에 없는 userid 는 WARN + anonymousUser 로 fallback
 *  - 화이트리스트(SYSTEM_USERIDS) 는 Guard 통과 (DB 조회 생략)
 */
@Slf4j
@Service
public class LogService {

    /** Spring Security 익명 사용자 표준 식별자 */
    public static final String ANONYMOUS_USERID = "anonymousUser";

    /**
     * Orphan Guard 화이트리스트.
     * 이 값들은 users 테이블에 존재하지 않아도 정상으로 간주 (Guard 통과 + DB 조회 생략).
     */
    public static final Set<String> SYSTEM_USERIDS =
            Set.of(ANONYMOUS_USERID, "system", "scheduler");

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * S9 신규 Enum 경로 — 앞으로 이 오버로드만 사용.
     * action Enum 의 label 한글이 DB 에 저장됨 (기존 데이터와 호환).
     */
    public void log(String menuNm, AccessActionType action, String detail) {
        String label = (action != null) ? action.getLabel() : null;
        logInternal(menuNm, label, detail);
    }

    /**
     * S9 Deprecated — 신규 호출 금지. ArchUnit 게이트로 회귀 차단.
     * 기존 DB action_type 값 호환 + unknown 리터럴 fail-soft 저장.
     *
     * @deprecated S9 (2026-04-21). {@link #log(String, AccessActionType, String)} 사용.
     */
    @Deprecated(since = "S9", forRemoval = false)
    public void log(String menuNm, String actionType, String detail) {
        AccessActionType normalized = AccessActionType.fromKoLabel(actionType);
        if (actionType != null && normalized == null) {
            log.warn("ACCESS_LOG_ACTION_UNKNOWN: raw='{}'", actionType);
        }
        // fail-soft: 매칭된 경우 정규 label 로, 미매칭 시 원본 문자열 그대로 저장
        String label = (normalized != null) ? normalized.getLabel() : actionType;
        logInternal(menuNm, label, detail);
    }

    /** 공통 저장 — Auth/IP/Guard/AccessLog 저장 공통 경로 */
    private void logInternal(String menuNm, String actionLabel, String detail) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes()).getRequest();
            String ip = request.getRemoteAddr();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userid = ANONYMOUS_USERID;
            String username = "";

            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
                userid = user.getUsername();              // CustomUserDetails.getUsername() == userid
                username = user.getUser().getUsername();   // User.getUsername() == 실명
            } else if (auth != null && auth.isAuthenticated()
                    && !ANONYMOUS_USERID.equals(auth.getName())) {
                userid = auth.getName();
            }

            // Orphan Guard — 화이트리스트 통과, 그 외 users 검증 후 fallback
            userid = applyOrphanGuard(userid);

            AccessLog entry = new AccessLog();
            entry.setUserid(userid);
            entry.setUsername(username);
            entry.setIpAddr(ip);
            entry.setMenuNm(menuNm);
            entry.setActionType(actionLabel);
            entry.setActionDetail(detail);

            accessLogRepository.save(entry);

        } catch (Exception e) {
            log.error("로그 저장 실패: {}", e.getMessage());
        }
    }

    /**
     * Orphan Guard: 화이트리스트가 아니고 users 테이블에 존재하지 않는 userid 는
     * WARN 로그 후 ANONYMOUS_USERID 로 강제 변환하여 access_logs 오염 재발 방지.
     *
     * 화이트리스트(anonymousUser/system/scheduler)는 DB 조회 생략하여 핫패스 비용 최소화.
     */
    String applyOrphanGuard(String userid) {
        if (userid == null || userid.isBlank()) {
            return ANONYMOUS_USERID;
        }
        if (SYSTEM_USERIDS.contains(userid)) {
            return userid;
        }
        if (!userRepository.existsByUserid(userid)) {
            log.warn("ACCESS_LOG_USERID_ORPHAN: userid='{}' not in users — writing as {}",
                    userid, ANONYMOUS_USERID);
            return ANONYMOUS_USERID;
        }
        return userid;
    }
}
