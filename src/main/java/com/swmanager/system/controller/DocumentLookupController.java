package com.swmanager.system.controller;

import com.swmanager.system.domain.workplan.ProcessMaster;
import com.swmanager.system.domain.workplan.ServicePurpose;
import com.swmanager.system.dto.workplan.InfraFindResult;
import com.swmanager.system.dto.workplan.InfraNotFound;
import com.swmanager.system.dto.workplan.ProcessMasterRow;
import com.swmanager.system.dto.workplan.ProjectFilterRow;
import com.swmanager.system.dto.workplan.RegionSigunguRow;
import com.swmanager.system.dto.workplan.ServicePurposeRow;
import com.swmanager.system.dto.workplan.SystemOptionRow;
import com.swmanager.system.dto.workplan.UserInfoRow;
import com.swmanager.system.dto.workplan.UserInfoSecureRow;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.ProcessMasterRepository;
import com.swmanager.system.repository.workplan.ServicePurposeRepository;
import com.swmanager.system.security.DocumentAccessSupport;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 문서작성 폼의 지역·시스템·사업 cascade 조회 API — DocumentController 에서 분리 (S4 giant-class-split §6-5).
 *
 * 클래스 레벨 @RequestMapping("/document") 을 DocumentController 와 동일하게 유지하여 URL 100% 보존.
 * 순수 read(repository 직접 조회), 권한 가드·공유 헬퍼 없음.
 */
@Controller
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentLookupController {

    private final SwProjectRepository swProjectRepository;
    private final SigunguCodeRepository sigunguCodeRepository;
    private final SysMstRepository sysMstRepository;
    private final InfraRepository infraRepository;
    private final ProcessMasterRepository processMasterRepository;
    private final ServicePurposeRepository servicePurposeRepository;
    // [S4 Phase 5] 사용자/사업 정보 조회 편입 — 신규 final 필드는 기존 6개 뒤(생성자 인자 순서 유지).
    private final UserRepository userRepository;
    private final PersonInfoRepository personInfoRepository;
    private final DocumentAccessSupport access;

    // === 사업 검색 3단계 필터 API ===

    /** 1단계: 연도 목록 */
    @ResponseBody
    @GetMapping("/api/project-years")
    public ResponseEntity<List<Integer>> getProjectYears() {
        return ResponseEntity.ok(swProjectRepository.findDistinctYears());
    }

    /** 2단계: 연도 선택 → 시도(cityNm) 목록 */
    @ResponseBody
    @GetMapping("/api/project-cities")
    public ResponseEntity<List<String>> getProjectCities(@RequestParam Integer year) {
        return ResponseEntity.ok(swProjectRepository.findDistinctCityNmByYear(year));
    }

    /** 2-1단계: 연도+시도 선택 → 시군구(distNm) 목록 */
    @ResponseBody
    @GetMapping("/api/project-districts")
    public ResponseEntity<List<String>> getProjectDistricts(
            @RequestParam Integer year, @RequestParam String cityNm) {
        return ResponseEntity.ok(swProjectRepository.findDistinctDistNmByYearAndCityNm(year, cityNm));
    }

    /** 3단계: 연도+지자체 → 시스템영문명 목록 */
    @ResponseBody
    @GetMapping("/api/project-systems")
    public ResponseEntity<List<String>> getProjectSystems(
            @RequestParam Integer year, @RequestParam String cityNm, @RequestParam String distNm) {
        return ResponseEntity.ok(swProjectRepository.findDistinctSysNmEnByYearAndCity(year, cityNm, distNm));
    }

    // ========== 스프린트 5 v2 (2026-04-19): 4개 문서용 지역+시스템 드롭다운 ==========
    // 4개 문서(장애/업무지원/설치/패치) 는 사업·인프라와 독립된 성과·히스토리 관리용.
    // 시도·시군구는 sigungu_code 마스터, 시스템은 sys_mst 마스터 기반.

    /** 시도 목록 (sigungu_code distinct) */
    @ResponseBody
    @GetMapping("/api/region-sidos")
    public ResponseEntity<List<String>> getRegionSidos() {
        return ResponseEntity.ok(sigunguCodeRepository.findDistinctSidoNm());
    }

    /** 시도 → 시군구 목록 (행정구역코드 + 시군구명) */
    @ResponseBody
    @GetMapping("/api/region-sigungus")
    public ResponseEntity<List<RegionSigunguRow>> getRegionSigungus(@RequestParam String sidoNm) {
        List<RegionSigunguRow> result = sigunguCodeRepository.findBySidoNmOrderBySggNm(sidoNm)
                .stream().map(RegionSigunguRow::from).toList();
        return ResponseEntity.ok(result);
    }

    /** 전체 시스템 목록 (sys_mst) */
    @ResponseBody
    @GetMapping("/api/systems-all")
    public ResponseEntity<List<SystemOptionRow>> getSystemsAll() {
        List<SystemOptionRow> result = sysMstRepository.findAll()
                .stream().map(SystemOptionRow::from).toList();
        return ResponseEntity.ok(result);
    }

    // ========== 스프린트 5 v1 (2026-04-19, 사용자 피드백 v2 로 사용 중단):
    // tb_infra_master 기반 3단 API. 현재는 사용처 없음. 추후 정리 대상.
    // 유지 이유: commence/inspect 의 점검내역서 infra 조회 경로(getInfraServers)가 있어
    // 단순 삭제 보류. ==========

    @ResponseBody
    @GetMapping("/api/infra-cities")
    public ResponseEntity<List<String>> getInfraCities() {
        return ResponseEntity.ok(infraRepository.findDistinctCities());
    }

    @ResponseBody
    @GetMapping("/api/infra-districts")
    public ResponseEntity<List<String>> getInfraDistricts(@RequestParam String cityNm) {
        return ResponseEntity.ok(infraRepository.findDistinctDistrictsByCity(cityNm));
    }

    @ResponseBody
    @GetMapping("/api/infra-systems")
    public ResponseEntity<List<String>> getInfraSystems(
            @RequestParam String cityNm, @RequestParam String distNm) {
        return ResponseEntity.ok(infraRepository.findDistinctSystemsByRegion(cityNm, distNm));
    }

    @ResponseBody
    @GetMapping("/api/infra-find")
    public ResponseEntity<?> findInfraByRegion(
            @RequestParam String cityNm, @RequestParam String distNm, @RequestParam String sysNmEn) {
        var list = infraRepository.findByCityDistSystem(cityNm, distNm, sysNmEn);
        if (list.isEmpty()) {
            return ResponseEntity.ok(InfraNotFound.instance());
        }
        return ResponseEntity.ok(InfraFindResult.of(list.get(0)));
    }

    /** 최종: 연도+지자체+시스템 → 사업 목록 */
    @ResponseBody
    @GetMapping("/api/projects")
    public ResponseEntity<List<ProjectFilterRow>> getProjectsFiltered(
            @RequestParam Integer year, @RequestParam String cityNm,
            @RequestParam String distNm, @RequestParam String sysNmEn) {
        List<ProjectFilterRow> result = swProjectRepository
                .findByYearAndCityNmAndDistNmAndSysNmEnOrderByProjIdDesc(year, cityNm, distNm, sysNmEn)
                .stream().map(ProjectFilterRow::from).toList();
        return ResponseEntity.ok(result);
    }

    /** 시스템별 공정명 목록 조회 */
    @GetMapping("/api/process-master")
    @ResponseBody
    public List<ProcessMasterRow> getProcessMasterList(@RequestParam String sysNmEn) {
        List<ProcessMaster> list = processMasterRepository.findBySysNmEnAndUseYnOrderBySortOrder(sysNmEn, "Y");
        List<ProcessMasterRow> result = new java.util.ArrayList<>();
        for (ProcessMaster pm : list) {
            result.add(ProcessMasterRow.from(pm));
        }
        return result;
    }

    /** 시스템별 용역목적/과업내용 조회 */
    @GetMapping("/api/service-purpose")
    @ResponseBody
    public List<ServicePurposeRow> getServicePurposeList(
            @RequestParam String sysNmEn,
            @RequestParam(required = false) String purposeType) {
        List<ServicePurpose> list;
        if (purposeType != null && !purposeType.isEmpty()) {
            list = servicePurposeRepository.findBySysNmEnAndPurposeTypeAndUseYnOrderBySortOrder(sysNmEn, purposeType, "Y");
        } else {
            list = servicePurposeRepository.findBySysNmEnAndUseYnOrderBySortOrder(sysNmEn, "Y");
        }
        List<ServicePurposeRow> result = new java.util.ArrayList<>();
        for (ServicePurpose sp : list) {
            result.add(ServicePurposeRow.from(sp));
        }
        return result;
    }

    // === 사용자 정보 API (현장대리인/과업참여자용) — S4 Phase 5: DocumentController 에서 편입 ===

    /**
     * 사용자 기본 정보 (비민감) — 감사 P1-3 조치 (2026-04-18):
     * 민감 필드(ssn/certificate/email) 는 이 엔드포인트에서 제거. 민감 정보가 필요하면
     * EDIT 권한 + {@code /api/user/{userSeq}/secure} 사용.
     */
    @ResponseBody
    @GetMapping("/api/user/{userSeq}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userSeq) {
        return userRepository.findById(userSeq)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(UserInfoRow.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자 전체 정보 (민감 필드 포함) — 감사 P1-3 조치 (2026-04-18):
     * ssn / certificate / email 이 필요한 화면(예: 과업 착수 문서 작성) 전용.
     * EDIT 권한 필수 — 비-EDIT 사용자는 403.
     */
    @ResponseBody
    @GetMapping("/api/user/{userSeq}/secure")
    public ResponseEntity<?> getUserInfoSecure(@PathVariable Long userSeq) {
        if (!"EDIT".equals(access.getAuth())) {
            // 현행 wire 보존: {error:{code,message}} (success 키 없음 — ApiResult.fail 은 success 추가하므로 미사용)
            return ResponseEntity.status(403)
                    .body(Map.of("error", Map.of("code", "FORBIDDEN", "message", "민감 정보 조회 권한이 없습니다")));
        }
        return userRepository.findById(userSeq)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(UserInfoSecureRow.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    // === sw_pjt 프로젝트 정보 API (착수계/기성계/준공계용) ===

    @ResponseBody
    @GetMapping("/api/project/{projId}")
    public ResponseEntity<Map<String, Object>> getProjectInfo(@PathVariable Long projId) {
        return swProjectRepository.findById(projId).map(p -> {
            Map<String, Object> data = new HashMap<>();
            data.put("projId", p.getProjId());
            data.put("projNm", p.getProjNm());
            data.put("sysNm", p.getSysNm());
            data.put("sysNmEn", p.getSysNmEn());
            data.put("client", p.getClient());
            data.put("orgNm", p.getOrgNm());
            data.put("orgLghNm", p.getOrgLghNm());
            data.put("cityNm", p.getCityNm());
            data.put("distNm", p.getDistNm());
            data.put("distCd", p.getDistCd());
            data.put("contAmt", p.getContAmt());
            data.put("contRt", p.getContRt());
            data.put("swAmt", p.getSwAmt());
            data.put("swRt", p.getSwRt());
            data.put("hwAmt", p.getHwAmt());
            data.put("contDt", p.getContDt() != null ? p.getContDt().toString() : null);
            data.put("startDt", p.getStartDt() != null ? p.getStartDt().toString() : null);
            data.put("endDt", p.getEndDt() != null ? p.getEndDt().toString() : null);
            data.put("instDt", p.getInstDt() != null ? p.getInstDt().toString() : null);
            data.put("contEnt", p.getContEnt());
            data.put("contDept", p.getContDept());
            data.put("contType", p.getContType());
            data.put("bizType", p.getBizType());
            data.put("bizCat", p.getBizCat());
            data.put("bizCatEn", p.getBizCatEn());
            data.put("maintType", p.getMaintType());
            data.put("prePay", p.getPrePay());
            data.put("payProg", p.getPayProg());
            data.put("payComp", p.getPayComp());
            data.put("year", p.getYear());

            // PersonInfo 연동 (담당자 정보)
            if (p.getPersonId() != null) {
                personInfoRepository.findById(p.getPersonId()).ifPresent(pi -> {
                    data.put("personNm", pi.getUserNm());
                    data.put("personTel", pi.getTel());
                    data.put("personEmail", pi.getEmail());
                    data.put("personDept", pi.getDeptNm());
                    data.put("personTeam", pi.getTeamNm());
                    data.put("personPos", pi.getPos());
                    data.put("personOrg", pi.getOrgNm());
                });
            }
            return ResponseEntity.ok(data);
        }).orElse(ResponseEntity.notFound().build());
    }
}
