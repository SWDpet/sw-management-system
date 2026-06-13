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

    /** 지식베이스 조회 검색 (ACTIVE, 시스템·구분·키워드 선택). 파라미터 null = 미적용. */
    @Query("SELECT k FROM OpsKb k WHERE k.status = 'ACTIVE' " +
           "AND (:sysType IS NULL OR k.sysType = :sysType) " +
           "AND (:gubun IS NULL OR k.gubun = :gubun) " +
           "AND (:kw IS NULL OR LOWER(k.symptom) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "     OR LOWER(k.keywords) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "     OR LOWER(k.cause) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
           "ORDER BY k.caseCount DESC, k.kbId DESC")
    List<OpsKb> search(@Param("sysType") String sysType, @Param("gubun") String gubun, @Param("kw") String kw);
}
