package com.swmanager.system.service;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.repository.InfraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InfraService {

    @Autowired private InfraRepository infraRepository;

    public Page<Infra> getInfraList(String type, String keyword, Pageable pageable) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        return infraRepository.findAllByKeyword(type, keyword, pageable);
    }

    public Infra getInfraById(Long id) {
        return infraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("자산 정보가 없습니다. ID: " + id));
    }

    public void saveInfra(Infra infra) {
        if (infra.getInfraType() == null || infra.getInfraType().isEmpty()) {
            infra.setInfraType("PROD");
        }
        
        // 부모-자식 연관관계 설정 (Cascade 저장 시 필수)
        if (infra.getServers() != null) {
            infra.getServers().forEach(server -> {
                server.setInfra(infra);
                if (server.getSoftwares() != null) server.getSoftwares().forEach(sw -> sw.setServer(server));
            });
        }
        if (infra.getUpisLinks() != null) infra.getUpisLinks().forEach(link -> link.setInfra(infra));
        if (infra.getApiLinks() != null) infra.getApiLinks().forEach(link -> link.setInfra(infra));
        if (infra.getMemos() != null) infra.getMemos().forEach(memo -> memo.setInfra(infra));
        
        infraRepository.save(infra);
    }

    public void deleteInfra(Long id) { infraRepository.deleteById(id); }
}