package com.swmanager.system.lsa.service;

import com.swmanager.system.lsa.domain.Lsa;
import com.swmanager.system.lsa.dto.LsaDTO;
import com.swmanager.system.lsa.repository.LsaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/** LsaService 단위테스트 (검색 키워드 정규화 + DTO 매핑, 순수 mock). */
class LsaServiceTest {

    private final LsaRepository repo = mock(LsaRepository.class);
    private final LsaService service = new LsaService(repo);

    private Lsa sample() {
        Lsa l = new Lsa();
        l.setId(7L);
        l.setCityNm("전라남도"); l.setDistNm("강진군");
        l.setDeptNm("정보화"); l.setTeamNm("GIS");
        l.setUserNm("홍길동"); l.setTel("061-123-4567"); l.setEmail("hong@x.go.kr");
        l.setVersion("v2.1"); l.setIssuer("박욱진");
        l.setCreatedAt(LocalDateTime.of(2026, 6, 29, 10, 0));
        return l;
    }

    // ── 1. null 키워드 → search(null) + DTO 매핑 전 필드 박제 ──────────────────
    @Test
    void list_nullKeyword_passesNullAndMapsAllFields() {
        when(repo.search(isNull())).thenReturn(List.of(sample()));

        List<LsaDTO> result = service.list(null);

        verify(repo).search(isNull());
        assertThat(result).hasSize(1);
        LsaDTO d = result.get(0);
        assertThat(d.id()).isEqualTo(7L);
        assertThat(d.cityNm()).isEqualTo("전라남도");
        assertThat(d.distNm()).isEqualTo("강진군");
        assertThat(d.deptNm()).isEqualTo("정보화");
        assertThat(d.teamNm()).isEqualTo("GIS");
        assertThat(d.userNm()).isEqualTo("홍길동");
        assertThat(d.tel()).isEqualTo("061-123-4567");
        assertThat(d.email()).isEqualTo("hong@x.go.kr");
        assertThat(d.version()).isEqualTo("v2.1");
        assertThat(d.issuer()).isEqualTo("박욱진");
        assertThat(d.createdAt()).isEqualTo(LocalDateTime.of(2026, 6, 29, 10, 0));
    }

    // ── 2. 공백 키워드 → null 정규화 ──────────────────────────────────────────
    @Test
    void list_blankKeyword_normalizedToNull() {
        when(repo.search(isNull())).thenReturn(List.of());
        service.list("   ");
        verify(repo).search(isNull());
    }

    // ── 3. 값 키워드 → trim 후 그대로 전달 ────────────────────────────────────
    @Test
    void list_valueKeyword_trimmedAndPassed() {
        when(repo.search("강진")).thenReturn(List.of());
        service.list("  강진  ");
        verify(repo).search("강진");
    }

    // ── 4. 빈 결과 → 빈 리스트 ────────────────────────────────────────────────
    @Test
    void list_empty_returnsEmpty() {
        when(repo.search(isNull())).thenReturn(List.of());
        assertThat(service.list(null)).isEmpty();
    }
}
