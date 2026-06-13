package com.swmanager.system.service.ops;

import java.util.List;
import java.util.Map;

/**
 * 장애/지원 KB 추천 매처 — ops-fault-support M3 (NFR-6).
 * 1차 구현 = RuleKbMatcher(규칙·검색). 후속 = OllamaEmbeddingMatcher(RAG) 교체.
 */
public interface KbMatcher {

    /**
     * @param gubun   장애 / 지원 (null = 전체)
     * @param sysType ops-doc sys_type (null/공백 = 미필터). 매칭 0건이면 필터 해제 폴백.
     * @param query   증상/키워드 입력
     * @param topN    상위 N
     */
    List<Map<String, Object>> recommend(String gubun, String sysType, String query, int topN);
}
