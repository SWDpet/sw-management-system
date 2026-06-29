package com.swmanager.system.lsa.service;

import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.exception.ResourceNotFoundException;
import com.swmanager.system.lsa.domain.Lsa;
import com.swmanager.system.lsa.dto.LsaDTO;
import com.swmanager.system.lsa.dto.LsaForm;
import com.swmanager.system.lsa.dto.PersonRow;
import com.swmanager.system.lsa.repository.LsaRepository;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * LSA 발급 대장 서비스. P2 목록·검색 / P3 작성(ps_info 대조·동시저장) + 발급자.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LsaService {

    private final LsaRepository lsaRepository;
    private final PersonInfoRepository personInfoRepository;
    private final SigunguCodeRepository sigunguCodeRepository;

    /** 시도 목록(드롭다운). */
    public List<String> sidoList() {
        return sigunguCodeRepository.findDistinctSidoNm();
    }

    /** 시군구 목록(시도 캐스케이드) — sggNm 만. */
    public List<String> districtList(String sido) {
        if (blankToNull(sido) == null) return List.of();
        return sigunguCodeRepository.findBySidoNmOrderBySggNm(sido.trim()).stream()
                .map(com.swmanager.system.domain.SigunguCode::getSggNm)
                .filter(java.util.Objects::nonNull).distinct().toList();
    }

    /** 키워드 검색 목록 (빈/공백 키워드 → 전체). */
    public List<LsaDTO> list(String keyword) {
        String kw = blankToNull(keyword);
        return lsaRepository.search(kw).stream().map(LsaDTO::fromEntity).toList();
    }

    /** prefill 후보 (지자체+부서·팀 매칭 담당자 — userNm/tel/email 만). */
    public List<PersonRow> findPersonCandidates(String city, String dist, String dept, String team) {
        if (blankToNull(city) == null || blankToNull(dist) == null) return List.of();
        return personInfoRepository
                .findCandidates(city.trim(), dist.trim(), blankToNull(dept), blankToNull(team))
                .stream().map(PersonRow::fromEntity).toList();
    }

    /**
     * LSA 발급 저장 (쓰기 트랜잭션) — ps_info 대조 후 동시저장.
     * 새 담당자(정규화 userNm+tel+email 불일치)면 ps_info 새 행 INSERT. tel·email 둘 다 빈값이면 항상 INSERT(중복판정 금지).
     * @param issuer  발급자(컨트롤러가 비관리자=로그인 실명 강제, 관리자=폼값 검증 후 전달)
     * @param createdBy 작성자 userid
     */
    /** 상세/수정 prefill 공용 — 없으면 NotFound. */
    public LsaDTO getById(Long id) {
        return lsaRepository.findById(id).map(LsaDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("LSA", id));
    }

    @Transactional
    public Long create(LsaForm form, String issuer, String createdBy) {
        validateRequired(form);
        Lsa lsa = new Lsa();
        applyForm(lsa, form);
        lsa.setIssuer(issuer);
        lsa.setPsInfoId(upsertPersonInfo(form));
        lsa.setCreatedBy(createdBy);
        return lsaRepository.save(lsa).getId();
    }

    /**
     * 수정 — 기존 행 로드(NotFound) + 필드 갱신 + ps_info 재대조 + updatedBy/At.
     * @param issuerOverride 관리자가 변경한 발급자(null=기존 발급자 보존 — 편집자 이름으로 덮어쓰지 않음).
     */
    @Transactional
    public Long update(Long id, LsaForm form, String issuerOverride, String updatedBy) {
        validateRequired(form);
        Lsa lsa = lsaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("LSA", id));
        applyForm(lsa, form);
        if (issuerOverride != null && !issuerOverride.isBlank()) lsa.setIssuer(issuerOverride.trim());
        // else: 기존 발급자(lsa.issuer) 보존
        lsa.setPsInfoId(upsertPersonInfo(form));   // 담당자 변경 시 재대조(기존 person 보존)
        lsa.setUpdatedBy(updatedBy);
        lsa.setUpdatedAt(java.time.LocalDateTime.now());
        return lsaRepository.save(lsa).getId();
    }

    /** 삭제 — lsa_license 행만(ps_info 보존). 없으면 NotFound. */
    @Transactional
    public void delete(Long id) {
        Lsa lsa = lsaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("LSA", id));
        lsaRepository.delete(lsa);
    }

    private void validateRequired(LsaForm form) {
        // 서버측 필수값 검증(클라이언트 required 우회 방어) — 지자체·담당자·버전
        if (blankToNull(form.getCityNm()) == null || blankToNull(form.getDistNm()) == null
                || blankToNull(form.getUserNm()) == null || blankToNull(form.getVersion()) == null) {
            throw new IllegalArgumentException("필수 항목 누락: 시도·시군구·담당자 이름·버전은 필수입니다.");
        }
    }

    /** 폼 → Lsa 필드 매핑(create/update 공용, issuer/createdBy/updatedBy 제외). */
    private void applyForm(Lsa lsa, LsaForm form) {
        lsa.setCityNm(blankToNull(form.getCityNm()));
        lsa.setDistNm(blankToNull(form.getDistNm()));
        lsa.setDeptNm(blankToNull(form.getDeptNm()));
        lsa.setTeamNm(blankToNull(form.getTeamNm()));
        lsa.setUserNm(blankToNull(form.getUserNm()));
        lsa.setTel(blankToNull(form.getTel()));
        lsa.setEmail(blankToNull(form.getEmail()));
        lsa.setVersion(blankToNull(form.getVersion()));
    }

    /** ps_info 대조 → 기존 담당자 id 재사용 또는 새 행 INSERT. */
    private Long upsertPersonInfo(LsaForm form) {
        String city = blankToNull(form.getCityNm());
        String dist = blankToNull(form.getDistNm());
        String dept = blankToNull(form.getDeptNm());
        String team = blankToNull(form.getTeamNm());
        String userNm = blankToNull(form.getUserNm());
        String tel = blankToNull(form.getTel());
        String email = blankToNull(form.getEmail());

        // tel·email 둘 다 빈값 → 동명이인 오매칭 방지: 무조건 새 행 INSERT
        boolean canMatch = (tel != null || email != null);
        if (canMatch && city != null && dist != null) {
            for (PersonInfo p : personInfoRepository.findCandidates(city, dist, dept, team)) {
                if (!normName(p.getUserNm()).equals(normName(userNm))) continue;
                boolean telMatch = tel != null && normTel(p.getTel()).equals(normTel(tel));
                boolean emailMatch = email != null && normEmail(p.getEmail()).equals(normEmail(email));
                if (telMatch || emailMatch) return p.getId();   // 기존 담당자 재사용
            }
        }
        // 새 담당자 INSERT
        PersonInfo np = new PersonInfo();
        np.setCityNm(city); np.setDistNm(dist); np.setDeptNm(dept); np.setTeamNm(team);
        np.setUserNm(userNm); np.setTel(tel); np.setEmail(email);
        return personInfoRepository.save(np).getId();
    }

    // ── 정규화 헬퍼 ──
    private static String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private static String normName(String s) { return s == null ? "" : s.trim(); }
    private static String normTel(String s) { return s == null ? "" : s.replaceAll("[\\s-]", ""); }
    private static String normEmail(String s) { return s == null ? "" : s.trim().toLowerCase(); }
}
