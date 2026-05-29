package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.InspectCheckResult;
import com.swmanager.system.domain.InspectReport;
import com.swmanager.system.domain.InspectTemplate;
import com.swmanager.system.domain.InspectVisitLog;
import com.swmanager.system.dto.InspectCheckResultDTO;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.dto.InspectVisitLogDTO;
import com.swmanager.system.repository.InspectCheckResultRepository;
import com.swmanager.system.repository.InspectReportRepository;
import com.swmanager.system.repository.InspectQrBatchRepository;
import com.swmanager.system.repository.InspectMetricSnapshotRepository;
import com.swmanager.system.repository.InspectTemplateRepository;
import com.swmanager.system.repository.InspectVisitLogRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import com.swmanager.system.service.ops.OpsDocLinkService;
import com.swmanager.system.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class InspectReportService {

    private final InspectReportRepository reportRepository;
    private final InspectVisitLogRepository visitLogRepository;
    private final InspectCheckResultRepository checkResultRepository;
    private final InspectTemplateRepository templateRepository;
    private final InspectQrBatchRepository qrBatchRepository;
    private final OpsDocumentRepository opsDocumentRepository;
    private final InspectMetricSnapshotRepository metricSnapshotRepository;
    private final OpsDocLinkService opsDocLinkService;  // doc-split-ops: tb_ops_doc 연계 (codex 권고로 별도 bean)
    private final MessageResolver messages;

    // ===== 저장 (신규/수정 통합) =====

    @Transactional
    public InspectReportDTO save(InspectReportDTO dto) {
        String user = currentUser();
        Set<String> protectedSections = Set.of();

        InspectReport report = dto.toEntity();

        if (report.getId() == null) {
            report.setCreatedBy(user);
            report.setUpdatedBy(user);
        } else {
            InspectReport existing = reportRepository.findById(report.getId())
                    .orElseThrow(() -> new IllegalArgumentException(messages.get("error.inspect_report.not_found", report.getId())));
            report.setCreatedBy(existing.getCreatedBy());
            report.setCreatedAt(existing.getCreatedAt());
            report.setUpdatedBy(user);

            visitLogRepository.deleteByReportId(report.getId());

            // QR 적재(resultCode 있는) 섹션 보호: incoming 에 resultCode 없으면 기존 데이터 보존
            Set<String> qrSections = new HashSet<>();
            for (InspectCheckResult c : checkResultRepository.findByReportIdOrderBySectionAscSortOrderAsc(report.getId())) {
                if (c.getResultCode() != null && !c.getResultCode().isEmpty()) {
                    qrSections.add(c.getSection());
                }
            }
            Set<String> incomingWithCode = new HashSet<>();
            if (dto.getCheckResults() != null) {
                for (InspectCheckResultDTO cr : dto.getCheckResults()) {
                    if (cr.getResultCode() != null && !cr.getResultCode().isEmpty()) {
                        incomingWithCode.add(cr.getSection());
                    }
                }
            }
            Set<String> toProtect = new HashSet<>();
            for (String s : qrSections) {
                if (!incomingWithCode.contains(s)) toProtect.add(s);
            }
            protectedSections = toProtect;

            if (protectedSections.isEmpty()) {
                checkResultRepository.deleteByReportId(report.getId());
            } else {
                Set<String> allSections = new HashSet<>();
                for (InspectCheckResult c : checkResultRepository.findByReportIdOrderBySectionAscSortOrderAsc(report.getId())) {
                    allSections.add(c.getSection());
                }
                allSections.removeAll(protectedSections);
                if (!allSections.isEmpty()) {
                    checkResultRepository.deleteByReportIdAndSectionIn(report.getId(), List.copyOf(allSections));
                }
                log.info("QR 적재 데이터 보호: sections={}", protectedSections);
            }
        }

        InspectReport saved = reportRepository.save(report);
        Long reportId = saved.getId();

        if (dto.getVisits() != null) {
            int order = 0;
            for (InspectVisitLogDTO visitDto : dto.getVisits()) {
                InspectVisitLog visitLog = visitDto.toEntity(reportId);
                visitLog.setId(null);
                if (visitLog.getSortOrder() == null || visitLog.getSortOrder() == 0) {
                    visitLog.setSortOrder(++order);
                }
                visitLogRepository.save(visitLog);
            }
        }

        final Set<String> skipSections = protectedSections;
        if (dto.getCheckResults() != null) {
            for (InspectCheckResultDTO checkDto : dto.getCheckResults()) {
                if (!skipSections.isEmpty() && skipSections.contains(checkDto.getSection())) {
                    continue;
                }
                InspectCheckResult checkResult = checkDto.toEntity(reportId);
                checkResult.setId(null);
                checkResultRepository.save(checkResult);
            }
        }

        // COMPLETED 상태면 운영문서(tb_ops_doc.INSPECT)에 연계 — doc-split-ops 스프린트 (FR-6)
        if (DocumentStatus.COMPLETED == saved.getStatus()) {
            opsDocLinkService.linkInspectReport(saved);
        }

        return findById(reportId);
    }

    // doc-split-ops: linkToDocument() 메서드 제거 (FR-6).
    // COMPLETED 시 tb_ops_doc.INSPECT row 연계는 OpsDocLinkService bean 이 담당 — Spring
    // self-invocation 회피로 @Transactional(REQUIRES_NEW) 가 정상 동작 (codex 권고).

    // ===== 단건 조회 (방문이력 + 점검결과 포함) =====

    @Transactional(readOnly = true)
    public InspectReportDTO findById(Long id) {
        InspectReport report = reportRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
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
        return reportRepository.findByPjtIdAndDeletedAtIsNullOrderByCreatedAtDesc(pjtId)
                .stream()
                .map(InspectReportDTO::fromEntity)
                .toList();
    }

    /**
     * inspection-report-d-v5: doc-inspect 진입 시 prefill 용.
     * 동일 프로젝트+점검월 의 기존 회차(QR 적재본 등)가 있으면 단건 반환, 없으면 null.
     */
    @Transactional(readOnly = true)
    public InspectReportDTO findByProjectAndMonth(Long pjtId, String inspectMonth) {
        if (pjtId == null || inspectMonth == null || inspectMonth.isEmpty()) return null;
        return reportRepository.findByPjtIdAndInspectMonthAndDeletedAtIsNull(pjtId, inspectMonth)
                .map(r -> findById(r.getId()))   // 풀 DTO (visits+checkResults 포함) 가져오기
                .orElse(null);
    }

    // ===== 삭제 (soft delete) =====

    @Transactional
    public void delete(Long id) {
        InspectReport report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.inspect_report.not_found", id)));
        report.setDeletedAt(LocalDateTime.now());
        report.setBatchId(null);
        reportRepository.save(report);
        qrBatchRepository.deleteByReportId(id);

        // tb_ops_doc 연계 레코드 삭제 (INSP-yyyy-{id} 등 3 포맷)
        String month = report.getInspectMonth();
        String yyyy = month != null && month.length() >= 4 ? month.substring(0, 4)
                : String.valueOf(java.time.LocalDate.now().getYear());
        for (String docNo : List.of("INSP-" + yyyy + "-" + id, "INSP-" + id)) {
            opsDocumentRepository.findByDocNo(docNo).ifPresent(opsDocumentRepository::delete);
        }
        if (month != null && month.length() >= 7) {
            String mm = month.substring(5, 7);
            opsDocumentRepository.findByDocNo("INSP-" + yyyy + "-" + mm + "-" + id)
                    .ifPresent(opsDocumentRepository::delete);
        }
    }

    /**
     * 테스트 데이터 일괄 초기화 — 점검 관련 전체 hard delete.
     * 매 부팅 cleanup SQL 을 대체하는 수동 초기화. 호출부(컨트롤러)에서 관리자 권한 확인 필수.
     * 삭제 순서: 자식(check_result/visit_log/qr_batch) → metric_snapshot → ops_doc(INSPECT) → report.
     */
    @Transactional
    public Map<String, Long> resetAllInspectData() {
        var inspectDocs = opsDocumentRepository.findByDocTypeOrderByCreatedAtDesc(OpsDocType.INSPECT);

        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("checkResult", checkResultRepository.count());
        counts.put("visitLog", visitLogRepository.count());
        counts.put("qrBatch", qrBatchRepository.count());
        counts.put("metricSnapshot", metricSnapshotRepository.count());
        counts.put("opsDocInspect", (long) inspectDocs.size());
        counts.put("report", reportRepository.count());

        checkResultRepository.deleteAllInBatch();
        visitLogRepository.deleteAllInBatch();
        qrBatchRepository.deleteAllInBatch();
        metricSnapshotRepository.deleteAllInBatch();
        opsDocumentRepository.deleteAll(inspectDocs);
        reportRepository.deleteAllInBatch();

        log.info("점검 데이터 일괄 초기화 완료: {}", counts);
        return counts;
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
