package com.swmanager.system.lsa.repository;

import com.swmanager.system.lsa.domain.Lsa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * LSA 발급 대장 리포지토리. 키워드 검색(지자체/이름/버전/발급자).
 */
public interface LsaRepository extends JpaRepository<Lsa, Long> {

    @Query("SELECT l FROM Lsa l WHERE (:kw IS NULL OR " +
           "l.cityNm LIKE %:kw% OR l.distNm LIKE %:kw% OR " +
           "l.userNm LIKE %:kw% OR l.version LIKE %:kw% OR l.issuer LIKE %:kw%) " +
           "ORDER BY l.createdAt DESC, l.id DESC")
    List<Lsa> search(@Param("kw") String keyword);

    /** 대시보드 KPI '이번 달 발급' — created_at 이 [start, end) 범위(월 경계, 포함/미포함)에 드는 건수. */
    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
}
