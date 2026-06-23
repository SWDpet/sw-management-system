package com.swmanager.system.controller.ops;

import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.domain.SysMst;
import com.swmanager.system.dto.ops.KbCreateResult;
import com.swmanager.system.dto.ops.OpsKbDto;
import com.swmanager.system.dto.ops.OpsKbForm;
import com.swmanager.system.dto.ops.RejectForm;
import com.swmanager.system.dto.ops.SysMstOption;
import com.swmanager.system.response.ApiResult;
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
@lombok.extern.slf4j.Slf4j
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
    /** 단건 소유 스코프: 관리자=무제한, 그 외 작성자 본인만(시드는 created_by=null 이라 비관리자 매칭 안 됨). */
    private boolean ownsOrAdmin(OpsKb kb, CustomUserDetails u) {
        if (isAdmin()) return true;
        String me = (u != null) ? u.getUsername() : null;
        return me != null && me.equals(kb.getCreatedBy());
    }
    /** ACTIVE 가 아닌 단건(PENDING/REJECTED/DELETED) 접근 가능 여부 — 비ACTIVE 는 편집권한+소유 필요. */
    private boolean canAccessNonActive(OpsKb kb, CustomUserDetails u) {
        if ("DELETED".equals(kb.getStatus())) return isAdmin();   // 삭제본은 관리자만
        return canEdit(u) && ownsOrAdmin(kb, u);
    }
    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "문서 편집 권한(authDocument=EDIT)이 필요합니다."));
    }
    private ResponseEntity<?> forbiddenAdmin() {
        return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "승인/반려는 관리자만 가능합니다."));
    }
    private List<String> sysList() {
        List<String> l = new ArrayList<>();
        for (SysMst s : sysMstRepository.findAll(Sort.by("nm"))) l.add(s.getCd());
        return l;
    }
    private List<SysMstOption> sysOptions() {
        List<SysMstOption> l = new ArrayList<>();
        for (SysMst s : sysMstRepository.findAll(Sort.by("nm"))) l.add(SysMstOption.from(s));
        return l;
    }

    // ===== 화면 =====
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal CustomUserDetails u) {
        if (!canView(u)) return "redirect:/";
        boolean admin = isAdmin();
        model.addAttribute("systems", sysOptions());
        model.addAttribute("canEdit", canEdit(u));
        model.addAttribute("isAdmin", admin);
        // 승인 대기 배지: 관리자=전체 / 편집권한자=본인 제출
        long pending = admin ? opsKbRepository.countByStatus("PENDING")
                : (canEdit(u) && u != null ? opsKbRepository.countByStatusAndCreatedBy("PENDING", u.getUsername()) : 0);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("myUsername", u != null ? u.getUsername() : "");
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
        if (kb == null || "DELETED".equals(kb.getStatus())) return "redirect:/ops-kb";
        // 비ACTIVE(대기/반려)는 작성자 본인 또는 관리자만 열람/수정
        if (!"ACTIVE".equals(kb.getStatus()) && !ownsOrAdmin(kb, u)) return "redirect:/ops-kb";
        model.addAttribute("systems", sysOptions());
        model.addAttribute("kb", kb);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeMenu", "ops");
        return "ops-doc/kb-form";
    }

    // ===== 조회 API =====
    @GetMapping("/api/list")
    @ResponseBody
    public List<OpsKbDto> search(@RequestParam(required = false) String sysType,
                                            @RequestParam(required = false) String gubun,
                                            @RequestParam(required = false) String kw,
                                            @RequestParam(required = false) String status,
                                            @AuthenticationPrincipal CustomUserDetails u) {
        List<OpsKbDto> out = new ArrayList<>();
        if (!canView(u)) return out;
        String st = blank(status);
        if (st == null) st = "ACTIVE";
        if ("DELETED".equals(st)) return out;  // 삭제본은 조회 불가
        // [ops-kb-approval] 비공개 상태(PENDING/REJECTED) 큐 접근 통제
        String ownerScope = null;
        if (!"ACTIVE".equals(st)) {
            if (!canEdit(u)) return out;                 // VIEW 전용은 대기/반려 큐 접근 불가
            if (!isAdmin()) ownerScope = (u != null ? u.getUsername() : " ");  // 편집권한자=본인 제출만
        }
        for (OpsKb kb : opsKbRepository.search(st, blank(sysType), blank(gubun), blank(kw), ownerScope))
            out.add(toDto(kb));
        return out;
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> detail(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        if (!canView(u)) return forbidden();
        OpsKb kb = opsKbRepository.findById(id).orElse(null);
        if (kb == null || "DELETED".equals(kb.getStatus())) return ResponseEntity.status(404).body(ApiResult.fail());
        // 비ACTIVE(대기/반려)는 작성자 본인 또는 관리자만 단건 조회
        if (!"ACTIVE".equals(kb.getStatus()) && !canAccessNonActive(kb, u)) return forbidden();
        return ResponseEntity.ok(toDto(kb));
    }

    // ===== CRUD (EDIT) =====
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody OpsKbForm form,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        log.info("[KB-CREATE] user={}, canEdit={}, gubun={}, sysType={}, symptomLen={}, actionLen={}, causeLen={}",
                (u != null ? u.getUsername() : null), canEdit(u), form.gubun(), form.sysType(),
                (form.symptom() + "").trim().length(), (form.action() + "").trim().length(),
                (form.cause() + "").trim().length());
        if (!canEdit(u)) return forbidden();
        String err = validate(form);
        if (err != null) {
            log.warn("[KB-CREATE] 검증 실패: {}", err);
            return ResponseEntity.badRequest().body(ApiResult.fail("INVALID_INPUT", err));
        }
        OpsKb kb = new OpsKb();
        apply(kb, form);
        kb.setSource("MANUAL");
        // [ops-kb-approval] 관리자 등록=즉시 게시(ACTIVE), 편집권한자 등록=승인 대기(PENDING)
        boolean admin = isAdmin();
        kb.setStatus(admin ? "ACTIVE" : "PENDING");
        if (admin) { kb.setReviewedBy(u != null ? u.getUsername() : null); kb.setReviewedAt(java.time.LocalDateTime.now()); }
        kb.setRewritten(false);
        if (kb.getCaseCount() == null) kb.setCaseCount(1);
        kb.setCreatedBy(u != null ? u.getUsername() : null);
        kb.setKbCode(genKbCode());
        OpsKb saved = opsKbRepository.save(kb);
        log.info("[KB-CREATE] 저장 성공 kb_id={}, code={}, status={}",
                saved.getKbId(), saved.getKbCode(), saved.getStatus());
        return ResponseEntity.ok(KbCreateResult.of(saved));
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody OpsKbForm form,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        OpsKb kb = opsKbRepository.findById(id).orElse(null);
        if (kb == null || "DELETED".equals(kb.getStatus())) return ResponseEntity.status(404).body(ApiResult.fail());
        // 비ACTIVE(대기/반려)는 작성자 본인 또는 관리자만 수정. ACTIVE 는 협업 편집 허용(→재승인).
        if (!"ACTIVE".equals(kb.getStatus()) && !ownsOrAdmin(kb, u)) return forbidden();
        String err = validate(form);
        if (err != null) return ResponseEntity.badRequest().body(ApiResult.fail("INVALID_INPUT", err));
        apply(kb, form);
        // [ops-kb-approval] 관리자 수정=ACTIVE 유지, 편집권한자 수정=PENDING 재승인(이전 반려/승인 흔적 초기화)
        if (isAdmin()) {
            if (!"DELETED".equals(kb.getStatus())) kb.setStatus("ACTIVE");
            kb.setReviewedBy(u != null ? u.getUsername() : null);
            kb.setReviewedAt(java.time.LocalDateTime.now());
            kb.setRejectReason(null);
        } else {
            kb.setStatus("PENDING");
            kb.setReviewedBy(null);
            kb.setReviewedAt(null);
            kb.setRejectReason(null);
        }
        opsKbRepository.save(kb);
        return ResponseEntity.ok(Map.of("success", true, "status", kb.getStatus()));
    }

    // ===== 승인/반려 (ADMIN 전용) =====
    @PostMapping("/api/{id}/approve")
    @ResponseBody
    public ResponseEntity<?> approve(@PathVariable Long id,
                                                       @AuthenticationPrincipal CustomUserDetails u) {
        if (!isAdmin()) return forbiddenAdmin();
        OpsKb kb = opsKbRepository.findById(id).orElse(null);
        if (kb == null || "DELETED".equals(kb.getStatus())) return ResponseEntity.status(404).body(ApiResult.fail());
        kb.setStatus("ACTIVE");
        kb.setReviewedBy(u != null ? u.getUsername() : null);
        kb.setReviewedAt(java.time.LocalDateTime.now());
        kb.setRejectReason(null);
        opsKbRepository.save(kb);
        return ResponseEntity.ok(ApiResult.ok());
    }

    @PostMapping("/api/{id}/reject")
    @ResponseBody
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody RejectForm form,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        if (!isAdmin()) return forbiddenAdmin();
        OpsKb kb = opsKbRepository.findById(id).orElse(null);
        if (kb == null || "DELETED".equals(kb.getStatus())) return ResponseEntity.status(404).body(ApiResult.fail());
        String reason = (form != null) ? blank(form.reason()) : null;
        if (reason == null) return ResponseEntity.badRequest().body(ApiResult.fail("INVALID_INPUT", "반려 사유는 필수입니다."));
        kb.setStatus("REJECTED");
        kb.setReviewedBy(u != null ? u.getUsername() : null);
        kb.setReviewedAt(java.time.LocalDateTime.now());
        kb.setRejectReason(reason);
        opsKbRepository.save(kb);
        return ResponseEntity.ok(ApiResult.ok());
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        OpsKb kb = opsKbRepository.findById(id).orElse(null);
        if (kb == null || "DELETED".equals(kb.getStatus())) return ResponseEntity.ok(ApiResult.ok());  // 멱등
        // 비관리자는 본인 등록분만 삭제(시드·타인 콘텐츠 보호)
        if (!ownsOrAdmin(kb, u)) return forbidden();
        kb.setStatus("DELETED");  // 소프트삭제
        opsKbRepository.save(kb);
        return ResponseEntity.ok(ApiResult.ok());
    }

    // ===== 헬퍼 =====
    private String validate(OpsKbForm f) {
        String gubun = blank(f.gubun());
        if (blank(f.sysType()) == null) return "시스템은 필수입니다.";
        if (gubun == null) return "구분(장애/업무)은 필수입니다.";
        // 업무(지원)는 요청내용/처리내용 중심 — 라벨 기준 안내
        boolean isFault = "장애".equals(gubun);
        if (blank(f.symptom()) == null) return isFault ? "증상은 필수입니다." : "요청내용은 필수입니다.";
        // 원인은 장애에만 필수(업무지원엔 해당 없음)
        if (isFault && blank(f.cause()) == null) return "원인은 필수입니다.";
        if (blank(f.action()) == null) return isFault ? "조치는 필수입니다." : "처리내용은 필수입니다.";
        return null;
    }
    private void apply(OpsKb kb, OpsKbForm f) {
        kb.setSysType(f.sysType());
        kb.setGubun(f.gubun());
        kb.setSymptom(f.symptom());
        kb.setCause(f.cause());
        kb.setAction(f.action());
        kb.setKeywords(f.keywords());
        kb.setPrevention(f.prevention());
        // category 는 폼에서 전송하지 않음 — 값이 있을 때만 설정(수정 시 기존 분류 보존)
        String cat = blank(f.category());
        if (cat != null) kb.setCategory(cat);
    }
    private String genKbCode() {
        int year = java.time.LocalDate.now().getYear();
        Long seq = opsKbRepository.nextManualSeq();
        return String.format("KB-%d-%05d", year, seq);
    }
    private OpsKbDto toDto(OpsKb k) {
        return OpsKbDto.from(k);
    }
    private static String blank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
}
