package com.swmanager.system.service;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.repository.AccessLogRepository;
import com.swmanager.system.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;

/**
 * S9 비오탐 샘플 — Enum 오버로드 호출 가능성만 보장.
 *
 * ArchUnit 규칙(LogServiceUsageArchTest)이 Enum 오버로드까지 잘못 잡으면
 * 본 테스트 호출 지점도 함께 실패 → 양방향 신호로 비오탐 검증.
 * "모든 컨트롤러가 Enum 오버로드를 호출해야 함" 같은 강제 규칙은 아님.
 */
@ExtendWith(MockitoExtension.class)
class LogServiceEnumOverloadSmokeTest {

    @Mock AccessLogRepository accessLogRepository;
    @Mock UserRepository userRepository;

    @InjectMocks LogService logService;

    @BeforeEach
    void setUp() {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void enum_overload_compiles_and_executes_without_archunit_false_positive() {
        logService.log(MenuName.QUOTATION, AccessActionType.VIEW,
                "archunit-false-positive-check");
        verify(accessLogRepository, atMostOnce()).save(any());
    }
}
