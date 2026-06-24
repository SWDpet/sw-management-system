package com.swmanager.system.service;

import com.swmanager.system.dto.InfraGraphDTO;
import com.swmanager.system.dto.InfraGraphDTO.InfraMasterView;
import com.swmanager.system.dto.InfraGraphDTO.InfraServerView;
import com.swmanager.system.dto.InfraGraphDTO.InfraSoftwareView;
import com.swmanager.system.dto.InfraGraphDTO.Node;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.InfraServerRepository;
import com.swmanager.system.repository.InfraSoftwareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * InfraGraphService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 생성자 주입이라 직접 생성. 계층(city→infra→server→software) 조립·city 중복제거·
 * sys_nm/sys_nm_en/placeholder fallback label·orphan 서버·SW 스킵·도메인 상속·카운트·null 안전 커버.
 */
class InfraGraphServiceTest {

    private final InfraRepository infraRepository = mock(InfraRepository.class);
    private final InfraServerRepository serverRepository = mock(InfraServerRepository.class);
    private final InfraSoftwareRepository softwareRepository = mock(InfraSoftwareRepository.class);

    private InfraGraphService service;

    @BeforeEach
    void setUp() {
        service = new InfraGraphService(infraRepository, serverRepository, softwareRepository);
    }

    private void stub(List<InfraMasterView> m, List<InfraServerView> s, List<InfraSoftwareView> w) {
        when(infraRepository.fetchAllMasterViews()).thenReturn(m);
        when(serverRepository.fetchAllServerViews()).thenReturn(s);
        when(softwareRepository.fetchAllSoftwareViews()).thenReturn(w);
    }

    private InfraMasterView master(Long id, String type, String city, String dist, String sysNm, String sysNmEn) {
        return new InfraMasterView(id, type, city, dist, sysNm, sysNmEn);
    }

    private InfraServerView server(Long id, Long infraId, String type, String ip) {
        return new InfraServerView(id, infraId, type, ip, null, null, null, null);
    }

    private InfraSoftwareView software(Long swId, Long serverId, String nm, String ver) {
        return new InfraSoftwareView(swId, serverId, null, nm, ver, null, null, null);
    }

    private Node node(InfraGraphDTO g, String id) {
        return g.getNodes().stream().filter(n -> n.id().equals(id)).findFirst().orElse(null);
    }

    /** 존재를 전제로 디레퍼런스하는 양성 단언용 — 누락 시 NPE 대신 명확한 실패 메시지. */
    private Node requireNode(InfraGraphDTO g, String id) {
        Node n = node(g, id);
        assertThat(n).as("node %s", id).isNotNull();
        return n;
    }

    private boolean hasEdge(InfraGraphDTO g, String from, String to) {
        return g.getEdges().stream().anyMatch(e -> e.from().equals(from) && e.to().equals(to));
    }

    // ===== 빈 입력 =====

    @Test
    void getGraph_emptyInputs_emptyGraph() {
        stub(List.of(), List.of(), List.of());
        InfraGraphDTO g = service.getGraph();
        assertThat(g.getNodes()).isEmpty();
        assertThat(g.getEdges()).isEmpty();
    }

    // ===== 계층 조립 + dedup + 카운트 + fallback label =====

    @Test
    void getGraph_buildsHierarchy_dedupCity_countsAndFallbackLabels() {
        stub(
            List.of(
                master(1L, "UPIS", "강원도", "춘천시", "상수도", "WS"),
                master(2L, "KRAS", "강원도", "춘천시", "", "GIS"),          // 같은 city/dist → dedup, sysNm 공백 → sysNmEn
                master(3L, "UPIS", "서울", "강남", "", "")                  // sysNm·sysNmEn 공백 → placeholder
            ),
            List.of(server(10L, 1L, "WEB", "1.2.3.4")),
            List.of(software(100L, 10L, "Tomcat", "9"))
        );

        InfraGraphDTO g = service.getGraph();

        // 노드 수: city 2 + infra 3 + server 1 + sw 1 = 7
        assertThat(g.getNodes()).hasSize(7);
        // 엣지 수: city→infra 3 + infra→server 1 + server→sw 1 = 5
        assertThat(g.getEdges()).hasSize(5);

        // city dedup: 강원도|춘천시 한 노드, sys_count=2
        assertThat(requireNode(g, "city:강원도|춘천시").attrs()).containsEntry("sys_count", 2L);
        assertThat(requireNode(g, "city:서울|강남").attrs()).containsEntry("sys_count", 1L);

        // fallback label: sysNm → sysNmEn → placeholder
        assertThat(requireNode(g, "infra:1").label()).isEqualTo("상수도");
        assertThat(requireNode(g, "infra:2").label()).isEqualTo("GIS");
        assertThat(requireNode(g, "infra:3").label()).isEqualTo("(이름 없음 #3)");

        // server_count: infra:1 에 서버 1
        assertThat(requireNode(g, "infra:1").attrs()).containsEntry("server_count", 1L);

        // server label = type + (ip), sw_count
        assertThat(requireNode(g, "srv:10").label()).isEqualTo("WEB (1.2.3.4)");
        assertThat(requireNode(g, "srv:10").attrs()).containsEntry("sw_count", 1L);

        // sw label = nm + ver
        assertThat(requireNode(g, "sw:100").label()).isEqualTo("Tomcat 9");

        // 엣지 연결 — dedup된 city 가 두 infra(1,2) 로 각각 fanout
        assertThat(hasEdge(g, "city:강원도|춘천시", "infra:1")).isTrue();
        assertThat(hasEdge(g, "city:강원도|춘천시", "infra:2")).isTrue();
        assertThat(hasEdge(g, "infra:1", "srv:10")).isTrue();
        assertThat(hasEdge(g, "srv:10", "sw:100")).isTrue();
    }

    // ===== orphan 서버·SW 스킵 =====

    @Test
    void getGraph_skipsOrphanServersAndDependentSoftware() {
        stub(
            List.of(master(1L, "UPIS", "강원도", "춘천시", "상수도", "WS")),
            List.of(
                server(10L, 1L, "WEB", null),       // 정상
                server(11L, 99L, "WAS", null),      // 부모 infra 99 없음 → orphan
                server(12L, null, "DB", null)       // infraId null → orphan
            ),
            List.of(
                software(100L, 10L, "Tomcat", null),  // 정상 (server 10 rendered)
                software(101L, 11L, "Oracle", null),  // 부모 server 11 orphan → 스킵
                software(102L, null, "Redis", null)   // serverId null → 스킵
            )
        );

        InfraGraphDTO g = service.getGraph();

        assertThat(node(g, "srv:10")).isNotNull();
        assertThat(node(g, "srv:11")).isNull();        // orphan 서버 미렌더
        assertThat(node(g, "srv:12")).isNull();
        assertThat(node(g, "sw:100")).isNotNull();
        assertThat(node(g, "sw:101")).isNull();        // orphan 서버에 딸린 SW 도 스킵
        assertThat(node(g, "sw:102")).isNull();
    }

    // ===== 도메인 상속 =====

    @Test
    void getGraph_inheritsInfraTypeToServerAndSoftware() {
        stub(
            List.of(master(1L, "KRAS", "서울", "강남", "지적", "CADASTRE")),
            List.of(server(10L, 1L, "WEB", null)),
            List.of(software(100L, 10L, "nginx", null))
        );

        InfraGraphDTO g = service.getGraph();

        assertThat(requireNode(g, "srv:10").domain()).isEqualTo("KRAS");   // 서버는 부모 infra_type 상속
        assertThat(requireNode(g, "sw:100").domain()).isEqualTo("KRAS");   // SW 는 서버(=infra) 도메인 상속
    }

    // ===== null 안전 (safe) =====

    @Test
    void getGraph_nullFields_renderedAsEmptyStrings() {
        stub(
            List.of(master(1L, null, null, null, null, null)),  // 전 필드 null
            List.of(),
            List.of()
        );

        InfraGraphDTO g = service.getGraph();

        // null city/dist → "|" 키
        assertThat(requireNode(g, "city:|").attrs()).containsEntry("city_nm", "").containsEntry("dist_nm", "");
        // sysNm·sysNmEn null → placeholder label
        assertThat(requireNode(g, "infra:1").label()).isEqualTo("(이름 없음 #1)");
        assertThat(requireNode(g, "infra:1").domain()).isEmpty();   // infra_type null → ""
    }
}
