package com.swmanager.system.quotation.repository;

import com.swmanager.system.quotation.domain.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuotationItemRepository extends JpaRepository<QuotationItem, Long> {

    List<QuotationItem> findByQuotationQuoteIdOrderByItemNoAsc(Long quoteId);

    void deleteByQuotationQuoteId(Long quoteId);
}
