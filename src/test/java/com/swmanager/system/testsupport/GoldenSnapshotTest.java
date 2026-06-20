package com.swmanager.system.testsupport;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * GoldenSnapshot net 메커니즘 증명: record → 동일(키순서 무관) 통과 → 변경 시 실패.
 */
class GoldenSnapshotTest {

    @Test
    void recordsThenMatches_andDetectsRegression() {
        String name = "smoke-net";

        // 1) 박제(첫 실행이면 기록) + 동일 입력 매치
        GoldenSnapshot.assertMatches(name, "{\"a\":1,\"b\":2}");

        // 2) 키 순서만 다른 동일 의미 → 통과 (canonicalizer 덕)
        GoldenSnapshot.assertMatches(name, "{\"b\":2,\"a\":1}");

        // 3) 값이 바뀌면 ("되던 게 안 되는 것") → AssertionError 로 잡는다
        assertThatThrownBy(() -> GoldenSnapshot.assertMatches(name, "{\"a\":1,\"b\":3}"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("golden 스냅샷 불일치");
    }

    @Test
    void volatileKeysMasked_soGeneratedIdsDontBreakNet() {
        String name = "smoke-volatile";
        // 생성 ID 가 달라도 net 이 안 깨져야 함
        GoldenSnapshot.assertMatches(name, "{\"success\":true,\"staff_id\":1}", java.util.Set.of("staff_id"));
        GoldenSnapshot.assertMatches(name, "{\"success\":true,\"staff_id\":99999}", java.util.Set.of("staff_id"));
        // 비휘발 값이 바뀌면 잡힌다
        assertThatThrownBy(() -> GoldenSnapshot.assertMatches(name,
                "{\"success\":false,\"staff_id\":1}", java.util.Set.of("staff_id")))
                .isInstanceOf(AssertionError.class);
    }
}
