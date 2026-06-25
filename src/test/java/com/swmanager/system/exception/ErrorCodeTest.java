package com.swmanager.system.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** ErrorCode enum 단위테스트 (커버리지 상향 beyond-A, 순수). */
class ErrorCodeTest {

    @Test
    void representativeCodes_haveExpectedStatusCodeMessage() {
        assertThat(ErrorCode.INVALID_INPUT_VALUE.getStatus()).isEqualTo(400);
        assertThat(ErrorCode.INVALID_INPUT_VALUE.getCode()).isEqualTo("G001");
        assertThat(ErrorCode.INVALID_INPUT_VALUE.getMessage()).isNotBlank();

        assertThat(ErrorCode.ACCESS_DENIED.getStatus()).isEqualTo(403);
        assertThat(ErrorCode.ACCESS_DENIED.getCode()).isEqualTo("A003");

        assertThat(ErrorCode.RESOURCE_NOT_FOUND.getStatus()).isEqualTo(404);
        assertThat(ErrorCode.RESOURCE_NOT_FOUND.getCode()).isEqualTo("R001");

        assertThat(ErrorCode.DUPLICATE_PROJECT.getStatus()).isEqualTo(409);
        assertThat(ErrorCode.DUPLICATE_PROJECT.getCode()).isEqualTo("D003");

        assertThat(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).isEqualTo(500);
        assertThat(ErrorCode.INTERNAL_SERVER_ERROR.getCode()).isEqualTo("S001");
    }
}
