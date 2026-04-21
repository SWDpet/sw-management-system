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
        return page.map(d -> enrichRegion(DocumentDTO.fromEntity(d)));
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
