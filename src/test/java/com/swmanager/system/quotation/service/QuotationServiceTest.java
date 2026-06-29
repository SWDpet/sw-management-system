package com.swmanager.system.quotation.service;

import com.swmanager.system.constant.enums.QtCategory;
import com.swmanager.system.quotation.domain.*;
import com.swmanager.system.quotation.dto.QuotationDTO;
import com.swmanager.system.quotation.dto.RemarksPatternDto;
import com.swmanager.system.quotation.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * QuotationService 단위테스트 (커버리지/뮤테이션 상향 beyond-A, mock 기반·운영DB 무관).
 *
 * 핵심 비즈니스 로직 고정: 견적번호 코드맵·시퀀스, calcGrandTotal(VAT·낙찰율·절사·경계),
 * 대장 자동등록/동기화(전 필드), 견적 생성/수정(전 필드·조건부 기본값 양분기), 패턴 복사(전 필드),
 * 노임단가 upsert(전 필드·반환), 조회 분기(비빈 distinct 데이터).
 * 약한 검증(호출만)·빈 리스트 stub 을 제거해 뮤테이션 생존을 죽인다(ArgumentCaptor + 효과 단언).
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

    /** new-seq 람다(orElseGet)가 year/category/lastSeq 를 올바로 세팅하는지 캡처 단언(VoidMethodCall kill). */
    @Test
    void generateQuoteNumber_newSeq_capturesYearCategoryLastSeq() {
        when(seqRepository.findByYearAndCategory(2026, "용역")).thenReturn(Optional.empty());
        when(seqRepository.save(any(QuoteNumberSeq.class))).thenAnswer(i -> i.getArgument(0));

        service.generateQuoteNumber("용역", 2026);

        ArgumentCaptor<QuoteNumberSeq> sc = ArgumentCaptor.forClass(QuoteNumberSeq.class);
        verify(seqRepository).save(sc.capture());
        QuoteNumberSeq seq = sc.getValue();
        assertThat(seq.getYear()).isEqualTo(2026);
        assertThat(seq.getCategory()).isEqualTo("용역");
        assertThat(seq.getLastSeq()).isEqualTo(1);   // setLastSeq(0) → +1
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
        // 저장 시 ID 부여(실제 흐름 모사) → registerLedger 의 ledger.quoteId 단언 가능.
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> {
            Quotation q = i.getArgument(0); q.setQuoteId(7L); return q;
        });
        when(ledgerRepository.findMaxLedgerNo(anyInt(), anyString())).thenReturn(0);
        when(ledgerRepository.save(any(QuotationLedger.class))).thenAnswer(i -> i.getArgument(0));
        return service.createQuotation(dto);
    }

    /** create 의 모든 setter 효과 + 자동 대장등록(registerLedger)의 전 필드를 캡처 단언. */
    @Test
    void createQuotation_capturesAllQuotationAndLedgerFields() {
        stubSeqEmpty();
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> {
            Quotation q = i.getArgument(0); q.setQuoteId(7L); return q;
        });
        when(ledgerRepository.findMaxLedgerNo(2026, "용역")).thenReturn(4);   // nextNo=5 (MathMutator kill)
        when(ledgerRepository.save(any(QuotationLedger.class))).thenAnswer(i -> i.getArgument(0));

        QuotationDTO d = QuotationDTO.builder()
                .category("용역").quoteDate(LocalDate.of(2026, 1, 15))
                .projectName("프로젝트X").recipient("수신처").referenceTo("참조처")
                .vatIncluded(false).bidRate(null).rounddownUnit(1).createdBy("작성자")
                .items(List.of(QuotationDTO.ItemDTO.builder().amount(1_000_000L).build()))
                .build();

        service.createQuotation(d);

        ArgumentCaptor<Quotation> qc = ArgumentCaptor.forClass(Quotation.class);
        verify(quotationRepository).save(qc.capture());
        Quotation saved = qc.getValue();
        assertThat(saved.getQuoteNumber()).isEqualTo("UIT - SQ - 2026 - 001");
        assertThat(saved.getTotalAmount()).isEqualTo(1_000_000L);
        assertThat(saved.getTotalAmountText()).isEqualTo(QuotationDTO.amountToKorean(1_000_000L));
        assertThat(saved.getGrandTotal()).isEqualTo(1_100_000L);   // +VAT
        assertThat(saved.getStatus()).isEqualTo("발행완료");
        assertThat(saved.getItems()).hasSize(1);

        ArgumentCaptor<QuotationLedger> lc = ArgumentCaptor.forClass(QuotationLedger.class);
        verify(ledgerRepository).save(lc.capture());
        QuotationLedger ledger = lc.getValue();
        assertThat(ledger.getQuoteId()).isEqualTo(7L);
        assertThat(ledger.getLedgerNo()).isEqualTo(5);              // 4+1
        assertThat(ledger.getYear()).isEqualTo(2026);
        assertThat(ledger.getCategory()).isEqualTo("용역");
        assertThat(ledger.getQuoteNumber()).isEqualTo("UIT - SQ - 2026 - 001");
        assertThat(ledger.getQuoteDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(ledger.getProjectName()).isEqualTo("프로젝트X");
        assertThat(ledger.getTotalAmount()).isEqualTo(1_000_000L);
        assertThat(ledger.getGrandTotal()).isEqualTo(1_100_000L);
        assertThat(ledger.getRecipient()).isEqualTo("수신처");
        assertThat(ledger.getReferenceTo()).isEqualTo("참조처");
        assertThat(ledger.getCreatedBy()).isEqualTo("작성자");
    }

    @Test
    void createQuotation_vatExcluded_adds10Percent() {
        Quotation q = createWith(dto(1_000_000L, false, null, 1));
        assertThat(q.getTotalAmount()).isEqualTo(1_000_000L);
        assertThat(q.getGrandTotal()).isEqualTo(1_100_000L);
        assertThat(q.getQuoteNumber()).isEqualTo("UIT - SQ - 2026 - 001");
        verify(ledgerRepository).save(any(QuotationLedger.class));
    }

    @Test
    void createQuotation_vatIncluded_equalsRaw() {
        Quotation q = createWith(dto(1_000_000L, true, null, 1));
        assertThat(q.getGrandTotal()).isEqualTo(1_000_000L);
    }

    @Test
    void createQuotation_bidRate_floored() {
        Quotation q = createWith(dto(1_000_000L, true, 90.0, 1));
        assertThat(q.getGrandTotal()).isEqualTo(900_000L);
    }

    /** bidRate=0 경계: `bidRate > 0` 이라 미적용(grandTotal=raw). `>=` mutant 면 0적용→0 이 되어 죽음. */
    @Test
    void createQuotation_bidRateZero_notApplied() {
        Quotation q = createWith(dto(1_000_000L, true, 0.0, 1));
        assertThat(q.getGrandTotal()).isEqualTo(1_000_000L);
    }

    @Test
    void createQuotation_rounddown_appliedToFinalTotal() {
        Quotation q = createWith(dto(1_234_567L, true, null, 10_000));
        assertThat(q.getGrandTotal()).isEqualTo(1_230_000L);
    }

    // ===== update / get / delete =====

    /** update 의 모든 setter 효과 + 조건부 기본값(dto null → 기존 유지) + createdBy(set) 분기. */
    @Test
    void updateQuotation_setsFields_keepsExistingOnNullDefaults() {
        Quotation existing = new Quotation();
        existing.setQuoteId(7L);
        existing.setRounddownUnit(100); existing.setShowSeal(true);
        existing.setTemplateType(2); existing.setStatus("기존상태");
        when(quotationRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> i.getArgument(0));
        when(ledgerRepository.findByQuoteId(7L)).thenReturn(Optional.empty());

        QuotationDTO d = QuotationDTO.builder()
                .quoteId(7L).category("용역").quoteDate(LocalDate.of(2026, 1, 15))
                .projectName("새프로젝트").recipient("새수신").referenceTo("새참조")
                .vatIncluded(true).bidRate(90.0).remarks("비고내용")
                // rounddownUnit/showSeal/templateType/status null → 기존 유지 분기
                .createdBy("새작성자")   // non-blank → 변경 분기(L166~167)
                .items(List.of(QuotationDTO.ItemDTO.builder().amount(1_000_000L).build()))
                .build();

        Quotation saved = service.updateQuotation(d);

        assertThat(saved.getProjectName()).isEqualTo("새프로젝트");
        assertThat(saved.getRecipient()).isEqualTo("새수신");
        assertThat(saved.getReferenceTo()).isEqualTo("새참조");
        assertThat(saved.getVatIncluded()).isTrue();
        assertThat(saved.getBidRate()).isEqualTo(90.0);
        assertThat(saved.getRemarks()).isEqualTo("비고내용");
        assertThat(saved.getCreatedBy()).isEqualTo("새작성자");
        // dto null → 기존값 유지 (NegateConditionals kill)
        assertThat(saved.getRounddownUnit()).isEqualTo(100);
        assertThat(saved.getShowSeal()).isTrue();
        assertThat(saved.getTemplateType()).isEqualTo(2);
        assertThat(saved.getStatus()).isEqualTo("기존상태");
    }

    /** update 조건부 기본값 반대 분기(dto set → dto 사용) + createdBy blank → 기존 유지. */
    @Test
    void updateQuotation_usesDtoWhenSet_keepsCreatedByOnBlank() {
        Quotation existing = new Quotation();
        existing.setQuoteId(8L);
        existing.setRounddownUnit(100); existing.setShowSeal(false);
        existing.setTemplateType(1); existing.setStatus("기존"); existing.setCreatedBy("원작성자");
        when(quotationRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> i.getArgument(0));
        when(ledgerRepository.findByQuoteId(8L)).thenReturn(Optional.empty());

        QuotationDTO d = QuotationDTO.builder()
                .quoteId(8L).category("용역").quoteDate(LocalDate.of(2026, 1, 15))
                .vatIncluded(true).rounddownUnit(1000).showSeal(true).templateType(3).status("새상태")
                .createdBy("   ")   // blank → 기존 유지 분기
                .items(List.of(QuotationDTO.ItemDTO.builder().amount(1L).build()))
                .build();

        Quotation saved = service.updateQuotation(d);

        assertThat(saved.getRounddownUnit()).isEqualTo(1000);
        assertThat(saved.getShowSeal()).isTrue();
        assertThat(saved.getTemplateType()).isEqualTo(3);
        assertThat(saved.getStatus()).isEqualTo("새상태");
        assertThat(saved.getCreatedBy()).isEqualTo("원작성자");   // blank → 유지
        // rounddownUnit=1000 적용 → roundDown(1,1000)=0. L182 negate(rdUnit→1) 시 1 이 되어 죽음.
        assertThat(saved.getGrandTotal()).isZero();
    }

    /** update 시 대장 동기화(ifPresent) 전 필드. */
    @Test
    void updateQuotation_recalculatesAndSyncsLedgerFields() {
        Quotation existing = new Quotation();
        existing.setQuoteId(7L);
        // 기존 품목 1건 → clearItems(L171) 제거 mutant 검출(제거 시 품목 2건 잔존)
        QuotationItem prior = new QuotationItem(); prior.setAmount(999L);
        existing.setItems(new ArrayList<>(List.of(prior)));
        when(quotationRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> i.getArgument(0));
        QuotationLedger ledger = new QuotationLedger();
        when(ledgerRepository.findByQuoteId(7L)).thenReturn(Optional.of(ledger));
        when(ledgerRepository.save(any(QuotationLedger.class))).thenAnswer(i -> i.getArgument(0));

        QuotationDTO d = QuotationDTO.builder()
                .quoteId(7L).category("용역").quoteDate(LocalDate.of(2026, 1, 15))
                .projectName("프로젝트Y").recipient("수신Y").referenceTo("참조Y")
                .vatIncluded(false).rounddownUnit(1)
                .items(List.of(QuotationDTO.ItemDTO.builder().amount(2_000_000L).build()))
                .build();

        Quotation saved = service.updateQuotation(d);

        assertThat(saved.getTotalAmount()).isEqualTo(2_000_000L);
        assertThat(saved.getTotalAmountText()).isEqualTo(QuotationDTO.amountToKorean(2_000_000L)); // L179 kill
        assertThat(saved.getGrandTotal()).isEqualTo(2_200_000L);
        assertThat(saved.getItems()).hasSize(1);            // clearItems(L171)+addItem(L175) kill
        assertThat(saved.getItems().get(0).getAmount()).isEqualTo(2_000_000L);
        // 대장 동기화 전 필드 (L190~194 VoidMethodCall kill)
        assertThat(ledger.getProjectName()).isEqualTo("프로젝트Y");
        assertThat(ledger.getTotalAmount()).isEqualTo(2_000_000L);
        assertThat(ledger.getGrandTotal()).isEqualTo(2_200_000L);
        assertThat(ledger.getRecipient()).isEqualTo("수신Y");
        assertThat(ledger.getReferenceTo()).isEqualTo("참조Y");
        verify(ledgerRepository).save(ledger);
    }

    @Test
    void updateQuotation_notFound_throws() {
        when(quotationRepository.findById(99L)).thenReturn(Optional.empty());
        QuotationDTO d = dto(1L, true, null, 1);
        d.setQuoteId(99L);
        // 메시지 단언 → orElseThrow 람다 NullReturnVals(null→NPE) mutant 검출(L153)
        assertThatThrownBy(() -> service.updateQuotation(d))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("견적서를 찾을 수 없습니다");
    }

    @Test
    void getQuotation_found_returnsDto() {
        Quotation q = new Quotation();
        q.setQuoteId(5L); q.setCategory("용역"); q.setQuoteDate(LocalDate.of(2026, 1, 1));
        q.setItems(new ArrayList<>());
        when(quotationRepository.findById(5L)).thenReturn(Optional.of(q));

        QuotationDTO out = service.getQuotation(5L);

        assertThat(out).isNotNull();
        assertThat(out.getQuoteId()).isEqualTo(5L);
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

    /** 복사본의 모든 필드가 원본에서 복사되고 usageCount=0 인지 캡처 단언(VoidMethodCall kill). */
    @Test
    void copyPatterns_copiesAllFields_usageReset() {
        ProductPattern src = new ProductPattern();
        src.setPatternGroup("그룹A"); src.setProductName("제품A"); src.setDefaultUnit("개");
        src.setDefaultUnitPrice(5000L); src.setDescription("설명A"); src.setSubItems("소항목A");
        src.setCalcType("SUM"); src.setOverheadRate(120.0); src.setTechFeeRate(30.0);
        when(patternRepository.findById(1L)).thenReturn(Optional.of(src));
        when(patternRepository.save(any(ProductPattern.class))).thenAnswer(i -> i.getArgument(0));

        int count = service.copyPatterns(List.of(1L), "제품");
        assertThat(count).isEqualTo(1);

        ArgumentCaptor<ProductPattern> pc = ArgumentCaptor.forClass(ProductPattern.class);
        verify(patternRepository).save(pc.capture());
        ProductPattern copy = pc.getValue();
        assertThat(copy.getCategory()).isEqualTo("제품");
        assertThat(copy.getPatternGroup()).isEqualTo("그룹A");
        assertThat(copy.getProductName()).isEqualTo("제품A");
        assertThat(copy.getDefaultUnit()).isEqualTo("개");
        assertThat(copy.getDefaultUnitPrice()).isEqualTo(5000L);
        assertThat(copy.getDescription()).isEqualTo("설명A");
        assertThat(copy.getSubItems()).isEqualTo("소항목A");
        assertThat(copy.getCalcType()).isEqualTo("SUM");
        assertThat(copy.getOverheadRate()).isEqualTo(120.0);
        assertThat(copy.getTechFeeRate()).isEqualTo(30.0);
        assertThat(copy.getUsageCount()).isEqualTo(0);
    }

    @Test
    void copyPatterns_skipsMissing_returnsCount() {
        ProductPattern src = new ProductPattern();
        src.setProductName("제품A");
        when(patternRepository.findById(1L)).thenReturn(Optional.of(src));
        when(patternRepository.findById(2L)).thenReturn(Optional.empty());   // skip
        when(patternRepository.save(any(ProductPattern.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(service.copyPatterns(List.of(1L, 2L), "제품")).isEqualTo(1);
        verify(patternRepository, times(1)).save(any(ProductPattern.class));
    }

    // ===== saveWageRate (upsert) =====

    @Test
    void saveWageRate_updatesAllFieldsWhenDifferentId_returnsSaved() {
        WageRate existing = new WageRate();
        existing.setWageId(5L); existing.setYear(2026); existing.setGradeName("중급");
        when(wageRateRepository.findByYearAndGradeName(2026, "중급")).thenReturn(Optional.of(existing));
        when(wageRateRepository.save(any(WageRate.class))).thenAnswer(i -> i.getArgument(0));

        WageRate input = new WageRate();
        input.setYear(2026); input.setGradeName("중급");   // wageId null → 기존 갱신
        input.setDailyRate(200_000L); input.setMonthlyRate(5_000_000L);
        input.setHourlyRate(25_000L); input.setDescription("설명");

        WageRate result = service.saveWageRate(input);

        assertThat(result).isSameAs(existing);          // NullReturnVals kill (non-null + 기존행)
        assertThat(existing.getDailyRate()).isEqualTo(200_000L);
        assertThat(existing.getMonthlyRate()).isEqualTo(5_000_000L);
        assertThat(existing.getHourlyRate()).isEqualTo(25_000L);
        assertThat(existing.getDescription()).isEqualTo("설명");
        verify(wageRateRepository).save(existing);
    }

    @Test
    void saveWageRate_insertsWhenNew_returnsInput() {
        when(wageRateRepository.findByYearAndGradeName(2027, "초급")).thenReturn(Optional.empty());
        when(wageRateRepository.save(any(WageRate.class))).thenAnswer(i -> i.getArgument(0));
        WageRate input = new WageRate();
        input.setYear(2027); input.setGradeName("초급");
        WageRate result = service.saveWageRate(input);
        assertThat(result).isSameAs(input);   // NullReturnVals kill (non-null + 입력 그대로)
        verify(wageRateRepository).save(input);
    }

    /** 기존행이 있으나 같은 wageId → 갱신 안 하고 입력 저장(조건 false 분기, L335). */
    @Test
    void saveWageRate_existingSameId_savesInputWithoutUpdate() {
        WageRate existing = new WageRate();
        existing.setWageId(5L); existing.setYear(2026); existing.setGradeName("중급");
        existing.setDailyRate(100_000L);
        when(wageRateRepository.findByYearAndGradeName(2026, "중급")).thenReturn(Optional.of(existing));
        when(wageRateRepository.save(any(WageRate.class))).thenAnswer(i -> i.getArgument(0));

        WageRate input = new WageRate();
        input.setWageId(5L); input.setYear(2026); input.setGradeName("중급"); input.setDailyRate(999L);

        WageRate result = service.saveWageRate(input);

        assertThat(result).isSameAs(input);
        assertThat(existing.getDailyRate()).isEqualTo(100_000L);   // 기존행 미갱신
        verify(wageRateRepository).save(input);
    }

    // ===== recalculateAllGrandTotals =====

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
        assertThat(q.getTotalAmount()).isEqualTo(1_000_000L);
        assertThat(q.getTotalAmountText()).isEqualTo(QuotationDTO.amountToKorean(1_000_000L)); // L366 kill
        assertThat(q.getGrandTotal()).isEqualTo(1_100_000L);
        verify(quotationRepository).save(q);
        assertThat(ledger.getTotalAmount()).isEqualTo(1_000_000L);
        assertThat(ledger.getGrandTotal()).isEqualTo(1_100_000L);
        verify(ledgerRepository).save(ledger);
    }

    /** rounddownUnit 비-1(절사 적용)로 L361 조건 분기 실효화 + 대장 없음 분기. */
    @Test
    void recalculateAllGrandTotals_rounddownApplied_noLedger() {
        Quotation q = new Quotation();
        q.setQuoteId(2L);
        q.setVatIncluded(true);           // raw
        q.setRounddownUnit(10_000);       // 절사 적용 → L361 non-null 분기 실효
        QuotationItem it = new QuotationItem(); it.setAmount(1_234_567L);
        q.setItems(new ArrayList<>(List.of(it)));
        when(quotationRepository.findAll()).thenReturn(List.of(q));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> i.getArgument(0));
        when(ledgerRepository.findByQuoteId(2L)).thenReturn(Optional.empty());

        int count = service.recalculateAllGrandTotals();

        assertThat(count).isEqualTo(1);
        assertThat(q.getGrandTotal()).isEqualTo(1_230_000L);   // roundDown(1,234,567, 10000)
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
        service.getQuotations("", 2026, "");
        verify(quotationRepository).findByFilter(null, 2026, null);
    }

    // ===== 조회 분기 (비빈 distinct 데이터로 EmptyObjectReturnVals + NegateConditionals kill) =====

    @Test
    void getLedger_categoryVsYearBranches_returnDistinctData() {
        QuotationLedger byCategory = new QuotationLedger(); byCategory.setLedgerNo(11);
        QuotationLedger byYear = new QuotationLedger(); byYear.setLedgerNo(22);
        when(ledgerRepository.findByYearAndCategoryOrderByLedgerNoAsc(2026, "용역")).thenReturn(List.of(byCategory));
        when(ledgerRepository.findByYearOrderByLedgerNoAsc(2026)).thenReturn(List.of(byYear));

        assertThat(service.getLedger(2026, "용역")).containsExactly(byCategory);   // category 분기 + non-empty
        assertThat(service.getLedger(2026, null)).containsExactly(byYear);         // year 분기
        assertThat(service.getLedger(2026, "")).containsExactly(byYear);           // 빈문자 → year 분기
    }

    @Test
    void getPatterns_categoryVsAllBranches_returnDistinctData() {
        ProductPattern byCat = new ProductPattern(); byCat.setProductName("카테고리것");
        ProductPattern all = new ProductPattern(); all.setProductName("전체것");
        when(patternRepository.findByCategoryOrderByPatternGroupAscProductNameAsc("제품")).thenReturn(List.of(byCat));
        when(patternRepository.findAllByOrderByCategoryAscPatternGroupAscProductNameAsc()).thenReturn(List.of(all));

        assertThat(service.getPatterns("제품")).containsExactly(byCat);
        assertThat(service.getPatterns(null)).containsExactly(all);
        assertThat(service.getPatterns("")).containsExactly(all);
    }

    @Test
    void getWageRates_yearVsAllBranches_returnDistinctData() {
        WageRate byYear = new WageRate(); byYear.setGradeName("연도것");
        WageRate all = new WageRate(); all.setGradeName("전체것");
        when(wageRateRepository.findByYearOrderByGradeNameAsc(2026)).thenReturn(List.of(byYear));
        when(wageRateRepository.findAllByOrderByYearDescGradeNameAsc()).thenReturn(List.of(all));

        assertThat(service.getWageRates(2026)).containsExactly(byYear);   // year 분기
        assertThat(service.getWageRates(null)).containsExactly(all);      // null 분기
    }

    @Test
    void getWageRateYears_delegates() {
        when(wageRateRepository.findDistinctYears()).thenReturn(List.of(2026, 2025));
        assertThat(service.getWageRateYears()).containsExactly(2026, 2025);
    }

    // ===== 단순 위임 (저장/삭제) =====

    @Test
    void simpleDelegates_saveDelete() {
        ProductPattern pp = new ProductPattern();
        when(patternRepository.save(pp)).thenReturn(pp);
        assertThat(service.savePattern(pp)).isSameAs(pp);
        RemarksPattern rp = new RemarksPattern();
        when(remarksPatternRepository.save(rp)).thenReturn(rp);
        assertThat(service.saveRemarksPattern(rp)).isSameAs(rp);

        when(patternRepository.count()).thenReturn(4L);
        assertThat(service.deleteAllPatterns()).isEqualTo(4L);
        verify(patternRepository).deleteAll();

        service.deletePattern(1L);        verify(patternRepository).deleteById(1L);
        service.deleteRemarksPattern(2L); verify(remarksPatternRepository).deleteById(2L);
        service.deleteWageRate(3L);       verify(wageRateRepository).deleteById(3L);
    }
}
