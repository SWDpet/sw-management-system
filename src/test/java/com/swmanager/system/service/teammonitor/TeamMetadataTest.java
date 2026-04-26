package com.swmanager.system.service.teammonitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TeamMetadata 단위 테스트 — Spring 컨텍스트 비의존 (S4-03).
 * (sprint team-monitor-wildcard-watcher §FR-7-d)
 */
class TeamMetadataTest {

    @TempDir
    Path tempDir;

    private TeamMetadata createMeta() {
        return new TeamMetadata(tempDir.resolve("teams.json"));
    }

    private void writeTeamsJson(String content) throws Exception {
        Files.writeString(tempDir.resolve("teams.json"), content);
    }

    @Test
    void loadsFromJson_appliesSortOrder() throws Exception {
        writeTeamsJson("""
                {
                  "schema_version": 1,
                  "teams": {
                    "alpha": {"emoji": "A", "sort_order": 50, "label": "ALPHA"},
                    "beta":  {"emoji": "B", "sort_order": 10, "label": "BETA"}
                  },
                  "default_emoji": "📋"
                }
                """);
        TeamMetadata meta = createMeta();
        meta.reload();

        assertThat(meta.sortOrder("alpha")).isEqualTo(50);
        assertThat(meta.sortOrder("beta")).isEqualTo(10);
        assertThat(meta.label("alpha")).isEqualTo("ALPHA");
        assertThat(meta.emoji("beta")).isEqualTo("B");
    }

    @Test
    void loadsFromJson_appliesSortOrderAlias() throws Exception {
        // S2-06: sortOrder camelCase 도 호환 (@JsonAlias)
        writeTeamsJson("""
                {
                  "schema_version": 1,
                  "teams": {
                    "x": {"emoji": "X", "sortOrder": 7, "label": "X"}
                  }
                }
                """);
        TeamMetadata meta = createMeta();
        meta.reload();

        assertThat(meta.sortOrder("x")).isEqualTo(7);
    }

    @Test
    void missingFile_appliesFallbackDefaults() {
        TeamMetadata meta = createMeta();
        meta.reload();

        assertThat(meta.isFallbackActive()).isTrue();
        // CODE_BUILTIN 5팀 fallback 적용
        assertThat(meta.emoji("planner")).isEqualTo("🧭");
        assertThat(meta.label("planner")).isEqualTo("PLANNER");
        assertThat(meta.sortOrder("planner")).isEqualTo(10);
    }

    @Test
    void missingTeam_returnsDefaultEmojiAndLabel() throws Exception {
        writeTeamsJson("""
                {
                  "schema_version": 1,
                  "teams": {"planner": {"emoji": "🧭", "sort_order": 10, "label": "PLANNER"}},
                  "default_emoji": "🟦"
                }
                """);
        TeamMetadata meta = createMeta();
        meta.reload();

        // 미등록 팀 — JSON default_emoji 우선 (S2-04)
        assertThat(meta.emoji("tester")).isEqualTo("🟦");
        assertThat(meta.label("tester")).isEqualTo("TESTER");
        assertThat(meta.sortOrder("tester")).isEqualTo(99);
    }

    @Test
    void emojiFallback_priorityOrder_jsonDefault_then_builtin_then_finalDefault() throws Exception {
        // S2-04: JSON default_emoji 우선 → CODE_BUILTIN → 최종 default
        writeTeamsJson("""
                {
                  "schema_version": 1,
                  "teams": {},
                  "default_emoji": "🟦"
                }
                """);
        TeamMetadata meta = createMeta();
        meta.reload();

        // 미등록 팀 — JSON default_emoji 우선
        assertThat(meta.emoji("anything")).isEqualTo("🟦");
        // 등록은 안 됐지만 CODE_BUILTIN 에 있는 팀 — JSON default_emoji 가 더 우선
        assertThat(meta.emoji("planner")).isEqualTo("🟦");
    }

    @Test
    void emojiFallback_noJsonDefault_usesBuiltin() throws Exception {
        writeTeamsJson("""
                {
                  "schema_version": 1,
                  "teams": {}
                }
                """);
        TeamMetadata meta = createMeta();
        meta.reload();

        // default_emoji 없음 → CODE_BUILTIN 적용
        assertThat(meta.emoji("planner")).isEqualTo("🧭");
        // CODE_BUILTIN 도 없음 → 최종 default 📋
        assertThat(meta.emoji("anything")).isEqualTo("📋");
    }

    @Test
    void corruptJson_initialLoad_returnsEmpty_withFallbackFlag() throws Exception {
        writeTeamsJson("{ corrupt JSON !@#$ ");
        TeamMetadata meta = createMeta();
        meta.reload();

        assertThat(meta.isFallbackActive()).isTrue();
        assertThat(meta.degradedReason()).isNotNull();
        // CODE_BUILTIN fallback 동작 확인
        assertThat(meta.emoji("planner")).isEqualTo("🧭");
    }

    @Test
    void corruptJson_keepsLastSnapshot_withDegradedFlag() throws Exception {
        // 1차: 정상 로드
        writeTeamsJson("""
                {
                  "schema_version": 1,
                  "teams": {"alpha": {"emoji": "A", "sort_order": 5, "label": "ALPHA"}},
                  "default_emoji": "📋"
                }
                """);
        TeamMetadata meta = createMeta();
        meta.reload();
        assertThat(meta.isFallbackActive()).isFalse();
        assertThat(meta.label("alpha")).isEqualTo("ALPHA");

        // 2차: 손상된 JSON 으로 reload
        writeTeamsJson("{ broken !!");
        meta.reload();

        // S2-03: 직전 정상 스냅샷 유지 + degraded 플래그
        assertThat(meta.isFallbackActive()).isTrue();
        assertThat(meta.degradedReason()).isNotNull();
        assertThat(meta.label("alpha")).isEqualTo("ALPHA"); // 직전 데이터 유지
    }

    @Test
    void metadata_safe_publication_concurrent() throws Exception {
        // R-9 / N12: AtomicReference 안전 공개 — 두 스레드 reload + get 경합
        writeTeamsJson("""
                {
                  "schema_version": 1,
                  "teams": {"a": {"emoji": "A", "sort_order": 1, "label": "A"}},
                  "default_emoji": "📋"
                }
                """);
        TeamMetadata meta = createMeta();
        meta.reload();

        ExecutorService exec = Executors.newFixedThreadPool(4);
        AtomicBoolean inconsistent = new AtomicBoolean(false);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(4);

        for (int i = 0; i < 2; i++) {
            exec.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < 100; j++) {
                        meta.reload();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        for (int i = 0; i < 2; i++) {
            exec.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < 1000; j++) {
                        String e = meta.emoji("a");
                        // emoji 는 항상 "A" (정상) 또는 코드 내장/default — 절대 null/빈문자 안 됨
                        if (e == null || e.isBlank()) {
                            inconsistent.set(true);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        boolean finished = done.await(10, TimeUnit.SECONDS);
        exec.shutdownNow();

        assertThat(finished).isTrue();
        assertThat(inconsistent).isFalse();
    }
}
