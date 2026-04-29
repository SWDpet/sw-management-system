package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.InspectCheckResult;
import com.swmanager.system.domain.InspectReport;
import com.swmanager.system.domain.InspectTemplate;
import com.swmanager.system.domain.InspectVisitLog;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.dto.InspectCheckResultDTO;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.dto.InspectVisitLogDTO;
import com.swmanager.system.repository.InspectCheckResultRepository;
import com.swmanager.system.repository.InspectReportRepository;
import com.swmanager.system.repository.InspectTemplateRepository;
import com.swmanager.system.repository.InspectVisitLogRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.DocumentRepository;
import com.swmanager.system.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InspectReportService {

    private final InspectReportRepository reportRepository;
    private final InspectVisitLogRepository visitLogRepository;
    private final InspectCheckResultRepository checkResultRepository;
    private final InspectTemplateRepository templateRepository;
    private final DocumentRepository documentRepository;
    private final SwProjectRepository swProjectRepository;
    private final UserRepository userRepository;
    private final MessageResolver messages;

    // ===== 저장 (신규/수정 통합) =====

    @Transactional
    public InspectReportDTO save(InspectReportDTO dto) {
        String user = currentUser();

        InspectReport report = dto.toEntity();

        if (report.getId() == null) {
            // 신규
            report.setCreatedBy(user);
            report.setUpdatedBy(user);
        } else {
            // 수정: 기존 레코드에서 createdBy 유지
            InspectReport existing = reportRepository.findById(report.getId())
                    .orElseThrow(() -> new IllegalArgumentException(messages.get("error.inspect_report.not_found", report.getId())));
            report.setCreatedBy(existing.getCreatedBy());
            report.setCreatedAt(existing.getCreatedAt());
            report.setUpdatedBy(user);

            // 기존 하위 데이터 삭제
            visitLogRepository.deleteByReportId(report.getId());
            checkResultRepository.deleteByReportId(report.getId());
        }

        InspectReport saved = reportRepository.save(report);
        Long reportId = saved.getId();

        // 방문이력 저장
        if (dto.getVisits() != null) {
            int order = 0;
            for (InspectVisitLogDTO visitDto : dto.getVisits()) {
                InspectVisitLog visitLog = visitDto.toEntity(reportId);
                visitLog.setId(null); // 항상 신규 삽입
                if (visitLog.getSortOrder() == null || visitLog.getSortOrder() == 0) {
                    visitLog.setSortOrder(++order);
                }
                visitLogRepository.save(visitLog);
            }
        }

        // 점검결과 저장
        if (dto.getCheckResults() != null) {
            for (InspectCheckResultDTO checkDto : dto.getCheckResults()) {
                InspectCheckResult checkResult = checkDto.toEntity(reportId);
                checkResult.setId(null); // 항상 신규 삽입
                checkResultRepository.save(checkResult);
            }
        }

        // COMPLETED 상태면 문서관리(tb_document)에 연계
        if (DocumentStatus.COMPLETED == saved.getStatus()) {
            linkToDocument(saved);
        }

        return findById(reportId);
    }

    /** COMPLETED 점검내역서를 tb_document에 연계 (없으면 생성, 있으면 갱신) */
    private void linkToDocument(InspectReport report) {
        try {
            String month = report.getInspectMonth();
            String yyyy = month != null && month.length() >= 4
                    ? month.substring(0, 4)
                    : String.valueOf(java.time.LocalDate.now().getYear());
            String mm = month != null && month.length() >= 7 ? month.substring(5, 7) : null;
            String docNo = "INSP-" + yyyy + "-" + report.getId();
            // 신규 포맷 → 월포함 중간포맷 → 최구포맷(id only) 순으로 룩업 후 마이그레이션
            String monthlyFormat = mm != null
                    ? "INSP-" + yyyy + "-" + mm + "-" + report.getId()
                    : null;
            Document doc = documentRepository.findByDocNo(docNo)
                    .orElseGet(() -> monthlyFormat != null
                            ? documentRepository.findByDocNo(monthlyFormat)
                                    .orElseGet(() -> documentRepository.findByDocNo("INSP-" + report.getId()).orElse(null))
                            : documentRepository.findByDocNo("INSP-" + report.getId()).orElse(null));

            if (doc == null) {
                doc = new Document();
                doc.setDocType(DocumentType.INSPECT);
            }
            doc.setDocNo(docNo);

            doc.setSysType(report.getSysType());
            doc.setTitle(report.getDocTitle() != null ? report.getDocTitle() : "점검내역서");
            doc.setStatus(DocumentStatus.COMPLETED);

            // 프로젝트 연결
            if (report.getPjtId() != null) {
                swProjectRepository.findById(report.getPjtId()).ifPresent(doc::setProject);
            }

            // 작성자 연결
            User author = userRepository.findByUserid(report.getCreatedBy()).orElse(null);
            if (author == null) {
                author = userRepository.findAll().stream().findFirst().orElse(null);
            }
            if (author != null) doc.setAuthor(author);

            documentRepository.save(doc);
            log.info("점검내역서 문서관리 연계 완료: docNo={}, reportId={}", docNo, report.getId());
        } catch (Exception e) {
            log.warn("문서관리 연계 실패 (무시): {}", e.getMessage());
        }
    }

    // ===== 단건 조회 (방문이력 + 점검결과 포함) =====

    @Transactional(readOnly = true)
    public InspectReportDTO findById(Long id) {
        InspectReport report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.inspect_report.not_found", id)));

        InspectReportDTO dto = InspectReportDTO.fromEntity(report);

        List<InspectVisitLogDTO> visits = visitLogRepository
                .findByReportIdOrderBySortOrderAsc(id)
                .stream()
                .map(InspectVisitLogDTO::fromEntity)
                .toList();
        dto.setVisits(visits);

        // 이전 월 이력 조회 (같은 프로젝트의 이전 월 COMPLETED 보고서의 방문이력)
        if (report.getPjtId() != null && report.getInspectMonth() != null) {
            List<InspectVisitLogDTO> previousVisits = visitLogRepository
                    .findPreviousVisitsByProject(report.getPjtId(), report.getInspectMonth())
                    .stream()
                    .map(InspectVisitLogDTO::fromEntity)
                    .toList();
            dto.setPreviousVisits(previousVisits);
        }

        List<InspectCheckResultDTO> checkResults = checkResultRepository
                .findByReportIdOrderBySectionAscSortOrderAsc(id)
                .stream()
                .map(InspectCheckResultDTO::fromEntity)
                .toList();
        dto.setCheckResults(checkResults);

        return dto;
    }

    /**
     * 신규 보고서 작성 시 사용: pjtId + inspectMonth로 이전 월 이력을 조회
     * (아직 저장되지 않은 새 보고서에서도 이전 이력을 표시하기 위함)
     */
    @Transactional(readOnly = true)
    public List<InspectVisitLogDTO> getPreviousVisits(Long pjtId, String inspectMonth) {
        if (pjtId == null || inspectMonth == null || inspectMonth.isEmpty()) {
            return List.of();
        }
        return visitLogRepository.findPreviousVisitsByProject(pjtId, inspectMonth)
                .stream()
                .map(InspectVisitLogDTO::fromEntity)
                .toList();
    }

    // ===== 프로젝트별 목록 =====

    @Transactional(readOnly = true)
    public List<InspectReportDTO> findByProject(Long pjtId) {
        return reportRepository.findByPjtIdOrderByCreatedAtDesc(pjtId)
                .stream()
                .map(InspectReportDTO::fromEntity)
                .toList();
    }

    // ===== 삭제 =====

    @Transactional
    public void delete(Long id) {
        InspectReport report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.inspect_report.not_found", id)));
        visitLogRepository.deleteByReportId(id);
        checkResultRepository.deleteByReportId(id);
        reportRepository.delete(report);
    }

    // ===== 템플릿 항목 조회 =====

    @Transactional(readOnly = true)
    public List<InspectCheckResultDTO> getTemplateItems(String templateType) {
        List<InspectTemplate> templates = templateRepository
                .findByTemplateTypeAndUseYnOrderBySectionAscSortOrderAsc(templateType, "Y");

        return templates.stream().map(t -> {
            InspectCheckResultDTO dto = new InspectCheckResultDTO();
            dto.setSection(t.getSection());
            dto.setCategory(t.getCategory());
            dto.setItemName(t.getItemName());
            dto.setItemMethod(t.getItemMethod());
            dto.setSortOrder(t.getSortOrder());
            return dto;
        }).toList();
    }

    // ===== 내부 유틸: 현재 사용자 =====

    private String currentUser() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
