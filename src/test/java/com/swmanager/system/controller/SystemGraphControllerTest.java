package com.swmanager.system.controller;

import com.swmanager.system.dto.ErdGraphDTO;
import com.swmanager.system.dto.InfraGraphDTO;
import com.swmanager.system.service.ErdGraphService;
import com.swmanager.system.service.InfraGraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SystemGraphController 단위 테스트 (beyond-A — coverage-misc-controllers).
 * 생성자 주입. view 뷰명 + ERD/Infra API 위임(클래스 @PreAuthorize 메서드 보안은 단위범위 밖).
 */
class SystemGraphControllerTest {

    private ErdGraphService erdGraphService;
    private InfraGraphService infraGraphService;
    private SystemGraphController controller;

    @BeforeEach
    void setUp() {
        erdGraphService = mock(ErdGraphService.class);
        infraGraphService = mock(InfraGraphService.class);
        controller = new SystemGraphController(erdGraphService, infraGraphService);
    }

    @Test
    void view_returnsTemplate() { // T-Y1
        assertThat(controller.view()).isEqualTo("admin-system-graph");
    }

    @Test
    void getErdGraph_delegates() { // T-Y2
        ErdGraphDTO dto = new ErdGraphDTO(java.util.List.of(), java.util.List.of());
        when(erdGraphService.getGraph()).thenReturn(dto);
        assertThat(controller.getErdGraph()).isSameAs(dto);
    }

    @Test
    void getInfraGraph_delegates() { // T-Y3
        InfraGraphDTO dto = new InfraGraphDTO(java.util.List.of(), java.util.List.of());
        when(infraGraphService.getGraph()).thenReturn(dto);
        assertThat(controller.getInfraGraph()).isSameAs(dto);
    }
}
