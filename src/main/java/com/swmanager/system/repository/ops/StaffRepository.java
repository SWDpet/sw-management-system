package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    /** 조직 단위 소속(퇴사 포함, 조직도 표시용). */
    List<Staff> findByOrgUnitIdOrderBySortOrderAscNameAsc(Long orgUnitId);

    /** 직원 요청자 검색 — 재직자, 이름 LIKE. */
    @Query("SELECT s FROM Staff s WHERE s.active = true " +
           "AND LOWER(s.name) LIKE LOWER(CONCAT('%', :kw, '%')) ORDER BY s.name")
    List<Staff> searchActive(@Param("kw") String kw);
}
