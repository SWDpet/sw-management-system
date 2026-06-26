package com.swmanager.system.service;

import com.swmanager.system.domain.ContStatMst;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.repository.ContStatMstRepository;
import com.swmanager.system.repository.SwProjectRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SwService 단위 테스트 (beyond-A — coverage-sw-service).
 *
 * <p>기획서 docs/product-specs/coverage-sw-service.md / 개발계획 docs/exec-plans/coverage-sw-service.md
 * (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인). 실DB 없이 mock 으로 위임 + search 분기 +
 * buildSearchSpec Specification 람다(캡처 후 toPredicate 실행)를 커버. 기존 SwServiceNormalizeTest·
 * SwServiceSearchTest(DB-gated) 와 별개로 기본 CI 커버 보강. 프로덕션 코드 변경 0.
 */
@SuppressWarnings("unchecked")
class SwServiceTest {

    private SwService service;
    private SwProjectRepository swProjectRepository;
    private ContStatMstRepository contStatMstRepository;

    @BeforeEach
    void setUp() throws Exception {
        service = new SwService();
        swProjectRepository = mock(SwProjectRepository.class);
        contStatMstRepository = mock(ContStatMstRepository.class);
        inject("swProjectRepository", swProjectRepository);
        inject("contStatMstRepository", contStatMstRepository);
    }

    private void inject(String field, Object value) throws Exception {
        Field f = SwService.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(service, value);
    }

    private static Pageable page() { return PageRequest.of(0, 10); }

    // ───────────────────────── 위임 ─────────────────────────

    @Test
    void getList_delegates() { // T-1
        Page<SwProject> p = Page.empty();
        when(swProjectRepository.findAll(any(Pageable.class))).thenReturn(p);
        assertThat(service.getList(page())).isSameAs(p);
    }

    @Test
    void getProject_found_andNotFound() { // T-2
        SwProject sp = new SwProject();
        when(swProjectRepository.findById(1L)).thenReturn(Optional.of(sp));
        when(swProjectRepository.findById(9L)).thenReturn(Optional.empty());
        assertThat(service.getProject(1L)).isSameAs(sp);
        assertThat(service.getProject(9L)).isNull();
    }

    @Test
    void save_delegates() { // T-3
        SwProject sp = new SwProject();
        when(swProjectRepository.save(sp)).thenReturn(sp);
        assertThat(service.save(sp)).isSameAs(sp);
    }

    @Test
    void delete_delegates() { // T-4
        service.delete(5L);
        verify(swProjectRepository).deleteById(5L);
    }

    @Test
    void searchKeywordOverload_allNull_findAllPageable() { // T-5
        when(swProjectRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        service.search("   ", page()); // blank → normalize null → 전 파라미터 null
        verify(swProjectRepository).findAll(any(Pageable.class));
        verify(swProjectRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    // ───────────────────────── search 분기 ─────────────────────────

    @Test
    void search_allBlank_shortCircuitsFindAll() { // T-6
        when(swProjectRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        service.search(null, null, null, null, null, page());
        verify(swProjectRepository).findAll(any(Pageable.class));
        verify(swProjectRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void search_withFilter_usesSpecification() { // T-7
        when(swProjectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        service.search(null, null, "서울특별시", null, null, page());
        verify(swProjectRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ───────────────────────── buildSearchSpec 람다 ─────────────────────────

    /** search() 가 repository 에 넘긴 Specification 을 캡처. */
    private Specification<SwProject> captureSpec() {
        ArgumentCaptor<Specification<SwProject>> cap = ArgumentCaptor.forClass(Specification.class);
        verify(swProjectRepository).findAll(cap.capture(), any(Pageable.class));
        return cap.getValue();
    }

    /** Criteria mock 세트 — Root 는 필드별 distinct Path mock(개발계획), CB 는 Predicate/Expression mock 반환. */
    private static class Crit {
        final Root<SwProject> root = mock(Root.class);
        final CriteriaQuery<?> query = mock(CriteriaQuery.class);
        final CriteriaBuilder cb = mock(CriteriaBuilder.class);
        final Predicate pred = mock(Predicate.class);
        Crit() {
            Expression expr = mock(Expression.class);
            // 필드별 distinct Path — buildSearchSpec 가 참조하는 컬럼 전부(codex: deep stub 대신 명시)
            for (String f : List.of("year", "cityNm", "distNm", "sysNmEn", "sysNm",
                    "projNm", "client", "orgNm", "pmsCd", "stat")) {
                lenient().when(root.get(f)).thenReturn(mock(Path.class));
            }
            lenient().when(root.get("year").as(String.class)).thenReturn(mock(Expression.class)); // year LIKE 용
            lenient().when(cb.lower(any())).thenReturn(expr);
            lenient().when(cb.upper(any())).thenReturn(expr);
            lenient().when(cb.like(any(Expression.class), anyString())).thenReturn(pred);
            // cb.equal 은 (Expression,Object)/(Expression,Expression) 오버로드 — 컨트롤러는 (Expression,Object). null 값도 매칭(nullable)
            lenient().when(cb.equal(any(Expression.class), nullable(Object.class))).thenReturn(pred);
            lenient().when(cb.or(any(Predicate[].class))).thenReturn(pred);
            lenient().when(cb.and(any(Predicate[].class))).thenReturn(pred);
            lenient().when(cb.conjunction()).thenReturn(pred);
        }
        Predicate run(Specification<SwProject> spec) { return spec.toPredicate(root, query, cb); }
    }

    @Test
    void spec_filtersOnly_equalBranches() { // T-8 (kw=null, year/city/sysNmEn equal)
        when(swProjectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        service.search(null, "2026", "서울특별시", null, "upis", page());
        Crit c = new Crit();
        assertThat(c.run(captureSpec())).isNotNull();
        verify(c.cb, atLeastOnce()).equal(any(Expression.class), any(Object.class));   // year/city/sysNmEn equal
        verify(c.cb, atLeastOnce()).upper(any());           // sysNmEn 대문자(엄격 횟수 회피)
    }

    @Test
    void spec_kwNonNumeric_orLike() { // T-9 (kw 비숫자 → like OR, 숫자파싱 실패 무시)
        when(contStatMstRepository.findAll()).thenReturn(List.of());
        when(swProjectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        service.search("강진", null, null, null, null, page());
        Crit c = new Crit();
        assertThat(c.run(captureSpec())).isNotNull();
        verify(c.cb, atLeastOnce()).like(any(Expression.class), anyString()); // 다컬럼 LIKE
        verify(c.cb).or(any(Predicate[].class));
    }

    @Test
    void spec_kwNumeric_addsYearEqual() { // T-10 (kw 숫자 → year equal 추가 경로)
        when(contStatMstRepository.findAll()).thenReturn(List.of());
        when(swProjectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        service.search("2026", null, null, null, null, page());
        Crit c = new Crit();
        assertThat(c.run(captureSpec())).isNotNull();
        verify(c.cb, atLeastOnce()).equal(any(Expression.class), any(Object.class)); // year equal(숫자 파싱 성공 분기)
    }

    @Test
    void spec_statLabelMatch_addsCodeEqual() { // T-11 (stat 매칭 + nm==null 무시)
        ContStatMst matched = new ContStatMst(); matched.setNm("진행중"); matched.setCd("P");
        ContStatMst nullNm = new ContStatMst();   nullNm.setCd("X");   // nm==null → 무시
        when(contStatMstRepository.findAll()).thenReturn(List.of(matched, nullNm));
        when(swProjectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        service.search("진행", null, null, null, null, page());
        Crit c = new Crit();
        assertThat(c.run(captureSpec())).isNotNull(); // nm==null 통계에도 NPE 없이 통과
        verify(c.cb, atLeastOnce()).equal(any(Expression.class), eq("P")); // 매칭 코드 "P" stat equal 추가 검증
    }

    @Test
    void spec_statNoMatch_ok() { // T-12 (stat 매칭 없음)
        ContStatMst s = new ContStatMst(); s.setNm("완료"); s.setCd("C");
        when(contStatMstRepository.findAll()).thenReturn(List.of(s));
        when(swProjectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        service.search("없는키워드zzz", null, null, null, null, page());
        Crit c = new Crit();
        assertThat(c.run(captureSpec())).isNotNull();
        verify(c.cb, never()).equal(any(Expression.class), eq("C")); // 매칭 없음 → stat 코드 equal 미추가
    }

    @Test
    void spec_cityNull_districtIgnored() { // T-13 (city=null → distNm 경로 미생성)
        when(swProjectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        service.search(null, null, null, "양양군", "upis", page()); // city=null, district 지정
        Crit c = new Crit();
        c.run(captureSpec());
        verify(c.root, never()).get("distNm"); // distN=null 정규화(L61-62) → 경로 자체 미생성
    }
}
