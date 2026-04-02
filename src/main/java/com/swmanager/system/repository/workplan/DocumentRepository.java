package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {

    // 문서번호로 조회
    Optional<Document> findByDocNo(String docNo);

    // 문서유형별 목록
    List<Document> findByDocTypeOrderByCreatedAtDesc(String docType);

    // 인프라별 문서 목록
    List<Document> findByInfra_InfraIdOrderByCreatedAtDesc(Long infraId);

    // 계약별 문서 목록
    List<Document> findByContract_ContractIdOrderByCreatedAtDesc(Integer contractId);

    // 작성자별 문서 목록
    List<Document> findByAuthor_UserSeqOrderByCreatedAtDesc(Long userSeq);

    // 복합 검색 (PostgreSQL 호환 - CAST로 파라미터 타입 명시)
    @Query(value = "SELECT * FROM tb_document d " +
           "WHERE (CAST(:docType AS VARCHAR) IS NULL OR d.doc_type = :docType) " +
           "AND (CAST(:status AS VARCHAR) IS NULL OR d.status = :status) " +
           "AND (CAST(:infraId AS BIGINT) IS NULL OR d.infra_id = :infraId) " +
           "AND (CAST(:authorId AS BIGINT) IS NULL OR d.author_id = :authorId) " +
           "AND (CAST(:fromDt AS TIMESTAMP) IS NULL OR d.created_at >= :fromDt) " +
           "AND (CAST(:toDt AS TIMESTAMP) IS NULL OR d.created_at <= :toDt) " +
           "AND (CAST(:kw AS VARCHAR) IS NULL OR (" +
           "   d.title LIKE '%' || :kw || '%' OR " +
           "   d.doc_no LIKE '%' || :kw || '%' " +
           ")) " +
           "ORDER BY d.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM tb_document d " +
           "WHERE (CAST(:docType AS VARCHAR) IS NULL OR d.doc_type = :docType) " +
           "AND (CAST(:status AS VARCHAR) IS NULL OR d.status = :status) " +
           "AND (CAST(:infraId AS BIGINT) IS NULL OR d.infra_id = :infraId) " +
           "AND (CAST(:authorId AS BIGINT) IS NULL OR d.author_id = :authorId) " +
           "AND (CAST(:fromDt AS TIMESTAMP) IS NULL OR d.created_at >= :fromDt) " +
           "AND (CAST(:toDt AS TIMESTAMP) IS NULL OR d.created_at <= :toDt) " +
           "AND (CAST(:kw AS VARCHAR) IS NULL OR (" +
           "   d.title LIKE '%' || :kw || '%' OR " +
           "   d.doc_no LIKE '%' || :kw || '%' " +
           "))",
           nativeQuery = true)
    Page<Document> searchDocuments(
            @Param("docType") String docType,
            @Param("status") String status,
            @Param("infraId") Long infraId,
            @Param("authorId") Long authorId,
            @Param("fromDt") LocalDateTime from,
            @Param("toDt") LocalDateTime to,
            @Param("kw") String keyword,
            Pageable pageable);

    // 문서번호 채번용: 해당 연도 최대 일련번호 조회
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(doc_no FROM '-(\\d+)호$') AS INTEGER)), 0) " +
                   "FROM tb_document WHERE doc_no LIKE :prefix",
           nativeQuery = true)
    Integer findMaxDocNoSeq(@Param("prefix") String prefix);

    // 성과통계용: 유형별/작성자별/기간별 건수
    @Query("SELECT COUNT(d) FROM Document d " +
           "WHERE d.docType = :docType " +
           "AND d.author.userSeq = :userId " +
           "AND d.status = 'APPROVED' " +
           "AND d.approvedAt BETWEEN :from AND :to")
    Long countApprovedByTypeAndUser(
            @Param("docType") String docType,
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
