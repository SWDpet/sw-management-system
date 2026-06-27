package com.swmanager.system.dto;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.OrgUnit;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.domain.workplan.WorkPlan;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * DocumentDTO 변환 로직 단위테스트 (커버리지 상향 beyond-A, 순수).
 * private 헬퍼(getEnvironmentLabel/buildOrgUnitPath/buildTargetDisplay)는 public fromEntity 경유 검증.
 */
class DocumentDTOTest {

    private String envLabel(String env) {
        Document d = new Document();
        d.setEnvironment(env);
        return DocumentDTO.fromEntity(d).getEnvironmentLabel();
    }

    @Test
    void fromEntity_environmentLabel_allBranches() {
        assertThat(envLabel("PROD")).isEqualTo("운영");
        assertThat(envLabel("TEST")).isEqualTo("테스트");
        assertThat(envLabel("STAGE")).isEqualTo("STAGE");           // default → 원본
        assertThat(DocumentDTO.fromEntity(new Document()).getEnvironmentLabel()).isNull();  // null → null
    }

    @Test
    void fromEntity_orgUnitPath_joinsRootToLeaf() {
        OrgUnit root = new OrgUnit(); root.setName("본부");
        OrgUnit team = new OrgUnit(); team.setName("개발팀"); team.setParent(root);
        Document d = new Document(); d.setOrgUnit(team);
        assertThat(DocumentDTO.fromEntity(d).getOrgUnitPath()).isEqualTo("본부 > 개발팀");
    }

    @Test
    void fromEntity_targetDisplay_regionCodeBranch() {
        Document d = new Document();
        d.setRegionCode("11110"); d.setSysType("UPIS");
        assertThat(DocumentDTO.fromEntity(d).getTargetDisplay()).isEqualTo("[11110] UPIS");
    }

    @Test
    void fromEntity_targetDisplay_projectBranch() {
        SwProject p = new SwProject();
        p.setCityNm("서울"); p.setDistNm("강남구"); p.setSysNm("GIS");
        Document d = new Document(); d.setProject(p);   // regionCode null → project 분기
        assertThat(DocumentDTO.fromEntity(d).getTargetDisplay()).isEqualTo("서울 강남구 - GIS");
    }

    @Test
    void fromEntity_targetDisplay_emptyWhenNoTarget() {
        assertThat(DocumentDTO.fromEntity(new Document()).getTargetDisplay()).isEmpty();
    }

    @Test
    void fromEntity_fullMapping_infraWorkplanAuthorApproverDetails() {
        Infra inf = new Infra();
        inf.setInfraId(11L); inf.setCityNm("부산"); inf.setDistNm("해운대"); inf.setSysNm("KRAS");
        WorkPlan wp = new WorkPlan(); wp.setPlanId(5);
        SwProject p = new SwProject();
        p.setProjId(3L); p.setProjNm("사업A"); p.setClient("발주처");
        p.setContAmt(100_000_000L);
        p.setStartDt(java.time.LocalDate.of(2026, 1, 1)); p.setEndDt(java.time.LocalDate.of(2026, 12, 31));
        User author = new User(); author.setUserSeq(1L); author.setUsername("작성자");
        User approver = new User(); approver.setUserSeq(2L); approver.setUsername("승인자");
        DocumentDetail det = new DocumentDetail();
        det.setDetailId(9); det.setSectionKey("cover"); det.setSectionData(Map.of("k", "v")); det.setSortOrder(0);

        Document d = new Document();
        d.setDocId(100); d.setDocNo("D-1"); d.setDocType(DocumentType.COMMENCE); d.setTitle("제목");
        d.setStatus(DocumentStatus.DRAFT);
        d.setInfra(inf); d.setWorkPlan(wp); d.setProject(p);
        d.setAuthor(author); d.setApprover(approver);
        d.setApprovedAt(LocalDateTime.of(2026, 6, 1, 10, 0));
        d.setCreatedAt(LocalDateTime.of(2026, 5, 1, 9, 0));
        d.setUpdatedAt(LocalDateTime.of(2026, 5, 2, 9, 0));
        d.setSignedScanPath("D:/scan/x.pdf");
        d.setDetails(List.of(det));

        DocumentDTO dto = DocumentDTO.fromEntity(d);

        // 스칼라 직매핑
        assertThat(dto.getDocId()).isEqualTo(100);
        assertThat(dto.getDocNo()).isEqualTo("D-1");
        assertThat(dto.getDocType()).isEqualTo(DocumentType.COMMENCE);
        assertThat(dto.getTitle()).isEqualTo("제목");
        assertThat(dto.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        // infra 분기(infra 우선)
        assertThat(dto.getInfraId()).isEqualTo(11L);
        assertThat(dto.getCityNm()).isEqualTo("부산");
        assertThat(dto.getDistNm()).isEqualTo("해운대");
        assertThat(dto.getSysNm()).isEqualTo("KRAS");
        // workPlan / project / author / approver
        assertThat(dto.getPlanId()).isEqualTo(5);
        assertThat(dto.getProjId()).isEqualTo(3L);
        assertThat(dto.getProjNm()).isEqualTo("사업A");
        assertThat(dto.getProjClient()).isEqualTo("발주처");
        assertThat(dto.getProjContAmt()).isEqualTo(100_000_000L);
        assertThat(dto.getProjStartDt()).isEqualTo("2026-01-01");
        assertThat(dto.getProjEndDt()).isEqualTo("2026-12-31");
        assertThat(dto.getAuthorId()).isEqualTo(1L);
        assertThat(dto.getAuthorName()).isEqualTo("작성자");
        assertThat(dto.getApproverId()).isEqualTo(2L);
        assertThat(dto.getApproverName()).isEqualTo("승인자");
        assertThat(dto.getApprovedAt()).isNotNull();
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
        assertThat(dto.isHasSignedScan()).isTrue();
        // details → sections (DetailSectionDTO.fromEntity)
        assertThat(dto.getSections()).hasSize(1);
        DocumentDTO.DetailSectionDTO sec = dto.getSections().get(0);
        assertThat(sec.getDetailId()).isEqualTo(9);
        assertThat(sec.getSectionKey()).isEqualTo("cover");
        assertThat(sec.getSectionData()).containsEntry("k", "v");
        assertThat(sec.getSortOrder()).isZero();
    }

    @Test
    void fromEntity_projectFallback_whenNoInfra() {
        SwProject p = new SwProject();
        p.setProjId(3L); p.setCityNm("서울"); p.setDistNm("강남구"); p.setSysNm("GIS");
        Document d = new Document(); d.setProject(p); // infra=null → project 에서 cityNm/distNm/sysNm fallback
        DocumentDTO dto = DocumentDTO.fromEntity(d);
        assertThat(dto.getCityNm()).isEqualTo("서울");
        assertThat(dto.getDistNm()).isEqualTo("강남구");
        assertThat(dto.getSysNm()).isEqualTo("GIS");
    }

    @Test
    void fromEntity_targetDisplay_infraBranch() {
        Infra inf = new Infra();
        inf.setCityNm("부산"); inf.setDistNm("해운대");
        Document d = new Document(); d.setInfra(inf); // regionCode/project null → infra 분기
        assertThat(DocumentDTO.fromEntity(d).getTargetDisplay()).isEqualTo("부산 해운대");
    }

    @Test
    void staticLabels_nullSafeAndDelegate() {
        assertThat(DocumentDTO.getDocTypeLabel(null)).isEqualTo("-");
        assertThat(DocumentDTO.getStatusLabel(null)).isEqualTo("-");
        assertThat(DocumentDTO.getStatusColor(null)).isEqualTo("#858796");
        assertThat(DocumentDTO.getDocTypeLabel(DocumentType.COMMENCE)).isEqualTo(DocumentType.COMMENCE.label());
        assertThat(DocumentDTO.getStatusLabel(DocumentStatus.DRAFT)).isEqualTo(DocumentStatus.DRAFT.label());
        assertThat(DocumentDTO.getStatusColor(DocumentStatus.DRAFT)).isEqualTo(DocumentStatus.DRAFT.color());
    }
}
