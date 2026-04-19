package com.swmanager.system.service;

import com.swmanager.system.domain.ContStatMst;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.repository.ContStatMstRepository;
import com.swmanager.system.repository.SwProjectRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SW 프로젝트 서비스
 *
 * 감사 P2 4-2 (스프린트 2b, 2026-04-19) 로 미사용 public 메서드 11개 및
 * 미호출 private 헬퍼 1종을 제거. 유지 API: getList / search /
 * getProject / save / delete.
 */
@Slf4j
@Service
@Transactional
public class SwService {

    @Autowired private SwProjectRepository swProjectRepository;
    @Autowired private ContStatMstRepository contStatMstRepository;

    // ========== 목록 / 검색 ==========

    @Transactional(readOnly = true)
    public Page<SwProject> getList(Pageable pageable) {
        return swProjectRepository.findAll(pageable);
    }

    /**
     * 통합 검색 (신규 — 드롭다운 필터 4종 + 자유 키워드)
     *
     * FR-1: & AND 토큰 방식 제거 → kw 전체 문자열을 단일 키워드로 OR 매칭
     * FR-10: city 미선택 시 district 무시
     *
     * @param kw 자유 키워드 (사업명·발주처·PMS코드 등 OR LIKE)
     * @param year String 으로 수신 → parseYearSafe 로 Integer 변환 (NFR-7)
     * @param city 시도명
     * @param district 시군구명 (city 필수)
     * @param sysNmEn 시스템영문명 (대문자 정규화)
     */
    @Transactional(readOnly = true)
    public Page<SwProject> search(
            String kw, String year, String city, String district, String sysNmEn,
            Pageable pageable) {

        String kwN   = normalize(kw);
        Integer yearN = parseYearSafe(year);
        String cityN = normalize(city);
        String distN = (cityN == null) ? null : normalize(district);  // FR-10
        String sysN  = normalizeUpper(sysNmEn);

        if (kwN == null && yearN == null && cityN == null && distN == null && sysN == null) {
            return swProjectRepository.findAll(pageable);
        }

        log.debug("검색 - kw={} year={} city={} dist={} sys={}", kwN, yearN, cityN, distN, sysN);
        return swProjectRepository.findAll(
                buildSearchSpec(kwN, yearN, cityN, distN, sysN),
                pageable);
    }

    /**
     * 레거시 호환 — 단일 kw 호출처 지원 (FR-9 부분 호환)
     */
    @Transactional(readOnly = true)
    public Page<SwProject> search(String keyword, Pageable pageable) {
        return search(keyword, null, null, null, null, pageable);
    }

    /**
     * 필터 조합 Specification
     *  - year/city/district/sysNmEn: equal AND
     *  - kw: 다중 컬럼 OR LIKE (구 AND 토큰 로직 제거)
     */
    private Specification<SwProject> buildSearchSpec(
            String kw, Integer year, String city, String district, String sysNmEn) {

        List<ContStatMst> allStats = contStatMstRepository.findAll();

        return (root, query, cb) -> {
            List<Predicate> andList = new ArrayList<>();

            if (year != null)     andList.add(cb.equal(root.get("year"), year));
            if (city != null)     andList.add(cb.equal(root.get("cityNm"), city));
            if (district != null) andList.add(cb.equal(root.get("distNm"), district));
            if (sysNmEn != null)  andList.add(cb.equal(cb.upper(root.<String>get("sysNmEn")), sysNmEn));

            if (kw != null) {
                String like = "%" + kw.toLowerCase() + "%";
                List<Predicate> orList = new ArrayList<>();

                // year LIKE (문자열 포함 검색용)
                orList.add(cb.like(root.get("year").as(String.class), "%" + kw + "%"));
                try {
                    int yr = Integer.parseInt(kw.trim());
                    orList.add(cb.equal(root.get("year"), yr));
                } catch (NumberFormatException ignored) {}

                for (String field : List.of(
                        "sysNm", "sysNmEn", "projNm", "client",
                        "cityNm", "distNm", "orgNm", "pmsCd")) {
                    orList.add(cb.like(cb.lower(root.<String>get(field)), like));
                }

                orList.add(cb.like(cb.lower(root.<String>get("stat")), like));
                List<String> matchedCodes = allStats.stream()
                        .filter(s -> s.getNm() != null && s.getNm().toLowerCase().contains(kw.toLowerCase()))
                        .map(ContStatMst::getCd)
                        .collect(Collectors.toList());
                for (String code : matchedCodes) {
                    orList.add(cb.equal(root.get("stat"), code));
                }

                andList.add(cb.or(orList.toArray(new Predicate[0])));
            }

            return andList.isEmpty()
                    ? cb.conjunction()
                    : cb.and(andList.toArray(new Predicate[0]));
        };
    }

    // ========== 파라미터 정규화 유틸 (NFR-5/NFR-7) ==========

    /** NFR: 길이 100자 이하로 잘라냄 (kw 등 텍스트용) */
    static final int KW_MAX_LEN = 100;  // package-private for tests

    // package-private 으로 노출 (단위 테스트에서 직접 검증)
    static String normalize(String v) {
        if (v == null) return null;
        String t = v.trim();
        if (t.isEmpty()) return null;
        return t.length() > KW_MAX_LEN ? t.substring(0, KW_MAX_LEN) : t;
    }

    static Integer parseYearSafe(String v) {
        String n = normalize(v);
        if (n == null) return null;
        try {
            int y = Integer.parseInt(n);
            return (y >= 2000 && y <= 2099) ? y : null;
        } catch (NumberFormatException e) {
            return null;  // NFR-7: 무효 입력 무시
        }
    }

    static String normalizeUpper(String v) {
        String n = normalize(v);
        return n == null ? null : n.toUpperCase();
    }

    // ========== 단건 / 저장 / 삭제 ==========

    @Transactional(readOnly = true)
    public SwProject getProject(Long id) {
        return swProjectRepository.findById(id).orElse(null);
    }

    public SwProject save(SwProject project) {
        log.info("프로젝트 저장 - 사업명: {}", project.getProjNm());
        return swProjectRepository.save(project);
    }

    public void delete(Long id) {
        log.warn("프로젝트 삭제 - ID: {}", id);
        swProjectRepository.deleteById(id);
    }
}
