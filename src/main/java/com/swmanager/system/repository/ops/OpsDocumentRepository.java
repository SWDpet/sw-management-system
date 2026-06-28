package com.swmanager.system.repository.ops;

import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.ops.OpsDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OpsDocumentRepository extends JpaRepository<OpsDocument, Long> {

    Optional<OpsDocument> findByDocNo(String docNo);

    List<OpsDocument> findByDocTypeOrderByCreatedAtDesc(OpsDocType docType);

    /** 채번용: 특정 prefix (예: "FLT-2026-") 의 max 일련번호. */
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(doc_no FROM '-(\\d+)$') AS INTEGER)), 0) " +
                   "FROM tb_ops_doc WHERE doc_no LIKE :prefix",
           nativeQuery = true)
    Integer findMaxDocNoSeq(@Param("prefix") String prefix);

    /** 성과집계: 운영문서 doc_type 별/작성자별/기간별 COMPLETED 건수. */
    @Query("SELECT COUNT(d) FROM OpsDocument d " +
           "WHERE d.docType = :docType " +
           "AND d.author.userSeq = :userId " +
           "AND d.status = com.swmanager.system.constant.enums.DocumentStatus.COMPLETED " +
           "AND d.approvedAt BETWEEN :from AND :to")
    Long countCompletedByTypeAndUser(
            @Param("docType") OpsDocType docType,
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
