package com.swmanager.system.controller;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.service.SwService;
import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.repository.*;

import com.swmanager.system.dto.SwProjectDTO;
import com.swmanager.system.exception.ResourceNotFoundException;
import com.swmanager.system.exception.InsufficientPermissionException;
import com.swmanager.system.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * SW 프로젝트 관리 Controller
 */
@Slf4j
@Controller
@RequestMapping("/projects")
public class SwController {

    @Autowired private SwService swService;
    @Autowired private com.swmanager.system.repository.SwProjectRepository swProjectRepository;

    // ★ personInfoRepository 제거 → userRepository 사용 (sw_pjt.person_id = users.user_id)
    @Autowired private UserRepository userRepository;
    @Autowired private SigunguCodeRepository sigunguRepository;
    @Autowired private SysMstRepository sysMstRepository;
    @Autowired private ContFrmMstRepository contFrmRepository;
    @Autowired private PrjTypesRepository prjTypesRepository;
    @Autowired private MaintTpMstRepository maintTpRepository;
    @Autowired private ContStatMstRepository contStatRepository;

    // ===== 날짜 형식 바인더 (다양한 형식 수용) =====

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            private static final List<DateTimeFormatter> FORMATTERS = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),      // 2026-02-13
                DateTimeFormatter.ofPattern("yyyy.MM.dd"),      // 2026.02.13
                DateTimeFormatter.ofPattern("yyyy. M. d."),     // 2026. 2. 13.
                DateTimeFormatter.ofPattern("yyyy. MM. dd."),   // 2026. 02. 13.
                DateTimeFormatter.ofPattern("yy. M. d."),       // 26. 2. 13.
                DateTimeFormatter.ofPattern("yy. MM. dd."),     // 26. 02. 13.
                DateTimeFormatter.ofPattern("yy.M.d"),          // 26.2.13
                DateTimeFormatter.ofPattern("yyyyMMdd")         // 20260213
            );

            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.isBlank()) {
                    setValue(null);
                    return;
                }
                String trimmed = text.trim();
                for (DateTimeFormatter fmt : FORMATTERS) {
                    try {
                        setValue(LocalDate.parse(trimmed, fmt));
                        return;
                    } catch (DateTimeParseException ignored) {}
                }
                throw new IllegalArgumentException("날짜 형식 오류: " + text + " (예: 2026-02-13)");
            }

            @Override
            public String getAsText() {
                LocalDate date = (LocalDate) getValue();
                return date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
            }
        });
    }

    // ===== 인증 헬퍼 =====

    private CustomUserDetails getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) return (CustomUserDetails) principal;
            return null;
        } catch (Exception e) {
            log.error("getCurrentUser error", e);
            return null;
        }
    }

    // ===== 폼/상세 공통 참조 데이터 =====

    /**
     * project-form / project-detail 에 공통으로 필요한 드롭다운 데이터
     *
     * personList  : users 테이블 (활성 계정) — sw_pjt.person_id = users.user_id
     * sidoList    : sigungu_code.sido_name 중복 제거 목록
     * sysMstList  : sys_mst (nm 오름차순)
     * contFrmList : cont_frm_mst
     * prjTypesList: prj_types
     * maintTpList : maint_tp_mst
     * contStatList: cont_stat_mst
     */
    private void addFormRefData(Model model) {
        model.addAttribute("personList",    userRepository.findByEnabledTrue());
        model.addAttribute("sidoList",      sigunguRepository.findDistinctSidoNm());
        model.addAttribute("sysMstList",    sysMstRepository.findAll(Sort.by(Sort.Direction.ASC, "nm")));
        model.addAttribute("contFrmList",   contFrmRepository.findAll());
        model.addAttribute("prjTypesList",  prjTypesRepository.findAll());
        model.addAttribute("maintTpList",   maintTpRepository.findAll());
        model.addAttribute("contStatList",  contStatRepository.findAll());
    }

    // ===== 시군구 목록 API =====

    /**
     * 시도광역 선택 시 시군구 목록 동적 로드
     * GET /projects/api/districts?sido=강원특별자치도
     * 반환: [{admSectC, sidoNm, sggNm, insttC}, ...]
     */
    @GetMapping("/api/districts")
    @ResponseBody
    public Object getDistricts(@RequestParam String sido) {
        return sigunguRepository.findBySidoNmOrderBySggNm(sido);
    }

    // ===== 목록 =====

    @GetMapping("/status")
    public String projectList(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw",   defaultValue = "") String kw,
            @RequestParam(value = "year",     required = false) String year,      // String: NFR-7
            @RequestParam(value = "city",     required = false) String city,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "sysNmEn",  required = false) String sysNmEn) {

        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return "redirect:/login";

        String auth = cu.getUser().getAuthProject();
        if ("NONE".equals(auth)) throw new InsufficientPermissionException("프로젝트 조회");

        Sort sort = Sort.by(Sort.Direction.DESC, "year")
                .and(Sort.by(Sort.Direction.ASC, "cityNm"))
                .and(Sort.by(Sort.Direction.ASC, "distNm"))
                .and(Sort.by(Sort.Direction.ASC, "sysNm"));
        Pageable pageable = PageRequest.of(Math.max(page, 0), 10, sort);
        Page<SwProject> paging = swService.search(kw, year, city, district, sysNmEn, pageable);

        // 드롭다운 옵션 주입
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("year", year);
        model.addAttribute("city", city);
        model.addAttribute("district", district);
        model.addAttribute("sysNmEn", sysNmEn);
        model.addAttribute("yearOptions", swProjectRepository.findDistinctYears());
        model.addAttribute("cityOptions", swProjectRepository.findAllDistinctCityNms());
        model.addAttribute("sysNmEnOptions", swProjectRepository.findAllDistinctSysNmEns());
        model.addAttribute("districtOptions",
                (city != null && !city.isBlank())
                        ? swProjectRepository.findAllDistinctDistNmsByCity(city)
                        : java.util.List.of());
        return "project-list";
    }

    /**
     * 시군구 AJAX — 시도 선택 시 하위 시군구 옵션 로드
     * (sw_pjt 기반 실제 존재 distinct, 기존 /api/districts 와 별개)
     */
    @GetMapping("/api/dist-options")
    @ResponseBody
    public java.util.List<String> getDistOptions(@RequestParam(required = false) String city) {
        if (city == null || city.isBlank()) return java.util.List.of();
        return swProjectRepository.findAllDistinctDistNmsByCity(city);
    }

    // ===== 상세 조회 =====

    /**
     * 상세 조회: DB 값 그대로 표시
     * - 드롭다운(담당자·시도·시스템명 등)도 DB 저장값 기준으로 선택 상태 표시
     * - addFormRefData() 로 참조 데이터 전달 (select option 목록 필요)
     */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return "redirect:/login";

        String auth = cu.getUser().getAuthProject();
        if ("NONE".equals(auth)) throw new InsufficientPermissionException("프로젝트 조회");

        SwProject project = swService.getProject(id);
        if (project == null) throw new ResourceNotFoundException("프로젝트", id);

        model.addAttribute("project", SwProjectDTO.fromEntity(project));
        addFormRefData(model);   // ★ 상세 페이지에도 참조 데이터 필요

        return "project-detail";
    }

    // ===== 신규 등록 폼 =====

    @GetMapping("/new")
    public String newProject(Model model) {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return "redirect:/login";

        if (!"EDIT".equals(cu.getUser().getAuthProject()))
            throw new InsufficientPermissionException("프로젝트 등록");

        model.addAttribute("project", new SwProjectDTO());
        addFormRefData(model);
        return "project-form";
    }

    // ===== 수정 폼 =====

    @GetMapping("/form")
    public String form(@RequestParam(value = "id", required = false) Long id, Model model) {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return "redirect:/login";

        if (!"EDIT".equals(cu.getUser().getAuthProject()))
            throw new InsufficientPermissionException("프로젝트 등록/수정");

        if (id != null) {
            SwProject project = swService.getProject(id);
            if (project == null) throw new ResourceNotFoundException("프로젝트", id);
            model.addAttribute("project", SwProjectDTO.fromEntity(project));
        } else {
            model.addAttribute("project", new SwProjectDTO());
        }

        addFormRefData(model);
        return "project-form";
    }

    // ===== 저장 =====

    @PostMapping("/save")
    public String saveProject(@ModelAttribute SwProject project) {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return "redirect:/login";

        if (!"EDIT".equals(cu.getUser().getAuthProject()))
            throw new InsufficientPermissionException("프로젝트 등록/수정");

        swService.save(project);
        log.info("프로젝트 저장 완료 - ID: {}", project.getProjId());
        return "redirect:/projects/status";
    }

    // ===== 삭제 =====

    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable("id") Long id) {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return "redirect:/login";

        if (!"EDIT".equals(cu.getUser().getAuthProject()))
            throw new InsufficientPermissionException("프로젝트 삭제");

        swService.delete(id);
        log.warn("프로젝트 삭제 완료 - ID: {}", id);
        return "redirect:/projects/status";
    }

    // ===== REST API =====

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<SwProjectDTO>> getProjectApi(@PathVariable Long id) {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        String auth = cu.getUser().getAuthProject();
        String role = cu.getUser().getUserRole();
        if (!"ROLE_ADMIN".equals(role) && (auth == null || "NONE".equals(auth))) {
            return ResponseEntity.status(403).body(ApiResponse.error("프로젝트 조회 권한이 없습니다."));
        }

        SwProject p = swService.getProject(id);
        if (p == null) throw new ResourceNotFoundException("프로젝트", id);
        return ResponseEntity.ok(ApiResponse.success(SwProjectDTO.fromEntity(p), "조회 성공"));
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<ApiResponse<SwProjectDTO>> createProjectApi(@RequestBody SwProject project) {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        String auth = cu.getUser().getAuthProject();
        String role = cu.getUser().getUserRole();
        if (!"ROLE_ADMIN".equals(role) && !"EDIT".equals(auth)) {
            return ResponseEntity.status(403).body(ApiResponse.error("프로젝트 등록 권한이 없습니다."));
        }

        SwProject saved = swService.save(project);
        log.info("프로젝트 API 등록 완료 - ID: {}, 사용자: {}", saved.getProjId(), cu.getUsername());
        return ResponseEntity.ok(ApiResponse.success(SwProjectDTO.fromEntity(saved), "등록 성공"));
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<SwProjectDTO>> updateProjectApi(
            @PathVariable Long id, @RequestBody SwProject project) {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        String auth = cu.getUser().getAuthProject();
        String role = cu.getUser().getUserRole();
        if (!"ROLE_ADMIN".equals(role) && !"EDIT".equals(auth)) {
            return ResponseEntity.status(403).body(ApiResponse.error("프로젝트 수정 권한이 없습니다."));
        }

        project.setProjId(id);
        SwProject updated = swService.save(project);
        log.info("프로젝트 API 수정 완료 - ID: {}, 사용자: {}", id, cu.getUsername());
        return ResponseEntity.ok(ApiResponse.success(SwProjectDTO.fromEntity(updated), "수정 성공"));
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> deleteProjectApi(@PathVariable Long id) {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        String auth = cu.getUser().getAuthProject();
        String role = cu.getUser().getUserRole();
        if (!"ROLE_ADMIN".equals(role) && !"EDIT".equals(auth)) {
            return ResponseEntity.status(403).body(ApiResponse.error("프로젝트 삭제 권한이 없습니다."));
        }

        swService.delete(id);
        log.warn("프로젝트 API 삭제 완료 - ID: {}, 사용자: {}", id, cu.getUsername());
        return ResponseEntity.ok(ApiResponse.success("삭제 성공"));
    }

    // S15 pjt-equip-decision (2026-04-22): 장비 관리 API 제거 (테이블 DROP + 0 rows)
    //  - /api/equip/{projId} GET/POST 엔드포인트 제거
    //  - PjtEquip / PjtEquipRepository 제거

}
