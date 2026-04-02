package com.swmanager.system.quotation.repository;

import com.swmanager.system.quotation.domain.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    List<Quotation> findAllByOrderByCreatedAtDesc();

    List<Quotation> findByCategoryOrderByCreatedAtDesc(String category);

    @Query("SELECT q FROM Quotation q WHERE " +
           "(:category IS NULL OR q.category = :category) AND " +
           "(:year IS NULL OR YEAR(q.quoteDate) = :year) AND " +
           "(:status IS NULL OR q.status = :status) " +
           "ORDER BY q.createdAt DESC")
    List<Quotation> findByFilter(@Param("category") String category,
                                  @Param("year") Integer year,
                                  @Param("status") String status);

    @Query("SELECT COUNT(q) FROM Quotation q WHERE q.category = :category")
    long countByCategory(@Param("category") String category);

    @Query("SELECT COALESCE(SUM(q.totalAmount), 0) FROM Quotation q WHERE q.category = :category")
    long sumAmountByCategory(@Param("category") String category);
}
