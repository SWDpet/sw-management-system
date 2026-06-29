package com.swmanager.system.util;

import com.swmanager.system.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** AuthSummary.summarize 단위테스트 (커버리지 상향 beyond-A, 순수). */
class AuthSummaryTest {

    private final AuthSummary authSummary = new AuthSummary();

    @Test
    void summarize_null_returnsEmpty() {
        AuthSummary.Result r = authSummary.summarize(null, 4);
        assertThat(r.getList()).isEmpty();
        assertThat(r.getMoreCount()).isZero();
    }

    @Test
    void summarize_editView_buildBadges_skipNoneAndUnknown() {
        User u = new User();
        u.setAuthDashboard("EDIT");   // 대시E (edit)
        u.setAuthProject("VIEW");     // 사업V (view)
        u.setAuthPerson("NONE");      // skip
        u.setAuthInfra("FOO");        // unknown → skip
        // 나머지 6권한 null → skip

        AuthSummary.Result r = authSummary.summarize(u, 4);

        assertThat(r.getList()).hasSize(2);
        assertThat(r.getList().get(0).getLabel()).isEqualTo("대시E");
        assertThat(r.getList().get(0).getCssClass()).isEqualTo("edit");
        assertThat(r.getList().get(1).getLabel()).isEqualTo("사업V");
        assertThat(r.getList().get(1).getCssClass()).isEqualTo("view");
        assertThat(r.getMoreCount()).isZero();
    }

    private User fiveEditAuths() {
        User u = new User();
        u.setAuthDashboard("EDIT");
        u.setAuthProject("EDIT");
        u.setAuthPerson("EDIT");
        u.setAuthInfra("EDIT");
        u.setAuthLicense("EDIT");
        return u;   // 5개 EDIT
    }

    @Test
    void summarize_maxCap_setsMoreCount() {
        AuthSummary.Result r = authSummary.summarize(fiveEditAuths(), 2);
        assertThat(r.getList()).hasSize(2);
        assertThat(r.getList().get(0).getLabel()).isEqualTo("대시E");   // FIELDS 순서 앞 2개
        assertThat(r.getList().get(1).getLabel()).isEqualTo("사업E");
        assertThat(r.getMoreCount()).isEqualTo(3);                      // 5 - 2
    }

    @Test
    void summarize_maxZero_returnsAll() {
        AuthSummary.Result r = authSummary.summarize(fiveEditAuths(), 0);
        assertThat(r.getList()).hasSize(5);
        assertThat(r.getMoreCount()).isZero();
    }

    /** 11권한(authLsa 포함) 전부 EDIT/VIEW 세팅한 User — readAuth 의 필드별 매핑 박제용. */
    private User allElevenAuths() {
        User u = new User();
        u.setAuthDashboard("EDIT");   u.setAuthProject("VIEW");
        u.setAuthPerson("EDIT");      u.setAuthInfra("VIEW");
        u.setAuthLicense("EDIT");     u.setAuthLsa("VIEW");
        u.setAuthQuotation("EDIT");   u.setAuthWorkPlan("VIEW");
        u.setAuthDocument("EDIT");    u.setAuthContract("VIEW");
        u.setAuthPerformance("EDIT");
        return u;   // 11개 — edit/view 교차(시작 edit)
    }

    /**
     * 11개 권한 전부 badge 생성 — readAuth 의 필드별 return-empty mutation + 값/라벨/순서 매핑을 박제.
     * (authLsa 는 authLicense 다음 6번째 — LSA 라벨. EDIT/VIEW 교차+containsExactly 로 어느 필드 반환을
     *  ""로 바꿔도 badge 누락→탐지.)
     */
    @Test
    void summarize_allElevenFields_eachBadgeMappedInOrder() {
        AuthSummary.Result r = authSummary.summarize(allElevenAuths(), 11);

        assertThat(r.getList())
                .extracting(AuthSummary.Badge::getLabel)
                .containsExactly("대시E", "사업V", "담당E", "서버V", "라이E", "LSAV",
                                 "견적E", "업무V", "문서E", "계약V", "성과E");
        assertThat(r.getList())
                .extracting(AuthSummary.Badge::getCssClass)
                .containsExactly("edit", "view", "edit", "view", "edit", "view",
                                 "edit", "view", "edit", "view", "edit");
        assertThat(r.getMoreCount()).isZero();
    }

    /** trim: 11권한 중 max=10 → 10 표시 + moreCount==1 (codex 권고: 경계 박제). */
    @Test
    void summarize_elevenFields_maxTen_trimsToTenWithMoreCountOne() {
        AuthSummary.Result r = authSummary.summarize(allElevenAuths(), 10);

        assertThat(r.getList()).hasSize(10);
        assertThat(r.getList().get(0).getLabel()).isEqualTo("대시E");
        assertThat(r.getList().get(9).getLabel()).isEqualTo("계약V");  // 11번째(성과E) 잘림
        assertThat(r.getMoreCount()).isEqualTo(1);                     // 11 - 10
    }
}
