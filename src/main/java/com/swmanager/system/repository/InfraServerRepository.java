package com.swmanager.system.repository;

import com.swmanager.system.domain.InfraServer;
import com.swmanager.system.dto.InfraGraphDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * InfraServer 전용 Repository (현재는 시스템 관계도 A 탭 projection 에서만 사용).
 * 다른 곳에서 서버를 조회할 때는 Infra 엔티티의 @OneToMany 관계를 사용한다.
 */
@Repository
public interface InfraServerRepository extends JpaRepository<InfraServer, Long> {

    /**
     * 시스템 관계도 A 탭용 — 서버 허용 필드만 조회 (Specs FR-3).
     * 민감 필드(acc_id/acc_pw/mac_addr) 는 SELECT 절에 포함하지 않음.
     */
    @Query("SELECT new com.swmanager.system.dto.InfraGraphDTO$InfraServerView(" +
           "  s.serverId, s.infra.infraId, s.serverType, s.ipAddr, s.osNm, " +
           "  s.serverModel, s.serialNo, s.cpuSpec) " +
           "FROM InfraServer s")
    List<InfraGraphDTO.InfraServerView> fetchAllServerViews();
}
