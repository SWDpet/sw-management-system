package com.swmanager.system.quotation.repository;

import com.swmanager.system.quotation.domain.QuotationLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface QuotationLedgerRepository extends JpaRepository<QuotationLedger, Long> {

    List<QuotationLedger> findByYearOrderByLedgerNoAsc(Integer year);

    List<QuotationLedger> findByYearAndCategoryOrderByLedgerNoAsc(Integer year, String category);

    @Query("SELECT COALESCE(MAX(l.ledgerNo), 0) FROM QuotationLedger l WHERE l.year = :year AND l.category = :category")
    int findMaxLedgerNo(@Param("year") Integer year, @Param("category") String category);

    Optional<QuotationLedger> findByQuoteId(Long quoteId);
}
