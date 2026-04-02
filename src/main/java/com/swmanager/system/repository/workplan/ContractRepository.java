package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {

    // 연도별 계약 목록
    List<Contract> findByContractYearOrderByCreatedAtDesc(Integer contractYear);

    // 인프라별 계약 목록
    List<Contract> findByInfra_InfraIdOrderByContractDateDesc(Long infraId);

    // 진행상태별 계약 목록
    List<Contract> findByProgressStatusOrderByCreatedAtDesc(String progressStatus);

    // 복합 검색 (연도 + 상태 + 키워드)
    @Query("SELECT c FROM Contract c " +
           "WHERE (:year IS NULL OR c.contractYear = :year) " +
           "AND (:status IS NULL OR c.progressStatus = :status) " +
           "AND (:kw IS NULL OR (" +
           "   c.contractName LIKE %:kw% OR " +
           "   c.contractNo LIKE %:kw% OR " +
           "   c.clientOrg LIKE %:kw% OR " +
           "   c.infra.cityNm LIKE %:kw% OR " +
           "   c.infra.distNm LIKE %:kw% " +
           "))")
    Page<Contract> searchContracts(
            @Param("year") Integer year,
            @Param("status") String status,
            @Param("kw") String keyword,
            Pageable pageable);

    // 연도 목록 조회 (필터용)
    @Query("SELECT DISTINCT c.contractYear FROM Contract c ORDER BY c.contractYear DESC")
    List<Integer> findDistinctYears();
}
