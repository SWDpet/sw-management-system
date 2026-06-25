package com.swmanager.system.quotation.service;

import com.swmanager.system.constant.enums.QtCategory;
import com.swmanager.system.quotation.domain.*;
import com.swmanager.system.quotation.dto.QuotationDTO;
import com.swmanager.system.quotation.dto.RemarksPatternDto;
import com.swmanager.system.quotation.repository.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * QuotationService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 *
 * 핵심 비즈니스 로직 고정: 견적번호 코드맵·시퀀스, calcGrandTotal(VAT·낙찰율·절사),
 * 대장 동기화, 패턴 복사, 노임단가 upsert.
 */
class QuotationServiceTest {

    private final QuotationRepository quotationRepository = mock(QuotationRepository.class);
    private final QuotationLedgerRepository ledgerRepository = mock(QuotationLedgerRepository.class);
    private final ProductPatternRepository patternRepository = mock(ProductPatternRepository.class);
    private final RemarksPatternRepository remarksPatternRepository = mock(RemarksPatternRepository.class);
    private final WageRateRepository wageRateRepository = mock(WageRateRepository.class);
    private final QuoteNumberSeqRepository seqRepository = mock(QuoteNumberSeqRepository.class);
    private final RemarksRenderer remarksRenderer = mock(RemarksRenderer.class);

    private final QuotationService service = new QuotationService(
            quotationRepository, ledgerRepository, patternRepository,
            remarksPatternRepository, wageRateRepository, seqRepository, remarksRenderer);

    private void stubSeqEmpty() {
        when(seqRepository.findByYearAndCategory(anyInt(), anyString())).thenReturn(Optional.empty());
        when(seqRepository.save(any(QuoteNumberSeq.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ===== generateQuoteNumber =====

    @Test
    void generateQuoteNumber_categoryCodeMap_andNewSeq() {
        stubSeqEmpty();
        assertThat(service.generateQuoteNumber("용역", 2026)).isEqualTo("UIT - SQ - 2026 - 001");
        assertThat(service.generateQuoteNumber("제품", 2026)).isEqualTo("UIT - PQ - 2026 - 001");
        assertThat(service.generateQuoteNumber("유지보수", 2026)).isEqualTo("UIT - MQ - 2026 - 001");
        assertThat(service.generateQuoteNumber("알수없음", 2026)).isEqualTo("UIT - EQ - 2026 - 001");
        verify(seqRepository, times(4)).save(any(QuoteNumberSeq.class));
    }

    @Test
    void generateQuoteNumber_existingSeq_increments() {
        QuoteNumberSeq seq = new QuoteNumberSeq();
        seq.setYear(2026); seq.setCategory("용역"); seq.setLastSeq(5);
        when(seqRepository.findByYearAndCategory(2026, "용역")).thenReturn(Optional.of(seq));
        when(seqRepository.save(any(QuoteNumberSeq.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(service.generateQuoteNumber("용역", 2026)).isEqualTo("UIT - SQ - 2026 - 006");
        assertThat(seq.getLastSeq()).isEqualTo(6);
    }

    @Test
    void previewNextNumber_doesNotIncrementOrSave() {
        when(seqRepository.findByYearAndCategory(2026, "제품")).thenReturn(Optional.empty());
        assertThat(service.previewNextNumber("제품", 2026)).isEqualTo("UIT - PQ - 2026 - 001");

        QuoteNumberSeq seq = new QuoteNumberSeq(); seq.setLastSeq(9);
        when(seqRepository.findByYearAndCategory(2026, "용역")).thenReturn(Optional.of(seq));
        assertThat(service.previewNextNumber("용역", 2026)).isEqualTo("UIT - SQ - 2026 - 010");

        verify(seqRepository, never()).save(any());
    }

    // ===== createQuotation / calcGrandTotal =====

    private QuotationDTO dto(long itemAmount, boolean vatIncluded, Double bidRate, Integer rdUnit) {
        return QuotationDTO.builder()
                .category("용역")
                .quoteDate(LocalDate.of(2026, 1, 15))
                .vatIncluded(vatIncluded)
                .bidRate(bidRate)
                .rounddownUnit(rdUnit)
                .items(List.of(QuotationDTO.ItemDTO.builder().amount(itemAmount).build()))
                .build();
    }

    private Quotation createWith(QuotationDTO dto) {
        stubSeqEmpty();
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> i.getArgument(0));
        when(ledgerRepository.findMaxLedgerNo(anyInt(), anyString())).thenReturn(0);
        when(ledgerRepository.save(any(QuotationLedger.class))).thenAnswer(i -> i.getArgument(0));
        return service.createQuotation(dto);
    }

    @Test
    void createQuotation_vatExcluded_adds10Percent() {
        Quotation q = createWith(dto(1_000_000L, false, null, 1));
        assertThat(q.getTotalAmount()).isEqualTo(1_000_000L);     // 품목 합계 원금
        assertThat(q.getGrandTotal()).isEqualTo(1_100_000L);      // +VAT 10%
        assertThat(q.getQuoteNumber()).isEqualTo("UIT - SQ - 2026 - 001");
        verify(ledgerRepository).save(any(QuotationLedger.class)); // 대장 등록
    }

    @Test
    void createQuotation_vatIncluded_equalsRaw() {
        Quotation q = createWith(dto(1_000_000L, true, null, 1));
        assertThat(q.getGrandTotal()).isEqualTo(1_000_000L);
    }

    @Test
    void createQuotation_bidRate_floored() {
        Quotation q = createWith(dto(1_000_000L, true, 90.0, 1));
        assertThat(q.getGrandTotal()).isEqualTo(900_000L);        // floor(1,000,000 * 90/100)
    }

    @Test
    void createQuotation_rounddown_appliedToFinalTotal() {
        Quotation q = createWith(dto(1_234_567L, true, null, 10_000));
        assertThat(q.getGrandTotal()).isEqualTo(1_230_000L);      // roundDown(.,10000)
    }

    // ===== update / get / delete =====

    @Test
    void updateQuotation_recalculatesAndSyncsLedger() {
        Quotation existing = new Quotation();
        existing.setQuoteId(7L);
        when(quotationRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> i.getArgument(0));
        QuotationLedger ledger = new QuotationLedger();
        when(ledgerRepository.findByQuoteId(7L)).thenReturn(Optional.of(ledger));

        QuotationDTO d = dto(2_000_000L, false, null, 1);
        d.setQuoteId(7L);
        Quotation saved = service.updateQuotation(d);

        assertThat(saved.getTotalAmount()).isEqualTo(2_000_000L);
        assertThat(saved.getGrandTotal()).isEqualTo(2_200_000L);  // +VAT
        assertThat(ledger.getGrandTotal()).isEqualTo(2_200_000L); // 대장 동기화
        verify(ledgerRepository).save(ledger);
    }

    @Test
    void updateQuotation_notFound_throws() {
        when(quotationRepository.findById(99L)).thenReturn(Optional.empty());
        QuotationDTO d = dto(1L, true, null, 1);
        d.setQuoteId(99L);
        assertThatThrownBy(() -> service.updateQuotation(d)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void getQuotation_notFound_throws() {
        when(quotationRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getQuotation(404L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    void deleteQuotation_removesLedgerAndQuotation() {
        QuotationLedger ledger = new QuotationLedger();
        when(ledgerRepository.findByQuoteId(3L)).thenReturn(Optional.of(ledger));
        service.deleteQuotation(3L);
        verify(ledgerRepository).delete(ledger);
        verify(quotationRepository).deleteById(3L);
    }

    // ===== copyPatterns =====

    @Test
    void copyPatterns_copiesFields_skipsMissing_returnsCount() {
        ProductPattern src = new ProductPattern();
        src.setPatternGroup("그룹A");
        src.setProductName("제품A");
        src.setDefaultUnitPrice(5000L);
        when(patternRepository.findById(1L)).thenReturn(Optional.of(src));
        when(patternRepository.findById(2L)).thenReturn(Optional.empty()); // skip
        when(patternRepository.save(any(ProductPattern.class))).thenAnswer(i -> i.getArgument(0));

        int count = service.copyPatterns(List.of(1L, 2L), "제품");

        assertThat(count).isEqualTo(1);
        verify(patternRepository, times(1)).save(argThat(p ->
                "제품".equals(p.getCategory())
                        && "제품A".equals(p.getProductName())
                        && Long.valueOf(5000L).equals(p.getDefaultUnitPrice())
                        && Integer.valueOf(0).equals(p.getUsageCount())));
    }

    // ===== saveWageRate (upsert) =====

    @Test
    void saveWageRate_updatesExistingWhenDifferentId() {
        WageRate existing = new WageRate();
        existing.setWageId(5L); existing.setYear(2026); existing.setGradeName("중급");
        when(wageRateRepository.findByYearAndGradeName(2026, "중급")).thenReturn(Optional.of(existing));
        when(wageRateRepository.save(any(WageRate.class))).thenAnswer(i -> i.getArgument(0));

        WageRate input = new WageRate();
        input.setYear(2026); input.setGradeName("중급"); input.setDailyRate(200_000L); // wageId null
        service.saveWageRate(input);

        assertThat(existing.getDailyRate()).isEqualTo(200_000L); // 기존 행 갱신
        verify(wageRateRepository).save(existing);
    }

    @Test
    void saveWageRate_insertsWhenNew() {
        when(wageRateRepository.findByYearAndGradeName(2027, "초급")).thenReturn(Optional.empty());
        when(wageRateRepository.save(any(WageRate.class))).thenAnswer(i -> i.getArgument(0));
        WageRate input = new WageRate();
        input.setYear(2027); input.setGradeName("초급");
        service.saveWageRate(input);
        verify(wageRateRepository).save(input); // 입력 그대로 저장
    }

    // ===== recalculateAllGrandTotals (마이그레이션 재계산 + 대장 동기화) =====

    @Test
    void recalculateAllGrandTotals_recomputesAndSyncsLedger() {
        Quotation q = new Quotation();
        q.setQuoteId(1L);
        q.setVatIncluded(false);          // +VAT 10%
        q.setRounddownUnit(1);
        QuotationItem it = new QuotationItem(); it.setAmount(1_000_000L);
        q.setItems(new ArrayList<>(List.of(it)));
        when(quotationRepository.findAll()).thenReturn(List.of(q));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> i.getArgument(0));
        QuotationLedger ledger = new QuotationLedger();
        when(ledgerRepository.findByQuoteId(1L)).thenReturn(Optional.of(ledger));
        when(ledgerRepository.save(any(QuotationLedger.class))).thenAnswer(i -> i.getArgument(0));

        int count = service.recalculateAllGrandTotals();

        assertThat(count).isEqualTo(1);
        assertThat(q.getTotalAmount()).isEqualTo(1_000_000L);     // 품목 합계 원금 복원
        assertThat(q.getGrandTotal()).isEqualTo(1_100_000L);      // calcGrandTotal: +VAT
        verify(quotationRepository).save(q);
        assertThat(ledger.getTotalAmount()).isEqualTo(1_000_000L); // 대장 동기화
        assertThat(ledger.getGrandTotal()).isEqualTo(1_100_000L);
        verify(ledgerRepository).save(ledger);
    }

    @Test
    void recalculateAllGrandTotals_noLedger_skipsLedgerSave() {
        Quotation q = new Quotation();
        q.setQuoteId(2L);
        q.setVatIncluded(true);           // VAT 포함 → 원금
        q.setRounddownUnit(1);
        QuotationItem it = new QuotationItem(); it.setAmount(500_000L);
        q.setItems(new ArrayList<>(List.of(it)));
        when(quotationRepository.findAll()).thenReturn(List.of(q));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> i.getArgument(0));
        when(ledgerRepository.findByQuoteId(2L)).thenReturn(Optional.empty());

        int count = service.recalculateAllGrandTotals();

        assertThat(count).isEqualTo(1);
        assertThat(q.getGrandTotal()).isEqualTo(500_000L);        // vatIncluded → raw
        verify(quotationRepository).save(q);
        verify(ledgerRepository, never()).save(any());
    }

    // ===== getStats =====

    @Test
    void getStats_returnsTotalAndCategoryCountsAmounts() {
        when(quotationRepository.count()).thenReturn(10L);
        when(quotationRepository.countByCategory("용역")).thenReturn(3L);
        when(quotationRepository.sumAmountByCategory("용역")).thenReturn(300L);
        when(quotationRepository.countByCategory("제품")).thenReturn(2L);
        when(quotationRepository.sumAmountByCategory("제품")).thenReturn(200L);
        when(quotationRepository.countByCategory("유지보수")).thenReturn(5L);
        when(quotationRepository.sumAmountByCategory("유지보수")).thenReturn(500L);

        Map<String, Object> stats = service.getStats();

        assertThat(stats)
                .containsEntry("total", 10L)
                .containsEntry(QtCategory.SERVICE.getLabel() + "_count", 3L)
                .containsEntry(QtCategory.SERVICE.getLabel() + "_amount", 300L)
                .containsEntry(QtCategory.PRODUCT.getLabel() + "_count", 2L)
                .containsEntry(QtCategory.MAINTENANCE.getLabel() + "_amount", 500L);
    }

    // ===== getRemarksPatterns =====

    @Test
    void getRemarksPatterns_mapsToDto_withRenderedContent() {
        RemarksPattern p = new RemarksPattern();
        p.setContent("원본 {name}");
        p.setUserId(7L);
        when(remarksPatternRepository.findAllByOrderBySortOrderAscPatternNameAsc()).thenReturn(List.of(p));
        when(remarksRenderer.render(eq("원본 {name}"), eq(7L))).thenReturn("렌더링 결과");

        List<RemarksPatternDto> out = service.getRemarksPatterns();

        assertThat(out).hasSize(1);
        assertThat(out.get(0).getRenderedContent()).isEqualTo("렌더링 결과");
    }

    // ===== getQuotations (필터 위임 + 매핑) =====

    @Test
    void getQuotations_delegatesFilter_normalizesBlankToNull() {
        Quotation q = new Quotation(); q.setQuoteId(1L); q.setCategory("용역");
        when(quotationRepository.findByFilter("용역", 2026, "작성")).thenReturn(List.of(q));
        assertThat(service.getQuotations("용역", 2026, "작성")).hasSize(1);

        when(quotationRepository.findByFilter(null, 2026, null)).thenReturn(List.of());
        service.getQuotations("", 2026, "");           // 빈문자 → null 정규화
        verify(quotationRepository).findByFilter(null, 2026, null);
    }

    // ===== 단순 위임 (조회/저장/삭제) =====

    @Test
    void simpleDelegates_readWriteDelete() {
        // getLedger: category 유/무 2분기
        when(ledgerRepository.findByYearAndCategoryOrderByLedgerNoAsc(2026, "용역")).thenReturn(List.of());
        assertThat(service.getLedger(2026, "용역")).isEmpty();
        when(ledgerRepository.findByYearOrderByLedgerNoAsc(2026)).thenReturn(List.of());
        assertThat(service.getLedger(2026, null)).isEmpty();

        // getPatterns: category 유/무 2분기
        when(patternRepository.findByCategoryOrderByPatternGroupAscProductNameAsc("제품")).thenReturn(List.of());
        assertThat(service.getPatterns("제품")).isEmpty();
        when(patternRepository.findAllByOrderByCategoryAscPatternGroupAscProductNameAsc()).thenReturn(List.of());
        assertThat(service.getPatterns(null)).isEmpty();

        // getWageRates: year 유/무 2분기 + getWageRateYears
        when(wageRateRepository.findByYearOrderByGradeNameAsc(2026)).thenReturn(List.of());
        assertThat(service.getWageRates(2026)).isEmpty();
        when(wageRateRepository.findAllByOrderByYearDescGradeNameAsc()).thenReturn(List.of());
        assertThat(service.getWageRates(null)).isEmpty();
        when(wageRateRepository.findDistinctYears()).thenReturn(List.of(2026));
        assertThat(service.getWageRateYears()).containsExactly(2026);

        // save 위임
        ProductPattern pp = new ProductPattern();
        when(patternRepository.save(pp)).thenReturn(pp);
        assertThat(service.savePattern(pp)).isSameAs(pp);
        RemarksPattern rp = new RemarksPattern();
        when(remarksPatternRepository.save(rp)).thenReturn(rp);
        assertThat(service.saveRemarksPattern(rp)).isSameAs(rp);

        // deleteAllPatterns: count 반환 + deleteAll
        when(patternRepository.count()).thenReturn(4L);
        assertThat(service.deleteAllPatterns()).isEqualTo(4L);
        verify(patternRepository).deleteAll();

        // delete 위임 verify
        service.deletePattern(1L);       verify(patternRepository).deleteById(1L);
        service.deleteRemarksPattern(2L); verify(remarksPatternRepository).deleteById(2L);
        service.deleteWageRate(3L);       verify(wageRateRepository).deleteById(3L);
    }
}
