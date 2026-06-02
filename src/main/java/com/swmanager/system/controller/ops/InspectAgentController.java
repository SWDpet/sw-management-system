package com.swmanager.system.controller.ops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.config.CustomUserDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 점검 수집모듈(agent-windows) 다운로드 — 전용 페이지 + zip 다운로드.
 *
 * 기획서: docs/product-specs/inspect-agent-download.md (v0.4, 최종승인)
 * 개발계획: docs/exec-plans/inspect-agent-download.md (v3)
 *
 * 빌드 패키징(pom prepare-package)이 다음을 classpath 에 배치한다:
 *  - agent/inspect-agent-&lt;ver&gt;.zip        (배포 zip)
 *  - agent/inspect-agent-&lt;ver&gt;.zip.sha256 (무결성)
 *  - agent/release-manifest.json              (버전·빌드시각·파일목록 SHA-256)
 *  - agent/VERSION                            (배포버전 properties)
 */
@Slf4j
@Controller
@RequestMapping("/ops-doc")
@RequiredArgsConstructor
public class InspectAgentController {

    private final ObjectMapper objectMapper;

    private volatile AgentMeta cachedMeta;

    /** 다운로드 전용 페이지 */
    @GetMapping("/inspect-agent")
    public String page(@AuthenticationPrincipal CustomUserDetails cu, Model model) {
        if ("NONE".equals(getAuth(cu))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수집모듈 조회 권한이 없습니다");
        }
        model.addAttribute("agent", meta()); // null 이면 템플릿에서 "준비중" 표시
        return "ops-doc/inspect-agent";
    }

    /** zip 다운로드 (application/zip + Content-Length + attachment) */
    @GetMapping("/inspect-agent/download")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal CustomUserDetails cu) {
        if ("NONE".equals(getAuth(cu))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AgentMeta m = meta();
        if (m == null) {
            return ResponseEntity.notFound().build(); // 빌드 산출물 미존재 (T-8)
        }
        ClassPathResource zip = new ClassPathResource("agent/" + m.getZipName());
        if (!zip.exists()) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] bytes = StreamUtils.copyToByteArray(zip.getInputStream());
            String encoded = URLEncoder.encode(m.getZipName(), StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/zip")
                    .header("Content-Length", String.valueOf(bytes.length))
                    .header("Content-Disposition", "attachment; filename*=UTF-8''" + encoded)
                    .body(bytes);
        } catch (Exception e) {
            log.error("[inspect-agent] zip 다운로드 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── 권한: DocumentController.getAuth() 동등 (isAdmin || authDocument != NONE) ──
    private boolean isAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } catch (Exception e) {
            return false;
        }
    }

    private String getAuth(CustomUserDetails cu) {
        if (isAdmin()) return "EDIT";
        if (cu == null) return "NONE";
        String auth = cu.getUser().getAuthDocument();
        return (auth != null) ? auth : "NONE";
    }

    // ── release-manifest.json + .sha256 로드 (1회 캐시) ──
    private AgentMeta meta() {
        AgentMeta m = cachedMeta;
        if (m != null) return m;
        synchronized (this) {
            if (cachedMeta != null) return cachedMeta;
            try {
                ClassPathResource mf = new ClassPathResource("agent/release-manifest.json");
                if (!mf.exists()) {
                    log.warn("[inspect-agent] release-manifest.json 미존재 — 빌드 패키징 누락?");
                    return null;
                }
                JsonNode n;
                try (var in = mf.getInputStream()) {
                    n = objectMapper.readTree(in);
                }
                String version = n.path("version").asText("");
                String zipName = "inspect-agent-" + version + ".zip";
                String sha256 = "";
                ClassPathResource sc = new ClassPathResource("agent/" + zipName + ".sha256");
                if (sc.exists()) {
                    try (var in = sc.getInputStream()) {
                        sha256 = new String(StreamUtils.copyToByteArray(in), StandardCharsets.UTF_8).trim();
                    }
                }
                cachedMeta = new AgentMeta(
                        version,
                        n.path("buildTimestamp").asText(""),
                        n.path("fileCount").asInt(0),
                        sha256,
                        zipName);
                return cachedMeta;
            } catch (Exception e) {
                log.error("[inspect-agent] 메타데이터 로드 실패", e);
                return null;
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public static class AgentMeta {
        private final String version;
        private final String buildTimestamp;
        private final int fileCount;
        private final String sha256;
        private final String zipName;
    }
}
