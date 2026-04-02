package com.swmanager.system.repository;

import com.swmanager.system.domain.SwProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SwRepository extends JpaRepository<SwProject, Long> {
    // 연도 내림차순(최신순), 시도명 오름차순(가나다순), 시군구명 오름차순(가나다순) 정렬
    List<SwProject> findAllByOrderByYearDescCityNmAscDistNmAsc();
}