package com.swmanager.system.repository;

import com.swmanager.system.domain.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    // 검색 기능 (아이디, 이름, 내용 등)
    @Query("SELECT l FROM AccessLog l WHERE " +
           "(:kw IS NULL OR l.userid LIKE %:kw% OR l.username LIKE %:kw% OR l.actionDetail LIKE %:kw%) " +
           "ORDER BY l.accessTime DESC")
    Page<AccessLog> findAllWithSearch(Pageable pageable, @Param("kw") String kw);

    // ===== [log-mgmt P3] 로그관리 탭 분리: 접속자 로그 / 메뉴·행위 로그 =====
    // 날짜 경계는 컨트롤러에서 LocalDateTime(fromStart=시작일 00:00, toExclusive=종료일+1 00:00)으로 전달.

    // fromStart/toExclusive 는 항상 바인딩(컨트롤러가 미지정 시 넓은 경계 주입).
    // JPQL `:param IS NULL` + null 날짜 바인딩은 PostgreSQL 타입추론 실패(42P18) → 회피.

    /** 접속자 로그 탭: menu_nm = '접속'(로그인/로그아웃). */
    @Query("SELECT l FROM AccessLog l WHERE l.menuNm = '접속' " +
           "AND (:kw IS NULL OR l.userid LIKE %:kw% OR l.username LIKE %:kw% OR l.actionDetail LIKE %:kw%) " +
           "AND l.accessTime >= :fromStart AND l.accessTime < :toExclusive " +
           "ORDER BY l.accessTime DESC")
    Page<AccessLog> findAccessTab(Pageable pageable, @Param("kw") String kw,
                                  @Param("fromStart") LocalDateTime fromStart,
                                  @Param("toExclusive") LocalDateTime toExclusive);

    /** 메뉴·행위 로그 탭: menu_nm <> '접속'(NULL 포함). */
    @Query("SELECT l FROM AccessLog l WHERE (l.menuNm IS NULL OR l.menuNm <> '접속') " +
           "AND (:kw IS NULL OR l.userid LIKE %:kw% OR l.username LIKE %:kw% OR l.actionDetail LIKE %:kw%) " +
           "AND l.accessTime >= :fromStart AND l.accessTime < :toExclusive " +
           "ORDER BY l.accessTime DESC")
    Page<AccessLog> findMenuTab(Pageable pageable, @Param("kw") String kw,
                                @Param("fromStart") LocalDateTime fromStart,
                                @Param("toExclusive") LocalDateTime toExclusive);

    // ===== [log-mgmt P4] 대시보드 통계 =====

    /** 최근 사업 변경이력 — 기간 무관 최신 6건(등록/수정/삭제). */
    List<AccessLog> findTop6ByMenuNmAndActionTypeInOrderByAccessTimeDesc(
            String menuNm, Collection<String> actionTypes);

    /** 일자별 접속/활동 추이(최근 30일): [date d, long 활동건수, long 접속자수(로그인 distinct userid)]. */
    @Query(value =
           "SELECT CAST(access_time AS date) AS d, COUNT(*) AS act, " +
           "COUNT(DISTINCT CASE WHEN action_type = '로그인' THEN userid END) AS visitors " +
           "FROM access_logs WHERE access_time >= now() - interval '30 days' " +
           "GROUP BY CAST(access_time AS date) ORDER BY d", nativeQuery = true)
    List<Object[]> findDailyTrend30d();

    /** 메뉴별 사용 빈도 TOP(최근 30일, 6건): [menu_nm, long count]. */
    @Query(value =
           "SELECT menu_nm, COUNT(*) AS c FROM access_logs " +
           "WHERE access_time >= now() - interval '30 days' AND menu_nm IS NOT NULL " +
           "GROUP BY menu_nm ORDER BY c DESC LIMIT 6", nativeQuery = true)
    List<Object[]> findMenuTop30d();

    /** 액션별 건수(최근 30일): [action_type, long count]. */
    @Query(value =
           "SELECT action_type, COUNT(*) AS c FROM access_logs " +
           "WHERE access_time >= now() - interval '30 days' AND action_type IS NOT NULL " +
           "GROUP BY action_type ORDER BY c DESC", nativeQuery = true)
    List<Object[]> findActionCounts30d();
}
