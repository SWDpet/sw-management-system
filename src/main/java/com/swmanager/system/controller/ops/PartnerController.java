package com.swmanager.system.controller.ops;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.domain.ops.Partner;
import com.swmanager.system.domain.ops.PartnerContact;
import com.swmanager.system.dto.ops.ContactForm;
import com.swmanager.system.dto.ops.PartnerForm;
import com.swmanager.system.dto.ops.PartnerListContactRow;
import com.swmanager.system.dto.ops.PartnerListRow;
import com.swmanager.system.repository.ops.PartnerContactRepository;
import com.swmanager.system.repository.ops.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 외부업체(협력사) 관리 — ops-fault-support M2/Step 3 (FR-M2-3).
 * 권한: authPerson 재사용 (담당자 관리와 동질의 관계자 마스터).
 */
@Controller
@RequestMapping("/partner")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerRepository partnerRepository;
    private final PartnerContactRepository partnerContactRepository;

    private String auth(CustomUserDetails u) {
        if (u == null || u.getUser() == null) return "NONE";
        String a = u.getUser().getAuthPerson();
        return a != null ? a : "NONE";
    }
    private boolean canView(CustomUserDetails u) { return !"NONE".equals(auth(u)); }
    private boolean canEdit(CustomUserDetails u) { return "EDIT".equals(auth(u)); }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("success", false,
                "error", Map.of("code", "FORBIDDEN", "message", "담당자 편집 권한(authPerson=EDIT)이 필요합니다.")));
    }

    // ===== 화면 =====
    @GetMapping
    public String manage(Model model, @AuthenticationPrincipal CustomUserDetails u) {
        model.addAttribute("canEdit", canEdit(u));
        model.addAttribute("activeMenu", "ops");
        return "ops-doc/partner-management";
    }

    // ===== 조회 =====
    @GetMapping("/api/list")
    @ResponseBody
    public List<PartnerListRow> list(@AuthenticationPrincipal CustomUserDetails u) {
        List<PartnerListRow> result = new ArrayList<>();
        if (!canView(u)) return result;
        for (Partner p : partnerRepository.findByUseYnOrderByNameAsc("Y")) {
            List<PartnerListContactRow> contacts = partnerContactRepository
                    .findByPartner_PartnerIdAndUseYnOrderByNameAsc(p.getPartnerId(), "Y")
                    .stream().map(PartnerListContactRow::from).toList();
            result.add(PartnerListRow.from(p, contacts));
        }
        return result;
    }

    // ===== 업체 CRUD =====
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody PartnerForm form,
                                    @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        String name = form.name();
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false,
                    "error", Map.of("code", "INVALID_INPUT", "message", "업체명은 필수입니다.")));
        }
        Partner p = new Partner();
        applyPartner(p, form);
        Partner saved = partnerRepository.save(p);
        return ResponseEntity.ok(Map.of("success", true, "partner_id", saved.getPartnerId()));
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PartnerForm form,
                                    @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        Partner p = partnerRepository.findById(id).orElse(null);
        if (p == null) return ResponseEntity.status(404).body(Map.of("success", false));
        applyPartner(p, form);
        partnerRepository.save(p);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        Partner p = partnerRepository.findById(id).orElse(null);
        if (p != null) { p.setUseYn("N"); partnerRepository.save(p); }  // 소프트 삭제
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void applyPartner(Partner p, PartnerForm form) {
        p.setName(form.name());
        p.setPartnerType(form.partnerType());
        p.setBizNo(form.bizNo());
        p.setMainTel(form.mainTel());
        p.setNote(form.note());
    }

    // ===== 담당자 CRUD =====
    @PostMapping("/api/{partnerId}/contact")
    @ResponseBody
    public ResponseEntity<?> addContact(@PathVariable Long partnerId,
                                        @RequestBody ContactForm form,
                                        @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        Partner p = partnerRepository.findById(partnerId).orElse(null);
        if (p == null) return ResponseEntity.status(404).body(Map.of("success", false));
        String name = form.name();
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false,
                    "error", Map.of("code", "INVALID_INPUT", "message", "담당자명은 필수입니다.")));
        }
        PartnerContact c = new PartnerContact();
        c.setPartner(p);
        c.setName(name);
        c.setPosition(form.position());
        c.setTel(form.tel());
        c.setEmail(form.email());
        PartnerContact saved = partnerContactRepository.save(c);
        return ResponseEntity.ok(Map.of("success", true, "contact_id", saved.getContactId()));
    }

    @DeleteMapping("/api/contact/{contactId}")
    @ResponseBody
    public ResponseEntity<?> deleteContact(@PathVariable Long contactId,
                                           @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        PartnerContact c = partnerContactRepository.findById(contactId).orElse(null);
        if (c != null) { c.setUseYn("N"); partnerContactRepository.save(c); }
        return ResponseEntity.ok(Map.of("success", true));
    }
}
