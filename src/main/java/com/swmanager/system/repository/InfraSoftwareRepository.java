package com.swmanager.system.repository;

import com.swmanager.system.domain.InfraSoftware;
import com.swmanager.system.dto.InfraGraphDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfraSoftwareRepository extends JpaRepository<InfraSoftware, Long> {

    /**
     * 시스템 관계도 A 탭용 — 소프트웨어 허용 필드만 조회 (Specs FR-3).
     * 민감 필드(sw_acc_id/sw_acc_pw) 는 SELECT 절에 포함하지 않음.
     */
    @Query("SELECT new com.swmanager.system.dto.InfraGraphDTO$InfraSoftwareView(" +
           "  w.swId, w.server.serverId, w.swCategory, w.swNm, w.swVer, " +
           "  w.port, w.installPath, w.sid) " +
           "FROM InfraSoftware w")
    List<InfraGraphDTO.InfraSoftwareView> fetchAllSoftwareViews();
}
