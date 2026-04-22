package com.swmanager.system.quotation.controller;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constant.enums.QuoteTemplateType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.quotation.domain.*;
import com.swmanager.system.quotation.dto.QuotationDTO;
import com.swmanager.system.quotation.service.QuotationService;
import com.swmanager.system.service.LogService;
import com.swmanager.system.exception.InsufficientPermissionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;
    private final LogService logService;
    private final UserRepository userRepository;
    private final SwProjectRepository swProjectRepository;

    /** ROUNDDOWN 절사 헬퍼 */
    private static long roundDown(long value, int unit) {
        if (unit <= 1) return value;
        return (value / unit) * unit;
    }

    // ===== 권한 & 사용자 헬퍼 =====

    /** 현재 로그인 사용자 가져오기 */
    private CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        }
        return null;
    }

    /** 견적서 조회 권한 체크 (VIEW 이상) */
    private void checkViewAuth() {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new InsufficientPermissionException("로그인");
        }
        String role = currentUser.getUser().getUserRole();
        String authQ = currentUser.getUser().getAuthQuotation();
        if (!"ROLE_ADMIN".equals(role) && (authQ == null || "NONE".equals(authQ))) {
            log.warn("견적서 접근 권한 없음 - 사용자: {}, authQuotation: {}", currentUser.getUsername(), authQ);
            throw new InsufficientPermissionException("견적서 조회");
        }
    }

    /** 견적서 편집 권한 체크 (EDIT만) */
    private void checkEditAuth() {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new InsufficientPermissionException("로그인");
        }
        String role = currentUser.getUser().getUserRole();
        String authQ = currentUser.getUser().getAuthQuotation();
        if (!"ROLE_ADMIN".equals(role) && !"EDIT".equals(authQ)) {
            log.warn("견적서 편집 권한 없음 - 사용자: {}, authQuotation: {}", currentUser.getUsername(), authQ);
            throw new InsufficientPermissionException("견적서 편집");
        }
    }

    /** 현재 사용자 userid 반환 */
    private String getCurrentUserId() {
        CustomUserDetails user = getCurrentUser();
        return (user != null) ? user.getUsername() : "unknown";
    }

    /** 현재 사용자 실명 반환 */
    private String getCurrentUserName() {
        CustomUserDetails user = getCurrentUser();
        if (user != null && user.getUser() != null && user.getUser().getUsername() != null) {
            return user.getUser().getUsername();
        }
        return getCurrentUserId();
    }

    /** 관리자 여부 */
    private boolean isAdmin() {
        CustomUserDetails user = getCurrentUser();
        if (user == null) return false;
        return "ROLE_ADMIN".equals(user.getUser().getUserRole());
    }

    /** 폼에 관리자용 작성자 선택 데이터 추가 */
    private void addCreatorSelectData(Model model) {
        model.addAttribute("isAdmin", isAdmin());
        model.addAttribute("currentUserId", getCurrentUserId());
        model.addAttribute("currentUserName", getCurrentUserName());
        if (isAdmin()) {
            List<User> users = userRepository.findByEnabledTrue();
            model.addAttribute("userList", users);
        }
    }

    // ===== 페이지 라우팅 =====

    /** 견적서 작성 폼 */
    @GetMapping("/quotation/new")
    public String newQuotation(Model model) {
        checkEditAuth();
        model.addAttribute("patterns", quotationService.getPatterns(null));
        addCreatorSelectData(model);
        logService.log(MenuName.QUOTATION, AccessActionType.VIEW, "견적서 작성 페이지 접근");
        return "quotation/quotation-form";
    }

    /** 견적서 목록 */
    @GetMapping("/quotation/list")
    public String listQuotations(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            Model model) {
        checkViewAuth();
        if (year == null) year = LocalDate.now().getYear();
        List<QuotationDTO> quotations = quotationService.getQuotations(category, year, status);

        // 키워드 검색 필터링 (견적번호, 건명, 수신, 견적일자, 작성자)
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            quotations = quotations.stream()
                    .filter(q -> (q.getQuoteNumber() != null && q.getQuoteNumber().toLowerCase().contains(kw))
                            || (q.getProjectName() != null && q.getProjectName().toLowerCase().contains(kw))
                            || (q.getRecipient() != null && q.getRecipient().toLowerCase().contains(kw))
                            || (q.getQuoteDate() != null && q.getQuoteDate().toString().contains(kw))
                            || (q.getCreatedBy() != null && q.getCreatedBy().toLowerCase().contains(kw)))
                    .collect(java.util.stream.Collectors.toList());
        }

        model.addAttribute("quotations", quotations);
        model.addAttribute("stats", quotationService.getStats());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedKeyword", keyword);
        // 편집 권한 여부
        CustomUserDetails currentUser = getCurrentUser();
        String role = currentUser.getUser().getUserRole();
        String authQ = currentUser.getUser().getAuthQuotation();
        model.addAttribute("canEdit", "ROLE_ADMIN".equals(role) || "EDIT".equals(authQ));
        return "quotation/quotation-list";
    }

    /** 견적서 상세보기 */
    @GetMapping("/quotation/{id}")
    public String viewQuotation(@PathVariable Long id, Model model) {
        checkViewAuth();
        model.addAttribute("quotation", quotationService.getQuotation(id));
        // 편집 권한 여부 전달
        CustomUserDetails currentUser = getCurrentUser();
        String role = currentUser.getUser().getUserRole();
        String authQ = currentUser.getUser().getAuthQuotation();
        boolean canEdit = "ROLE_ADMIN".equals(role) || "EDIT".equals(authQ);
        model.addAttribute("canEdit", canEdit);
        return "quotation/quotation-detail";
    }

    /** 견적서 인쇄 미리보기 */
    @GetMapping("/quotation/{id}/preview")
    public String previewQuotation(@PathVariable Long id,
                                   @RequestParam(required = false) Boolean seal,
                                   Model model) {
        checkViewAuth();
        QuotationDTO dto = quotationService.getQuotation(id);
        model.addAttribute("quotation", dto);

        // totalAmount = 품목 합계 원금 (rawTotal)
        long rawTotal = dto.getTotalAmount() != null ? dto.getTotalAmount() : 0;
        int rdUnit = dto.getRounddownUnit() != null ? dto.getRounddownUnit() : 1;
        boolean vatIncluded = dto.getVatIncluded() != null && dto.getVatIncluded();
        long supplyAmount, vatAmount, grandTotal;
        if (vatIncluded) {
            // VAT포함: 품목 합계가 곧 합계
            grandTotal = rawTotal;
            supplyAmount = 0;
            vatAmount = 0;
        } else {
            // VAT미포함: 소계 + 부가세 10% (절사 없이)
            supplyAmount = rawTotal;
            vatAmount = supplyAmount * 10 / 100;
            grandTotal = supplyAmount + vatAmount;
        }
        // 절사는 합계에만 적용
        grandTotal = roundDown(grandTotal, rdUnit);

        // 낙찰율 적용
        Double bidRate = dto.getBidRate();
        long totalBeforeBid = grandTotal;
        if (bidRate != null && bidRate > 0) {
            grandTotal = roundDown((long) Math.floor(totalBeforeBid * bidRate / 100.0), rdUnit);
        }

        model.addAttribute("vatIncluded", vatIncluded);
        model.addAttribute("supplyAmount", supplyAmount);
        model.addAttribute("vatAmount", vatAmount);
        model.addAttribute("totalBeforeBid", totalBeforeBid);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("grandTotalText", QuotationDTO.amountToKorean(grandTotal));
        model.addAttribute("bidRate", bidRate);
        model.addAttribute("rounddownUnit", rdUnit);

        // 도장: URL 파라미터(seal)로 오버라이드 가능, 없으면 견적서 저장값 사용
        boolean showSealValue = seal != null ? seal : (dto.getShowSeal() != null ? dto.getShowSeal() : true);
        model.addAttribute("showSeal", showSealValue);
        // S8-C qt-quotation-template-type-enum (2026-04-22): 매직넘버 → QuoteTemplateType
        model.addAttribute("templateType", dto.getTemplateType() != null ? dto.getTemplateType() : QuoteTemplateType.BASIC.getCode());

        logService.log(MenuName.QUOTATION, AccessActionType.PREVIEW, "견적서 미리보기: " + dto.getQuoteNumber());
        int tplType = dto.getTemplateType() != null ? dto.getTemplateType() : QuoteTemplateType.BASIC.getCode();
        return tplType == QuoteTemplateType.LABOR_COST_INTEGRATED.getCode()
                ? "quotation/quotation-preview2"
                : "quotation/quotation-preview";
    }

    /** 견적서 수정 폼 */
    @GetMapping("/quotation/{id}/edit")
    public String editQuotation(@PathVariable Long id, Model model) {
        checkEditAuth();
        model.addAttribute("quotation", quotationService.getQuotation(id));
        model.addAttribute("patterns", quotationService.getPatterns(null));
        addCreatorSelectData(model);
        return "quotation/quotation-form";
    }

    /** 견적번호 관리대장 */
    @GetMapping("/quotation/ledger")
    public String ledger(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category,
            Model model) {
        checkViewAuth();
        if (year == null) year = LocalDate.now().getYear();
        model.addAttribute("ledgerList", quotationService.getLedger(year, category));
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedCategory", category);
        return "quotation/quotation-ledger";
    }

    /** 품명 패턴 관리 */
    @GetMapping("/quotation/patterns")
    public String patterns(@RequestParam(required = false) String category, Model model) {
        checkViewAuth();
        model.addAttribute("patterns", quotationService.getPatterns(category));
        model.addAttribute("selectedCategory", category);
        model.addAttribute("isAdmin", isAdmin());
        return "quotation/pattern-list";
    }

    /** 비고 패턴 관리 */
    @GetMapping("/quotation/remarks-patterns")
    public String remarksPatterns(Model model) {
        checkViewAuth();
        return "quotation/remarks-pattern-list";
    }

    /** 노임단가 관리 */
    @GetMapping("/quotation/wage-rates")
    public String wageRates(@RequestParam(required = false) Integer year, Model model) {
        checkViewAuth();
        model.addAttribute("wageRates", quotationService.getWageRates(year));
        model.addAttribute("years", quotationService.getWageRateYears());
        model.addAttribute("selectedYear", year);
        return "quotation/wage-rate-list";
    }

    // ===== REST API =====

    /** 견적서 금액 일괄 재계산 (관리자 전용) */
    @PostMapping("/api/quotation/recalculate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recalculateAmounts() {
        checkEditAuth();
        int count = quotationService.recalculateAllGrandTotals();
        logService.log(MenuName.QUOTATION, AccessActionType.BATCH, "견적서 " + count + "건 금액 일괄 재계산 완료");
        return ResponseEntity.ok(Map.of("success", true, "count", count));
    }

    /** 견적서 목록 검색 (불러오기용) */
    @GetMapping("/api/quotation/search")
    @ResponseBody
    public List<Map<String, Object>> searchQuotations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year) {
        checkViewAuth();
        if (year == null) year = LocalDate.now().getYear();
        List<QuotationDTO> list = quotationService.getQuotations(category, year, null);
        return list.stream()
                .filter(q -> keyword == null || keyword.isBlank()
                        || (q.getProjectName() != null && q.getProjectName().contains(keyword))
                        || (q.getQuoteNumber() != null && q.getQuoteNumber().contains(keyword))
                        || (q.getRecipient() != null && q.getRecipient().contains(keyword)))
                .map(q -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("quoteId", q.getQuoteId());
                    m.put("quoteNumber", q.getQuoteNumber());
                    m.put("quoteDate", q.getQuoteDate() != null ? q.getQuoteDate().toString() : "");
                    m.put("category", q.getCategory());
                    m.put("projectName", q.getProjectName());
                    m.put("recipient", q.getRecipient());
                    m.put("grandTotal", q.getGrandTotal());
                    m.put("createdBy", q.getCreatedBy());
                    return m;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /** 견적서 상세 데이터 (불러오기용 JSON) */
    @GetMapping("/api/quotation/{id}")
    @ResponseBody
    public QuotationDTO getQuotationJson(@PathVariable Long id) {
        checkViewAuth();
        return quotationService.getQuotation(id);
    }

    /** 다음 견적번호 미리보기 */
    @GetMapping("/api/quotation/next-number")
    @ResponseBody
    public Map<String, String> nextNumber(
            @RequestParam String category,
            @RequestParam(required = false) Integer year) {
        checkViewAuth();
        if (year == null) year = LocalDate.now().getYear();
        return Map.of("nextNumber", quotationService.previewNextNumber(category, year));
    }

    /** 견적서 저장 (생성) */
    @PostMapping("/api/quotation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createQuotation(@RequestBody QuotationDTO dto) {
        checkEditAuth();
        try {
            // 관리자가 작성자를 지정한 경우 허용, 아니면 현재 사용자 이름
            if (!isAdmin() || dto.getCreatedBy() == null || dto.getCreatedBy().isBlank()) {
                dto.setCreatedBy(getCurrentUserName());
            }
            Quotation saved = quotationService.createQuotation(dto);
            logService.log(MenuName.QUOTATION, AccessActionType.CREATE, "견적서 발행: " + saved.getQuoteNumber() + " - " + saved.getProjectName());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", saved.getQuoteId(),
                    "quoteNumber", saved.getQuoteNumber(),
                    "message", "견적서가 발행되고 대장에 자동 등록되었습니다."
            ));
        } catch (Exception e) {
            log.error("견적서 생성 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /** 견적서 수정 */
    @PutMapping("/api/quotation/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuotation(
            @PathVariable Long id, @RequestBody QuotationDTO dto) {
        checkEditAuth();
        try {
            dto.setQuoteId(id);
            quotationService.updateQuotation(dto);
            logService.log(MenuName.QUOTATION, AccessActionType.UPDATE, "견적서 수정: ID " + id);
            return ResponseEntity.ok(Map.of("success", true, "message", "견적서가 수정되었습니다."));
        } catch (Exception e) {
            log.error("견적서 수정 실패", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 견적서 삭제 */
    @DeleteMapping("/api/quotation/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteQuotation(@PathVariable Long id) {
        checkEditAuth();
        try {
            quotationService.deleteQuotation(id);
            logService.log(MenuName.QUOTATION, AccessActionType.DELETE, "견적서 삭제: ID " + id);
            return ResponseEntity.ok(Map.of("success", true, "message", "견적서가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("견적서 삭제 실패", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 분류별 패턴 조회 (JSON) */
    @GetMapping("/api/quotation/patterns")
    @ResponseBody
    public List<ProductPattern> getPatterns(@RequestParam(required = false) String category) {
        checkViewAuth();
        return quotationService.getPatterns(category);
    }

    /** 패턴 저장 (신규) */
    @PostMapping("/api/quotation/pattern")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> savePattern(@RequestBody ProductPattern pattern) {
        checkEditAuth();
        try {
            ProductPattern saved = quotationService.savePattern(pattern);
            logService.log(MenuName.QUOTATION, AccessActionType.PATTERN_CRUD, "품명 패턴 등록: " + pattern.getProductName());
            return ResponseEntity.ok(Map.of("success", true, "patternId", saved.getPatternId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 패턴 수정 */
    @PutMapping("/api/quotation/pattern/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePattern(@PathVariable Long id, @RequestBody ProductPattern pattern) {
        checkEditAuth();
        try {
            pattern.setPatternId(id);
            quotationService.savePattern(pattern);
            logService.log(MenuName.QUOTATION, AccessActionType.PATTERN_CRUD, "품명 패턴 수정: " + pattern.getProductName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 패턴 삭제 */
    @DeleteMapping("/api/quotation/pattern/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deletePattern(@PathVariable Long id) {
        checkEditAuth();
        try {
            quotationService.deletePattern(id);
            logService.log(MenuName.QUOTATION, AccessActionType.PATTERN_CRUD, "품명 패턴 삭제: ID " + id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 전체 패턴 초기화 (관리자 전용) */
    @DeleteMapping("/api/quotation/patterns/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAllPatterns() {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "error", "관리자만 전체 초기화가 가능합니다."));
        }
        try {
            long count = quotationService.deleteAllPatterns();
            logService.log(MenuName.QUOTATION, AccessActionType.BATCH, "전체 패턴 초기화: " + count + "건 삭제");
            return ResponseEntity.ok(Map.of("success", true, "deleted", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 패턴 복사 (다른 분류로) */
    @PostMapping("/api/quotation/patterns/copy")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> copyPatterns(@RequestBody Map<String, Object> request) {
        checkEditAuth();
        try {
            @SuppressWarnings("unchecked")
            List<Number> ids = (List<Number>) request.get("patternIds");
            String targetCategory = (String) request.get("targetCategory");
            if (ids == null || ids.isEmpty() || targetCategory == null || targetCategory.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "패턴 ID와 대상 분류를 지정해주세요."));
            }
            int copied = quotationService.copyPatterns(ids.stream().map(Number::longValue).toList(), targetCategory);
            logService.log(MenuName.QUOTATION, AccessActionType.BATCH, copied + "건 패턴을 '" + targetCategory + "' 분류로 복사");
            return ResponseEntity.ok(Map.of("success", true, "copied", copied));
        } catch (Exception e) {
            log.error("패턴 복사 실패", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ===== SW 프로젝트 도입금액 조회 API =====

    /** 시스템명 목록 (중복 제거) */
    @GetMapping("/api/quotation/sw-projects/systems")
    @ResponseBody
    public List<String> getSwSystemNames() {
        checkViewAuth();
        return swProjectRepository.findAll().stream()
                .map(p -> p.getSysNm())
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** 특정 시스템의 클라이언트 목록 */
    @GetMapping("/api/quotation/sw-projects/clients")
    @ResponseBody
    public List<Map<String, Object>> getSwClients(@RequestParam String sysNm) {
        checkViewAuth();
        return swProjectRepository.findAll().stream()
                .filter(p -> sysNm.equals(p.getSysNm()) && p.getClient() != null && !p.getClient().isBlank())
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("projId", p.getProjId());
                    m.put("client", p.getClient());
                    m.put("swAmt", p.getSwAmt());
                    m.put("projNm", p.getProjNm());
                    m.put("year", p.getYear());
                    m.put("distNm", p.getDistNm());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // ===== 비고 패턴 REST API =====

    /** 비고 패턴 목록 (S3: DTO + renderedContent 포함) */
    @GetMapping("/api/quotation/remarks-patterns")
    @ResponseBody
    public List<com.swmanager.system.quotation.dto.RemarksPatternDto> getRemarksPatterns() {
        checkViewAuth();
        return quotationService.getRemarksPatterns();
    }

    /** 비고 패턴 저장 */
    @PostMapping("/api/quotation/remarks-pattern")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveRemarksPattern(
            @RequestBody com.swmanager.system.quotation.domain.RemarksPattern pattern) {
        checkEditAuth();
        try {
            com.swmanager.system.quotation.domain.RemarksPattern saved = quotationService.saveRemarksPattern(pattern);
            logService.log(MenuName.QUOTATION, AccessActionType.PATTERN_CRUD, "비고 패턴 등록: " + pattern.getPatternName());
            return ResponseEntity.ok(Map.of("success", true, "patternId", saved.getPatternId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 비고 패턴 수정 */
    @PutMapping("/api/quotation/remarks-pattern/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateRemarksPattern(
            @PathVariable Long id,
            @RequestBody com.swmanager.system.quotation.domain.RemarksPattern pattern) {
        checkEditAuth();
        try {
            pattern.setPatternId(id);
            quotationService.saveRemarksPattern(pattern);
            logService.log(MenuName.QUOTATION, AccessActionType.PATTERN_CRUD, "비고 패턴 수정: " + pattern.getPatternName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 비고 패턴 삭제 */
    @DeleteMapping("/api/quotation/remarks-pattern/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteRemarksPattern(@PathVariable Long id) {
        checkEditAuth();
        try {
            quotationService.deleteRemarksPattern(id);
            logService.log(MenuName.QUOTATION, AccessActionType.PATTERN_CRUD, "비고 패턴 삭제: ID " + id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ===== 노임단가 REST API =====

    /** 노임단가 목록 (JSON) */
    @GetMapping("/api/quotation/wage-rates")
    @ResponseBody
    public List<com.swmanager.system.quotation.domain.WageRate> getWageRates(
            @RequestParam(required = false) Integer year) {
        checkViewAuth();
        return quotationService.getWageRates(year);
    }

    /** 노임단가 연도 목록 */
    @GetMapping("/api/quotation/wage-rates/years")
    @ResponseBody
    public List<Integer> getWageRateYears() {
        checkViewAuth();
        return quotationService.getWageRateYears();
    }

    /** 노임단가 저장 */
    @PostMapping("/api/quotation/wage-rate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveWageRate(
            @RequestBody com.swmanager.system.quotation.domain.WageRate wageRate) {
        checkEditAuth();
        try {
            com.swmanager.system.quotation.domain.WageRate saved = quotationService.saveWageRate(wageRate);
            logService.log(MenuName.QUOTATION, AccessActionType.WAGE_CRUD, wageRate.getYear() + "년 " + wageRate.getGradeName() + " 노임단가 등록");
            return ResponseEntity.ok(Map.of("success", true, "wageId", saved.getWageId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 노임단가 수정 */
    @PutMapping("/api/quotation/wage-rate/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateWageRate(
            @PathVariable Long id, @RequestBody com.swmanager.system.quotation.domain.WageRate wageRate) {
        checkEditAuth();
        try {
            wageRate.setWageId(id);
            quotationService.saveWageRate(wageRate);
            logService.log(MenuName.QUOTATION, AccessActionType.WAGE_CRUD, wageRate.getYear() + "년 " + wageRate.getGradeName() + " 노임단가 수정");
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 노임단가 삭제 */
    @DeleteMapping("/api/quotation/wage-rate/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteWageRate(@PathVariable Long id) {
        checkEditAuth();
        try {
            quotationService.deleteWageRate(id);
            logService.log(MenuName.QUOTATION, AccessActionType.WAGE_CRUD, "노임단가 삭제: ID " + id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 대장 데이터 (JSON) */
    @GetMapping("/api/quotation/ledger")
    @ResponseBody
    public List<QuotationLedger> getLedgerApi(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category) {
        checkViewAuth();
        if (year == null) year = LocalDate.now().getYear();
        return quotationService.getLedger(year, category);
    }

    /** 통계 (대시보드 카드용) */
    @GetMapping("/api/quotation/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        checkViewAuth();
        return quotationService.getStats();
    }
}
