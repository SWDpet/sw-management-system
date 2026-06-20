package com.swmanager.system.testsupport;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Map&lt;String,Object&gt; 부채 ratchet 게이트 (QUALITY_CHARTER §3-S1, F1).
 *
 * main 자바의 {@code Map<String,Object>} 사용 라인 총량을 baseline 으로 고정하고,
 * <b>증가하면 빌드 실패</b>(= 새 부채 금지), 감소만 허용한다.
 *
 * 갱신(감소 시): {@code GOLDEN_RECORD=1 ./mvnw test -Dtest=MapDebtRatchetTest} 후
 * {@code golden/map-debt-baseline.txt} 커밋.
 */
class MapDebtRatchetTest {

    private static final Path BASELINE = Path.of("src", "test", "resources", "golden", "map-debt-baseline.txt");
    private static final Pattern MAP_OBJ = Pattern.compile("Map<\\s*String\\s*,\\s*Object\\s*>");

    @Test
    void mapStringObject_doesNotIncrease() throws Exception {
        int current = countMapStringObjectLines();

        if (!Files.exists(BASELINE)) {
            if (GoldenSnapshot.isRecordMode()) {
                Files.createDirectories(BASELINE.getParent());
                Files.writeString(BASELINE, current + "\n");
                System.out.println("[MapDebtRatchet] baseline 기록: " + current + " — 커밋 필요");
                return;
            }
            throw new AssertionError("map-debt baseline 없음. GOLDEN_RECORD=1 로 1회 기록 후 커밋.");
        }

        int baseline = Integer.parseInt(Files.readString(BASELINE).strip());

        if (current < baseline && GoldenSnapshot.isRecordMode()) {
            Files.writeString(BASELINE, current + "\n"); // 감소분 tighten
            System.out.println("[MapDebtRatchet] baseline tighten: " + baseline + " → " + current);
            return;
        }

        assertThat(current)
                .as("Map<String,Object> 부채가 baseline(%d)보다 늘었다(%d). 새 코드는 타입 DTO/ApiResult 사용. "
                        + "정당한 감소면 GOLDEN_RECORD=1 로 baseline 갱신.", baseline, current)
                .isLessThanOrEqualTo(baseline);
    }

    private int countMapStringObjectLines() {
        int n = 0;
        for (Path p : MainSources.allJava()) {
            for (String line : MainSources.lines(p)) {
                if (MAP_OBJ.matcher(line).find()) n++;
            }
        }
        return n;
    }
}
