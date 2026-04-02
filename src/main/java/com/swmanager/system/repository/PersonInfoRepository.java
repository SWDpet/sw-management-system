package com.swmanager.system.repository;

import com.swmanager.system.domain.PersonInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonInfoRepository extends JpaRepository<PersonInfo, Long> {

    /**
     * [통합 검색 쿼리]
     * 엔티티 필드명을 기준으로 검색해야 합니다.
     * rankNm 대신 실제 필드인 'pos'를 사용하도록 수정했습니다.
     */
    @Query("SELECT p FROM PersonInfo p WHERE " +
           "(:kw IS NULL OR :kw = '' OR " +
           "p.cityNm LIKE %:kw% OR " +
           "p.distNm LIKE %:kw% OR " +
           "p.orgNm LIKE %:kw% OR " +
           "p.sysNmEn LIKE %:kw% OR " +
           "p.deptNm LIKE %:kw% OR " +
           "p.teamNm LIKE %:kw% OR " +
           "p.userNm LIKE %:kw% OR " +
           "p.pos LIKE %:kw% OR " +    // [수정됨] p.rankNm -> p.pos
           "p.tel LIKE %:kw% OR " +
           "p.email LIKE %:kw%)")
    Page<PersonInfo> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
}