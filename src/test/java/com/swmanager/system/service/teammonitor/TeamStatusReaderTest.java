package com.swmanager.system.service.teammonitor;

import com.swmanager.system.config.TeamMonitorProperties;
import com.swmanager.system.dto.TeamStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TeamStatusReader 단위 테스트.
 * 정상/필드 누락/잘못된 progress/한글 task/updated 누락(null) 케이스.
 *
 * sprint team-monitor-wildcard-watcher: listTeams + 캐시 + 정렬 테스트 추가 (FR-7-a).
 */
class TeamStatusReaderTest {

    @TempDir Path tmp;
    private TeamStatusReader reader;
    private TeamMetadata meta;

    @BeforeEach
    void init() {
        TeamMonitorProperties props = new TeamMonitorProperties();
        props.setStatusDir(tmp.toString());
        // teams.json 부재 → CODE_BUILTIN fallback (planner=10, db=20, ...)
        meta = new TeamMetadata(tmp.resolve("absent-teams.json"));
        meta.reload();
        reader = new TeamStatusReader(props, meta);
    }

    @Test
    void readOne_normal_parsesAllFields() throws IOException {
        write("planner.status", """
                team=planner
                state=진행중
                progress=75
                task=v2 작성 중
                updated=1777170000
                """);

        Optional<TeamStatus> result = reader.readOne("planner");

        assertThat(result).isPresent();
        TeamStatus s = result.get();
        assertThat(s.team()).isEqualTo("planner");
        assertThat(s.state()).isEqualTo("진행중");
        assertThat(s.progress()).isEqualTo(75);
        assertThat(s.task()).isEqualTo("v2 작성 중");
        assertThat(s.updatedAt()).startsWith("2026-").contains("+09:00");
    }

    @Test
    void readOne_missingFile_returnsEmpty() throws IOException {
        assertThat(reader.readOne("planner")).isEmpty();
    }

    @Test
    void readOne_missingUpdated_keepsNull() throws IOException {
        write("db.status", """
                team=db
                state=대기
                progress=0
                task=DB 변경 없음 예상
                """);
        TeamStatus s = reader.readOne("db").orElseThrow();
        assertThat(s.updatedAt()).isNull(); // S1: now() fallback 금지
    }

    @Test
    void readOne_invalidUpdated_keepsNull() throws IOException {
        write("developer.status", """
                team=developer
                state=대기
                progress=0
                task=
                updated=NOT_A_NUMBER
                """);
        TeamStatus s = reader.readOne("developer").orElseThrow();
        assertThat(s.updatedAt()).isNull();
    }

    @Test
    void readOne_invalidProgress_returnsNullProgress() throws IOException {
        write("codex.status", """
                team=codex
                state=완료
                progress=abc
                task=검토
                updated=1777170000
                """);
        TeamStatus s = reader.readOne("codex").orElseThrow();
        assertThat(s.progress()).isNull();
    }

    @Test
    void readOne_koreanTaskWithEquals_parsesValueAfterFirstEquals() throws IOException {
        write("planner.status", """
                team=planner
                state=진행중
                progress=10
                task=key=value 형식 한글 작업명
                updated=1777170000
                """);
        TeamStatus s = reader.readOne("planner").orElseThrow();
        assertThat(s.task()).isEqualTo("key=value 형식 한글 작업명");
    }

    @Test
    void readAll_onlyReturnsPresentTeams() throws IOException {
        // sprint team-monitor-wildcard-watcher: 디렉토리 스캔 기반.
        // planner 파일만 있으면 readAll 도 planner 만 entry (FR-1).
        write("planner.status", "team=planner\nstate=완료\nprogress=100\ntask=done\nupdated=1777170000\n");

        Map<String, TeamStatus> all = reader.readAll();
        assertThat(all).containsOnlyKeys("planner");
        assertThat(all.get("planner")).isNotNull();
    }

    @Test
    void readAll_designerOnlyPresent_returnsOnlyDesigner() throws IOException {
        write("designer.status", "team=designer\nstate=대기\nprogress=0\ntask=onboarding\nupdated=1777170000\n");
        Map<String, TeamStatus> all = reader.readAll();
        assertThat(all).containsOnlyKeys("designer");
        assertThat(all.get("designer").team()).isEqualTo("designer");
    }

    // === sprint team-monitor-wildcard-watcher 신규 테스트 (FR-7-a) ===

    @Test
    void listTeams_emptyDir_returnsEmpty() {
        assertThat(reader.listTeams()).isEmpty();
    }

    @Test
    void listTeams_returnsTeamsFromStatusFiles() throws IOException {
        write("planner.status", "team=planner\n");
        write("db.status", "team=db\n");
        write("developer.status", "team=developer\n");

        List<String> teams = reader.listTeams();
        // CODE_BUILTIN sort_order: planner=10, db=20, developer=30
        assertThat(teams).containsExactly("planner", "db", "developer");
    }

    @Test
    void listTeams_ignoresDotPrefixedTempFiles() throws IOException {
        write("planner.status", "team=planner\n");
        write(".planner.status.XXXX", "temp content"); // atomic write 임시파일
        write(".tester.status.YYYY", "temp content");

        List<String> teams = reader.listTeams();
        assertThat(teams).containsExactly("planner");
    }

    @Test
    void listTeams_ignoresNonStatusSuffix() throws IOException {
        write("planner.status", "team=planner\n");
        write("readme.txt", "some doc");
        write("status.bak", "backup");

        List<String> teams = reader.listTeams();
        assertThat(teams).containsExactly("planner");
    }

    @Test
    void listTeams_appliesSortOrderFromMetadata() throws IOException {
        // CODE_BUILTIN sort_order: codex=40, designer=50, planner=10
        write("planner.status", "team=planner\n");
        write("codex.status", "team=codex\n");
        write("designer.status", "team=designer\n");

        List<String> teams = reader.listTeams();
        assertThat(teams).containsExactly("planner", "codex", "designer");
    }

    @Test
    void listTeams_unknownTeam_appliesFallbackSortOrder99() throws IOException {
        // tester 는 CODE_BUILTIN 미등록 → sort_order=99
        write("planner.status", "team=planner\n"); // 10
        write("tester.status", "team=tester\n");   // 99 → 뒤로
        write("apple.status", "team=apple\n");     // 99 + 알파벳 (apple)

        List<String> teams = reader.listTeams();
        // planner(10) → apple(99,a) → tester(99,t)
        assertThat(teams).containsExactly("planner", "apple", "tester");
    }

    @Test
    void listTeams_cacheTtl_invalidatesAfter1s() throws IOException {
        // S6-04: Clock 주입으로 가짜 시간
        AtomicLong now = new AtomicLong(0L);
        TeamMonitorProperties props = new TeamMonitorProperties();
        props.setStatusDir(tmp.toString());
        TeamStatusReader r = new TeamStatusReader(props, meta, now::get);

        // 첫 호출: 빈 상태 캐시
        assertThat(r.listTeams()).isEmpty();

        // 파일 추가하지만 TTL 만료 전 — 캐시 hit
        write("planner.status", "team=planner\n");
        now.addAndGet(500L); // 0.5s 경과
        assertThat(r.listTeams()).isEmpty(); // 캐시 hit (만료 전)

        // TTL 만료 후 — 재스캔
        now.addAndGet(600L); // 누적 1.1s
        assertThat(r.listTeams()).containsExactly("planner");
    }

    @Test
    void listTeams_invalidateCache_immediateRescan() throws IOException {
        AtomicLong now = new AtomicLong(0L);
        TeamMonitorProperties props = new TeamMonitorProperties();
        props.setStatusDir(tmp.toString());
        TeamStatusReader r = new TeamStatusReader(props, meta, now::get);

        assertThat(r.listTeams()).isEmpty();

        write("planner.status", "team=planner\n");
        // TTL 만료 전이지만 invalidateCache → 즉시 재계산
        r.invalidateCache();
        assertThat(r.listTeams()).containsExactly("planner");
    }

    @Test
    void listTeams_nullMeta_alphabeticalFallback() throws IOException {
        // S4-01-a-NPE: 1-인자 생성자 (deprecated) 사용 시 NPE 차단 + 알파벳 정렬
        TeamMonitorProperties props = new TeamMonitorProperties();
        props.setStatusDir(tmp.toString());
        @SuppressWarnings("removal")
        TeamStatusReader r = new TeamStatusReader(props);

        write("zebra.status", "team=zebra\n");
        write("apple.status", "team=apple\n");
        write("mango.status", "team=mango\n");

        List<String> teams = r.listTeams();
        assertThat(teams).containsExactly("apple", "mango", "zebra");
    }

    private void write(String name, String content) throws IOException {
        Files.writeString(tmp.resolve(name), content, StandardCharsets.UTF_8);
    }
}
