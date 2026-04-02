package com.swmanager.system.quotation.repository;

import com.swmanager.system.quotation.domain.WageRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface WageRateRepository extends JpaRepository<WageRate, Long> {

    List<WageRate> findByYearOrderByGradeNameAsc(Integer year);

    Optional<WageRate> findByYearAndGradeName(Integer year, String gradeName);

    @Query("SELECT DISTINCT w.year FROM WageRate w ORDER BY w.year DESC")
    List<Integer> findDistinctYears();

    List<WageRate> findAllByOrderByYearDescGradeNameAsc();
}
