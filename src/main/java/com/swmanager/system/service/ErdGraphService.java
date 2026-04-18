package com.swmanager.system.service;

import com.swmanager.system.dto.ErdGraphDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 시스템 관계도 C 탭 — ERD 인터랙티브용 .mmd 파서 + 병합 + 캐시.
 *
 * Specs:
 *   docs/plans/system-graph-erd.md (v2)
 *   docs/dev-plans/system-graph-erd.md (v2)
 *
 * 파일 위치는 FileSystemResource(설정값) 우선 → ClassPathResource(fallback) 순.
 * 서버 기동 시 1회 파싱 후 in-memory 캐시. 재파싱은 서버 재기동 필요.
 */
@Slf4j
@Service
public class ErdGraphService {

    /** 파일 탐색 순서이자 도메인 목록 (FR-1-MERGE tiebreaker 우선순위) */
    private static final List<String> DOMAINS = List.of("core", "infra", "contract", "document", "quotation");

    private static final Map<String, Integer> DOMAIN_PRIORITY;
    static {
        Map<String, Integer> m = new HashMap<>();
        for (int i = 0; i < DOMAINS.size(); i++) m.put(DOMAINS.get(i), i);
        DOMAIN_PRIORITY = Map.copyOf(m);
    }

    /** 파싱 정규식 (v2: 가변 공백, 정밀도 타입, modifier 순서 허용) */
    private static final Pattern BLOCK_START = Pattern.compile("^\\s+(\\w+)\\s*\\{\\s*$");
    private static final Pattern BLOCK_END   = Pattern.compile("^\\s+\\}\\s*$");
    private static final Pattern COLUMN      = Pattern.compile("^\\s+([A-Za-z][A-Za-z0-9_\\[\\](),]*)\\s+(\\w+)(\\s+(PK|FK|UK|PK,FK|FK,PK))?\\s*$");
    private static final Pattern RELATION    = Pattern.compile("^\\s+(\\w+)\\s+(\\S+)\\s+(\\w+)\\s*:\\s*\"([^\"]*)\"\\s*$");

    @Value("${app.erd.mmd-dir:docs}")
    private String mmdDir;

    private volatile ErdGraphDTO cache;

    @PostConstruct
    public void init() {
        long start = System.currentTimeMillis();
        try {
            this.cache = load();
            log.info("ERD parsing: {}ms, nodes={}, edges={}",
                    System.currentTimeMillis() - start,
                    cache.getNodes().size(), cache.getEdges().size());
        } catch (Exception e) {
            log.error("ERD 초기 파싱 실패 — 빈 그래프로 대체", e);
            this.cache = new ErdGraphDTO(List.of(), List.of());
        }
    }

    /** 캐시된 그래프 반환. 캐시가 비어도 빈 DTO (null 반환 안 함). */
    public ErdGraphDTO getGraph() {
        return cache != null ? cache : new ErdGraphDTO(List.of(), List.of());
    }

    // ─────────────────────────── 내부 구현 ───────────────────────────

    private static class RawTable {
        String name;
        String domain;
        final List<ErdGraphDTO.Column> columns = new ArrayList<>();
    }

    private static class RawRelation {
        String from, to, cardinality, label;
    }

    private ErdGraphDTO load() {
        Map<String, List<RawTable>> byName = new LinkedHashMap<>();
        List<RawRelation> allRelations = new ArrayList<>();

        for (String domain : DOMAINS) {
            try {
                List<String> lines = readFile(domain);
                if (lines == null) continue;
                parseFile(domain, lines, byName, allRelations);
            } catch (Exception e) {
                log.warn("ERD 파일 파싱 실패: erd-{}.mmd", domain, e);
            }
        }

        // FR-1-MERGE: 컬럼 수 최대 정본, 동률이면 도메인 우선순위로 tiebreaker
        Map<String, String> tableDomain = new HashMap<>();
        Map<String, ErdGraphDTO.Column> pkByTable = new HashMap<>();
        Map<String, List<ErdGraphDTO.Column>> canonicalCols = new LinkedHashMap<>();

        for (Map.Entry<String, List<RawTable>> e : byName.entrySet()) {
            RawTable canonical = pickCanonical(e.getValue());
            tableDomain.put(canonical.name, canonical.domain);
            canonicalCols.put(canonical.name, canonical.columns);
            canonical.columns.stream()
                    .filter(ErdGraphDTO.Column::pk)
                    .findFirst()
                    .ifPresent(pk -> pkByTable.put(canonical.name, pk));
        }

        // 엣지 합집합 (from + to + cardinality 기준 중복 제거)
        Set<String> edgeKeys = new HashSet<>();
        List<ErdGraphDTO.Edge> edges = new ArrayList<>();
        for (RawRelation r : allRelations) {
            // 양쪽 모두 정본에 존재하는 테이블에 한해서만 엣지 추가
            if (!canonicalCols.containsKey(r.from) || !canonicalCols.containsKey(r.to)) continue;
            String key = r.from + "|" + r.to + "|" + r.cardinality;
            if (edgeKeys.add(key)) {
                edges.add(new ErdGraphDTO.Edge(r.from, r.to, r.label, r.cardinality));
            }
        }

        // FR-5-A: FK → fkRef 매핑 (관계선에 존재하는 FK 에 대해서만)
        // 2단계 휴리스틱:
        //   1단계 — A ||--o{ B 관계에서, B 의 FK 컬럼 중 A 의 PK 컬럼명과 동일한 것이 있으면 fkRef=A
        //   2단계 — 1단계 후 B 테이블에 미매칭 관계선이 정확히 1건 + 미할당 FK 컬럼이 정확히 1개 남으면 그 쌍을 매핑
        //           (이름이 달라도 유일한 쌍이면 관계선 기반으로 명확히 연결)
        Map<String, Map<String, String>> fkRef = new HashMap<>();
        Set<Integer> matchedRelIdx = new HashSet<>();

        // ── 1단계: 정확 이름 일치
        for (int i = 0; i < allRelations.size(); i++) {
            RawRelation r = allRelations.get(i);
            ErdGraphDTO.Column pkOfFrom = pkByTable.get(r.from);
            if (pkOfFrom == null) continue;
            List<ErdGraphDTO.Column> toCols = canonicalCols.get(r.to);
            if (toCols == null) continue;
            for (ErdGraphDTO.Column c : toCols) {
                if (c.fk() && c.name().equals(pkOfFrom.name())) {
                    boolean added = fkRef.computeIfAbsent(r.to, k -> new HashMap<>())
                                         .putIfAbsent(c.name(), r.from) == null;
                    if (added) {
                        matchedRelIdx.add(i);
                        break; // 이 관계선은 매칭 완료
                    }
                }
            }
        }

        // ── 2단계: 테이블별 잔여가 1:1 이면 유일 쌍 매핑
        Map<String, List<Integer>> remainingRelByTable = new HashMap<>();
        for (int i = 0; i < allRelations.size(); i++) {
            if (matchedRelIdx.contains(i)) continue;
            RawRelation r = allRelations.get(i);
            // 양쪽 테이블 모두 정본에 존재해야 함 (엣지 생성 로직과 일관성 유지)
            if (!canonicalCols.containsKey(r.from)) continue;
            if (!canonicalCols.containsKey(r.to)) continue;
            remainingRelByTable.computeIfAbsent(r.to, k -> new ArrayList<>()).add(i);
        }
        for (Map.Entry<String, List<Integer>> e : remainingRelByTable.entrySet()) {
            String tableName = e.getKey();
            List<Integer> remainingRels = e.getValue();
            if (remainingRels.size() != 1) continue; // 관계선이 1개가 아니면 모호

            Map<String, String> assigned = fkRef.getOrDefault(tableName, Map.of());
            List<ErdGraphDTO.Column> remainingFks = canonicalCols.get(tableName).stream()
                    .filter(c -> c.fk() && !assigned.containsKey(c.name()))
                    .collect(Collectors.toList());
            if (remainingFks.size() != 1) continue; // FK 컬럼이 1개가 아니면 모호

            RawRelation r = allRelations.get(remainingRels.get(0));
            fkRef.computeIfAbsent(tableName, k -> new HashMap<>())
                 .put(remainingFks.get(0).name(), r.from);
            log.debug("FK heuristic 2단계 매핑: {}.{} → {}",
                    tableName, remainingFks.get(0).name(), r.from);
        }

        // 최종 노드 리스트 생성 (fkRef 채워넣음)
        List<ErdGraphDTO.Node> nodes = new ArrayList<>();
        for (Map.Entry<String, List<ErdGraphDTO.Column>> e : canonicalCols.entrySet()) {
            String tableName = e.getKey();
            String domain = tableDomain.get(tableName);
            Map<String, String> refMap = fkRef.getOrDefault(tableName, Map.of());
            List<ErdGraphDTO.Column> finalCols = e.getValue().stream()
                    .map(c -> new ErdGraphDTO.Column(
                            c.name(), c.type(), c.pk(), c.fk(),
                            c.fk() ? refMap.get(c.name()) : null))
                    .collect(Collectors.toList());
            nodes.add(new ErdGraphDTO.Node(tableName, tableName, domain, finalCols));
        }

        return new ErdGraphDTO(nodes, edges);
    }

    private RawTable pickCanonical(List<RawTable> candidates) {
        // 컬럼 수 내림차순, 동률이면 도메인 우선순위 오름차순 (낮은 숫자 = 높은 우선순위)
        return candidates.stream()
                .min(Comparator
                        .comparingInt((RawTable t) -> -t.columns.size())
                        .thenComparingInt(t -> DOMAIN_PRIORITY.getOrDefault(t.domain, Integer.MAX_VALUE)))
                .orElseThrow();
    }

    private List<String> readFile(String domain) {
        String filename = "erd-" + domain + ".mmd";

        // 1) 파일시스템 (app.erd.mmd-dir 기준)
        FileSystemResource fs = new FileSystemResource(mmdDir + "/" + filename);
        if (fs.exists()) {
            return readLines(fs, "fs:" + mmdDir + "/" + filename);
        }

        // 2) classpath fallback
        ClassPathResource cp = new ClassPathResource("erd/" + filename);
        if (cp.exists()) {
            return readLines(cp, "classpath:erd/" + filename);
        }

        log.warn("ERD 파일 없음: {} (fs-base={}, classpath=erd/)", filename, mmdDir);
        return null;
    }

    private List<String> readLines(Resource res, String label) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> out = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) out.add(line);
            log.debug("ERD read OK: {} ({} lines)", label, out.size());
            return out;
        } catch (Exception e) {
            log.warn("ERD 파일 읽기 실패: {}", label, e);
            return null;
        }
    }

    private void parseFile(String domain, List<String> lines,
                           Map<String, List<RawTable>> byName,
                           List<RawRelation> allRelations) {
        RawTable current = null;
        for (String line : lines) {
            if (current == null) {
                Matcher mStart = BLOCK_START.matcher(line);
                if (mStart.matches()) {
                    String name = mStart.group(1);
                    if ("erDiagram".equals(name)) continue; // Mermaid 키워드 제외
                    current = new RawTable();
                    current.name = name;
                    current.domain = domain;
                    continue;
                }
                Matcher mRel = RELATION.matcher(line);
                if (mRel.matches()) {
                    RawRelation r = new RawRelation();
                    r.from = mRel.group(1);
                    r.cardinality = mRel.group(2);
                    r.to = mRel.group(3);
                    r.label = mRel.group(4);
                    allRelations.add(r);
                }
            } else {
                if (BLOCK_END.matcher(line).matches()) {
                    byName.computeIfAbsent(current.name, k -> new ArrayList<>()).add(current);
                    current = null;
                    continue;
                }
                Matcher mCol = COLUMN.matcher(line);
                if (mCol.matches()) {
                    String type = mCol.group(1).trim();
                    String colName = mCol.group(2);
                    String mod = mCol.group(4);
                    boolean pk = mod != null && mod.contains("PK");
                    boolean fk = mod != null && mod.contains("FK");
                    current.columns.add(new ErdGraphDTO.Column(colName, type, pk, fk, null));
                }
            }
        }
        if (current != null) {
            byName.computeIfAbsent(current.name, k -> new ArrayList<>()).add(current);
        }
    }
}
