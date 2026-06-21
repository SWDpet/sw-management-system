package com.swmanager.system.controller;

import com.swmanager.system.dto.UserLightDto;
import com.swmanager.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 경량 조회 API — 드롭다운 등 화면 컴포넌트용 (S3 qt-remarks-users-link).
 *
 * 마스킹 정책: 본 API 는 user_id / userid / username / dept_nm / position_title / enabled 만 반환.
 * 마스킹 대상(tel/mobile/email)은 미반환 (분리 원칙).
 */
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 활성 + 비활성 사용자 모두 (드롭다운용).
     *  - R6 완화: 비활성 사용자 참조 패턴 편집 시 유실 방지.
     *  - 비활성은 라벨에 (비활성) 표기를 위해 enabled 필드 동봉.
     */
    @GetMapping("/all-with-disabled")
    public List<UserLightDto> getAllWithDisabled() {
        return userRepository.findAll().stream()
                .map(UserLightDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 활성 사용자만 (선택 사항).
     */
    @GetMapping("/active")
    public List<UserLightDto> getActive() {
        return userRepository.findByEnabledTrue().stream()
                .map(UserLightDto::from)
                .collect(Collectors.toList());
    }
}
