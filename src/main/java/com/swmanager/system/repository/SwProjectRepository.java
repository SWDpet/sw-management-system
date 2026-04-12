package com.swmanager.system.repository;

import com.swmanager.system.domain.SwProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * SW 프로젝트 Repository
 * - 기존 쿼리 유지
 * - 추가 검색 메서드
 */
@Repository
public interface SwProjectRepository extends JpaRepository<SwProject, Long>,
        JpaSpecificationExecutor<SwProject> {

    // ========== 기존 메서드 (유지) ==========

    /**
     * 1. 대시보드 연도 드롭다운용
     */
    @Query("SELECT DISTINCT p.year FROM SwProject p ORDER BY p.year DESC")
    List<Integer> findDistinctYears();

    /**
     * 2. 대시보드 통계 쿼리
     */
    @Query("SELECT p.sysNm as sysNm, p.sysNmEn as sysNmEn, COUNT(p) as totalCnt, " +
           "SUM(COALESCE(p.swAmt, 0) + COALESCE(p.cnsltAmt, 0) + COALESCE(p.dbImplAmt, 0) + " +
           "    COALESCE(p.pkgSwAmt, 0) + COALESCE(p.sysDevAmt, 0) + COALESCE(p.hwAmt, 0) + COALESCE(p.etcAmt, 0)) as sumCont, " +
           "SUM(COALESCE(p.swAmt, 0)) as sumSw, " +
           "SUM(COALESCE(p.cnsltAmt, 0)) as sumCnslt, " +
           "SUM(COALESCE(p.dbImplAmt, 0)) as sumDb, " +
           "SUM(COALESCE(p.pkgSwAmt, 0)) as sumPkg, " +
           "SUM(COALESCE(p.sysDevAmt, 0)) as sumDev, " +
           "SUM(COALESCE(p.hwAmt, 0)) as sumHw, " +
           "SUM(COALESCE(p.etcAmt, 0)) as sumEtc, " +
           "SUM(CASE WHEN p.stat = '2' THEN 1 ELSE 0 END) as compCnt, " +
           "SUM(CASE WHEN p.stat = '1' THEN 1 ELSE 0 END) as progCnt " +
           "FROM SwProject p WHERE (:year IS NULL OR p.year = :year) GROUP BY p.sysNm, p.sysNmEn ORDER BY p.sysNm ASC")
    List<Map<String, Object>> getSystemStats(@Param("year") Integer year);

    /**
     * 3. 커스텀 정렬 목록
     */
    @Query("SELECT p FROM SwProject p ORDER BY p.year DESC, p.orgNm ASC")
    List<SwProject> findAllOrderByCustom();

    /**
     * 4. 중복 체크
     */
    boolean existsByYearAndDistCdAndSysNmEn(Integer year, String distCd, String sysNmEn);

    /**
     * 5. 통합 검색 (페이징 지원)
     */
    @Query("SELECT p FROM SwProject p WHERE " +
           "(:kw IS NULL OR :kw = '' OR " +
           "CAST(p.year AS string) LIKE %:kw% OR " +
           "p.cityNm LIKE %:kw% OR " +
           "p.distNm LIKE %:kw% OR " +
           "p.sysNmEn LIKE %:kw% OR " +
           "p.pmsCd LIKE %:kw% OR " +
           "p.projNm LIKE %:kw% OR " +
           "p.client LIKE %:kw%)")
    Page<SwProject> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

    // ========== 추가 메서드 (선택사항) ==========

    /**
     * 프로젝트명으로 검색
     */
    Page<SwProject> findByProjNmContaining(String projNm, Pageable pageable);

    /**
     * 시스템명으로 검색
     */
    Page<SwProject> findBySysNmContaining(String sysNm, Pageable pageable);

    /**
     * 연도로 검색
     */
    Page<SwProject> findByYear(Integer year, Pageable pageable);

    /**
     * 연도와 상태로 검색
     */
    Page<SwProject> findByYearAndStat(Integer year, String stat, Pageable pageable);

    /**
     * 지자체(city/dist)별 프로젝트 목록
     */
    List<SwProject> findByCityNmAndDistNmOrderByYearDescProjIdDesc(String cityNm, String distNm);

    /**
     * 3단계 필터: 연도+지자체+시스템영문명으로 프로젝트 조회
     */
    List<SwProject> findByYearAndCityNmAndDistNmAndSysNmEnOrderByProjIdDesc(
            Integer year, String cityNm, String distNm, String sysNmEn);

    /**
     * 연도+지자체별 시스템영문명 목록
     */
    @Query("SELECT DISTINCT p.sysNmEn FROM SwProject p " +
           "WHERE p.year = :year AND p.cityNm = :cityNm AND p.distNm = :distNm " +
           "ORDER BY p.sysNmEn")
    List<String> findDistinctSysNmEnByYearAndCity(
            @Param("year") Integer year,
            @Param("cityNm") String cityNm,
            @Param("distNm") String distNm);

    /**
     * 연도별 지자체 목록 (city+dist 조합)
     */
    @Query("SELECT DISTINCT p.cityNm, p.distNm FROM SwProject p " +
           "WHERE p.year = :year ORDER BY p.cityNm, p.distNm")
    List<Object[]> findDistinctCityDistByYear(@Param("year") Integer year);

    /**
     * 연도별 시도(cityNm) 목록
     */
    @Query("SELECT DISTINCT p.cityNm FROM SwProject p " +
           "WHERE p.year = :year AND p.cityNm IS NOT NULL ORDER BY p.cityNm")
    List<String> findDistinctCityNmByYear(@Param("year") Integer year);

    /**
     * 연도+시도별 시군구(distNm) 목록
     */
    @Query("SELECT DISTINCT p.distNm FROM SwProject p " +
           "WHERE p.year = :year AND p.cityNm = :cityNm AND p.distNm IS NOT NULL " +
           "ORDER BY p.distNm")
    List<String> findDistinctDistNmByYearAndCityNm(
            @Param("year") Integer year, @Param("cityNm") String cityNm);

    /**
     * 기성계 대상 사업: pay_prog_yn = 'Y' (레거시)
     */
    List<SwProject> findByYearAndPayProgYnOrderByCityNmAscDistNmAsc(Integer year, String payProgYn);

    /**
     * 준공계 대상 사업: comp_yn = 'Y' (레거시)
     */
    List<SwProject> findByYearAndCompYnOrderByCityNmAscDistNmAsc(Integer year, String compYn);

    /**
     * 기성계 대상 사업: interim_yn = 'Y'
     */
    List<SwProject> findByYearAndInterimYnOrderByCityNmAscDistNmAsc(Integer year, String interimYn);

    /**
     * 준공계 대상 사업: completion_yn = 'Y'
     */
    List<SwProject> findByYearAndCompletionYnOrderByCityNmAscDistNmAsc(Integer year, String completionYn);
}
