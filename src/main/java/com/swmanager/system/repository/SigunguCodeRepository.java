package com.swmanager.system.repository;

import com.swmanager.system.domain.SigunguCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SigunguCodeRepository extends JpaRepository<SigunguCode, String> {
    
    // 시도 목록 (중복제거)
    @Query("SELECT DISTINCT s.sidoNm FROM SigunguCode s ORDER BY s.sidoNm")
    List<String> findDistinctSidoNm();

    // [★추가] Controller에서 호출하는 메서드명과 정확히 일치해야 합니다.
    List<SigunguCode> findBySidoNm(String sidoNm);

    // (기존 메서드 유지: 정렬이 필요할 때 사용 가능)
    List<SigunguCode> findBySidoNmOrderBySggNm(String sidoNm);
}