package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.*;
import com.swmanager.system.dto.DocumentDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.workplan.*;
import com.swmanager.system.i18n.MessageResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentService {

    @Autowired private DocumentRepository documentRepository;
    @Autowired private DocumentDetailRepository documentDetailRepository;
    @Autowired private DocumentHistoryRepository documentHistoryRepository;
    @Autowired private DocumentSignatureRepository documentSignatureRepository;
    // S1 inspect-comprehensive-redesign: InspectChecklist/Issue Repository 제거 (테이블 DROP)
    @Autowired private InfraRepository infraRepository;
    @Autowired private MessageResolver messages;

    // === 문서번호: 수동입력 (타부서에서 부여) ===
    // generateDocNo() 삭제됨 - 문서번호는 비워두고 사용자가 직접 입력

    // === CRUD ===

    @Transactional(readOnly = true)
    public Document getDocumentById(Integer docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.document.not_found", docId)));
    }

    @Transactional(readOnly = true)
    public DocumentDTO getDocumentDTOById(Integer docId) {
        return enrichRegion(DocumentDTO.fromEntity(getDocumentById(docId)));
    }

    @Autowired(required = false)
    private com.swmanager.system.repository.SigunguCodeRepository sigunguCodeRepository;
    @Autowired(required = false)
    private com.swmanager.system.repository.SysMstRepository sysMstRepository;
    @Autowired(required = false)
    private com.swmanager.system.repository.InspectReportRepository inspectReportRepository;
    @Autowired(required = false)
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public Page<DocumentDTO> searchDocuments(String docType, String status,
                                              String cityNm, String distNm,
                                              Long authorId, LocalDateTime from, LocalDateTime to,
                                              String keyword, Pageable pageable) {
        if (docType != null && docType.trim().isEmpty()) docType = null;
        if (status != null && status.trim().isEmpty()) status = null;
        if (cityNm != null && cityNm.trim().isEmpty()) cityNm = null;
        if (distNm != null && distNm.trim().isEmpty()) distNm = null;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;

        Page<Document> page = documentRepository.searchDocuments(
                docType, status, cityNm, distNm, authorId, from, to, keyword, pageable);
        return page.map(d -> enrichInspectMonth(enrichRegion(DocumentDTO.fromEntity(d))));
    }

    /** 점검내역서(INSPECT) docNo "INSP-{year}-{id}" 의 id 로 inspect_report.inspect_month 룩업해서 DTO 에 채움.
     *  라이브 테이블이 비었으면 4/21 백업 테이블에서 fallback. 값은 "MM월" 형태로 정규화. */
    private DocumentDTO enrichInspectMonth(DocumentDTO dto) {
        if (dto == null) return dto;
        if (dto.getDocType() == null || !"INSPECT".equals(dto.getDocType().name())) return dto;
        String docNo = dto.getDocNo();
        if (docNo == null) return dto;
        String[] parts = docNo.split("-");
        String idStr = parts[parts.length - 1];
        long id;
        try { id = Long.parseLong(idStr); } catch (NumberFormatException e) { return dto; }

        String raw = null;
        if (inspectReportRepository != null) {
            raw = inspectReportRepository.findById(id).map(ir -> ir.getInspectMonth()).orElse(null);
        }
        if ((raw == null || raw.isBlank()) && jdbcTemplate != null) {
            try {
                raw = jdbcTemplate.query(
                        "SELECT inspect_month FROM inspect_report_backup_20260421_235813 WHERE id = ?",
                        rs -> rs.next() ? rs.getString(1) : null,
                        id);
            } catch (Exception ignore) {}
        }
        if (raw != null && !raw.isBlank()) dto.setInspectMonth(toMonthLabel(raw));
        return dto;
    }

    /** "yyyy-MM" / "yyyy.MM" / "MM" / "yyyy년 MM월" 등에서 MM 만 추출 → "MM월" */
    private String toMonthLabel(String s) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(0[1-9]|1[0-2])(?!\\d)").matcher(s);
        return m.find() ? m.group(1) + "월" : s;
    }

    /**
     * 같은 사업에 동일 문서가 이미 있는지 확인.
     *  - COMMENCE / COMPLETION : projId 당 1건
     *  - INTERIM               : projId + paymentRound 당 1건
     * @return 충돌 docId, 없으면 null
     */
    @Transactional(readOnly = true)
    public Integer findDuplicateProjDoc(Long projId, com.swmanager.system.constant.enums.DocumentType docType,
                                        Integer paymentRound, Integer excludeDocId) {
        if (projId == null || docType == null) return null;
        List<Document> docs = documentRepository.findByDocTypeOrderByCreatedAtDesc(docType).stream()
                .filter(d -> d.getProject() != null && projId.equals(d.getProject().getProjId()))
                .filter(d -> excludeDocId == null || !excludeDocId.equals(d.getDocId()))
                .toList();
        if (com.swmanager.system.constant.enums.DocumentType.INTERIM == docType) {
            if (paymentRound == null) return null;
            for (Document d : docs) {
                if (d.getDetails() == null) continue;
                for (com.swmanager.system.domain.workplan.DocumentDetail dt : d.getDetails()) {
                    if (!"inspector".equals(dt.getSectionKey())) continue;
                    Map<String, Object> sd = dt.getSectionData();
                    if (sd == null) continue;
                    Object r = sd.get("paymentRound");
                    if (r == null) continue;
                    try {
                        if (Integer.parseInt(r.toString().trim()) == paymentRound) return d.getDocId();
                    } catch (NumberFormatException ignore) {}
                }
            }
            return null;
        }
        return docs.isEmpty() ? null : docs.get(0).getDocId();
    }

    /**
     * [스프린트 5 v2] 4개 문서의 region_code / sys_type 을 sigungu_code / sys_mst 에서 룩업해
     * DTO 에 시도·시군구·시스템명을 채움. 목록/상세 표시용.
     */
    private DocumentDTO enrichRegion(DocumentDTO dto) {
        if (dto == null) return null;
        if (dto.getRegionCode() != null && sigunguCodeRepository != null) {
            sigunguCodeRepository.findById(dto.getRegionCode()).ifPresent(sc -> {
                dto.setRegionSidoNm(sc.getSidoNm());
                dto.setRegionSigunguNm(sc.getSggNm());
            });
        }
        if (dto.getSysType() != null && sysMstRepository != null) {
            sysMstRepository.findById(dto.getSysType()).ifPresent(sm -> dto.setSysNm(sm.getNm()));
        }
        // 4개 문서 targetDisplay 재계산: "시도 시군구 - 시스템명"
        if (dto.getRegionCode() != null) {
            StringBuilder sb = new StringBuilder();
            if (dto.getRegionSidoNm() != null) sb.append(dto.getRegionSidoNm()).append(' ');
            if (dto.getRegionSigunguNm() != null) sb.append(dto.getRegionSigunguNm());
            if (dto.getSysNm() != null) {
                if (sb.length() > 0) sb.append(" - ");
                sb.append(dto.getSysNm());
            } else if (dto.getSysType() != null) {
                if (sb.length() > 0) sb.append(" - ");
                sb.append(dto.getSysType());
            }
            String s = sb.toString().trim();
            if (!s.isEmpty()) dto.setTargetDisplay(s);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<String> getCityNames() {
        return documentRepository.findDistinctCityNames();
    }

    @Transactional(readOnly = true)
    public List<String> getDistNamesByCity(String cityNm) {
        if (cityNm == null || cityNm.isBlank()) return List.of();
        return documentRepository.findDistinctDistNamesByCity(cityNm);
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByInfra(Long infraId) {
        return documentRepository.findByInfra_InfraIdOrderByCreatedAtDesc(infraId)
                .stream().map(DocumentDTO::fromEntity).collect(Collectors.toList());
    }

    /**
     * 문서 생성
     */
    public Document createDocument(DocumentType docType, String sysType, Long infraId,
                                    Integer contractId, Integer planId,
                                    String title, User author) {
        Document doc = new Document();
        // 문서번호는 비워둠 (수동입력)
        doc.setDocType(docType);
        doc.setSysType(sysType);
        doc.setTitle(title);
        doc.setStatus(DocumentStatus.DRAFT);
        doc.setAuthor(author);

        if (infraId != null) {
            doc.setInfra(infraRepository.findById(infraId).orElse(null));
        }

        Document saved = documentRepository.save(doc);

        // 이력 기록
        recordHistory(saved, "CREATE", null, null, null, author, "문서 생성");

        return saved;
    }

    /**
     * 문서 상세 섹션 저장 (JSON)
     */
    public DocumentDetail saveSection(Integer docId, String sectionKey,
                                       Map<String, Object> sectionData, Integer sortOrder) {
        Document doc = getDocumentById(docId);

        // 기존 섹션 업데이트 또는 신규 생성
        DocumentDetail detail = doc.getDetails().stream()
                .filter(d -> d.getSectionKey().equals(sectionKey))
                .findFirst()
                .orElseGet(() -> {
                    DocumentDetail newDetail = new DocumentDetail();
                    newDetail.setDocument(doc);
                    newDetail.setSectionKey(sectionKey);
                    return newDetail;
                });

        detail.setSectionData(sectionData);
        detail.setSortOrder(sortOrder != null ? sortOrder : 0);
        return documentDetailRepository.save(detail);
    }

    /**
     * 문서 상태 변경 (DRAFT ↔ COMPLETED 만 지원)
     */
    public Document changeStatus(Integer docId, DocumentStatus newStatus, User actor, String comment) {
        Document doc = getDocumentById(docId);
        DocumentStatus oldStatus = doc.getStatus();
        doc.setStatus(newStatus);

        recordHistory(doc, "STATUS_CHANGE", "status",
                oldStatus != null ? oldStatus.name() : null,
                newStatus != null ? newStatus.name() : null,
                actor, comment);
        return documentRepository.save(doc);
    }

    /**
     * 문서 삭제
     */
    public void deleteDocument(Integer docId) {
        documentRepository.deleteById(docId);
    }

    // === 이력 관리 ===

    public void recordHistory(Document doc, String action, String changedField,
                               String oldValue, String newValue, User actor, String comment) {
        DocumentHistory history = new DocumentHistory();
        history.setDocument(doc);
        history.setAction(action);
        history.setChangedField(changedField);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setActor(actor);
        history.setComment(comment);
        documentHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<DocumentHistory> getDocumentHistory(Integer docId) {
        return documentHistoryRepository.findByDocument_DocIdOrderByCreatedAtDesc(docId);
    }

    // S1 inspect-comprehensive-redesign (2026-04-21):
    // 점검 체크리스트(tb_inspect_checklist), 점검 이슈(tb_inspect_issue) 기능은
    // inspect_check_result / inspect_visit_log 에 통합되어 본 Service 에서 제거.
    // 관련 API 엔드포인트 / Repository / Entity 모두 삭제됨.

    // === 전자서명 ===

    public DocumentSignature saveSignature(Integer docId, String signerType,
                                             String signerName, String signerOrg, String signatureImage) {
        Document doc = getDocumentById(docId);
        DocumentSignature sig = new DocumentSignature();
        sig.setDocument(doc);
        sig.setSignerType(signerType);
        sig.setSignerName(signerName);
        sig.setSignerOrg(signerOrg);
        sig.setSignatureImage(signatureImage);
        return documentSignatureRepository.save(sig);
    }

    // === 성과통계용 ===

    @Transactional(readOnly = true)
    public Long countApprovedDocuments(DocumentType docType, Long userId, LocalDateTime from, LocalDateTime to) {
        return documentRepository.countApprovedByTypeAndUser(docType, userId, from, to);
    }
}
