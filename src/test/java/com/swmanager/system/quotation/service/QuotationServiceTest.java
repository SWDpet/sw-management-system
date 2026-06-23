package com.swmanager.system.quotation.service;

import com.swmanager.system.quotation.domain.*;
import com.swmanager.system.quotation.dto.QuotationDTO;
import com.swmanager.system.quotation.repository.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
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
}
