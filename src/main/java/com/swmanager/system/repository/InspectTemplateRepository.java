package com.swmanager.system.repository;

import com.swmanager.system.domain.InspectTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectTemplateRepository extends JpaRepository<InspectTemplate, Long> {

    List<InspectTemplate> findByTemplateTypeAndUseYnOrderBySectionAscSortOrderAsc(
            String templateType, String useYn);

    /**
     * 표준시스템(APP) 점검 템플릿 보유 여부 — maint_type 점검범위 분기의 "표준보유" 판단 기준.
     * (UPIS=보유, KRAS=미보유). 기획서: docs/product-specs/inspect-maint-profile.md
     */
    boolean existsByTemplateTypeAndSectionAndUseYn(String templateType, String section, String useYn);
}
