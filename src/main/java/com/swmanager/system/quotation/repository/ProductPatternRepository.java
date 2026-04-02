package com.swmanager.system.quotation.repository;

import com.swmanager.system.quotation.domain.ProductPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductPatternRepository extends JpaRepository<ProductPattern, Long> {

    List<ProductPattern> findByCategoryOrderByPatternGroupAscProductNameAsc(String category);

    List<ProductPattern> findAllByOrderByCategoryAscPatternGroupAscProductNameAsc();
}
