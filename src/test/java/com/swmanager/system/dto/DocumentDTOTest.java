package com.swmanager.system.dto;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.OrgUnit;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.workplan.Document;
import org.junit.jupiter.api.Test;

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
    void staticLabels_nullSafeAndDelegate() {
        assertThat(DocumentDTO.getDocTypeLabel(null)).isEqualTo("-");
        assertThat(DocumentDTO.getStatusLabel(null)).isEqualTo("-");
        assertThat(DocumentDTO.getStatusColor(null)).isEqualTo("#858796");
        assertThat(DocumentDTO.getDocTypeLabel(DocumentType.COMMENCE)).isEqualTo(DocumentType.COMMENCE.label());
        assertThat(DocumentDTO.getStatusLabel(DocumentStatus.DRAFT)).isEqualTo(DocumentStatus.DRAFT.label());
        assertThat(DocumentDTO.getStatusColor(DocumentStatus.DRAFT)).isEqualTo(DocumentStatus.DRAFT.color());
    }
}
