package com.swmanager.system.domain.workplan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "tb_document_detail")
@Getter @Setter
public class DocumentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private Document document;

    @Column(name = "section_key", nullable = false, length = 50)
    private String sectionKey;
    // 착수계: letter/commence/schedule/site_rep/security/participants/pledge
    // 기성계: letter/inspector/detail_sheet
    // 준공계: letter/completion/inspector/target/inspect_summary
    // 점검내역서: cover/report/monthly_detail

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "section_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> sectionData;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
