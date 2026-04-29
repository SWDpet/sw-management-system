package com.swmanager.system.service.ops;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentDetail;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocumentDetailRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 운영·유지보수 문서 CRUD 서비스 — 4 종 (FAULT/SUPPORT/INSTALL/PATCH).
 * 점검내역서(INSPECT) 는 InspectReportService 가 처리, 본 서비스는 row 조회만 담당.
 *
 * 기획서: docs/product-specs/doc-split-ops.md (v3, FR-3, FR-4)
 * 개발계획: docs/exec-plans/doc-split-ops.md (v2, Step 4)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OpsDocService {

    private final OpsDocumentRepository opsDocumentRepository;
    private final OpsDocumentDetailRepository opsDocumentDetailRepository;
    private final UserRepository userRepository;

    /** doc_type 별 section_data 필수 키 (codex 권고 — FR-13). */
    private static final Map<OpsDocType, Set<String>> REQUIRED_SECTION_KEYS = Map.of(
            OpsDocType.FAULT,   Set.of("fault_date", "severity", "symptom", "action"),
            OpsDocType.SUPPORT, Set.of("request_date", "request_channel", "requester", "support_content"),
            OpsDocType.INSTALL, Set.of("install_date", "pre_check_completed", "version", "verification"),
            OpsDocType.PATCH,   Set.of("patch_date", "patch_kind", "target", "version", "rollback_plan")
    );

    @Transactional(readOnly = true)
    public Optional<OpsDocument> findById(Long docId) {
        return opsDocumentRepository.findById(docId);
    }

    @Transactional(readOnly = true)
    public List<OpsDocument> findByDocType(OpsDocType docType) {
        return opsDocumentRepository.findByDocTypeOrderByCreatedAtDesc(docType);
    }

    @Transactional(readOnly = true)
    public List<OpsDocument> findAll() {
        return opsDocumentRepository.findAll();
    }

    /** 신규 저장 — section_data 필수 필드 검증 후 row + detail 저장. */
    public OpsDocument create(OpsDocument doc, Map<String, Object> sectionData, String currentUserId) {
        validateSectionData(doc.getDocType(), sectionData);
        if (doc.getDocNo() == null || doc.getDocNo().isBlank()) {
            doc.setDocNo(generateDocNo(doc.getDocType()));
        }
        if (doc.getAuthor() == null && currentUserId != null) {
            User u = userRepository.findByUserid(currentUserId).orElse(null);
            if (u != null) doc.setAuthor(u);
        }
        if (doc.getStatus() == null) doc.setStatus(DocumentStatus.DRAFT);
        // doc-split-ops Step 10 P1: COMPLETED 상태로 신규 저장 시 approvedAt 동기화
        // (PerformanceService.countCompletedByTypeAndUser BETWEEN approvedAt 매치 보장).
        if (DocumentStatus.COMPLETED == doc.getStatus() && doc.getApprovedAt() == null) {
            doc.setApprovedAt(LocalDateTime.now());
        }
        doc.setCreatedBy(currentUserId);
        doc.setUpdatedBy(currentUserId);

        OpsDocument saved = opsDocumentRepository.save(doc);

        if (sectionData != null && !sectionData.isEmpty()) {
            OpsDocumentDetail detail = new OpsDocumentDetail();
            detail.setDocument(saved);
            detail.setSectionKey("main");
            detail.setSectionData(new HashMap<>(sectionData));
            opsDocumentDetailRepository.save(detail);
        }

        return saved;
    }

    /** 수정 — 기존 row + detail 갱신. */
    public OpsDocument update(Long docId, OpsDocument changes, Map<String, Object> sectionData, String currentUserId) {
        OpsDocument existing = opsDocumentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("ops_doc not found: " + docId));
        validateSectionData(existing.getDocType(), sectionData);

        if (changes.getTitle() != null) existing.setTitle(changes.getTitle());
        if (changes.getStatus() != null) {
            DocumentStatus prev = existing.getStatus();
            existing.setStatus(changes.getStatus());
            // doc-split-ops Step 10 P1: 비-COMPLETED → COMPLETED 전이 시 approvedAt 세팅.
            if (DocumentStatus.COMPLETED == changes.getStatus() && prev != DocumentStatus.COMPLETED
                    && existing.getApprovedAt() == null) {
                existing.setApprovedAt(LocalDateTime.now());
            }
        }
        if (changes.getSysType() != null) existing.setSysType(changes.getSysType());
        if (changes.getRegionCode() != null) existing.setRegionCode(changes.getRegionCode());
        if (changes.getEnvironment() != null) existing.setEnvironment(changes.getEnvironment());
        if (changes.getSupportTargetType() != null) existing.setSupportTargetType(changes.getSupportTargetType());
        if (changes.getInfra() != null) existing.setInfra(changes.getInfra());
        if (changes.getOrgUnit() != null) existing.setOrgUnit(changes.getOrgUnit());
        existing.setUpdatedBy(currentUserId);

        opsDocumentRepository.save(existing);

        if (sectionData != null && !sectionData.isEmpty()) {
            opsDocumentDetailRepository.deleteByDocument_DocId(docId);
            OpsDocumentDetail detail = new OpsDocumentDetail();
            detail.setDocument(existing);
            detail.setSectionKey("main");
            detail.setSectionData(new HashMap<>(sectionData));
            opsDocumentDetailRepository.save(detail);
        }
        return existing;
    }

    public void delete(Long docId) {
        opsDocumentRepository.deleteById(docId);
    }

    /** docNo 자동 채번 — prefix-yyyy-{seq}호 (사업측 패턴 호환). */
    private String generateDocNo(OpsDocType docType) {
        String yyyy = String.valueOf(LocalDate.now().getYear());
        String prefix = docType.docNoPrefix() + "-" + yyyy + "-";
        Integer max = opsDocumentRepository.findMaxDocNoSeq(prefix + "%");
        int next = (max != null ? max : 0) + 1;
        return prefix + next;
    }

    /** doc_type 별 필수 section_data 키 검증 (codex 권고 — FR-13).
     *  INSPECT 는 InspectReportService 가 별도 처리하므로 검증 면제. */
    private void validateSectionData(OpsDocType docType, Map<String, Object> sectionData) {
        if (docType == null || docType == OpsDocType.INSPECT) return;
        Set<String> required = REQUIRED_SECTION_KEYS.get(docType);
        if (required == null) return;
        if (sectionData == null) {
            throw new IllegalArgumentException("section_data required for " + docType + " — keys: " + required);
        }
        for (String key : required) {
            Object v = sectionData.get(key);
            if (v == null || (v instanceof String s && s.isBlank())) {
                throw new IllegalArgumentException("section_data[" + key + "] is required for " + docType);
            }
        }
    }
}
