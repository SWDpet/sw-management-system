package com.swmanager.system.dto;

import java.util.List;

/**
 * 시스템 관계도 C 탭(ERD 인터랙티브) 응답 DTO.
 * docs/plans/system-graph-erd.md v2 FR-2 참조.
 */
public final class ErdGraphDTO {

    public record Column(String name, String type, boolean pk, boolean fk, String fkRef) {}
    public record Node(String id, String label, String domain, List<Column> columns) {}
    public record Edge(String from, String to, String label, String cardinality) {}

    private final List<Node> nodes;
    private final List<Edge> edges;

    public ErdGraphDTO(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}
