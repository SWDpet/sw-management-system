package com.swmanager.system.service;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
        // DB 조회 1회만 수행
        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음: " + userid));

        // 조회된 User 객체로 잠금 여부 확인 (추가 DB 조회 없음)
        if (loginAttemptService.isLocked(user)) {
            long remaining = loginAttemptService.getRemainingLockMinutes(user);
            log.warn("잠긴 계정 로그인 시도 차단 - userid: {}, 남은시간: {}분", userid, remaining);
            throw new LockedException("계정이 잠겼습니다. " + remaining + "분 후 다시 시도해주세요.");
        }

        return new CustomUserDetails(user);
    }
}
