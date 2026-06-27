package com.swmanager.system.service;

import com.swmanager.system.dto.ErdGraphDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ErdGraphService 단위테스트 (beyond-A 커버리지 B).
 *
 * 순수 .mmd 파서(repo 의존 0, docs fixture 의존). new + ReflectionTestUtils 로 @Value 주입 후 init().
 * - 2-A 실데이터 smoke: docs/erd-*.mmd 구조 단언(회귀 안전망).
 * - 2-B @TempDir synthetic: pickCanonical 동률 tiebreaker + FK 2단계 유일쌍 매핑 정밀 검증(codex).
 * - 2-C 엣지: init 전 빈 DTO, 존재하지 않는 mmdDir warn-only.
 */
class ErdGraphServiceTest {

    private ErdGraphService svc(String mmdDir, String descFile) {
        ErdGraphService s = new ErdGraphService();
        ReflectionTestUtils.setField(s, "mmdDir", mmdDir);
        ReflectionTestUtils.setField(s, "descriptionsFile", descFile);
        return s;
    }

    private void write(Path dir, String domain, String content) throws IOException {
        Files.writeString(dir.resolve("erd-" + domain + ".mmd"), content);
    }

    private void emptyDomains(Path dir, String... domains) throws IOException {
        for (String d : domains) write(dir, d, "erDiagram\n");
    }

    // ── 2-A 실데이터 smoke ────────────────────────────────────────────────
    @Test
    void realMmd_parsesNodesEdgesWithPkFk() {
        ErdGraphService s = svc("docs", "docs/erd-descriptions.yml");
        s.init();
        ErdGraphDTO g = s.getGraph();

        assertThat(g.getNodes()).isNotEmpty();
        // 단언 약화(새 도메인 추가에 brittle 회피): id/domain non-blank + 핵심 도메인 core 존재만
        assertThat(g.getNodes()).allSatisfy(n -> {
            assertThat(n.id()).isNotBlank();
            assertThat(n.domain()).isNotBlank();
        });
        assertThat(g.getNodes()).anySatisfy(n -> assertThat(n.domain()).isEqualTo("core"));
        // PK Column 존재(parseColumn PK 분기)
        assertThat(g.getNodes()).anySatisfy(n -> assertThat(n.columns()).anyMatch(ErdGraphDTO.Column::pk));
        // FK + fkRef 매핑 존재(FK 휴리스틱)
        assertThat(g.getNodes()).anySatisfy(n ->
                assertThat(n.columns()).anyMatch(c -> c.fk() && c.fkRef() != null));
        // 엣지: from/to 가 실제 Node id, cardinality non-null
        Set<String> ids = g.getNodes().stream().map(ErdGraphDTO.Node::id).collect(Collectors.toSet());
        assertThat(g.getEdges()).isNotEmpty();
        assertThat(g.getEdges()).allSatisfy(e -> {
            assertThat(ids).contains(e.from());
            assertThat(ids).contains(e.to());
            assertThat(e.cardinality()).isNotNull();
        });
    }

    // ── 2-B synthetic: pickCanonical 동률 tiebreaker → 우선순위 높은 도메인 ──
    @Test
    void synthetic_tiebreaker_prefersHigherPriorityDomain(@TempDir Path dir) throws IOException {
        // 동일 컬럼 수(2) 중복 테이블을 core·infra 에 → 정본은 우선순위 높은 core
        // ⚠infra 를 먼저 써서 "파일/순회 순서가 아닌 DOMAIN_PRIORITY 로 core 선택"임을 증명
        String dup = "erDiagram\n    dup {\n        bigint id PK\n        text name\n    }\n";
        write(dir, "infra", dup);
        write(dir, "core", dup);
        emptyDomains(dir, "contract", "document", "quotation");

        ErdGraphService s = svc(dir.toString(), dir.resolve("none.yml").toString());
        s.init();

        ErdGraphDTO.Node node = s.getGraph().getNodes().stream()
                .filter(n -> n.id().equals("dup")).findFirst().orElseThrow();
        assertThat(node.domain()).isEqualTo("core");   // 동률 → DOMAIN_PRIORITY core(0) < infra(1), 순서 무관
        assertThat(node.columns()).hasSize(2);
    }

    // ── 2-B synthetic: FK 2단계 유일 쌍 매핑(이름 불일치) ──
    @Test
    void synthetic_fk2ndStage_uniquePairMapping(@TempDir Path dir) throws IOException {
        // parent.id(PK) 와 child.parent_ref(FK) 이름 불일치 → 1단계 실패
        // 잔여 관계 1 + 잔여 FK 1 → 2단계 유일쌍 매핑 fkRef=parent
        String core = "erDiagram\n"
                + "    parent {\n        bigint id PK\n    }\n"
                + "    child {\n        bigint child_id PK\n        bigint parent_ref FK\n    }\n"
                + "    parent ||--o{ child : \"has\"\n";
        write(dir, "core", core);
        emptyDomains(dir, "infra", "contract", "document", "quotation");

        ErdGraphService s = svc(dir.toString(), dir.resolve("none.yml").toString());
        s.init();

        ErdGraphDTO.Node child = s.getGraph().getNodes().stream()
                .filter(n -> n.id().equals("child")).findFirst().orElseThrow();
        ErdGraphDTO.Column fk = child.columns().stream()
                .filter(c -> c.name().equals("parent_ref")).findFirst().orElseThrow();
        assertThat(fk.fk()).isTrue();
        assertThat(fk.fkRef()).isEqualTo("parent");   // 2단계 유일쌍 매핑
        // 엣지 from=parent · to=child 전체 검증
        assertThat(s.getGraph().getEdges()).anySatisfy(e -> {
            assertThat(e.from()).isEqualTo("parent");
            assertThat(e.to()).isEqualTo("child");
        });
    }

    // ── 2-C 엣지: init 전 빈 DTO / 존재하지 않는 dir warn-only ──
    @Test
    void getGraph_beforeInit_returnsEmptyDto() {
        // init() 미호출 → 경로 무관(no-init 계약 명시 위해 더미 경로)
        ErdGraphDTO g = svc("dummy-dir", "dummy.yml").getGraph();
        assertThat(g.getNodes()).isEmpty();
        assertThat(g.getEdges()).isEmpty();
    }

    @Test
    void nonexistentDir_warnOnly_emptyGraph(@TempDir Path dir) {
        ErdGraphService s = svc(dir.resolve("nonexistent").toString(), dir.resolve("none.yml").toString());
        s.init();
        assertThat(s.getGraph().getNodes()).isEmpty();
        assertThat(s.getGraph().getEdges()).isEmpty();
    }
}
