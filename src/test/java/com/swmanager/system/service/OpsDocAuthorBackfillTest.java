package com.swmanager.system.service;

import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.User;
import com.swmanager.system.repository.InspectReportRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import com.swmanager.system.service.ops.OpsDocLinkService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 일회성 백필 — INSPECT 운영문서의 작성자(author) 정정.
 *
 * <p>버그: QR 자동생성 report 는 createdBy 에 숫자 user_id 를 저장하는데 OpsDocLinkService 가
 * findByUserid 로만 조회 → 못 찾아 '첫 사용자(박상현)' 로 폴백. 수정된 OpsDocLinkService 로 재연계.
 */
@SpringBootTest
@ActiveProfiles("local")
class OpsDocAuthorBackfillTest {

    @Autowired private OpsDocumentRepository opsDocumentRepository;
    @Autowired private InspectReportRepository inspectReportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OpsDocLinkService opsDocLinkService;

    private User resolve(String createdBy, Long inspUserId) {
        User author = null;
        if (createdBy != null && !createdBy.isBlank()) {
            if (createdBy.matches("\\d+")) author = userRepository.findById(Long.valueOf(createdBy)).orElse(null);
            if (author == null) author = userRepository.findByUserid(createdBy).orElse(null);
        }
        if (author == null && inspUserId != null) author = userRepository.findById(inspUserId).orElse(null);
        return author;
    }

    private Long parseReportId(String docNo) {
        if (docNo == null) return null;
        try { return Long.valueOf(docNo.substring(docNo.lastIndexOf('-') + 1)); }
        catch (Exception e) { return null; }
    }

    @Test
    @Disabled("일회성 백필 완료(2026-05-30). 재연계 필요 시 @Disabled 제거. linkInspectReport 가 REQUIRES_NEW 로 자체 커밋.")
    void backfillInspectAuthors() {
        var docs = opsDocumentRepository.findAll().stream()
                .filter(d -> d.getDocType() == OpsDocType.INSPECT).toList();
        int n = 0;
        for (var d : docs) {
            Long reportId = parseReportId(d.getDocNo());
            if (reportId == null) continue;
            var report = inspectReportRepository.findById(reportId).orElse(null);
            if (report == null) continue;
            User resolved = resolve(report.getCreatedBy(), report.getInspUserId());
            System.out.println("[BACKFILL] docNo=" + d.getDocNo()
                    + " createdBy=" + report.getCreatedBy() + " inspUserId=" + report.getInspUserId()
                    + " → author=" + (resolved != null ? resolved.getUsername() + "(" + resolved.getUserid() + ")" : "MISSING"));
            opsDocLinkService.linkInspectReport(report);   // 수정된 로직으로 재연계 (자체 커밋)
            n++;
        }
        System.out.println("[BACKFILL] re-linked INSPECT docs = " + n);
    }
}
