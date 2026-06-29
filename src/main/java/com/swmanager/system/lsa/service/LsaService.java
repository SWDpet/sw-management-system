package com.swmanager.system.lsa.service;

import com.swmanager.system.lsa.dto.LsaDTO;
import com.swmanager.system.lsa.repository.LsaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * LSA 발급 대장 서비스. P2: 목록 + 키워드 검색.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LsaService {

    private final LsaRepository lsaRepository;

    /** 키워드 검색 목록 (빈/공백 키워드 → 전체). */
    public List<LsaDTO> list(String keyword) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return lsaRepository.search(kw).stream()
                .map(LsaDTO::fromEntity)
                .toList();
    }
}
