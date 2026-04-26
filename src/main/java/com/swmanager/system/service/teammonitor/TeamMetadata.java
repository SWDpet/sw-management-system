package com.swmanager.system.service.teammonitor;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.config.TeamMonitorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 팀 메타데이터 (이모지 / 라벨 / sort_order) 단일 진실원.
 * (sprint team-monitor-wildcard-watcher, 기획서 §FR-4)
 *
 * <p>로드 우선순위: teams.json (workspaceDir) → 코드 내장 5팀 fallback → 최종 default.
 * <p>안전 공개: AtomicReference + 불변 record (R-9 / N12).
 * <p>reload 정책 (S2-03): 정상 → snapshot 갱신, 파싱 실패 → 직전 정상 스냅샷 유지 + degraded.
 *
 * <p><b>⚠ 보안 (R-12 / N13)</b>: isFallbackActive() 결과는 TeamMonitorInfoContributor 를 통해
 * /actuator/info 에 노출. ADMIN 권한 인증 필수 (SecurityConfig 매처 참조).
 */
@Component
public class TeamMetadata {

    private static final Logger log = LoggerFactory.getLogger(TeamMetadata.class);

    /** record (모든 필드 final — 안전 공개). sort_order ↔ sortOrder 양방향 호환 (N5 / S2-06). */
    public record TeamMeta(
            String emoji,
            @JsonProperty("sort_order") @JsonAlias({"sort_order", "sortOrder"}) int sortOrder,
            String label
    ) {}

    /** snapshot — 모든 필드 final, 외부 변형 불가 (Map.copyOf). */
    public record MetaSnapshot(
            Map<String, TeamMeta> teams,
            String defaultEmoji,
            boolean fallbackActive,
            String degradedReason
    ) {
        public static MetaSnapshot empty(String reason) {
            return new MetaSnapshot(Map.of(), null, true, reason);
        }
        public static MetaSnapshot empty() {
            return empty("teams.json 부재");
        }
    }

    /** 코드 내장 5팀 — teams.json 부재 시 fallback. */
    // ALLOW_FIVE_TEAM_FALLBACK
    private static final Map<String, TeamMeta> CODE_BUILTIN = Map.of(
            "planner",   new TeamMeta("🧭", 10, "PLANNER"),
            "db",        new TeamMeta("🗄️", 20, "DB"),
            "developer", new TeamMeta("🛠️", 30, "DEVELOPER"),
            "codex",     new TeamMeta("🤖", 40, "CODEX"),
            "designer",  new TeamMeta("🎨", 50, "DESIGNER")
    );

    private static final String FALLBACK_DEFAULT_EMOJI = "📋";

    private final Path teamsJsonPath;
    private final ObjectMapper mapper;
    private final AtomicReference<MetaSnapshot> snapshot = new AtomicReference<>(MetaSnapshot.empty());

    public TeamMetadata(TeamMonitorProperties props) {
        this.teamsJsonPath = props.getTeamsJsonPath();
        this.mapper = buildMapper();
    }

    /** 테스트 전용 — 임의 경로 + ObjectMapper 주입. */
    TeamMetadata(Path teamsJsonPath) {
        this.teamsJsonPath = teamsJsonPath;
        this.mapper = buildMapper();
    }

    private static ObjectMapper buildMapper() {
        ObjectMapper m = new ObjectMapper();
        // Java 17 record 컴포넌트의 @JsonProperty/@JsonAlias 인식을 위해 ParameterNamesModule 등록
        m.findAndRegisterModules();
        return m;
    }

    @PostConstruct
    public void init() {
        reload();
    }

    /**
     * teams.json 재로드.
     * 정상 → snapshot 갱신.
     * 파일 부재 → empty + isFallbackActive=true.
     * 파싱 실패 → 직전 정상 스냅샷 유지 + degradedReason 갱신 + isFallbackActive=true.
     */
    public synchronized void reload() {
        if (!Files.exists(teamsJsonPath)) {
            snapshot.set(MetaSnapshot.empty("teams.json 부재: " + teamsJsonPath));
            log.info("TeamMetadata: teams.json 부재 — fallback (코드 내장 5팀) 동작. path={}", teamsJsonPath);
            return;
        }
        try {
            JsonRoot root = mapper.readValue(teamsJsonPath.toFile(), JsonRoot.class);
            Map<String, TeamMeta> teams = (root.teams != null) ? Map.copyOf(root.teams) : Map.of();
            String defaultEmoji = root.defaultEmoji;
            snapshot.set(new MetaSnapshot(teams, defaultEmoji, false, null));
            log.info("TeamMetadata: teams.json 로드 완료 ({} 팀, schema_version={}).",
                    teams.size(), root.schemaVersion);
        } catch (IOException e) {
            String reason = e.getClass().getSimpleName() + ": " + e.getMessage();
            MetaSnapshot prev = snapshot.get();
            // S2-03: 직전 정상 스냅샷 유지 + degraded 플래그
            MetaSnapshot degraded = new MetaSnapshot(
                    prev.teams(), prev.defaultEmoji(), true, reason);
            snapshot.set(degraded);
            log.warn("TeamMetadata: teams.json 파싱 실패 — 직전 스냅샷 유지 + degraded. reason={}", reason);
        }
    }

    /** 단일 팀 메타 (raw — fallback 적용 X). */
    public Optional<TeamMeta> get(String teamName) {
        TeamMeta m = snapshot.get().teams().get(teamName);
        return Optional.ofNullable(m);
    }

    /**
     * 이모지 fallback 우선순위 (S2-04):
     * 1) teams.json 의 해당 팀 emoji
     * 2) teams.json 의 default_emoji
     * 3) CODE_BUILTIN 의 해당 팀 emoji
     * 4) 최종 FALLBACK_DEFAULT_EMOJI (📋)
     */
    public String emoji(String teamName) {
        MetaSnapshot s = snapshot.get();
        TeamMeta m = s.teams().get(teamName);
        if (m != null && m.emoji() != null && !m.emoji().isBlank()) return m.emoji();
        if (s.defaultEmoji() != null && !s.defaultEmoji().isBlank()) return s.defaultEmoji();
        TeamMeta builtin = CODE_BUILTIN.get(teamName);
        if (builtin != null) return builtin.emoji();
        return FALLBACK_DEFAULT_EMOJI;
    }

    /** 라벨 fallback: JSON → CODE_BUILTIN → 팀명 대문자. */
    public String label(String teamName) {
        TeamMeta m = snapshot.get().teams().get(teamName);
        if (m != null && m.label() != null && !m.label().isBlank()) return m.label();
        TeamMeta builtin = CODE_BUILTIN.get(teamName);
        if (builtin != null) return builtin.label();
        return teamName.toUpperCase();
    }

    /** sort_order fallback: JSON → CODE_BUILTIN → 99. */
    public int sortOrder(String teamName) {
        TeamMeta m = snapshot.get().teams().get(teamName);
        if (m != null) return m.sortOrder();
        TeamMeta builtin = CODE_BUILTIN.get(teamName);
        if (builtin != null) return builtin.sortOrder();
        return 99;
    }

    /** health/info 노출용 — fallback / degraded 상태 진단 플래그. */
    public boolean isFallbackActive() {
        return snapshot.get().fallbackActive();
    }

    /** 진단 사유 (degraded 시 원인). 정상 시 null. */
    public String degradedReason() {
        return snapshot.get().degradedReason();
    }

    // === Jackson 매핑 전용 내부 클래스 (public 필드 — Jackson default visibility 호환) ===
    public static class JsonRoot {
        @JsonProperty("schema_version") public int schemaVersion;
        public Map<String, TeamMeta> teams;
        @JsonProperty("default_emoji") public String defaultEmoji;
    }
}
