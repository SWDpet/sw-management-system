package com.swmanager.system.quotation.service;

import com.swmanager.system.quotation.domain.*;
import com.swmanager.system.quotation.dto.QuotationDTO;
import com.swmanager.system.quotation.dto.RemarksPatternDto;
import com.swmanager.system.quotation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final QuotationLedgerRepository ledgerRepository;
    private final ProductPatternRepository patternRepository;
    private final RemarksPatternRepository remarksPatternRepository;
    private final WageRateRepository wageRateRepository;
    private final QuoteNumberSeqRepository seqRepository;
    private final RemarksRenderer remarksRenderer;  // S3

    /** ROUNDDOWN 절사 헬퍼 */
    private static long roundDown(long value, int unit) {
        if (unit <= 1) return value;
        return (value / unit) * unit;
    }

    /** 최종 합계 계산 (절사는 최종 합계에만 적용) */
    private static long calcGrandTotal(long rawTotal, int rdUnit, boolean vatIncluded, Double bidRate) {
        long total;
        if (vatIncluded) {
            // VAT포함: 품목 합계가 곧 합계
            total = rawTotal;
        } else {
            // VAT미포함: 소계 + 부가세 10% (절사 없이 계산)
            long supply = rawTotal;
            long vat = supply * 10 / 100;
            total = supply + vat;
        }
        // 낙찰율 적용
        if (bidRate != null && bidRate > 0) {
            total = (long) Math.floor(total * bidRate / 100.0);
        }
        // 절사는 최종 합계에만 적용
        total = roundDown(total, rdUnit);
        return total;
    }

    // ===== 견적번호 생성 =====

    @Transactional
    public String generateQuoteNumber(String category, int year) {
        Map<String, String> codeMap = Map.of("용역", "SQ", "제품", "PQ", "유지보수", "MQ");
        String code = codeMap.getOrDefault(category, "EQ");

        QuoteNumberSeq seq = seqRepository.findByYearAndCategory(year, category)
                .orElseGet(() -> {
                    QuoteNumberSeq newSeq = new QuoteNumberSeq();
                    newSeq.setYear(year);
                    newSeq.setCategory(category);
                    newSeq.setLastSeq(0);
                    return newSeq;
                });

        seq.setLastSeq(seq.getLastSeq() + 1);
        seqRepository.save(seq);

        return String.format("UIT - %s - %d - %03d", code, year, seq.getLastSeq());
    }

    /** 다음 번호 미리보기 (실제 증가 없이) */
    public String previewNextNumber(String category, int year) {
        Map<String, String> codeMap = Map.of("용역", "SQ", "제품", "PQ", "유지보수", "MQ");
        String code = codeMap.getOrDefault(category, "EQ");
        int nextSeq = seqRepository.findByYearAndCategory(year, category)
                .map(s -> s.getLastSeq() + 1)
                .orElse(1);
        return String.format("UIT - %s - %d - %03d", code, year, nextSeq);
    }

    // ===== 견적서 CRUD =====

    @Transactional
    public Quotation createQuotation(QuotationDTO dto) {
        Quotation q = dto.toEntity();

        // 견적번호 자동 생성
        int year = q.getQuoteDate().getYear();
        String quoteNumber = generateQuoteNumber(q.getCategory(), year);
        q.setQuoteNumber(quoteNumber);

        // 금액 계산
        long rawTotal = dto.getItems().stream()
                .mapToLong(i -> i.getAmount() != null ? i.getAmount() : 0L)
                .sum();
        q.setTotalAmount(rawTotal); // 품목 합계 원금
        q.setTotalAmountText(QuotationDTO.amountToKorean(rawTotal));

        // 최종 합계 (ROUNDDOWN + VAT + 낙찰율 반영, 견적서 출력물과 동일)
        int rdUnit = q.getRounddownUnit() != null ? q.getRounddownUnit() : 1;
        boolean vatIncluded = q.getVatIncluded() != null && q.getVatIncluded();
        q.setGrandTotal(calcGrandTotal(rawTotal, rdUnit, vatIncluded, q.getBidRate()));
        q.setStatus("발행완료");

        // 품목 추가
        for (QuotationDTO.ItemDTO itemDTO : dto.getItems()) {
            QuotationItem item = itemDTO.toEntity();
            q.addItem(item);
        }

        Quotation saved = quotationRepository.save(q);

        // 대장 자동 등록
        registerLedger(saved);

        return saved;
    }

    @Transactional(readOnly = true)
    public QuotationDTO getQuotation(Long id) {
        Quotation q = quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("견적서를 찾을 수 없습니다: " + id));
        return QuotationDTO.fromEntity(q);
    }

    @Transactional(readOnly = true)
    public List<QuotationDTO> getQuotations(String category, Integer year, String status) {
        List<Quotation> list = quotationRepository.findByFilter(
                (category != null && !category.isEmpty()) ? category : null,
                year,
                (status != null && !status.isEmpty()) ? status : null
        );
        return list.stream().map(QuotationDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public Quotation updateQuotation(QuotationDTO dto) {
        Quotation q = quotationRepository.findById(dto.getQuoteId())
                .orElseThrow(() -> new RuntimeException("견적서를 찾을 수 없습니다"));

        q.setProjectName(dto.getProjectName());
        q.setRecipient(dto.getRecipient());
        q.setReferenceTo(dto.getReferenceTo());
        q.setVatIncluded(dto.getVatIncluded());
        q.setBidRate(dto.getBidRate());
        q.setRounddownUnit(dto.getRounddownUnit() != null ? dto.getRounddownUnit() : q.getRounddownUnit());
        q.setShowSeal(dto.getShowSeal() != null ? dto.getShowSeal() : q.getShowSeal());
        q.setTemplateType(dto.getTemplateType() != null ? dto.getTemplateType() : q.getTemplateType());
        q.setRemarks(dto.getRemarks());
        q.setStatus(dto.getStatus() != null ? dto.getStatus() : q.getStatus());
        // 관리자가 작성자를 변경한 경우
        if (dto.getCreatedBy() != null && !dto.getCreatedBy().isBlank()) {
            q.setCreatedBy(dto.getCreatedBy());
        }

        // 품목 교체
        q.clearItems();
        long rawTotal = 0;
        for (QuotationDTO.ItemDTO itemDTO : dto.getItems()) {
            QuotationItem item = itemDTO.toEntity();
            q.addItem(item);
            rawTotal += item.getAmount() != null ? item.getAmount() : 0L;
        }
        q.setTotalAmount(rawTotal); // 품목 합계 원금
        q.setTotalAmountText(QuotationDTO.amountToKorean(rawTotal));

        // 최종 합계 (ROUNDDOWN + VAT + 낙찰율 반영)
        int rdUnit = q.getRounddownUnit() != null ? q.getRounddownUnit() : 1;
        boolean vatIncluded = q.getVatIncluded() != null && q.getVatIncluded();
        q.setGrandTotal(calcGrandTotal(rawTotal, rdUnit, vatIncluded, q.getBidRate()));

        Quotation saved = quotationRepository.save(q);

        // 대장도 업데이트
        ledgerRepository.findByQuoteId(saved.getQuoteId()).ifPresent(ledger -> {
            ledger.setProjectName(saved.getProjectName());
            ledger.setTotalAmount(saved.getTotalAmount());
            ledger.setGrandTotal(saved.getGrandTotal());
            ledger.setRecipient(saved.getRecipient());
            ledger.setReferenceTo(saved.getReferenceTo());
            ledgerRepository.save(ledger);
        });

        return saved;
    }

    @Transactional
    public void deleteQuotation(Long id) {
        ledgerRepository.findByQuoteId(id).ifPresent(ledgerRepository::delete);
        quotationRepository.deleteById(id);
    }

    // ===== 대장 =====

    private void registerLedger(Quotation q) {
        int year = q.getQuoteDate().getYear();
        int nextNo = ledgerRepository.findMaxLedgerNo(year, q.getCategory()) + 1;

        QuotationLedger ledger = new QuotationLedger();
        ledger.setQuoteId(q.getQuoteId());
        ledger.setLedgerNo(nextNo);
        ledger.setYear(year);
        ledger.setCategory(q.getCategory());
        ledger.setQuoteNumber(q.getQuoteNumber());
        ledger.setQuoteDate(q.getQuoteDate());
        ledger.setProjectName(q.getProjectName());
        ledger.setTotalAmount(q.getTotalAmount());
        ledger.setGrandTotal(q.getGrandTotal());
        ledger.setRecipient(q.getRecipient());
        ledger.setReferenceTo(q.getReferenceTo());
        ledger.setCreatedBy(q.getCreatedBy());
        ledgerRepository.save(ledger);
    }

    @Transactional(readOnly = true)
    public List<QuotationLedger> getLedger(Integer year, String category) {
        if (category != null && !category.isEmpty()) {
            return ledgerRepository.findByYearAndCategoryOrderByLedgerNoAsc(year, category);
        }
        return ledgerRepository.findByYearOrderByLedgerNoAsc(year);
    }

    // ===== 패턴 =====

    @Transactional(readOnly = true)
    public List<ProductPattern> getPatterns(String category) {
        if (category != null && !category.isEmpty()) {
            return patternRepository.findByCategoryOrderByPatternGroupAscProductNameAsc(category);
        }
        return patternRepository.findAllByOrderByCategoryAscPatternGroupAscProductNameAsc();
    }

    @Transactional
    public ProductPattern savePattern(ProductPattern pattern) {
        return patternRepository.save(pattern);
    }

    @Transactional
    public void deletePattern(Long patternId) {
        patternRepository.deleteById(patternId);
    }

    @Transactional
    public long deleteAllPatterns() {
        long count = patternRepository.count();
        patternRepository.deleteAll();
        return count;
    }

    @Transactional
    public int copyPatterns(List<Long> patternIds, String targetCategory) {
        int count = 0;
        for (Long id : patternIds) {
            ProductPattern src = patternRepository.findById(id).orElse(null);
            if (src == null) continue;
            ProductPattern copy = new ProductPattern();
            copy.setCategory(targetCategory);
            copy.setPatternGroup(src.getPatternGroup());
            copy.setProductName(src.getProductName());
            copy.setDefaultUnit(src.getDefaultUnit());
            copy.setDefaultUnitPrice(src.getDefaultUnitPrice());
            copy.setDescription(src.getDescription());
            copy.setSubItems(src.getSubItems());
            copy.setCalcType(src.getCalcType());
            copy.setOverheadRate(src.getOverheadRate());
            copy.setTechFeeRate(src.getTechFeeRate());
            copy.setUsageCount(0);
            patternRepository.save(copy);
            count++;
        }
        return count;
    }

    // ===== 비고 패턴 =====

    /**
     * 비고 패턴 목록 (S3: DTO + renderedContent 추가).
     *  - 각 패턴의 user_id 기반 placeholder 치환 결과를 함께 반환.
     */
    @Transactional(readOnly = true)
    public List<RemarksPatternDto> getRemarksPatterns() {
        return remarksPatternRepository.findAllByOrderBySortOrderAscPatternNameAsc().stream()
                .map(p -> {
                    RemarksPatternDto dto = RemarksPatternDto.from(p);
                    dto.setRenderedContent(remarksRenderer.render(p.getContent(), p.getUserId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public RemarksPattern saveRemarksPattern(RemarksPattern pattern) {
        return remarksPatternRepository.save(pattern);
    }

    @Transactional
    public void deleteRemarksPattern(Long patternId) {
        remarksPatternRepository.deleteById(patternId);
    }

    // ===== 노임단가 =====

    @Transactional(readOnly = true)
    public List<WageRate> getWageRates(Integer year) {
        if (year != null) {
            return wageRateRepository.findByYearOrderByGradeNameAsc(year);
        }
        return wageRateRepository.findAllByOrderByYearDescGradeNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Integer> getWageRateYears() {
        return wageRateRepository.findDistinctYears();
    }

    @Transactional(readOnly = true)
    public Optional<WageRate> getWageRate(Integer year, String gradeName) {
        return wageRateRepository.findByYearAndGradeName(year, gradeName);
    }

    @Transactional
    public WageRate saveWageRate(WageRate wageRate) {
        // Check for existing entry and update
        Optional<WageRate> existing = wageRateRepository.findByYearAndGradeName(
                wageRate.getYear(), wageRate.getGradeName());
        if (existing.isPresent() && (wageRate.getWageId() == null || !wageRate.getWageId().equals(existing.get().getWageId()))) {
            WageRate ex = existing.get();
            ex.setDailyRate(wageRate.getDailyRate());
            ex.setMonthlyRate(wageRate.getMonthlyRate());
            ex.setHourlyRate(wageRate.getHourlyRate());
            ex.setDescription(wageRate.getDescription());
            return wageRateRepository.save(ex);
        }
        return wageRateRepository.save(wageRate);
    }

    @Transactional
    public void deleteWageRate(Long wageId) {
        wageRateRepository.deleteById(wageId);
    }

    // ===== grandTotal 일괄 계산 (기존 데이터 마이그레이션용) =====

    @Transactional
    public int recalculateAllGrandTotals() {
        List<Quotation> all = quotationRepository.findAll();
        int count = 0;
        for (Quotation q : all) {
            long rawTotal = q.getItems().stream()
                    .mapToLong(i -> i.getAmount() != null ? i.getAmount() : 0L)
                    .sum();
            int rdUnit = q.getRounddownUnit() != null ? q.getRounddownUnit() : 1;
            boolean vatInc = q.getVatIncluded() != null && q.getVatIncluded();

            // totalAmount를 품목 합계 원금으로 복원
            q.setTotalAmount(rawTotal);
            q.setTotalAmountText(QuotationDTO.amountToKorean(rawTotal));
            // grandTotal 계산 (낙찰율 포함)
            q.setGrandTotal(calcGrandTotal(rawTotal, rdUnit, vatInc, q.getBidRate()));
            quotationRepository.save(q);

            // 대장도 동기화
            ledgerRepository.findByQuoteId(q.getQuoteId()).ifPresent(ledger -> {
                ledger.setTotalAmount(q.getTotalAmount());
                ledger.setGrandTotal(q.getGrandTotal());
                ledgerRepository.save(ledger);
            });
            count++;
        }
        return count;
    }

    // ===== 통계 =====

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        long total = quotationRepository.count();
        return Map.of(
                "total", total,
                "용역_count", quotationRepository.countByCategory("용역"),
                "용역_amount", quotationRepository.sumAmountByCategory("용역"),
                "제품_count", quotationRepository.countByCategory("제품"),
                "제품_amount", quotationRepository.sumAmountByCategory("제품"),
                "유지보수_count", quotationRepository.countByCategory("유지보수"),
                "유지보수_amount", quotationRepository.sumAmountByCategory("유지보수")
        );
    }
}
