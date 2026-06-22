package com.swmanager.system.controller;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.domain.workplan.ContractParticipant;
import com.swmanager.system.dto.workplan.ContractParticipantRow;
import com.swmanager.system.dto.workplan.ParticipantSaveResult;
import com.swmanager.system.dto.workplan.SecureContractParticipantRow;
import com.swmanager.system.response.ApiResult;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.ContractParticipantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 사업별 과업참여자(contract-participant) API — DocumentController 에서 분리 (S4 giant-class-split §6-5).
 *
 * 클래스 레벨 @RequestMapping("/document") 을 DocumentController 와 동일하게 유지하여 URL 100% 보존.
 * 권한 헬퍼(getAuth/getCurrentUser/isAdmin)는 분리 시 복제(공용 util 추출은 후속).
 */
@Slf4j
@Controller
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentParticipantController {

    private final ContractParticipantRepository contractParticipantRepository;
    private final SwProjectRepository swProjectRepository;
    private final UserRepository userRepository;

    // === 권한 헬퍼 (DocumentController 와 공용 — 분리 시 복제, S4) ===

    private CustomUserDetails getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) return (CustomUserDetails) principal;
            return null;
        } catch (Exception e) { return null; }
    }

    private boolean isAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } catch (Exception e) { return false; }
    }

    private String getAuth() {
        if (isAdmin()) return "EDIT";
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return "NONE";
        String auth = cu.getUser().getAuthDocument();
        return (auth != null) ? auth : "NONE";
    }

    /**
     * 사업별 과업참여자 조회 (비민감) — 감사 P1-3 조치 (2026-04-18):
     * ssn/certificate 제거. 민감 정보가 필요하면 `/secure` 엔드포인트 사용.
     */
    @GetMapping("/api/contract-participants/{projId}")
    @ResponseBody
    public List<ContractParticipantRow> getContractParticipants(@PathVariable Long projId) {
        List<ContractParticipant> list = contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(projId);
        List<ContractParticipantRow> result = new ArrayList<>();
        for (ContractParticipant cp : list) {
            result.add(ContractParticipantRow.from(cp));
        }
        return result;
    }

    /**
     * 사업별 과업참여자 조회 (민감 필드 포함) — 감사 P1-3 조치 (2026-04-18):
     * EDIT 권한 필수.
     */
    @GetMapping("/api/contract-participants/{projId}/secure")
    @ResponseBody
    public ResponseEntity<?> getContractParticipantsSecure(@PathVariable Long projId) {
        if (!"EDIT".equals(getAuth())) {
            // 현행 wire 보존: {error:{code,message}} (success 키 없음 — ApiResult.fail 은 success 추가하므로 미사용)
            return ResponseEntity.status(403)
                    .body(Map.of("error", Map.of("code", "FORBIDDEN", "message", "민감 정보 조회 권한이 없습니다")));
        }
        List<ContractParticipant> list = contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(projId);
        List<SecureContractParticipantRow> result = new ArrayList<>();
        for (ContractParticipant cp : list) {
            result.add(SecureContractParticipantRow.from(cp));
        }
        return ResponseEntity.ok(result);
    }

    /** 사업별 과업참여자 저장 — 감사 P1-2 조치: EDIT 권한 체크 + HTTP 403 반환 (2026-04-19) */
    @PostMapping("/api/contract-participants/{projId}")
    @ResponseBody
    public ResponseEntity<?> saveContractParticipants(
            @PathVariable Long projId,
            @RequestBody List<Map<String, Object>> participantList) {
        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "수정 권한이 없습니다"));
        }
        try {
            var project = swProjectRepository.findById(projId).orElse(null);
            if (project == null) {
                return ResponseEntity.status(404).body(ApiResult.failMessage("사업을 찾을 수 없습니다."));
            }

            // 기존 참여자 삭제 후 재등록
            List<ContractParticipant> existing = contractParticipantRepository.findByProject_ProjIdOrderBySortOrder(projId);
            contractParticipantRepository.deleteAll(existing);

            // 요청바디는 lenient Map 파싱 보존(§6-4 participant-save-dto: 누락/null·string-boolean·malformed→200 의미 유지)
            int order = 0;
            for (Map<String, Object> item : participantList) {
                ContractParticipant cp = new ContractParticipant();
                cp.setProject(project);

                Object userIdObj = item.get("userId");
                if (userIdObj != null) {
                    Long userId = Long.parseLong(userIdObj.toString());
                    userRepository.findById(userId).ifPresent(cp::setUser);
                }

                cp.setRoleType((String) item.getOrDefault("roleType", "PARTICIPANT"));
                cp.setTechGrade((String) item.getOrDefault("techGrade", ""));
                cp.setTaskDesc((String) item.getOrDefault("taskDesc", ""));
                cp.setIsSiteRep(Boolean.TRUE.equals(item.get("isSiteRep")));
                cp.setSortOrder(order++);

                contractParticipantRepository.save(cp);
            }

            return ResponseEntity.ok(new ParticipantSaveResult(true, participantList.size()));
        } catch (Exception e) {
            log.error("과업참여자 저장 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));
        }
    }
}
