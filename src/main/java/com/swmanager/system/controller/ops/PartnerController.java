package com.swmanager.system.controller.ops;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.domain.ops.Partner;
import com.swmanager.system.domain.ops.PartnerContact;
import com.swmanager.system.repository.ops.PartnerContactRepository;
import com.swmanager.system.repository.ops.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    private ResponseEntity<Map<String, Object>> forbidden() {
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
    public List<Map<String, Object>> list(@AuthenticationPrincipal CustomUserDetails u) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (!canView(u)) return result;
        for (Partner p : partnerRepository.findByUseYnOrderByNameAsc("Y")) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("partner_id", p.getPartnerId());
            m.put("name", p.getName());
            m.put("partner_type", p.getPartnerType());
            m.put("main_tel", p.getMainTel());
            m.put("note", p.getNote());
            List<Map<String, Object>> contacts = new ArrayList<>();
            for (PartnerContact c : partnerContactRepository
                    .findByPartner_PartnerIdAndUseYnOrderByNameAsc(p.getPartnerId(), "Y")) {
                Map<String, Object> cm = new LinkedHashMap<>();
                cm.put("contact_id", c.getContactId());
                cm.put("name", c.getName());
                cm.put("position", c.getPosition());
                cm.put("tel", c.getTel());
                cm.put("email", c.getEmail());
                contacts.add(cm);
            }
            m.put("contacts", contacts);
            result.add(m);
        }
        return result;
    }

    // ===== 업체 CRUD =====
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body,
                                                       @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false,
                    "error", Map.of("code", "INVALID_INPUT", "message", "업체명은 필수입니다.")));
        }
        Partner p = new Partner();
        applyPartner(p, body);
        Partner saved = partnerRepository.save(p);
        return ResponseEntity.ok(Map.of("success", true, "partner_id", saved.getPartnerId()));
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        Partner p = partnerRepository.findById(id).orElse(null);
        if (p == null) return ResponseEntity.status(404).body(Map.of("success", false));
        applyPartner(p, body);
        partnerRepository.save(p);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        Partner p = partnerRepository.findById(id).orElse(null);
        if (p != null) { p.setUseYn("N"); partnerRepository.save(p); }  // 소프트 삭제
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void applyPartner(Partner p, Map<String, Object> body) {
        p.setName((String) body.get("name"));
        p.setPartnerType((String) body.get("partner_type"));
        p.setBizNo((String) body.get("biz_no"));
        p.setMainTel((String) body.get("main_tel"));
        p.setNote((String) body.get("note"));
    }

    // ===== 담당자 CRUD =====
    @PostMapping("/api/{partnerId}/contact")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addContact(@PathVariable Long partnerId,
                                                          @RequestBody Map<String, Object> body,
                                                          @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        Partner p = partnerRepository.findById(partnerId).orElse(null);
        if (p == null) return ResponseEntity.status(404).body(Map.of("success", false));
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false,
                    "error", Map.of("code", "INVALID_INPUT", "message", "담당자명은 필수입니다.")));
        }
        PartnerContact c = new PartnerContact();
        c.setPartner(p);
        c.setName(name);
        c.setPosition((String) body.get("position"));
        c.setTel((String) body.get("tel"));
        c.setEmail((String) body.get("email"));
        PartnerContact saved = partnerContactRepository.save(c);
        return ResponseEntity.ok(Map.of("success", true, "contact_id", saved.getContactId()));
    }

    @DeleteMapping("/api/contact/{contactId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Long contactId,
                                                             @AuthenticationPrincipal CustomUserDetails u) {
        if (!canEdit(u)) return forbidden();
        PartnerContact c = partnerContactRepository.findById(contactId).orElse(null);
        if (c != null) { c.setUseYn("N"); partnerContactRepository.save(c); }
        return ResponseEntity.ok(Map.of("success", true));
    }
}
