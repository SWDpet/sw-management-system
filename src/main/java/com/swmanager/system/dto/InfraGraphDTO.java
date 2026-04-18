package com.swmanager.system.dto;

import java.util.List;
import java.util.Map;

/**
 * 시스템 관계도 A 탭(인프라 구성도) 응답 DTO.
 *
 * Specs:
 *   docs/plans/system-graph-infra.md (v2)
 *   docs/dev-plans/system-graph-infra.md (v2)
 *
 * 민감 필드(acc_id/acc_pw/mac_addr/sw_acc_id/sw_acc_pw) 는
 * projection view records 에서 아예 제외하여 SELECT 단계부터 배제.
 */
public final class InfraGraphDTO {

    public record Node(String id, String label, String level, String domain, Map<String, Object> attrs) {}
    public record Edge(String from, String to) {}

    // ─── Projection records (허용 필드만) ────────────────────────
    public record InfraMasterView(
            Long infraId,
            String infraType,
            String cityNm,
            String distNm,
            String sysNm,
            String sysNmEn) {}

    public record InfraServerView(
            Long serverId,
            Long infraId,
            String serverType,
            String ipAddr,
            String osNm,
            String serverModel,
            String serialNo,
            String cpuSpec) {}

    public record InfraSoftwareView(
            Long swId,
            Long serverId,
            String swCategory,
            String swNm,
            String swVer,
            String port,
            String installPath,
            String sid) {}

    private final List<Node> nodes;
    private final List<Edge> edges;

    public InfraGraphDTO(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}
