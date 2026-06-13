package com.swmanager.system.controller.ops;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.domain.SysMst;
import com.swmanager.system.domain.ops.OpsKb;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.repository.ops.OpsKbRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 장애·지원 지식베이스 워크벤치 — ops-kb-workbench.
 * 문서 작성 없이 KB 직접 등록/수정/삭제·조회·추천. 입력→분류→제공(추천) 축적 루프.
 * 권한: ADMIN || authDocument (조회 != NONE, 등록/수정/삭제 == EDIT).
 */
@Controller
@RequestMapping("/ops-kb")
@RequiredArgsConstructor
public class OpsKbController {

    private final OpsKbRepository opsKbRepository;
    private final SysMstRepository sysMstRepository;

    // ===== 권한 (requireDocEdit private 이라 동일 정책 복제, ADMIN 통과) =====
    private boolean isAdmin() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } catch (Exception e) { return false; }
    }
    private String docAuth(CustomUserDetails u) {
        String a = (u != null && u.getUser() != null) ? u.getUser().getAuthDocument() : null;
        return a != null ? a : "NONE";
    }
    private boolean canView(CustomUserDetails u) { return isAdmin() || !"NONE".equals(docAuth(u)); }
    private boolean canEdit(CustomUserDetails u) { return isAdmin() || "EDIT".equals(docAuth(u)); }
    private ResponseEntity<Map<String, Object>> forbidden() {
        return ResponseEntity.status(403).body(Map.of("success", false,
                "error", Map.of("code", "FORBIDDEN", "message", "문서 편집 권한(authDocument=EDIT)이 필요합니다.")));
    }
    private List<String> sysList() {
        List<String> l = new ArrayList<>();
        for (SysMst s : sysMstRepository.findAll(Sort.by("nm"))) l.add(s.getCd());
        return l;
    }
    private List<Map<String, Object>> sysOptions() {
        List<Map<String, Object>> l = new ArrayList<>();
        for (SysMst s : sysMstRepository.findAll(Sort.by("nm"))) {
            Map<String, Object> m = new LinkedHashMap<>(); m.put("cd", s.getCd()); m.put("nm", s.getNm()); l.add(m);
        }
        return l;
    }

    // ===== 화면 =====
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal CustomUserDetails u) {
        if (!canView(u)) return "redirect:/";
        model.addAttribute("systems", sysOptions());
        model.addAttribute("canEdit", canEdit(u));
        model.addAttribute("activeMenu", "ops");
        return "ops-doc/kb-list";
    }

    @GetMapping("/new")
    public String createForm(Model model, @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return "redirect:/ops-kb";
        model.addAttribute("systems", sysOptions());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeMenu", "ops");
        return "ops-doc/kb-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return "redirect:/ops-kb";
        OpsKb kb = opsKbRepository.findById(id).orElse(null);
        if (kb == null) return "redirect:/ops-kb";
        model.addAttribute("systems", sysOptions());
        model.addAttribute("kb", kb);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeMenu", "ops");
        return "ops-doc/kb-form";
    }

    // ===== 조회 API =====
    @GetMapping("/api/list")
    @ResponseBody
    public List<Map<String, Object>> search(@RequestParam(required = false) String sysType,
                                            @RequestParam(required = false) String gubun,
                                            @RequestParam(required = false) String kw,
                                            @AuthenticationPrincipal CustomUserDetails u) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (!canView(u)) return out;
        String s = blank(sysType), g = blank(gubun), k = blank(kw);
        for (OpsKb kb : opsKbRepository.search(s, g, k)) out.add(toDto(kb));
        return out;
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        if (!canView(u)) return forbidden();
        OpsKb kb = opsKbRepository.findById(id).orElse(null);
        if (kb == null) return ResponseEntity.status(404).body(Map.of("success", false));
        return ResponseEntity.ok(toDto(kb));
    }

    // ===== CRUD (EDIT) =====
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        String err = validate(body);
        if (err != null) return ResponseEntity.badRequest().body(Map.of("success", false,
                "error", Map.of("code", "INVALID_INPUT", "message", err)));
        OpsKb kb = new OpsKb();
        apply(kb, body);
        kb.setSource("MANUAL");
        kb.setStatus("ACTIVE");
        kb.setRewritten(false);
        if (kb.getCaseCount() == null) kb.setCaseCount(1);
        kb.setCreatedBy(u != null ? u.getUsername() : null);
        kb.setKbCode(genKbCode());
        OpsKb saved = opsKbRepository.save(kb);
        return ResponseEntity.ok(Map.of("success", true, "kb_id", saved.getKbId(), "kb_code", saved.getKbCode()));
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        OpsKb kb = opsKbRepository.findById(id).orElse(null);
        if (kb == null) return ResponseEntity.status(404).body(Map.of("success", false));
        String err = validate(body);
        if (err != null) return ResponseEntity.badRequest().body(Map.of("success", false,
                "error", Map.of("code", "INVALID_INPUT", "message", err)));
        apply(kb, body);
        opsKbRepository.save(kb);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        OpsKb kb = opsKbRepository.findById(id).orElse(null);
        if (kb != null) { kb.setStatus("DELETED"); opsKbRepository.save(kb); }  // 소프트삭제
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== 헬퍼 =====
    private String validate(Map<String, Object> b) {
        if (blank((String) b.get("sys_type")) == null) return "시스템은 필수입니다.";
        if (blank((String) b.get("gubun")) == null) return "구분(장애/지원)은 필수입니다.";
        if (blank((String) b.get("symptom")) == null) return "증상은 필수입니다.";
        if (blank((String) b.get("cause")) == null) return "원인은 필수입니다.";
        if (blank((String) b.get("action")) == null) return "조치는 필수입니다.";
        return null;
    }
    private void apply(OpsKb kb, Map<String, Object> b) {
        kb.setSysType((String) b.get("sys_type"));
        kb.setGubun((String) b.get("gubun"));
        kb.setSymptom((String) b.get("symptom"));
        kb.setCause((String) b.get("cause"));
        kb.setAction((String) b.get("action"));
        kb.setKeywords((String) b.get("keywords"));
        kb.setPrevention((String) b.get("prevention"));
        // category 는 폼에서 전송하지 않음 — 값이 있을 때만 설정(수정 시 기존 분류 보존)
        String cat = blank((String) b.get("category"));
        if (cat != null) kb.setCategory(cat);
    }
    private String genKbCode() {
        int year = java.time.LocalDate.now().getYear();
        Long seq = opsKbRepository.nextManualSeq();
        return String.format("KB-%d-%05d", year, seq);
    }
    private Map<String, Object> toDto(OpsKb k) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kb_id", k.getKbId());
        m.put("kb_code", k.getKbCode());
        m.put("gubun", k.getGubun());
        m.put("sys_type", k.getSysType());
        m.put("category", k.getCategory());
        m.put("symptom", k.getSymptom());
        m.put("cause", k.getCause());
        m.put("summary", k.getSummary());
        m.put("action", (k.getAction() != null && !k.getAction().isBlank()) ? k.getAction() : k.getCauseDesc());
        m.put("prevention", k.getPrevention());
        m.put("keywords", k.getKeywords());
        m.put("case_count", k.getCaseCount());
        m.put("source", k.getSource());
        return m;
    }
    private static String blank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
}
