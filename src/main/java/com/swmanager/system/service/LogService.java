package com.swmanager.system.service;

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

    public void log(String menuNm, String actionType, String detail) {
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
            entry.setActionType(actionType);
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
