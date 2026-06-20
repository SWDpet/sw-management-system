package com.swmanager.system.testsupport;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 거대 클래스 ratchet 게이트 (QUALITY_CHARTER §2-F2, §3-S1).
 *
 * 임계(컨트롤러 1,500 / 서비스 2,000줄) 초과 클래스를 baseline 으로 고정하고:
 * <ul>
 *   <li><b>새 거대 클래스 금지</b>: baseline 에 없는 신규 초과 → 실패</li>
 *   <li><b>증가 금지</b>: baseline 클래스가 더 커지면 → 실패 (감소만 허용)</li>
 * </ul>
 * 갱신(분리/감소 시): {@code GOLDEN_RECORD=1 ./mvnw test -Dtest=GiantClassRatchetTest} 후
 * {@code golden/giant-class-baseline.txt} 커밋.
 */
class GiantClassRatchetTest {

    private static final Path BASELINE = Path.of("src", "test", "resources", "golden", "giant-class-baseline.txt");
    private static final int CONTROLLER_MAX = 1500;
    private static final int SERVICE_MAX = 2000;

    @Test
    void giantClasses_doNotGrowOrAppear() throws Exception {
        Map<String, Integer> current = currentOverThreshold();

        if (!Files.exists(BASELINE)) {
            if (GoldenSnapshot.isRecordMode()) {
                writeBaseline(current);
                System.out.println("[GiantClassRatchet] baseline 기록: " + current + " — 커밋 필요");
                return;
            }
            throw new AssertionError("giant-class baseline 없음. GOLDEN_RECORD=1 로 1회 기록 후 커밋.");
        }

        Map<String, Integer> baseline = readBaseline();

        List<String> violations = new ArrayList<>();
        for (var e : current.entrySet()) {
            Integer base = baseline.get(e.getKey());
            if (base == null) {
                violations.add("신규 거대 클래스: " + e.getKey() + " (" + e.getValue() + "줄)");
            } else if (e.getValue() > base) {
                violations.add("커짐: " + e.getKey() + " " + base + " → " + e.getValue() + "줄");
            }
        }

        // record 모드라도 *위반(신규/증가)은 블레스 불가* — 감소/정리(위반 없음)일 때만 baseline 갱신 (codex).
        if (GoldenSnapshot.isRecordMode() && violations.isEmpty()) {
            writeBaseline(current);
            System.out.println("[GiantClassRatchet] baseline 갱신(감소/정리): " + baseline + " → " + current);
            return;
        }

        assertThat(violations)
                .as("거대 클래스 ratchet 위반 — 새 거대클래스 또는 기존이 더 커짐. "
                        + "(record 모드로도 증가/신규는 블레스 불가 — 분리하라.)")
                .isEmpty();
    }

    private Map<String, Integer> currentOverThreshold() {
        Map<String, Integer> over = new TreeMap<>();
        for (Path p : MainSources.allJava()) {
            int max = MainSources.isUnder(p, "controller") ? CONTROLLER_MAX
                    : MainSources.isUnder(p, "service") ? SERVICE_MAX : Integer.MAX_VALUE;
            if (max == Integer.MAX_VALUE) continue;
            int lines = MainSources.lines(p).size();
            if (lines > max) over.put(rel(p), lines);
        }
        return over;
    }

    private static String rel(Path p) {
        return MainSources.ROOT.relativize(p).toString().replace('\\', '/');
    }

    private Map<String, Integer> readBaseline() throws Exception {
        Map<String, Integer> m = new TreeMap<>();
        for (String line : Files.readAllLines(BASELINE)) {
            line = line.strip();
            if (line.isEmpty()) continue;
            int eq = line.lastIndexOf('=');
            m.put(line.substring(0, eq), Integer.parseInt(line.substring(eq + 1).trim()));
        }
        return m;
    }

    private void writeBaseline(Map<String, Integer> m) throws Exception {
        Files.createDirectories(BASELINE.getParent());
        StringBuilder sb = new StringBuilder();
        for (var e : new TreeMap<>(m).entrySet()) sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
        Files.writeString(BASELINE, sb.toString());
    }
}
