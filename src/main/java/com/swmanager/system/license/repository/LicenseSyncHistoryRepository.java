package com.swmanager.system.license.repository;

import com.swmanager.system.license.domain.LicenseSyncHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicenseSyncHistoryRepository extends JpaRepository<LicenseSyncHistory, Long> {

    /** 최근 연동 이력 (최대 20개) */
    List<LicenseSyncHistory> findTop20ByOrderByStartedAtDesc();

    /** 진행 중(RUNNING) 건수 — 동시 실행 방지용 */
    long countByStatus(String status);
}
