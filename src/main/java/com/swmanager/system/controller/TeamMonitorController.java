package com.swmanager.system.controller;

import com.swmanager.system.config.TeamMonitorProperties;
import com.swmanager.system.exception.OriginNotAllowedException;
import com.swmanager.system.service.teammonitor.TeamMonitorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

/**
 * 가상 팀 진행율 모니터링 페이지 + SSE 스트림.
 * (sprint team-monitor-dashboard, 개발계획 Step 4-1 — codex v2 보완 S4/S7)
 */
@Controller
@RequestMapping("/admin/team-monitor")
@PreAuthorize("hasRole('ADMIN')")
public class TeamMonitorController {

    private final TeamMonitorService service;
    private final TeamMonitorProperties props;

    public TeamMonitorController(TeamMonitorService service, TeamMonitorProperties props) {
        this.service = service;
        this.props = props;
    }

    @GetMapping
    public String page(@RequestParam(value = "fullscreen", required = false) String fullscreen,
                       Model model) {
        model.addAttribute("fullscreen", "1".equals(fullscreen));
        return "admin/team-monitor";
    }

    // S4: produces 는 base 만, charset 은 헤더로
    @GetMapping(path = "/stream", produces = "text/event-stream")
    public SseEmitter stream(HttpServletResponse resp,
                              HttpServletRequest req,
                              Principal principal) {  // S7: @AuthenticationPrincipal 제거
        validateOrigin(req);

        // §4-7 응답 헤더 고정
        resp.setHeader("Content-Type", "text/event-stream; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-transform");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Connection", "keep-alive");
        resp.setHeader("X-Accel-Buffering", "no");
        resp.setHeader("Vary", "Accept, Origin");

        long timeoutMs = props.getSse().getTimeoutMs();
        SseEmitter emitter = new SseEmitter(timeoutMs == 0 ? 0L : timeoutMs);
        return service.register(principal, emitter);
    }

    private void validateOrigin(HttpServletRequest req) {
        String origin = req.getHeader("Origin");
        if (origin == null || origin.isBlank()) return; // 동일출처 (Origin 미발급)
        String allowed = props.getSecurity().getAllowedOrigins();
        if (allowed == null || allowed.isBlank()) {
            // 동일출처만 허용 — Origin 이 host 와 일치하면 통과
            String scheme = req.getScheme();
            String host = req.getHeader("Host");
            String expected = scheme + "://" + host;
            if (!origin.equals(expected)) {
                throw new OriginNotAllowedException(origin);
            }
            return;
        }
        List<String> list = Arrays.stream(allowed.split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList();
        if (!list.contains(origin)) {
            throw new OriginNotAllowedException(origin);
        }
    }
}
