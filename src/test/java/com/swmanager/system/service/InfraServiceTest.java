package com.swmanager.system.service;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.InfraServer;
import com.swmanager.system.domain.InfraSoftware;
import com.swmanager.system.repository.InfraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * InfraService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 필드 주입이라 ReflectionTestUtils 로 mock 주입. 검색 blank 정규화·단건 조회(없으면 예외)·
 * 저장 시 infra_type 기본값(PROD)·부모-자식 cascade 연관 설정·삭제 위임 커버.
 */
class InfraServiceTest {

    private final InfraRepository infraRepository = mock(InfraRepository.class);

    private InfraService service;

    @BeforeEach
    void setUp() {
        service = new InfraService();
        ReflectionTestUtils.setField(service, "infraRepository", infraRepository);
    }

    // ===== getInfraList =====

    @Test
    void getInfraList_blankKeyword_normalizedToNull() {
        Page<Infra> empty = new PageImpl<>(List.of());
        when(infraRepository.findAllByKeyword(eq("PROD"), isNull(), any())).thenReturn(empty);

        service.getInfraList("PROD", "   ", PageRequest.of(0, 10));

        verify(infraRepository).findAllByKeyword(eq("PROD"), isNull(), any());   // blank → null
    }

    @Test
    void getInfraList_keywordPreserved() {
        when(infraRepository.findAllByKeyword(any(), eq("강원"), any())).thenReturn(new PageImpl<>(List.of()));
        service.getInfraList(null, "강원", PageRequest.of(0, 10));
        verify(infraRepository).findAllByKeyword(isNull(), eq("강원"), any());
    }

    // ===== getInfraById =====

    @Test
    void getInfraById_found() {
        Infra infra = new Infra();
        when(infraRepository.findById(1L)).thenReturn(Optional.of(infra));
        assertThat(service.getInfraById(1L)).isSameAs(infra);
    }

    @Test
    void getInfraById_notFound_throwsWithId() {
        when(infraRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getInfraById(9L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("9");
    }

    // ===== saveInfra =====

    @Test
    void saveInfra_defaultsTypeToProd_whenNull() {
        Infra infra = new Infra();   // infraType null, 컬렉션 기본 빈 리스트
        service.saveInfra(infra);
        assertThat(infra.getInfraType()).isEqualTo("PROD");
        verify(infraRepository).save(infra);
    }

    @Test
    void saveInfra_defaultsTypeToProd_whenEmpty() {
        Infra infra = new Infra();
        infra.setInfraType("");
        service.saveInfra(infra);
        assertThat(infra.getInfraType()).isEqualTo("PROD");
    }

    @Test
    void saveInfra_keepsExplicitType() {
        Infra infra = new Infra();
        infra.setInfraType("STAGE");
        service.saveInfra(infra);
        assertThat(infra.getInfraType()).isEqualTo("STAGE");
    }

    @Test
    void saveInfra_wiresServerAndSoftwareCascade() {
        Infra infra = new Infra();
        InfraServer server = new InfraServer();
        InfraSoftware sw = new InfraSoftware();
        server.getSoftwares().add(sw);
        infra.setServers(List.of(server));

        service.saveInfra(infra);

        assertThat(server.getInfra()).isSameAs(infra);     // 서버 → infra 역참조 설정
        assertThat(sw.getServer()).isSameAs(server);       // SW → 서버 역참조 설정
    }

    // ===== deleteInfra =====

    @Test
    void deleteInfra_delegatesToRepo() {
        service.deleteInfra(3L);
        verify(infraRepository).deleteById(3L);
    }
}
