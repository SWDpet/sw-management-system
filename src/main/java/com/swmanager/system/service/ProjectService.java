package com.swmanager.system.service;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.repository.SwProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    @Autowired
    private SwProjectRepository swProjectRepository;

    @Transactional
    public void saveProject(SwProject project) {
        // 신규 등록일 경우에만 중복 체크
        if (project.getProjId() == null) { 
            boolean exists = swProjectRepository.existsByYearAndDistCdAndSysNmEn(
                project.getYear(),
                project.getDistCd(), 
                project.getSysNmEn()
            );
            
            if (exists) {
                throw new RuntimeException("해당 연도와 행정구역코드(지역)에 이미 동일한 시스템이 등록되어 있습니다.");
            }
        }
        swProjectRepository.save(project);
    }
}