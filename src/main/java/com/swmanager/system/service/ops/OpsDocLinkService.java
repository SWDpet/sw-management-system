package com.swmanager.system.service.ops;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.InspectReport;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 점검내역서(InspectReport) → tb_ops_doc.INSPECT row 자동 연계 전용 bean.
 *
 * codex 권고로 별도 bean 분리 — InspectReportService 의 private 메서드에서 호출하면
 * Spring self-invocation 으로 @Transactional(REQUIRES_NEW) 가 무효. 별도 bean 호출은
 * Spring 프록시를 경유하므로 트랜잭션 격리가 보장됨.
 *
 * 기획서: docs/product-specs/doc-split-ops.md (v3, FR-6, §4-9)
 * 개발계획: docs/exec-plans/doc-split-ops.md (v2, Step 4)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpsDocLinkService {

    private final OpsDocumentRepository opsDocumentRepository;
    private final UserRepository userRepository;

    /**
     * COMPLETED 점검내역서를 tb_ops_doc.INSPECT row 로 연계 (없으면 생성, 있으면 갱신).
     *
     * REQUIRES_NEW: ops 연계 실패가 본 점검 보고서 트랜잭션을 롤백하지 않음.
     * 호환성을 위해 기존 InspectReportService.linkToDocument 의 3 포맷 룩업 그대로 보존:
     *   INSP-yyyy-{reportId} (신규)
     *   INSP-yyyy-mm-{reportId} (중간 포맷, 마이그레이션용)
     *   INSP-{reportId} (구포맷)
     *
     * 사업측 (tb_document) fallback 은 제거 — 사업문서는 INSP- prefix 미사용으로 충돌 불가.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void linkInspectReport(InspectReport report) {
        try {
            String month = report.getInspectMonth();
            String yyyy = month != null && month.length() >= 4
                    ? month.substring(0, 4)
                    : String.valueOf(java.time.LocalDate.now().getYear());
            String mm = month != null && month.length() >= 7 ? month.substring(5, 7) : null;
            String docNo = "INSP-" + yyyy + "-" + report.getId();
            String monthlyFormat = mm != null
                    ? "INSP-" + yyyy + "-" + mm + "-" + report.getId()
                    : null;

            OpsDocument doc = opsDocumentRepository.findByDocNo(docNo)
                    .orElseGet(() -> monthlyFormat != null
                            ? opsDocumentRepository.findByDocNo(monthlyFormat)
                                    .orElseGet(() -> opsDocumentRepository.findByDocNo("INSP-" + report.getId()).orElse(null))
                            : opsDocumentRepository.findByDocNo("INSP-" + report.getId()).orElse(null));

            if (doc == null) {
                doc = new OpsDocument();
                doc.setDocType(OpsDocType.INSPECT);
            }
            doc.setDocNo(docNo);
            doc.setSysType(report.getSysType());
            doc.setTitle(report.getDocTitle() != null ? report.getDocTitle() : "점검내역서");
            doc.setStatus(DocumentStatus.COMPLETED);
            // doc-split-ops Step 10 P1: 성과 집계가 BETWEEN approvedAt 으로 매치하므로 동기화.
            if (doc.getApprovedAt() == null) {
                doc.setApprovedAt(java.time.LocalDateTime.now());
            }

            // 점검 본 데이터(InspectReport)는 infra 와 직접 연결되지 않으므로 infra_id 매핑 없음.
            // sw_pjt → infra 의 자동 매핑은 후속 스프린트로 분리.

            User author = userRepository.findByUserid(report.getCreatedBy()).orElse(null);
            if (author == null) {
                author = userRepository.findAll().stream().findFirst().orElse(null);
            }
            if (author != null) doc.setAuthor(author);

            opsDocumentRepository.save(doc);
            log.info("점검내역서 운영문서 연계 완료: docNo={}, reportId={}", docNo, report.getId());
        } catch (Exception e) {
            log.warn("점검내역서 운영문서 연계 실패 (REQUIRES_NEW 로 본 트랜잭션 격리됨): reportId={}, msg={}",
                    report.getId(), e.getMessage());
        }
    }
}
