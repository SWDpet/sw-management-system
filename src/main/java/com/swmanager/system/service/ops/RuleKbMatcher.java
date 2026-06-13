package com.swmanager.system.service.ops;

import com.swmanager.system.domain.ops.OpsKb;
import com.swmanager.system.repository.ops.OpsKbRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 규칙·검색 기반 KB 추천 (M3 1차) — 토큰 매칭 + 사례수 가중.
 * sysType 필터 후 0건이면 필터 해제 폴백. 토큰 매칭 0건이면 인기(사례수)순.
 * pg_trgm 인덱스 유무와 무관하게 인메모리 LIKE 로 동작(코퍼스 ~126행).
 */
@Service
@RequiredArgsConstructor
public class RuleKbMatcher implements KbMatcher {

    private final OpsKbRepository opsKbRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> recommend(String gubun, String sysType, String query, int topN) {
        // [ops-kb-workbench] ACTIVE 만 추천(소프트삭제·미검증 제외). 시드는 status='ACTIVE' 라 ops-doc 추천 불변.
        List<OpsKb> candidates = (gubun != null && !gubun.isBlank())
                ? opsKbRepository.findByGubunAndStatus(gubun, "ACTIVE")
                : opsKbRepository.findByStatusOrderByCaseCountDesc("ACTIVE");

        // sysType 필터 (있으면 엄격 적용 — 선택 시스템의 KB 만. 없으면 미적용)
        List<OpsKb> pool = candidates;
        if (sysType != null && !sysType.isBlank()) {
            pool = new ArrayList<>();
            for (OpsKb k : candidates) if (sysType.equalsIgnoreCase(k.getSysType())) pool.add(k);
        }

        List<String> tokens = tokenize(query);

        List<double[]> idxScore = new ArrayList<>();  // [poolIndex, tokenScore, total]
        for (int i = 0; i < pool.size(); i++) {
            OpsKb k = pool.get(i);
            String hay = (nz(k.getSymptom()) + " " + nz(k.getKeywords()) + " " + nz(k.getCause())
                    + " " + nz(k.getSymptomDesc())).toLowerCase();
            double ts = 0;
            for (String t : tokens) if (hay.contains(t)) ts += 1;
            int cc = k.getCaseCount() == null ? 0 : k.getCaseCount();
            double total = ts + Math.log1p(cc) * 0.5;
            idxScore.add(new double[]{i, ts, total});
        }

        // 토큰 매칭(ts>0) 우선, 없으면 전체(인기순)
        List<double[]> hits = new ArrayList<>();
        for (double[] r : idxScore) if (r[1] > 0) hits.add(r);
        List<double[]> base = hits.isEmpty() ? idxScore : hits;
        base.sort((a, b) -> Double.compare(b[2], a[2]));

        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < base.size() && i < topN; i++) {
            OpsKb k = pool.get((int) base.get(i)[0]);
            String action = (k.getAction() != null && !k.getAction().isBlank())
                    ? k.getAction() : nz(k.getCauseDesc());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("kb_id", k.getKbId());
            m.put("kb_code", k.getKbCode());
            m.put("symptom", k.getSymptom());
            m.put("cause", k.getCause());
            m.put("summary", k.getSummary());
            m.put("action", action);
            m.put("prevention", k.getPrevention());
            m.put("case_count", k.getCaseCount());
            m.put("score", Math.round(base.get(i)[2] * 10) / 10.0);
            out.add(m);
        }
        return out;
    }

    private List<String> tokenize(String q) {
        List<String> t = new ArrayList<>();
        if (q == null) return t;
        for (String p : q.toLowerCase().split("[\\s,/·\\-()\\[\\]]+")) {
            if (p.length() >= 2) t.add(p);
        }
        return t;
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
