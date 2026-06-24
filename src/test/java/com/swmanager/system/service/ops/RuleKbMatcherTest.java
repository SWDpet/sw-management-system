package com.swmanager.system.service.ops;

import com.swmanager.system.domain.ops.OpsKb;
import com.swmanager.system.repository.ops.OpsKbRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RuleKbMatcher.recommend 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 *
 * <p>gubun 유무에 따른 리포지토리 분기, sysType 엄격 필터, 토큰 매칭 우선·인기순 폴백,
 * 점수 산식(ts + log1p(caseCount)·0.5)·1자리 반올림, topN 컷, action→causeDesc 폴백,
 * 출력 맵 필드 매핑을 검증한다.</p>
 */
class RuleKbMatcherTest {

    private final OpsKbRepository repo = mock(OpsKbRepository.class);
    private final RuleKbMatcher matcher = new RuleKbMatcher(repo);

    /** 최소 필드 OpsKb 빌더. */
    private OpsKb kb(Long id, String sysType, String symptom, String keywords, Integer caseCount) {
        OpsKb k = new OpsKb();
        k.setKbId(id);
        k.setKbCode("KB-" + id);
        k.setSysType(sysType);
        k.setSymptom(symptom);
        k.setKeywords(keywords);
        k.setCaseCount(caseCount);
        return k;
    }

    // ── 리포지토리 분기 (gubun) ─────────────────────────────────────

    @Test
    void recommend_withGubun_usesGubunStatusQuery() {
        when(repo.findByGubunAndStatus("장애", "ACTIVE"))
                .thenReturn(List.of(kb(1L, "UPIS", "CPU high", "cpu", 0)));

        matcher.recommend("장애", null, "cpu", 5);

        verify(repo).findByGubunAndStatus("장애", "ACTIVE");
        verify(repo, never()).findByStatusOrderByCaseCountDesc(anyString());
    }

    @Test
    void recommend_blankGubun_usesPopularityQuery() {
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE"))
                .thenReturn(List.of(kb(1L, "UPIS", "CPU high", "cpu", 3)));

        matcher.recommend("  ", null, "cpu", 5);   // blank → 인기순 전체 조회

        verify(repo).findByStatusOrderByCaseCountDesc("ACTIVE");
        verify(repo, never()).findByGubunAndStatus(anyString(), anyString());
    }

    // ── sysType 엄격 필터 ───────────────────────────────────────────

    @Test
    void recommend_sysTypeFilter_keepsOnlyMatchingCaseInsensitive() {
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE")).thenReturn(List.of(
                kb(1L, "UPIS", "CPU high", "cpu", 0),
                kb(2L, "KRAS", "CPU high", "cpu", 0)));

        List<Map<String, Object>> out = matcher.recommend(null, "upis", "cpu", 5);  // 소문자 → UPIS 매칭

        assertThat(out).hasSize(1);
        assertThat(out.get(0).get("kb_id")).isEqualTo(1L);   // KRAS 제외
    }

    // ── 토큰 매칭 우선 + 점수 산식 ──────────────────────────────────

    @Test
    void recommend_tokenHitsPrioritized_overPopularNonHits() {
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE")).thenReturn(List.of(
                kb(1L, "UPIS", "메모리 사용률 높음", "메모리", 0),     // 토큰 매칭 ts>0
                kb(2L, "UPIS", "디스크 가득", "디스크", 1000)));        // 인기 높지만 토큰 미매칭

        List<Map<String, Object>> out = matcher.recommend(null, null, "메모리 부족", 5);

        // 토큰 매칭(ts>0)이 있으면 인기 높은 비매칭(kb2)은 제외
        assertThat(out).hasSize(1);
        assertThat(out.get(0).get("kb_id")).isEqualTo(1L);
    }

    @Test
    void recommend_scoreIsTokenCountPlusCaseWeight_roundedToOneDecimal() {
        // query "cpu high" → tokens [cpu, high] 둘 다 hay 포함 → ts=2, caseCount=0 → total=2.0
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE")).thenReturn(List.of(
                kb(1L, "UPIS", "cpu high usage", "cpu", 0)));

        List<Map<String, Object>> out = matcher.recommend(null, null, "cpu high", 5);

        assertThat(out).hasSize(1);
        assertThat((Double) out.get(0).get("score")).isEqualTo(2.0);
    }

    @Test
    void recommend_caseCountAddsLogWeightToScore() {
        // ts=1(cpu) + log1p(15)(=2.7726)*0.5(=1.3863) = 2.3863 → round(23.863)/10 = 2.4
        // (ts 단독이면 1.0 → 2.4 의 +1.4 는 log 가중 기여를 뚜렷한 버킷[2.35,2.45)로 고정)
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE")).thenReturn(List.of(
                kb(1L, "UPIS", "cpu spike", "cpu", 15)));

        List<Map<String, Object>> out = matcher.recommend(null, null, "cpu", 5);

        assertThat((Double) out.get(0).get("score")).isCloseTo(2.4, within(1e-9));
        assertThat(out.get(0).get("case_count")).isEqualTo(15);
    }

    // ── 토큰 미매칭 폴백 (인기순) + topN ────────────────────────────

    @Test
    void recommend_noTokenHits_fallsBackToPopularityOrder() {
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE")).thenReturn(List.of(
                kb(1L, "UPIS", "디스크", "disk", 1),
                kb(2L, "UPIS", "메모리", "mem", 100)));      // 사례수 큼 → 폴백 시 우선

        List<Map<String, Object>> out = matcher.recommend(null, null, "zzz없는토큰", 5);

        assertThat(out).hasSize(2);
        assertThat(out.get(0).get("kb_id")).isEqualTo(2L);   // caseCount 100 먼저
        assertThat(out.get(1).get("kb_id")).isEqualTo(1L);
    }

    @Test
    void recommend_respectsTopN() {
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE")).thenReturn(List.of(
                kb(1L, "UPIS", "메모리 a", "메모리", 5),
                kb(2L, "UPIS", "메모리 b", "메모리", 3),
                kb(3L, "UPIS", "메모리 c", "메모리", 1)));

        List<Map<String, Object>> out = matcher.recommend(null, null, "메모리", 2);

        assertThat(out).hasSize(2);   // topN=2 컷
    }

    // ── action → causeDesc 폴백 + 필드 매핑 ─────────────────────────

    @Test
    void recommend_blankAction_fallsBackToCauseDesc() {
        OpsKb k = kb(1L, "UPIS", "메모리", "메모리", 0);
        k.setAction("   ");            // blank → causeDesc 폴백
        k.setCauseDesc("스왑 증설 및 누수 점검");
        k.setSummary("요약");
        k.setCause("메모리 누수");
        k.setPrevention("모니터링");
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE")).thenReturn(List.of(k));

        Map<String, Object> m = matcher.recommend(null, null, "메모리", 5).get(0);

        assertThat(m.get("action")).isEqualTo("스왑 증설 및 누수 점검");   // causeDesc 폴백
        assertThat(m.get("kb_code")).isEqualTo("KB-1");
        assertThat(m.get("symptom")).isEqualTo("메모리");
        assertThat(m.get("cause")).isEqualTo("메모리 누수");
        assertThat(m.get("summary")).isEqualTo("요약");
        assertThat(m.get("prevention")).isEqualTo("모니터링");
    }

    @Test
    void recommend_presentAction_usedDirectly() {
        OpsKb k = kb(1L, "UPIS", "메모리", "메모리", 0);
        k.setAction("재기동");
        k.setCauseDesc("무시되어야 함");
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE")).thenReturn(List.of(k));

        Map<String, Object> m = matcher.recommend(null, null, "메모리", 5).get(0);

        assertThat(m.get("action")).isEqualTo("재기동");   // action 있으면 그대로
    }

    @Test
    void recommend_emptyPool_returnsEmpty() {
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE")).thenReturn(List.of());
        assertThat(matcher.recommend(null, null, "cpu", 5)).isEmpty();
    }

    @Test
    void recommend_shortTokensIgnored() {
        // "a" (len 1) 는 토큰에서 제외 → 매칭 토큰 0 → 인기순 폴백
        when(repo.findByStatusOrderByCaseCountDesc("ACTIVE")).thenReturn(List.of(
                kb(1L, "UPIS", "a b c", "x", 0)));

        List<Map<String, Object>> out = matcher.recommend(null, null, "a", 5);

        assertThat(out).hasSize(1);                       // 폴백으로 1건
        assertThat((Double) out.get(0).get("score")).isEqualTo(0.0);   // ts=0, caseCount=0
    }
}
