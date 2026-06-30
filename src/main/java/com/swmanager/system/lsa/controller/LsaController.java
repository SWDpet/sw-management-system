package com.swmanager.system.lsa.controller;

import com.swmanager.system.exception.InsufficientPermissionException;
import com.swmanager.system.lsa.dto.LsaDTO;
import com.swmanager.system.lsa.dto.LsaForm;
import com.swmanager.system.lsa.dto.PersonRow;
import com.swmanager.system.lsa.service.LsaService;
import com.swmanager.system.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * LSA(라이선스 발급 대장) 컨트롤러.
 * License4J/GeoNURIS 와 독립된 auth_lsa 권한. 인증=QuotationController 패턴(throw → GlobalExceptionHandler 403).
 * P2: 목록·검색. P3 입력폼·ps_info / P4 상세·수정·삭제.
 */
@Slf4j
@Controller
@RequestMapping("/lsa")
@RequiredArgsConstructor
public class LsaController {

    private final LsaService lsaService;

    /** 현재 로그인 사용자 */
    private CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        }
        return null;
    }

    /** LSA 조회 권한 체크 (VIEW 이상 또는 관리자) */
    private void checkViewAuth() {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) throw new InsufficientPermissionException("로그인");
        String role = cu.getUser().getUserRole();
        String authLsa = cu.getUser().getAuthLsa();
        if (!"ROLE_ADMIN".equals(role) && (authLsa == null || "NONE".equals(authLsa))) {
            log.warn("LSA 접근 권한 없음 - 사용자: {}, authLsa: {}", cu.getUsername(), authLsa);
            throw new InsufficientPermissionException("LSA 조회");
        }
    }

    /** LSA 편집 권한 체크 (EDIT 또는 관리자) */
    private void checkEditAuth() {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) throw new InsufficientPermissionException("로그인");
        String role = cu.getUser().getUserRole();
        String authLsa = cu.getUser().getAuthLsa();
        if (!"ROLE_ADMIN".equals(role) && !"EDIT".equals(authLsa)) {
            log.warn("LSA 편집 권한 없음 - 사용자: {}, authLsa: {}", cu.getUsername(), authLsa);
            throw new InsufficientPermissionException("LSA 작성");
        }
    }

    private boolean isAdmin() {
        CustomUserDetails cu = getCurrentUser();
        return cu != null && "ROLE_ADMIN".equals(cu.getUser().getUserRole());
    }

    /** 현재 사용자가 해당 LSA 작성자인지 (createdBy = 로그인 ID — getDisplayName 실명 아님). */
    private boolean isOwner(LsaDTO l) {
        CustomUserDetails cu = getCurrentUser();
        return l != null && l.createdBy() != null && cu != null
                && l.createdBy().equals(cu.getUsername());
    }

    /** 편집 권한(EDIT|admin) + 소유권(또는 관리자) 게이트. 작성자 본인 아니면 403. */
    private void checkEditOwnership(Long id) {
        checkEditAuth();   // EDIT|admin 1차 (위조 1차 차단)
        if (!isAdmin() && !isOwner(lsaService.getById(id))) {
            log.warn("LSA 작성자 본인 아님 - 사용자: {}, lsaId: {}", getCurrentUser().getUsername(), id);
            throw new InsufficientPermissionException("LSA 작성자 본인");
        }
    }

    /** LSA 목록 + 키워드 검색 (지자체/이름/버전/발급자) */
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String keyword, Model model) {
        checkViewAuth();
        model.addAttribute("lsaList", lsaService.list(keyword));
        model.addAttribute("keyword", keyword);
        CustomUserDetails cu = getCurrentUser();
        String role = cu.getUser().getUserRole();
        String authLsa = cu.getUser().getAuthLsa();
        boolean admin = "ROLE_ADMIN".equals(role);
        // 행단위 소유권 판정용: admin 은 전체, EDIT 는 본인 작성건만 (템플릿에서 dto.createdBy 대조).
        model.addAttribute("isAdmin", admin);
        model.addAttribute("canEditBase", admin || "EDIT".equals(authLsa));
        model.addAttribute("currentUserId", cu.getUsername());   // 로그인 ID(createdBy 와 동일 소스)
        return "lsa/lsa-list";
    }

    /** LSA 작성 폼 (EDIT|admin) */
    @GetMapping("/new")
    public String newForm(Model model) {
        checkEditAuth();
        model.addAttribute("sidoList", lsaService.sidoList());
        model.addAttribute("isAdmin", isAdmin());
        model.addAttribute("issuer", getCurrentUser().getUser().getUsername());   // 로그인 실명
        model.addAttribute("userList", lsaService.issuerCandidates());            // 관리자 발급자 선택
        return "lsa/lsa-form";
    }

    /** LSA 상세 (VIEW) */
    @GetMapping("/{id:\\d+}")
    public String detail(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        checkViewAuth();
        LsaDTO lsa = lsaService.getById(id);
        model.addAttribute("lsa", lsa);
        // 수정/삭제 버튼: 관리자=전체, EDIT=본인 작성건만 (서버 가드 checkEditOwnership 와 일치)
        boolean canEdit = isAdmin()
                || ("EDIT".equals(getCurrentUser().getUser().getAuthLsa()) && isOwner(lsa));
        model.addAttribute("canEdit", canEdit);
        return "lsa/lsa-detail";
    }

    /** LSA 수정 폼 (EDIT|admin + 작성자 본인) — 작성 폼 재사용 + 기존값 prefill */
    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        checkEditOwnership(id);
        model.addAttribute("lsa", lsaService.getById(id));
        model.addAttribute("sidoList", lsaService.sidoList());
        model.addAttribute("isAdmin", isAdmin());
        model.addAttribute("issuer", getCurrentUser().getUser().getUsername());
        model.addAttribute("userList", lsaService.issuerCandidates());
        return "lsa/lsa-form";
    }

    /** LSA 저장(신규/수정) — 발급자: 비관리자=로그인 실명 강제, 관리자=폼값. id 있으면 update. */
    @PostMapping("/save")
    public String save(@ModelAttribute LsaForm form) {
        boolean adminIssuerChange;
        CustomUserDetails cu;
        if (form.getId() != null) {
            // 수정: EDIT|admin + 작성자 본인(관리자 우회). create 와 달리 소유권 가드.
            checkEditOwnership(form.getId());
            cu = getCurrentUser();
            adminIssuerChange = isAdmin() && form.getIssuer() != null && !form.getIssuer().isBlank();
            // 수정: 발급자 보존(비관리자 override=null), 관리자만 폼값으로 변경
            String issuerOverride = adminIssuerChange ? form.getIssuer().trim() : null;
            lsaService.update(form.getId(), form, issuerOverride, cu.getUsername());
        } else {
            // 신규: 소유권 제한 없음(EDIT면 누구나 작성)
            checkEditAuth();
            cu = getCurrentUser();
            String loginName = cu.getUser().getUsername();
            adminIssuerChange = isAdmin() && form.getIssuer() != null && !form.getIssuer().isBlank();
            // 신규: 비관리자=로그인 실명 강제(위조 방지), 관리자=폼값
            String issuer = adminIssuerChange ? form.getIssuer().trim() : loginName;
            lsaService.create(form, issuer, cu.getUsername());
        }
        return "redirect:/lsa/list";
    }

    /** LSA 삭제 (EDIT|admin + 작성자 본인) — lsa_license 만, ps_info 보존 */
    @PostMapping("/{id:\\d+}/delete")
    public String delete(@org.springframework.web.bind.annotation.PathVariable Long id) {
        checkEditOwnership(id);
        lsaService.delete(id);
        return "redirect:/lsa/list";
    }

    /** 시군구 캐스케이드 (시도 → sggNm 목록) */
    @GetMapping("/api/districts")
    @ResponseBody
    public List<String> districts(@RequestParam String sido) {
        checkViewAuth();
        return lsaService.districtList(sido);
    }

    /** ps_info prefill 담당자 후보 (userNm/tel/email 만 — 엔티티 직접반환 금지) */
    @GetMapping("/api/persons")
    @ResponseBody
    public List<PersonRow> persons(@RequestParam String city, @RequestParam String dist,
                                   @RequestParam(required = false) String dept,
                                   @RequestParam(required = false) String team) {
        checkViewAuth();
        return lsaService.findPersonCandidates(city, dist, dept, team);
    }
}
