package com.swmanager.system.service;

import com.swmanager.system.dto.InfraGraphDTO;
import com.swmanager.system.dto.InfraGraphDTO.Edge;
import com.swmanager.system.dto.InfraGraphDTO.InfraMasterView;
import com.swmanager.system.dto.InfraGraphDTO.InfraServerView;
import com.swmanager.system.dto.InfraGraphDTO.InfraSoftwareView;
import com.swmanager.system.dto.InfraGraphDTO.Node;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.InfraServerRepository;
import com.swmanager.system.repository.InfraSoftwareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 시스템 관계도 A 탭 — 인프라 구성도 그래프 빌더.
 *
 * 전략 (Specs FR-3, NFR-4):
 *   - JPQL projection 3개로 민감 필드 없이 조회 (master/server/software)
 *   - 메모리에서 계층(city→infra→server→software) 조립
 *   - 단일 요청당 SELECT 쿼리 3개 (레벨별 1개씩)
 *
 * domain 필드는 infra_type 을 기반으로 하며, 하위 레벨(서버/소프트웨어)은 조상의 infra_type 을
 * 상속하여 프론트 필터 구현을 단순화한다 (Specs FR-2).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InfraGraphService {

    private final InfraRepository infraRepository;
    private final InfraServerRepository serverRepository;
    private final InfraSoftwareRepository softwareRepository;

    public InfraGraphDTO getGraph() {
        List<InfraMasterView> masters = infraRepository.fetchAllMasterViews();
        List<InfraServerView> servers = serverRepository.fetchAllServerViews();
        List<InfraSoftwareView> softwares = softwareRepository.fetchAllSoftwareViews();

        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        // 정본 존재 여부 룩업 (orphan 탐지)
        Set<Long> existingInfraIds = new HashSet<>();
        for (InfraMasterView m : masters) existingInfraIds.add(m.infraId());
        // 실제 노드로 추가되는 서버 ID — orphan 서버가 스킵되므로 그 서버에 딸린 SW 도 스킵
        Set<Long> renderedServerIds = new HashSet<>();

        // 도메인 상속용 룩업
        Map<Long, String> domainByInfra = new HashMap<>();
        for (InfraMasterView m : masters) {
            domainByInfra.put(m.infraId(), safe(m.infraType()));
        }
        Map<Long, String> domainByServer = new HashMap<>();
        for (InfraServerView s : servers) {
            domainByServer.put(s.serverId(), domainByInfra.getOrDefault(s.infraId(), ""));
        }

        // 지자체(city|dist) 노드 — 중복 제거
        Set<String> cityKeys = new HashSet<>();
        for (InfraMasterView m : masters) {
            String cityKey = safe(m.cityNm()) + "|" + safe(m.distNm());
            String cityId = "city:" + cityKey;
            if (cityKeys.add(cityKey)) {
                long cnt = masters.stream()
                        .filter(x -> Objects.equals(x.cityNm(), m.cityNm())
                                  && Objects.equals(x.distNm(), m.distNm()))
                        .count();
                Map<String, Object> attrs = new LinkedHashMap<>();
                attrs.put("city_nm", safe(m.cityNm()));
                attrs.put("dist_nm", safe(m.distNm()));
                attrs.put("sys_count", cnt);
                nodes.add(new Node(cityId, safe(m.cityNm()) + " " + safe(m.distNm()),
                        "city", null, attrs));
            }
        }

        // 시스템 노드 + 지자체→시스템 엣지
        Map<Long, Long> serverCountByInfra = new HashMap<>();
        for (InfraServerView s : servers) {
            serverCountByInfra.merge(s.infraId(), 1L, Long::sum);
        }
        for (InfraMasterView m : masters) {
            String infraId = "infra:" + m.infraId();
            String cityId = "city:" + safe(m.cityNm()) + "|" + safe(m.distNm());
            // sys_nm 이 null/공백이면 식별 가능한 fallback label 표시
            String sysNm = safe(m.sysNm());
            String sysNmEn = safe(m.sysNmEn());
            String label;
            if (!sysNm.isEmpty()) label = sysNm;
            else if (!sysNmEn.isEmpty()) label = sysNmEn;
            else label = "(이름 없음 #" + m.infraId() + ")";
            Map<String, Object> attrs = new LinkedHashMap<>();
            attrs.put("sys_nm", sysNm);
            attrs.put("sys_nm_en", sysNmEn);
            attrs.put("infra_type", safe(m.infraType()));
            attrs.put("city_nm", safe(m.cityNm()));
            attrs.put("dist_nm", safe(m.distNm()));
            attrs.put("server_count", serverCountByInfra.getOrDefault(m.infraId(), 0L));
            nodes.add(new Node(infraId, label, "infra", safe(m.infraType()), attrs));
            edges.add(new Edge(cityId, infraId));
        }

        // 서버 노드 + 시스템→서버 엣지
        Map<Long, Long> swCountByServer = new HashMap<>();
        for (InfraSoftwareView w : softwares) {
            swCountByServer.merge(w.serverId(), 1L, Long::sum);
        }
        int orphanServers = 0;
        for (InfraServerView s : servers) {
            // orphan (부모 infra 없음) — 노드/엣지 생성 스킵. 별도로 보여주려면 Phase 2 에서.
            if (s.infraId() == null || !existingInfraIds.contains(s.infraId())) {
                orphanServers++;
                continue;
            }
            String srvId = "srv:" + s.serverId();
            String infraId = "infra:" + s.infraId();
            String label = safe(s.serverType())
                    + (s.ipAddr() != null && !s.ipAddr().isEmpty() ? " (" + s.ipAddr() + ")" : "");
            Map<String, Object> attrs = new LinkedHashMap<>();
            attrs.put("server_type", safe(s.serverType()));
            attrs.put("ip_addr", safe(s.ipAddr()));
            attrs.put("os_nm", safe(s.osNm()));
            attrs.put("server_model", safe(s.serverModel()));
            attrs.put("serial_no", safe(s.serialNo()));
            attrs.put("cpu_spec", safe(s.cpuSpec()));
            attrs.put("sw_count", swCountByServer.getOrDefault(s.serverId(), 0L));
            nodes.add(new Node(srvId, label, "server",
                    domainByInfra.getOrDefault(s.infraId(), ""), attrs));
            edges.add(new Edge(infraId, srvId));
            renderedServerIds.add(s.serverId());
        }

        // SW 노드 + 서버→SW 엣지
        int orphanSoftwares = 0;
        for (InfraSoftwareView w : softwares) {
            if (w.serverId() == null || !renderedServerIds.contains(w.serverId())) {
                orphanSoftwares++;
                continue;
            }
            String swId = "sw:" + w.swId();
            String srvId = "srv:" + w.serverId();
            String label = safe(w.swNm())
                    + (w.swVer() != null && !w.swVer().isEmpty() ? " " + w.swVer() : "");
            Map<String, Object> attrs = new LinkedHashMap<>();
            attrs.put("sw_category", safe(w.swCategory()));
            attrs.put("sw_nm", safe(w.swNm()));
            attrs.put("sw_ver", safe(w.swVer()));
            attrs.put("port", safe(w.port()));
            attrs.put("install_path", safe(w.installPath()));
            attrs.put("sid", safe(w.sid()));
            nodes.add(new Node(swId, label, "software",
                    domainByServer.getOrDefault(w.serverId(), ""), attrs));
            edges.add(new Edge(srvId, swId));
        }

        log.info("InfraGraph built: city={}, infra={}, server={}(orphan {}), sw={}(orphan {}), nodes={}, edges={}",
                cityKeys.size(), masters.size(),
                servers.size(), orphanServers,
                softwares.size(), orphanSoftwares,
                nodes.size(), edges.size());
        if (orphanServers > 0 || orphanSoftwares > 0) {
            log.warn("Orphan detected — 부모 infra/server 가 없는 데이터가 있습니다. DB 정합성 점검 권장 (servers: {}, softwares: {})",
                    orphanServers, orphanSoftwares);
        }
        return new InfraGraphDTO(nodes, edges);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
