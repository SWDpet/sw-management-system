package com.swmanager.system.service;

import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.*;
import com.swmanager.system.dto.DocumentDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.workplan.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    @Autowired private InspectChecklistRepository inspectChecklistRepository;
    @Autowired private InspectIssueRepository inspectIssueRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private InfraRepository infraRepository;

    // === 문서번호 자동 채번 ===

    public String generateDocNo() {
        int year = LocalDate.now().getYear();
        String prefix = "(주)정도UIT " + year + "-";
        Integer maxSeq = documentRepository.findMaxDocNoSeq(prefix + "%");
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;
        return prefix + String.format("%03d", nextSeq) + "호";
    }

    // === CRUD ===

    @Transactional(readOnly = true)
    public Document getDocumentById(Integer docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. ID: " + docId));
    }

    @Transactional(readOnly = true)
    public DocumentDTO getDocumentDTOById(Integer docId) {
        return DocumentDTO.fromEntity(getDocumentById(docId));
    }

    @Transactional(readOnly = true)
    public Page<DocumentDTO> searchDocuments(String docType, String status, Long infraId,
                                              Long authorId, LocalDateTime from, LocalDateTime to,
                                              String keyword, Pageable pageable) {
        if (docType != null && docType.trim().isEmpty()) docType = null;
        if (status != null && status.trim().isEmpty()) status = null;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;

        Page<Document> page = documentRepository.searchDocuments(
                docType, status, infraId, authorId, from, to, keyword, pageable);
        return page.map(DocumentDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByInfra(Long infraId) {
        return documentRepository.findByInfra_InfraIdOrderByCreatedAtDesc(infraId)
                .stream().map(DocumentDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByContract(Integer contractId) {
        return documentRepository.findByContract_ContractIdOrderByCreatedAtDesc(contractId)
                .stream().map(DocumentDTO::fromEntity).collect(Collectors.toList());
    }

    /**
     * 문서 생성
     */
    public Document createDocument(String docType, String sysType, Long infraId,
                                    Integer contractId, Integer planId,
                                    String title, User author) {
        Document doc = new Document();
        doc.setDocNo(generateDocNo());
        doc.setDocType(docType);
        doc.setSysType(sysType);
        doc.setTitle(title);
        doc.setStatus("DRAFT");
        doc.setAuthor(author);

        if (infraId != null) {
            doc.setInfra(infraRepository.findById(infraId).orElse(null));
        }
        if (contractId != null) {
            doc.setContract(contractRepository.findById(contractId).orElse(null));
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
     * 문서 상태 변경
     */
    public Document changeStatus(Integer docId, String newStatus, User actor, String comment) {
        Document doc = getDocumentById(docId);
        String oldStatus = doc.getStatus();
        doc.setStatus(newStatus);

        if ("APPROVED".equals(newStatus)) {
            doc.setApprover(actor);
            doc.setApprovedAt(LocalDateTime.now());
        }
        if ("REJECTED".equals(newStatus)) {
            // 반려 시 DRAFT로 복귀 가능하도록
            doc.setApprover(actor);
        }

        recordHistory(doc, "STATUS_CHANGE", "status", oldStatus, newStatus, actor, comment);
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

    // === 점검 체크리스트 ===

    public InspectChecklist saveChecklist(Integer docId, Integer inspectMonth,
                                           String targetSw, String checkItem,
                                           String checkMethod, String checkResult, Integer sortOrder) {
        Document doc = getDocumentById(docId);
        InspectChecklist item = new InspectChecklist();
        item.setDocument(doc);
        item.setInspectMonth(inspectMonth);
        item.setTargetSw(targetSw);
        item.setCheckItem(checkItem);
        item.setCheckMethod(checkMethod);
        item.setCheckResult(checkResult);
        item.setSortOrder(sortOrder);
        return inspectChecklistRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<InspectChecklist> getChecklists(Integer docId) {
        return inspectChecklistRepository.findByDocument_DocIdOrderBySortOrder(docId);
    }

    // === 점검 이슈/장애 이력 ===

    public InspectIssue saveIssue(Integer docId, Integer issueYear, Integer issueMonth,
                                    Integer issueDay, String taskType, String symptom, String actionTaken) {
        Document doc = getDocumentById(docId);
        InspectIssue issue = new InspectIssue();
        issue.setDocument(doc);
        issue.setIssueYear(issueYear);
        issue.setIssueMonth(issueMonth);
        issue.setIssueDay(issueDay);
        issue.setTaskType(taskType);
        issue.setSymptom(symptom);
        issue.setActionTaken(actionTaken);
        return inspectIssueRepository.save(issue);
    }

    @Transactional(readOnly = true)
    public List<InspectIssue> getIssues(Integer docId) {
        return inspectIssueRepository.findByDocument_DocIdOrderByIssueYearDescIssueMonthDescIssueDayDesc(docId);
    }

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
    public Long countApprovedDocuments(String docType, Long userId, LocalDateTime from, LocalDateTime to) {
        return documentRepository.countApprovedByTypeAndUser(docType, userId, from, to);
    }
}
