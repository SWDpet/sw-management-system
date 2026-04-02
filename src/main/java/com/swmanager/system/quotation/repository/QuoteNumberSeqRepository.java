package com.swmanager.system.quotation.repository;

import com.swmanager.system.quotation.domain.QuoteNumberSeq;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuoteNumberSeqRepository extends JpaRepository<QuoteNumberSeq, Long> {

    Optional<QuoteNumberSeq> findByYearAndCategory(Integer year, String category);
}
