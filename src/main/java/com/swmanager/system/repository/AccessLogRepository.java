package com.swmanager.system.repository;

import com.swmanager.system.domain.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    
    // 검색 기능 (아이디, 이름, 내용 등)
    @Query("SELECT l FROM AccessLog l WHERE " +
           "(:kw IS NULL OR l.userid LIKE %:kw% OR l.username LIKE %:kw% OR l.actionDetail LIKE %:kw%) " +
           "ORDER BY l.accessTime DESC")
    Page<AccessLog> findAllWithSearch(Pageable pageable, @Param("kw") String kw);
}