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
     * (server_role, host_name) 별 <b>최신 1건</b> — inspect-infra-diff-alert 비교용 (NFR-9 / R-14).
     *
     * <p>동일 host 에 collected_at 다중 row 존재 시 가장 최근 1건만. 동시각(tie)이면 snapshot_id 큰 것.
     * native window query — JPQL MAX() 는 tie 시 중복 반환하므로 row_number() 사용 (codex 검토).
     * 외부 SELECT 의 extra {@code rn} 컬럼은 엔티티 매핑에서 무시됨.
     */
    @Query(value = """
        SELECT * FROM (
          SELECT s.*, row_number() OVER (
            PARTITION BY pjt_id, server_role, COALESCE(host_name, '')
            ORDER BY collected_at DESC, snapshot_id DESC) rn
          FROM inspect_metric_snapshot s WHERE pjt_id = :pjtId
        ) t WHERE rn = 1
        """, nativeQuery = true)
    List<InspectMetricSnapshot> findLatestPerRoleHost(@Param("pjtId") Long pjtId);

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
