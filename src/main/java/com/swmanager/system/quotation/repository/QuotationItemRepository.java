package com.swmanager.system.quotation.repository;

import com.swmanager.system.quotation.domain.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationItemRepository extends JpaRepository<QuotationItem, Long> {
}
