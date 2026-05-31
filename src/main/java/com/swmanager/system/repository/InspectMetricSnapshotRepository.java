package com.swmanager.system.repository;

import com.swmanager.system.domain.InspectMetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * inspect_metric_snapshot Repository — P5 메트릭 추이 차트 30일 데이터 + 호스트 목록.
 *
 * <p>적재는 INSERT ... ON CONFLICT DO NOTHING 으로 멱등성 보장 (DB팀 R5 / D-5).
 * Spring Data JPA 가 기본 제공하는 save() 는 멱등성 보장 X (PK 중복 시 예외) — 따라서
 * {@link #upsertIgnore} native query 로 적재.
 */
@Repository
public interface InspectMetricSnapshotRepository extends JpaRepository<InspectMetricSnapshot, Long> {

    /**
     * 30일 차트용 — pjt + serverRole + (선택) hostName 단일 호스트 시계열.
     *
     * @param pjtId      SwProject.proj_id
     * @param serverRole 'AP' / 'DB'
     * @param hostName   특정 호스트 — null/blank 이면 {@link #findRecentByPjtRole} 사용 권장
     * @param since      30일 전 시각 (예: now() - 30d)
     */
    @Query("""
        SELECT m FROM InspectMetricSnapshot m
         WHERE m.pjtId = :pjtId
           AND m.serverRole = :role
           AND m.hostName = :host
           AND m.collectedAt >= :since
         ORDER BY m.collectedAt ASC
        """)
    List<InspectMetricSnapshot> findRecentByPjtRoleHost(
            @Param("pjtId") Long pjtId,
            @Param("role") String serverRole,
            @Param("host") String hostName,
            @Param("since") OffsetDateTime since);

    /**
     * pjt + serverRole 의 모든 호스트 시계열 — 호스트별 line 분리 표시용 (DB팀 D-3).
     */
    @Query("""
        SELECT m FROM InspectMetricSnapshot m
         WHERE m.pjtId = :pjtId
           AND m.serverRole = :role
           AND m.collectedAt >= :since
         ORDER BY m.hostName ASC, m.collectedAt ASC
        """)
    List<InspectMetricSnapshot> findRecentByPjtRole(
            @Param("pjtId") Long pjtId,
            @Param("role") String serverRole,
            @Param("since") OffsetDateTime since);

    /**
     * pjt + serverRole 안 unique 호스트 목록 — 차트 line 색 매핑용.
     */
    @Query("""
        SELECT DISTINCT m.hostName FROM InspectMetricSnapshot m
         WHERE m.pjtId = :pjtId
           AND m.serverRole = :role
           AND m.collectedAt >= :since
         ORDER BY m.hostName ASC
        """)
    List<String> findHostsByPjtRole(
            @Param("pjtId") Long pjtId,
            @Param("role") String serverRole,
            @Param("since") OffsetDateTime since);

    // ── 점검주기 윈도우 [since, until) — 직전 점검월 ~ 이번 점검월 추이용 (2026-05-31) ──
    @Query("""
        SELECT m FROM InspectMetricSnapshot m
         WHERE m.pjtId = :pjtId AND m.serverRole = :role AND m.hostName = :host
           AND m.collectedAt >= :since AND m.collectedAt < :until
         ORDER BY m.collectedAt ASC
        """)
    List<InspectMetricSnapshot> findRangeByPjtRoleHost(
            @Param("pjtId") Long pjtId, @Param("role") String serverRole,
            @Param("host") String hostName,
            @Param("since") OffsetDateTime since, @Param("until") OffsetDateTime until);

    @Query("""
        SELECT m FROM InspectMetricSnapshot m
         WHERE m.pjtId = :pjtId AND m.serverRole = :role
           AND m.collectedAt >= :since AND m.collectedAt < :until
         ORDER BY m.hostName ASC, m.collectedAt ASC
        """)
    List<InspectMetricSnapshot> findRangeByPjtRole(
            @Param("pjtId") Long pjtId, @Param("role") String serverRole,
            @Param("since") OffsetDateTime since, @Param("until") OffsetDateTime until);

    @Query("""
        SELECT DISTINCT m.hostName FROM InspectMetricSnapshot m
         WHERE m.pjtId = :pjtId AND m.serverRole = :role
           AND m.collectedAt >= :since AND m.collectedAt < :until
         ORDER BY m.hostName ASC
        """)
    List<String> findHostsByPjtRoleRange(
            @Param("pjtId") Long pjtId, @Param("role") String serverRole,
            @Param("since") OffsetDateTime since, @Param("until") OffsetDateTime until);

    /**
     * 멱등 적재 — 동일 (pjt_id, server_role, host_name, collected_at) 재INSERT 시 무시.
     * PostgreSQL native query. raw_payload 는 jsonb 캐스팅.
     *
     * <p>반환: 영향 row 수 (1 = 신규 적재, 0 = 중복 무시).
     */
    @Modifying
    @Query(value = """
        INSERT INTO inspect_metric_snapshot
            (pjt_id, server_role, host_name, host_ip, collected_at,
             cpu_pct, mem_pct, disk_pct, raw_payload, created_at)
        VALUES
            (:pjtId, :serverRole, :hostName, :hostIp, :collectedAt,
             :cpuPct, :memPct, :diskPct, cast(:rawPayloadJson as jsonb), now())
        ON CONFLICT (pjt_id, server_role, COALESCE(host_name, ''), collected_at)
        DO NOTHING
        """, nativeQuery = true)
    int upsertIgnore(
            @Param("pjtId") Long pjtId,
            @Param("serverRole") String serverRole,
            @Param("hostName") String hostName,
            @Param("hostIp") String hostIp,
            @Param("collectedAt") OffsetDateTime collectedAt,
            @Param("cpuPct") java.math.BigDecimal cpuPct,
            @Param("memPct") java.math.BigDecimal memPct,
            @Param("diskPct") java.math.BigDecimal diskPct,
            @Param("rawPayloadJson") String rawPayloadJson);
}
