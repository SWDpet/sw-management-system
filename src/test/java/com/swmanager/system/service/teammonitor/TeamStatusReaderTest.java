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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TeamStatusReader 단위 테스트.
 * 정상/필드 누락/잘못된 progress/한글 task/updated 누락(null) 케이스.
 */
class TeamStatusReaderTest {

    @TempDir Path tmp;
    private TeamStatusReader reader;

    @BeforeEach
    void init() {
        TeamMonitorProperties props = new TeamMonitorProperties();
        props.setStatusDir(tmp.toString());
        reader = new TeamStatusReader(props);
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
        // task 값에 '=' 포함되어도 첫 '=' 만 split.
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
    void readAll_returnsAllFourTeams_evenIfFilesMissing() throws IOException {
        write("planner.status", "team=planner\nstate=완료\nprogress=100\ntask=done\nupdated=1777170000\n");
        // db/developer/codex 파일 없음
        Map<String, TeamStatus> all = reader.readAll();
        assertThat(all).containsOnlyKeys("planner", "db", "developer", "codex");
        assertThat(all.get("planner")).isNotNull();
        assertThat(all.get("db")).isNull();
        assertThat(all.get("developer")).isNull();
        assertThat(all.get("codex")).isNull();
    }

    private void write(String name, String content) throws IOException {
        Files.writeString(tmp.resolve(name), content, StandardCharsets.UTF_8);
    }
}
