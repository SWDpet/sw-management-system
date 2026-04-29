package com.swmanager.system.domain.ops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "tb_ops_doc_detail")
@Getter @Setter
public class OpsDocumentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private OpsDocument document;

    @Column(name = "section_key", nullable = false, length = 50)
    private String sectionKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "section_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> sectionData;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
