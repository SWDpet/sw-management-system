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

    // 작성자별 문서 목록
    List<Document> findByAuthor_UserSeqOrderByCreatedAtDesc(Long userSeq);

    // 복합 검색 (PostgreSQL 호환 - CAST로 파라미터 타입 명시)
    // cityNm/distNm은 tb_infra_master 또는 sw_pjt 둘 중 한 곳에서 조회 (LEFT JOIN + COALESCE)
    @Query(value = "SELECT d.* FROM tb_document d " +
           "LEFT JOIN tb_infra_master im ON d.infra_id = im.infra_id " +
           "LEFT JOIN sw_pjt p ON d.proj_id = p.proj_id " +
           "WHERE (CAST(:docType AS VARCHAR) IS NULL OR d.doc_type = :docType) " +
           "AND (CAST(:status AS VARCHAR) IS NULL OR d.status = :status) " +
           "AND (CAST(:cityNm AS VARCHAR) IS NULL OR COALESCE(im.city_nm, p.city_nm) = :cityNm) " +
           "AND (CAST(:distNm AS VARCHAR) IS NULL OR COALESCE(im.dist_nm, p.dist_nm) = :distNm) " +
           "AND (CAST(:authorId AS BIGINT) IS NULL OR d.author_id = :authorId) " +
           "AND (CAST(:fromDt AS TIMESTAMP) IS NULL OR d.created_at >= :fromDt) " +
           "AND (CAST(:toDt AS TIMESTAMP) IS NULL OR d.created_at <= :toDt) " +
           "AND (CAST(:kw AS VARCHAR) IS NULL OR (" +
           "   d.title LIKE '%' || :kw || '%' OR " +
           "   d.doc_no LIKE '%' || :kw || '%' " +
           ")) " +
           "ORDER BY d.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM tb_document d " +
           "LEFT JOIN tb_infra_master im ON d.infra_id = im.infra_id " +
           "LEFT JOIN sw_pjt p ON d.proj_id = p.proj_id " +
           "WHERE (CAST(:docType AS VARCHAR) IS NULL OR d.doc_type = :docType) " +
           "AND (CAST(:status AS VARCHAR) IS NULL OR d.status = :status) " +
           "AND (CAST(:cityNm AS VARCHAR) IS NULL OR COALESCE(im.city_nm, p.city_nm) = :cityNm) " +
           "AND (CAST(:distNm AS VARCHAR) IS NULL OR COALESCE(im.dist_nm, p.dist_nm) = :distNm) " +
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
            @Param("cityNm") String cityNm,
            @Param("distNm") String distNm,
            @Param("authorId") Long authorId,
            @Param("fromDt") LocalDateTime from,
            @Param("toDt") LocalDateTime to,
            @Param("kw") String keyword,
            Pageable pageable);

    // 시도(광역시) 목록: infra + project 통합, 중복 제거
    @Query(value = "SELECT DISTINCT city_nm FROM (" +
           "  SELECT city_nm FROM tb_infra_master WHERE city_nm IS NOT NULL AND city_nm != '' " +
           "  UNION " +
           "  SELECT city_nm FROM sw_pjt WHERE city_nm IS NOT NULL AND city_nm != ''" +
           ") t ORDER BY city_nm",
           nativeQuery = true)
    List<String> findDistinctCityNames();

    // 시군구 목록: 특정 시도 기준, infra + project 통합
    @Query(value = "SELECT DISTINCT dist_nm FROM (" +
           "  SELECT dist_nm FROM tb_infra_master WHERE city_nm = :cityNm AND dist_nm IS NOT NULL AND dist_nm != '' " +
           "  UNION " +
           "  SELECT dist_nm FROM sw_pjt WHERE city_nm = :cityNm AND dist_nm IS NOT NULL AND dist_nm != ''" +
           ") t ORDER BY dist_nm",
           nativeQuery = true)
    List<String> findDistinctDistNamesByCity(@Param("cityNm") String cityNm);

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
