package com.swmanager.system.service.teammonitor;

import com.swmanager.system.config.TeamMonitorProperties;
import com.swmanager.system.dto.TeamStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * .team-workspace/status/{team}.status 파일 파싱.
 * (sprint team-monitor-dashboard, 개발계획 Step 1-4)
 *
 * - updated 키 누락/파싱 실패 → updatedAt = null (R-2-c "갱신: 미기록" 정합).
 * - 다른 필드 누락 시 직전 캐시 유지는 Service 책임 (본 클래스는 IOException 만 전파).
 */
@Component
public class TeamStatusReader {

    private static final Logger log = LoggerFactory.getLogger(TeamStatusReader.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    public static final List<String> TEAMS = List.of("planner", "db", "developer", "codex");

    private final Path statusDir;

    public TeamStatusReader(TeamMonitorProperties props) {
        this.statusDir = Path.of(props.getStatusDir());
    }

    /** 4팀 전부 read. 개별 팀 파일 부재 시 null 값 entry. IOException 은 호출자에 전파. */
    public Map<String, TeamStatus> readAll() throws IOException {
        Map<String, TeamStatus> result = new LinkedHashMap<>();
        for (String team : TEAMS) {
            result.put(team, readOne(team).orElse(null));
        }
        return result;
    }

    /** 단일 팀 read. 파일 부재 → Optional.empty. 파싱 실패 → IOException. */
    public Optional<TeamStatus> readOne(String team) throws IOException {
        Path file = statusDir.resolve(team + ".status");
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        Map<String, String> kv = new LinkedHashMap<>();
        for (String line : lines) {
            int eq = line.indexOf('=');
            if (eq <= 0) continue;
            kv.put(line.substring(0, eq).trim(), line.substring(eq + 1));
        }

        String teamField = kv.getOrDefault("team", team);
        String state = kv.get("state");
        Integer progress = parseInt(kv.get("progress"));
        String task = kv.getOrDefault("task", "");
        String updatedAt = parseUpdated(kv.get("updated"));

        return Optional.of(new TeamStatus(teamField, state, progress, task, updatedAt));
    }

    private Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException ignored) { return null; }
    }

    /**
     * S1: updated 누락 또는 정수 파싱 실패 시 null 반환 (now() 대체 금지).
     * 정상 epoch seconds 면 Asia/Seoul ISO-8601 with offset.
     */
    private String parseUpdated(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            long epoch = Long.parseLong(raw.trim());
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), KST).toString();
        } catch (NumberFormatException e) {
            log.warn("set-status updated 키 파싱 실패: '{}' → null 처리", raw);
            return null;
        }
    }
}
