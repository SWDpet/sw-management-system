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
import java.util.Arrays;
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
     * 통합 검색
     *  - '&' 포함 → 각 토큰 AND 조건 (각 토큰은 모든 컬럼 OR)
     *  - '&' 미포함 → 기존 단일 키워드 LIKE
     */
    @Transactional(readOnly = true)
    public Page<SwProject> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return swProjectRepository.findAll(pageable);
        }

        // 항상 Specification 방식 사용 (계약상태명 검색 지원)
        List<String> tokens = Arrays.stream(keyword.split("&"))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
        log.debug("키워드 검색 - 토큰: {}", tokens);
        return swProjectRepository.findAll(buildAndSpec(tokens), pageable);
    }

    /**
     * 각 토큰 → (모든 컬럼 OR) 을 AND 로 묶은 Specification
     *
     * 검색 대상 컬럼:
     *   year, sysNm, sysNmEn, projNm, client, cityNm, distNm, orgNm, pmsCd, stat
     */
    private Specification<SwProject> buildAndSpec(List<String> tokens) {
        // 미리 상태코드 매핑 로드
        List<ContStatMst> allStats = contStatMstRepository.findAll();

        return (root, query, cb) -> {
            List<Predicate> andList = new ArrayList<>();

            for (String token : tokens) {
                String like = "%" + token.toLowerCase() + "%";
                List<Predicate> orList = new ArrayList<>();

                // year: Integer → String 변환 후 LIKE
                orList.add(cb.like(root.get("year").as(String.class), "%" + token + "%"));

                // 정확한 연도 숫자면 equal 도 추가
                try {
                    int yr = Integer.parseInt(token.trim());
                    orList.add(cb.equal(root.get("year"), yr));
                } catch (NumberFormatException ignored) {}

                // 문자열 컬럼들
                for (String field : List.of(
                        "sysNm", "sysNmEn", "projNm", "client",
                        "cityNm", "distNm", "orgNm", "pmsCd")) {
                    orList.add(cb.like(cb.lower(root.<String>get(field)), like));
                }

                // 계약상태: stat 코드 직접 매칭 + 상태명으로 코드 변환 매칭
                orList.add(cb.like(cb.lower(root.<String>get("stat")), like));
                List<String> matchedCodes = allStats.stream()
                        .filter(s -> s.getNm() != null && s.getNm().toLowerCase().contains(token.toLowerCase()))
                        .map(ContStatMst::getCd)
                        .collect(Collectors.toList());
                for (String code : matchedCodes) {
                    orList.add(cb.equal(root.get("stat"), code));
                }

                andList.add(cb.or(orList.toArray(new Predicate[0])));
            }

            return cb.and(andList.toArray(new Predicate[0]));
        };
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
