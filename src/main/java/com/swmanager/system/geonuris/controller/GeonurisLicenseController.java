package com.swmanager.system.geonuris.controller;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.geonuris.domain.GeonurisLicense;
import com.swmanager.system.geonuris.service.GeonurisLicenseService;
import com.swmanager.system.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * GeoNURIS 라이선스 발급 컨트롤러
 * URL: /geonuris/license/**
 */
@Slf4j
@Controller
@RequestMapping("/geonuris/license")
@RequiredArgsConstructor
public class GeonurisLicenseController {

    private final GeonurisLicenseService licenseService;
    private final LogService logService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ── 권한 헬퍼 ─────────────────────────────────────────────
    // ADMIN         : 모든 기능 (isAdmin 체크)
    // EDIT (편집자)  : 모든 기능 (발급, 조회, CSV 다운로드)
    // VIEW (조회자)  : 조회 + CSV 다운로드만 가능, 발급/폼접근 불가
    private boolean isAdmin(CustomUserDetails u) {
        return u.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    /** 목록·상세 조회 + CSV 다운로드 권한 (VIEW 이상) */
    private boolean hasViewPermission(CustomUserDetails u) {
        if (isAdmin(u)) return true;
        String auth = u.getUser().getAuthLicense();
        return "VIEW".equals(auth) || "EDIT".equals(auth);
    }
    /** 발급·수정·삭제 권한 (EDIT 또는 ADMIN) */
    private boolean hasEditPermission(CustomUserDetails u) {
        if (isAdmin(u)) return true;
        return "EDIT".equals(u.getUser().getAuthLicense());
    }
    private void addPermModel(Model m, CustomUserDetails u) {
        m.addAttribute("hasViewPermission", hasViewPermission(u));
        m.addAttribute("hasEditPermission", hasEditPermission(u));
        m.addAttribute("currentUser", u.getUser().getUsername());
    }

    // ========================================================
    // 목록
    // GET /geonuris/license/list
    // ========================================================
    @GetMapping({"/list", "", "/"})
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String type,
                       @AuthenticationPrincipal CustomUserDetails u,
                       Model m) {
        if (!hasViewPermission(u)) {
            m.addAttribute("error", true);
            m.addAttribute("message", "접근 권한이 없습니다. (VIEW 이상 필요)");
            return "geonuris/geonuris-list";
        }
        logService.log("GeoNURIS라이선스", "목록조회",
            u.getUsername() + (keyword != null && !keyword.isBlank() ? " [검색:" + keyword + "]" : "")
            + (type != null && !type.isBlank() ? " [타입:" + type + "]" : ""));
        List<GeonurisLicense> list;
        if (keyword != null && !keyword.isBlank()) {
            list = licenseService.search(keyword);
        } else if (type != null && !type.isBlank()) {
            list = licenseService.findAll().stream()
                    .filter(l -> type.equals(l.getLicenseType()))
                    .toList();
        } else {
            list = licenseService.findAll();
        }

        m.addAttribute("licenses",    list);
        m.addAttribute("stats",       licenseService.getStats());
        m.addAttribute("keyword",     keyword);
        m.addAttribute("selectedType", type);
        m.addAttribute("isAdmin", isAdmin(u));
        addPermModel(m, u);
        return "geonuris/geonuris-list";
    }

    // ========================================================
    // 발급 폼
    // GET /geonuris/license/new
    // ========================================================
    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal CustomUserDetails u, Model m) {
        if (!hasEditPermission(u)) {
            m.addAttribute("error", true);
            m.addAttribute("message", "발급 권한이 없습니다. (EDIT 권한 필요)");
            return "geonuris/geonuris-list";
        }
        logService.log("GeoNURIS라이선스", "발급폼접근", u.getUsername());
        m.addAttribute("license",      new GeonurisLicense());
        m.addAttribute("defaultIssuer", u.getUser().getUsername());
        m.addAttribute("today",        LocalDate.now().format(DATE_FMT));
        addPermModel(m, u);
        return "geonuris/geonuris-form";
    }

    // ========================================================
    // 발급 처리
    // POST /geonuris/license/issue
    // ========================================================
    @PostMapping("/issue")
    public String issue(@RequestParam String licenseType,
                        @RequestParam String userName,
                        @RequestParam(required = false) String organization,
                        @RequestParam(required = false) String phone,
                        @RequestParam(required = false) String email,
                        @RequestParam String issuer,
                        @RequestParam String macAddress,
                        @RequestParam(required = false) String permission,
                        @RequestParam String startDate,
                        @RequestParam String expiryDate,
                        @RequestParam(required = false) String dbmsType,
                        @RequestParam(required = false, defaultValue = "0") Integer setlCount,
                        @RequestParam(required = false, defaultValue = "false") Boolean pluginEdit,
                        @RequestParam(required = false, defaultValue = "false") Boolean pluginGdm,
                        @RequestParam(required = false, defaultValue = "false") Boolean pluginTmbuilder,
                        @RequestParam(required = false, defaultValue = "false") Boolean pluginSetl,
                        @AuthenticationPrincipal CustomUserDetails u,
                        RedirectAttributes ra) {

        if (!hasEditPermission(u)) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("message", "발급 권한이 없습니다.");
            return "redirect:/geonuris/license/list";
        }

        try {
            GeonurisLicense lic = new GeonurisLicense();
            lic.setLicenseType(licenseType);
            lic.setUserName(userName);
            lic.setOrganization(organization);
            lic.setPhone(phone);
            lic.setEmail(email);
            lic.setIssuer(issuer);
            lic.setMacAddress(macAddress.trim());
            lic.setPermission(permission != null && !permission.isBlank() ? permission.toUpperCase() : "A");
            lic.setStartDate(LocalDate.parse(startDate, DATE_FMT).atStartOfDay());
            lic.setExpiryDate(LocalDate.parse(expiryDate, DATE_FMT).atTime(LocalTime.MAX));
            lic.setDbmsType(dbmsType);
            lic.setSetlCount(setlCount);
            lic.setPluginEdit(pluginEdit);
            lic.setPluginGdm(pluginGdm);
            lic.setPluginTmbuilder(pluginTmbuilder);
            lic.setPluginSetl(pluginSetl);
            lic.setCreatedBy(u.getUsername());

            GeonurisLicense saved = licenseService.issue(lic);

            logService.log("GeoNURIS라이선스", "발급",
                    u.getUsername() + " → " + saved.getLicenseTypeLabel() + " [" + saved.getUserName() + "]");

            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("message", saved.getLicenseTypeLabel() + " 라이선스가 발급되었습니다.");
            ra.addFlashAttribute("newId", saved.getId());
            return "redirect:/geonuris/license/list";

        } catch (Exception e) {
            log.error("GeoNURIS 라이선스 발급 실패", e);
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("message", "발급 실패: " + e.getMessage());
            return "redirect:/geonuris/license/new";
        }
    }

    // ========================================================
    // 상세보기
    // GET /geonuris/license/{id}
    // ========================================================
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails u,
                         Model m) {
        if (!hasViewPermission(u)) {
            m.addAttribute("error", true);
            m.addAttribute("message", "접근 권한이 없습니다.");
            return "geonuris/geonuris-list";
        }
        try {
            GeonurisLicense lic = licenseService.getById(id);
            logService.log("GeoNURIS라이선스", "상세조회",
                u.getUsername() + " → ID:" + id + " [" + lic.getLicenseType() + "]");
            m.addAttribute("license", lic);
            m.addAttribute("isAdmin", isAdmin(u));
            addPermModel(m, u);
            return "geonuris/geonuris-detail";
        } catch (Exception e) {
            m.addAttribute("error", true);
            m.addAttribute("message", e.getMessage());
            return "redirect:/geonuris/license/list";
        }
    }

    // ========================================================
    // 수정 폼 (관리자 전용)
    // GET /geonuris/license/{id}/edit
    // ========================================================
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails u,
                           Model m) {
        if (!isAdmin(u)) {
            m.addAttribute("error", true);
            m.addAttribute("message", "수정 권한이 없습니다. (관리자 전용)");
            return "geonuris/geonuris-list";
        }
        try {
            GeonurisLicense lic = licenseService.getById(id);
            logService.log("GeoNURIS라이선스", "수정폼접근",
                u.getUsername() + " → ID:" + id + " [" + lic.getLicenseType() + "]");
            m.addAttribute("license", lic);
            addPermModel(m, u);
            m.addAttribute("isAdmin", true);
            return "geonuris/geonuris-edit";
        } catch (Exception e) {
            m.addAttribute("error", true);
            m.addAttribute("message", e.getMessage());
            return "redirect:/geonuris/license/list";
        }
    }

    // ========================================================
    // 수정 처리 (관리자 전용)
    // POST /geonuris/license/{id}/update
    // ========================================================
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam String licenseType,
                         @RequestParam String userName,
                         @RequestParam(required = false) String organization,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String email,
                         @RequestParam String issuer,
                         @RequestParam String macAddress,
                         @RequestParam(required = false) String permission,
                         @RequestParam String startDate,
                         @RequestParam String expiryDate,
                         @RequestParam(required = false) String dbmsType,
                         @RequestParam(required = false, defaultValue = "0") Integer setlCount,
                         @RequestParam(required = false, defaultValue = "false") Boolean pluginEdit,
                         @RequestParam(required = false, defaultValue = "false") Boolean pluginGdm,
                         @RequestParam(required = false, defaultValue = "false") Boolean pluginTmbuilder,
                         @RequestParam(required = false, defaultValue = "false") Boolean pluginSetl,
                         @AuthenticationPrincipal CustomUserDetails u,
                         RedirectAttributes ra) {

        if (!isAdmin(u)) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("message", "수정 권한이 없습니다. (관리자 전용)");
            return "redirect:/geonuris/license/list";
        }

        try {
            GeonurisLicense lic = licenseService.getById(id);

            lic.setLicenseType(licenseType);
            lic.setUserName(userName);
            lic.setOrganization(organization);
            lic.setPhone(phone);
            lic.setEmail(email);
            lic.setIssuer(issuer);
            lic.setMacAddress(macAddress.trim());
            lic.setPermission(permission != null && !permission.isBlank() ? permission.toUpperCase() : "A");
            lic.setStartDate(LocalDate.parse(startDate, DATE_FMT).atStartOfDay());
            lic.setExpiryDate(LocalDate.parse(expiryDate, DATE_FMT).atTime(LocalTime.MAX));
            lic.setDbmsType(dbmsType);
            lic.setSetlCount(setlCount);
            lic.setPluginEdit(pluginEdit);
            lic.setPluginGdm(pluginGdm);
            lic.setPluginTmbuilder(pluginTmbuilder);
            lic.setPluginSetl(pluginSetl);

            GeonurisLicense saved = licenseService.update(lic);

            logService.log("GeoNURIS라이선스", "수정",
                    u.getUsername() + " → ID:" + saved.getId() + " " + saved.getLicenseTypeLabel()
                    + " [" + saved.getUserName() + "]");

            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("message", "라이선스가 수정되었습니다. (ID: " + saved.getId() + ")");
            return "redirect:/geonuris/license/" + saved.getId();

        } catch (Exception e) {
            log.error("GeoNURIS 라이선스 수정 실패 id={}", id, e);
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("message", "수정 실패: " + e.getMessage());
            return "redirect:/geonuris/license/" + id + "/edit";
        }
    }

    // ========================================================
    // 삭제 처리 (관리자 전용)
    // POST /geonuris/license/{id}/delete
    // ========================================================
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails u,
                         RedirectAttributes ra) {

        if (!isAdmin(u)) {
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("message", "삭제 권한이 없습니다. (관리자 전용)");
            return "redirect:/geonuris/license/list";
        }

        try {
            GeonurisLicense lic = licenseService.getById(id);
            String info = lic.getLicenseTypeLabel() + " [" + lic.getUserName() + "]";

            licenseService.delete(id);

            logService.log("GeoNURIS라이선스", "삭제",
                    u.getUsername() + " → ID:" + id + " " + info);

            ra.addFlashAttribute("success", true);
            ra.addFlashAttribute("message", "라이선스가 삭제되었습니다. (ID: " + id + ", " + info + ")");
            return "redirect:/geonuris/license/list";

        } catch (Exception e) {
            log.error("GeoNURIS 라이선스 삭제 실패 id={}", id, e);
            ra.addFlashAttribute("error", true);
            ra.addFlashAttribute("message", "삭제 실패: " + e.getMessage());
            return "redirect:/geonuris/license/" + id;
        }
    }

    // ========================================================
    // 대시보드용 건수 API
    // GET /geonuris/license/stats-count
    // ========================================================
    @GetMapping("/stats-count")
    @ResponseBody
    public Map<String, Object> statsCount(@AuthenticationPrincipal CustomUserDetails u) {
        if (!hasViewPermission(u)) return Map.of("total", 0);
        return licenseService.getStats();
    }

    // ========================================================
    // CSV 다운로드
    // GET /geonuris/license/download/csv?type=GSS30 (미입력시 전체)
    // ========================================================
    @GetMapping("/download/csv")
    public ResponseEntity<byte[]> downloadCsv(
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal CustomUserDetails u) {

        if (!hasViewPermission(u)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<GeonurisLicense> list = (type != null && !type.isBlank())
                    ? licenseService.findAll().stream()
                        .filter(l -> type.equals(l.getLicenseType())).toList()
                    : licenseService.findAll();

            String csv = buildCsv(list);
            byte[] bytes = csv.getBytes("UTF-8");

            // BOM 추가 (Excel 한글 깨짐 방지)
            byte[] bom = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
            byte[] result = new byte[bom.length + bytes.length];
            System.arraycopy(bom, 0, result, 0, bom.length);
            System.arraycopy(bytes, 0, result, bom.length, bytes.length);

            String label = (type != null && !type.isBlank()) ? type : "전체";
            String encodedName = java.net.URLEncoder.encode(
                "GeoNURIS라이선스_" + label + "_" + java.time.LocalDate.now() + ".csv",
                java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");

            logService.log("GeoNURIS라이선스", "CSV다운로드",
                u.getUsername() + " (" + label + " " + list.size() + "건)");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName)
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(result);

        } catch (Exception e) {
            log.error("CSV 다운로드 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** CSV 문자열 빌드 */
    private String buildCsv(List<GeonurisLicense> list) {
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dFmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();

        // 헤더
        sb.append("ID,라이선스종류,사용자,기관회사,연락처,이메일,발급자,MAC주소,권한,발효일,만료일,DBMS,SETL동시접속,플러그인_Edit,플러그인_GDM,플러그인_TM,플러그인_SETL,발급일시\n");

        for (GeonurisLicense l : list) {
            sb.append(l.getId()).append(",");
            sb.append(csvEsc(l.getLicenseTypeLabel())).append(",");
            sb.append(csvEsc(l.getUserName())).append(",");
            sb.append(csvEsc(l.getOrganization())).append(",");
            sb.append(csvEsc(l.getPhone())).append(",");
            sb.append(csvEsc(l.getEmail())).append(",");
            sb.append(csvEsc(l.getIssuer())).append(",");
            sb.append(csvEsc(l.getMacAddress())).append(",");
            sb.append(csvEsc(l.getPermission())).append(",");
            sb.append(l.getStartDate()  != null ? l.getStartDate().format(dFmt)   : "").append(",");
            sb.append(l.getExpiryDate() != null ? l.getExpiryDate().format(dFmt)  : "").append(",");
            sb.append(csvEsc(l.getDbmsType())).append(",");
            sb.append(l.getSetlCount() != null ? l.getSetlCount() : "").append(",");
            sb.append(l.getPluginEdit()      != null && l.getPluginEdit()      ? "Y" : "N").append(",");
            sb.append(l.getPluginGdm()       != null && l.getPluginGdm()       ? "Y" : "N").append(",");
            sb.append(l.getPluginTmbuilder() != null && l.getPluginTmbuilder() ? "Y" : "N").append(",");
            sb.append(l.getPluginSetl()      != null && l.getPluginSetl()      ? "Y" : "N").append(",");
            sb.append(l.getCreatedAt() != null ? l.getCreatedAt().format(dtFmt) : "").append("\n");
        }
        return sb.toString();
    }

    private String csvEsc(String v) {
        if (v == null || v.isBlank()) return "";
        // 쉼표, 줄바꿈, 따옴표 포함 시 따옴표로 감싸기
        if (v.contains(",") || v.contains("\n") || v.contains("\"")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    // ========================================================
    // 다운로드
    // GET /geonuris/license/{id}/download
    // ========================================================
    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails u) {
        if (!hasViewPermission(u)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            GeonurisLicense lic = licenseService.getById(id);
            byte[] bytes = licenseService.getLicenseBytes(lic);

            String licFileName = lic.getFileName() != null ? lic.getFileName() : "license.lic";
            // 사용자별 파일명: {type}_{id}_{licFileName}  (한글 별도 인코딩)
            String downloadName = lic.getLicenseType() + "_" + id + "_" + licFileName;

            // RFC 5987: 한글 포함 파일명을 UTF-8 URL 인코딩으로 처리
            // filename*= 은 최신 브라우저, filename= 은 구형 호환
            String encodedName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8)
                                           .replace("+", "%20");
            String contentDisposition = "attachment; filename=\"" + encodedName + "\"; "
                                      + "filename*=UTF-8''" + encodedName;

            logService.log("GeoNURIS라이선스", "다운로드",
                    u.getUsername() + " → ID:" + id + " [" + lic.getUserName() + "]");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(bytes.length)
                    .body(new ByteArrayResource(bytes));

        } catch (Exception e) {
            log.error("다운로드 실패 id={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
