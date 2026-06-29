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

    /**
     * [LSA prefill/dedup] 지자체(시도·시군구) + 부서·팀 매칭 담당자 후보.
     * dept/team 은 blank→null 정규화 후 호출(null 이면 해당 조건 무시).
     */
    @Query("SELECT p FROM PersonInfo p WHERE p.cityNm = :city AND p.distNm = :dist " +
           "AND (:dept IS NULL OR p.deptNm = :dept) AND (:team IS NULL OR p.teamNm = :team) " +
           "ORDER BY p.userNm")
    java.util.List<PersonInfo> findCandidates(@Param("city") String city, @Param("dist") String dist,
                                              @Param("dept") String dept, @Param("team") String team);
}