package com.swmanager.system.service.teammonitor;

import com.swmanager.system.config.TeamMonitorProperties;
import com.swmanager.system.dto.TeamStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

/**
 * .team-workspace/status/{team}.status 파일 파싱.
 * (sprint team-monitor-dashboard, 개발계획 Step 1-4)
 * (sprint team-monitor-wildcard-watcher: TEAMS 상수 → listTeams() + 캐시, FR-1)
 *
 * - updated 키 누락/파싱 실패 → updatedAt = null (R-2-c "갱신: 미기록" 정합).
 * - 다른 필드 누락 시 직전 캐시 유지는 Service 책임 (본 클래스는 IOException 만 전파).
 * - listTeams() — 디렉토리 스캔 (*.status, dot-prefix 제외) + 캐시 (TTL 1초) + sort_order 정렬.
 */
@Component
public class TeamStatusReader {

    private static final Logger log = LoggerFactory.getLogger(TeamStatusReader.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * 코드 내장 5팀 상수 — sprint team-monitor-wildcard-watcher 임시 유지.
     * commit 5 시점에 모든 호출자 listTeams() 전환 후 최종 제거 예정.
     * T-H grep 회귀 차단 예외 (ALLOW_FIVE_TEAM_FALLBACK 센티넬).
     */
    // ALLOW_FIVE_TEAM_FALLBACK
    @Deprecated(forRemoval = true, since = "team-monitor-wildcard-watcher commit 3")
    public static final List<String> TEAMS = List.of("planner", "db", "developer", "codex", "designer");

    private static final long CACHE_TTL_MS = 1000L;
    private static final PathMatcher MATCHER =
            FileSystems.getDefault().getPathMatcher("glob:*.status");

    private final Path statusDir;
    private final TeamMetadata teamMetadata;
    private final LongSupplier nowSupplier;
    private final AtomicReference<CachedTeams> cache = new AtomicReference<>();

    /** 불변 record — 모든 필드 final, 외부 변형 불가 (R-9 / N12). */
    record CachedTeams(List<String> teams, long expiresAtMs) {}

    /**
     * Spring 프로덕션 생성자 — TeamMetadata 주입 (sort_order 정렬).
     */
    public TeamStatusReader(TeamMonitorProperties props, TeamMetadata meta) {
        this(props, meta, System::currentTimeMillis);
    }

    /**
     * 후방 호환 생성자 (sprint team-monitor-wildcard-watcher commit 3 — S4-01-a).
     * commit 5 에서 모든 호출자 2-인자 생성자로 전환 후 본 생성자 제거.
     * teamMetadata == null 시 buildComparator() 가 알파벳 폴백 (NPE 차단 — S4-01-a-NPE).
     */
    @Deprecated(forRemoval = true, since = "team-monitor-wildcard-watcher commit 3")
    public TeamStatusReader(TeamMonitorProperties props) {
        this(props, null, System::currentTimeMillis);
    }

    /** 테스트 전용 — Clock 주입 (S6-04 — TTL 플래키 차단). */
    TeamStatusReader(TeamMonitorProperties props, TeamMetadata meta, LongSupplier nowSupplier) {
        this.statusDir = props.getStatusPath();
        this.teamMetadata = meta;
        this.nowSupplier = nowSupplier;
    }

    /**
     * 디렉토리 스캔 기반 팀 목록 (캐시 TTL 1초).
     * 매칭 룰:
     *  - {@code Files.isRegularFile(p)} (디렉토리/심볼릭 제외)
     *  - {@code !p.getFileName().startsWith(".")} (임시파일 제외)
     *  - {@code MATCHER.matches(p.getFileName())} — *.status (N9 SSoT — 파일명 기준)
     * 정렬: teamMetadata.sortOrder() → 알파벳 (FR-1-c). meta null 시 알파벳만.
     */
    public List<String> listTeams() {
        CachedTeams c = cache.get();
        long now = nowSupplier.getAsLong();
        if (c != null && c.expiresAtMs() > now) return c.teams();
        List<String> fresh = scanAndSort();
        cache.set(new CachedTeams(fresh, now + CACHE_TTL_MS));
        return fresh;
    }

    /** watcher 가 호출 — ENTRY_CREATE/DELETE/OVERFLOW 시 즉시 invalidate. */
    public void invalidateCache() {
        cache.set(null);
    }

    private List<String> scanAndSort() {
        try {
            Files.createDirectories(statusDir);  // C3-01-A — graceful degrade
        } catch (IOException e) {
            log.warn("statusDir 생성 실패: {}", e.getMessage());
            return List.of();
        }
        try (Stream<Path> s = Files.list(statusDir)) {
            List<String> raw = s.filter(p -> {
                        Path fn = p.getFileName();
                        return Files.isRegularFile(p)
                                && !fn.toString().startsWith(".")
                                && MATCHER.matches(fn);
                    })
                    .map(p -> {
                        String n = p.getFileName().toString();
                        return n.substring(0, n.length() - ".status".length());
                    })
                    .sorted(buildComparator())
                    .toList();
            return List.copyOf(raw);  // 불변 (S2-01)
        } catch (IOException e) {
            log.warn("listTeams 스캔 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /** S4-01-a-NPE: teamMetadata == null 시 알파벳 폴백 (1-인자 생성자 사용 시). */
    private Comparator<String> buildComparator() {
        if (teamMetadata == null) {
            return Comparator.naturalOrder();
        }
        return Comparator
                .comparingInt((String t) -> teamMetadata.sortOrder(t))
                .thenComparing(Comparator.naturalOrder());
    }

    /** 모든 팀 read (디렉토리 스캔 결과 + 각 팀 readOne). 파일 부재 → null entry. */
    public Map<String, TeamStatus> readAll() throws IOException {
        Map<String, TeamStatus> result = new LinkedHashMap<>();
        for (String team : listTeams()) {
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
