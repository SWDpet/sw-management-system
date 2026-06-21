package com.swmanager.system.controller;

import com.swmanager.system.domain.workplan.ProcessMaster;
import com.swmanager.system.domain.workplan.ServicePurpose;
import com.swmanager.system.dto.workplan.ProcessMasterRow;
import com.swmanager.system.dto.workplan.ServicePurposeRow;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.repository.workplan.ProcessMasterRepository;
import com.swmanager.system.repository.workplan.ServicePurposeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public ResponseEntity<List<Map<String, Object>>> getRegionSigungus(@RequestParam String sidoNm) {
        var list = sigunguCodeRepository.findBySidoNmOrderBySggNm(sidoNm);
        List<Map<String, Object>> result = list.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("admSectC", s.getAdmSectC());
            m.put("sggNm", s.getSggNm());
            m.put("sidoNm", s.getSidoNm());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    /** 전체 시스템 목록 (sys_mst) */
    @ResponseBody
    @GetMapping("/api/systems-all")
    public ResponseEntity<List<Map<String, Object>>> getSystemsAll() {
        var list = sysMstRepository.findAll();
        List<Map<String, Object>> result = list.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("cd", s.getCd());
            m.put("nm", s.getNm());
            return m;
        }).toList();
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
    public ResponseEntity<Map<String, Object>> findInfraByRegion(
            @RequestParam String cityNm, @RequestParam String distNm, @RequestParam String sysNmEn) {
        var list = infraRepository.findByCityDistSystem(cityNm, distNm, sysNmEn);
        Map<String, Object> body = new HashMap<>();
        if (list.isEmpty()) {
            body.put("found", false);
            return ResponseEntity.ok(body);
        }
        var infra = list.get(0);
        body.put("found", true);
        body.put("infraId", infra.getInfraId());
        body.put("cityNm", infra.getCityNm());
        body.put("distNm", infra.getDistNm());
        body.put("sysNm", infra.getSysNm());
        body.put("sysNmEn", infra.getSysNmEn());
        return ResponseEntity.ok(body);
    }

    /** 최종: 연도+지자체+시스템 → 사업 목록 */
    @ResponseBody
    @GetMapping("/api/projects")
    public ResponseEntity<List<Map<String, Object>>> getProjectsFiltered(
            @RequestParam Integer year, @RequestParam String cityNm,
            @RequestParam String distNm, @RequestParam String sysNmEn) {
        var projects = swProjectRepository.findByYearAndCityNmAndDistNmAndSysNmEnOrderByProjIdDesc(
                year, cityNm, distNm, sysNmEn);
        List<Map<String, Object>> result = projects.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("projId", p.getProjId());
            m.put("year", p.getYear());
            m.put("projNm", p.getProjNm());
            m.put("sysNm", p.getSysNm());
            m.put("sysNmEn", p.getSysNmEn());
            m.put("contAmt", p.getContAmt());
            m.put("cityNm", p.getCityNm());
            m.put("distNm", p.getDistNm());
            return m;
        }).toList();
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
}
