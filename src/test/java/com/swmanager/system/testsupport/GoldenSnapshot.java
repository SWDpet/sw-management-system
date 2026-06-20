package com.swmanager.system.testsupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 골든 스냅샷 net (net 인프라 #1, QUALITY_CHARTER §4-1).
 *
 * characterization test 의 박제 메커니즘:
 * <ol>
 *   <li>응답 JSON 을 {@link JsonCanonicalizer} 로 정규화(키 정렬 + 휘발값 마스킹)</li>
 *   <li>golden 파일({@code src/test/resources/golden/&lt;name&gt;.json})이 <b>없으면 기록</b>(첫 실행 = 현재 동작 박제)</li>
 *   <li>있으면 <b>일치 단언</b> — 리팩토링으로 "되던 게 안 되면" 빌드 실패로 잡는다</li>
 * </ol>
 *
 * 워크플로: 박제할 코드에 골든 테스트 추가 → 1회 실행해 golden 기록 → <b>golden 파일 커밋</b> →
 *           이후 리팩토링은 이 net 아래에서 안전.
 */
public final class GoldenSnapshot {

    private GoldenSnapshot() {}

    private static final Path DIR = Path.of("src", "test", "resources", "golden");

    /**
     * 기록 모드 여부. {@code GOLDEN_RECORD} 환경변수 또는 {@code -Dgolden.record} 가 1/true/yes 일 때만 true.
     * <p>기본(미설정)에서는 golden 부재 시 <b>기록하지 않고 실패</b> — CI 에서 golden 누락이 silent pass
     * 되는 것을 방지(codex 지적). 새 golden 기록: {@code GOLDEN_RECORD=1 mvn test -Dtest=...}.
     */
    public static boolean isRecordMode() {
        return truthy(System.getenv("GOLDEN_RECORD")) || truthy(System.getProperty("golden.record"));
    }

    private static boolean truthy(String s) {
        return s != null && (s.equals("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes"));
    }

    /** 휘발 키 없이 박제·대조. */
    public static void assertMatches(String name, String actualJson) {
        assertMatches(name, actualJson, Set.of());
    }

    /**
     * 정규화 후 golden 과 대조. golden 부재 시 기록(첫 실행).
     * @param name         golden 파일 이름(확장자 제외). 테스트별 고유.
     * @param actualJson   실제 응답 JSON
     * @param volatileKeys 값 마스킹할 휘발 키(생성 ID·타임스탬프 등)
     */
    public static void assertMatches(String name, String actualJson, Set<String> volatileKeys) {
        String canonical = JsonCanonicalizer.canonicalize(actualJson, volatileKeys);
        Path file = DIR.resolve(name + ".json");
        try {
            if (!Files.exists(file)) {
                if (isRecordMode()) {
                    // 기록 모드(GOLDEN_RECORD): 현재 동작을 박제. golden 파일을 커밋해야 net 활성화.
                    Files.createDirectories(DIR);
                    Files.writeString(file, canonical);
                    System.out.println("[GoldenSnapshot] 기록: " + file.toAbsolutePath() + " — 커밋 필요");
                    return;
                }
                // 기본: golden 부재 = 실패 (CI silent pass 방지, codex). 기록은 GOLDEN_RECORD=1 로 1회.
                throw new AssertionError("golden 없음 '" + name + "' (" + file
                        + "). 최초 기록: GOLDEN_RECORD=1 로 1회 실행 후 커밋.");
            }
            String expected = Files.readString(file).strip();
            assertThat(canonical.strip())
                    .as("golden 스냅샷 불일치 '%s' — 리팩토링이 응답을 바꿨다. 의도된 변경이면 golden 갱신.", name)
                    .isEqualTo(expected);
        } catch (java.io.IOException e) {
            throw new RuntimeException("golden 파일 IO 실패: " + file, e);
        }
    }
}
