package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.OpsKb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpsKbRepository extends JpaRepository<OpsKb, Long> {

    List<OpsKb> findByGubun(String gubun);

    // [ops-kb-workbench] active 필터 (추천 후보 — 소프트삭제/검증 제외)
    List<OpsKb> findByGubunAndStatus(String gubun, String status);
    List<OpsKb> findByStatusOrderByCaseCountDesc(String status);

    /** MANUAL kb_code 채번 시퀀스 (KB-yyyy-#####). */
    @Query(value = "SELECT nextval('seq_ops_kb_manual')", nativeQuery = true)
    Long nextManualSeq();

    /**
     * 지식베이스 조회 검색. status 는 필수(기본 'ACTIVE'). 시스템·구분·키워드·작성자 선택(null=미적용).
     * createdBy 는 비ACTIVE(PENDING/REJECTED) 를 본인 것만 보여줄 때 사용(관리자는 null 로 전체).
     */
    @Query("SELECT k FROM OpsKb k WHERE k.status = :status " +
           "AND (:sysType IS NULL OR k.sysType = :sysType) " +
           "AND (:gubun IS NULL OR k.gubun = :gubun) " +
           "AND (:createdBy IS NULL OR k.createdBy = :createdBy) " +
           // [fix] :kw 가 null 일 때 PostgreSQL 이 함수 인자 바인드를 bytea 로 추론해
           //       lower(bytea) 미존재 오류 → 명시적 CAST(:kw AS string) 로 타입 고정.
           "AND (:kw IS NULL OR LOWER(k.symptom) LIKE LOWER(CONCAT('%', CAST(:kw AS string), '%')) " +
           "     OR LOWER(k.keywords) LIKE LOWER(CONCAT('%', CAST(:kw AS string), '%')) " +
           "     OR LOWER(k.cause) LIKE LOWER(CONCAT('%', CAST(:kw AS string), '%'))) " +
           "ORDER BY k.caseCount DESC, k.kbId DESC")
    List<OpsKb> search(@Param("status") String status, @Param("sysType") String sysType,
                       @Param("gubun") String gubun, @Param("kw") String kw,
                       @Param("createdBy") String createdBy);

    /** 승인 대기(PENDING) 건수 — 관리자 배지. */
    long countByStatus(String status);
    /** 본인 제출의 특정 상태 건수 — 편집권한자 배지. */
    long countByStatusAndCreatedBy(String status, String createdBy);
}
