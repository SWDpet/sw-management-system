package com.swmanager.system.service;

import com.swmanager.system.repository.InspectMetricSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * InspectMetricChartService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 데이터 0건 시 "수집 대기" 안내 emptyChart(AWT/ImageIO PNG, 외부의존 0·headless-OK) 경로 박제.
 * 데이터 있는 렌더(JFreeChart)는 기존 커버 — 본 클래스는 no-data emptyChart 만 보강.
 */
class InspectMetricChartServiceTest {

    private InspectMetricSnapshotRepository repository;
    private InspectMetricChartService service;

    // 고정 instant — repository stub 이 any() 라 값은 임의지만, wall-clock 의존(재현성↓) 회피
    private final OffsetDateTime since = OffsetDateTime.parse("2025-06-01T00:00:00Z");
    private final OffsetDateTime until = OffsetDateTime.parse("2026-06-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        repository = mock(InspectMetricSnapshotRepository.class);
        service = new InspectMetricChartService(repository);
    }

    /** PNG 매직(0x89 P N G) + 실제 decode 가능 여부까지 확인(빈/위조 byte[] 회귀 차단). */
    private void assertDecodablePng(byte[] png) throws IOException {
        assertThat(png).isNotEmpty()
                .startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);   // \x89PNG
        assertThat(ImageIO.read(new ByteArrayInputStream(png)))
                .as("emptyChart 가 decode 가능한 PNG 를 생성").isNotNull();
    }

    // M1: host 0 → dataset 0 시리즈 → emptyChart("수집 대기…") PNG
    @Test
    void renderChart_noHosts_returnsEmptyChartPng() throws IOException {
        when(repository.findHostsByPjtRoleRange(anyLong(), anyString(), any(), any()))
                .thenReturn(List.of());   // AP/DB 둘 다 host 없음

        byte[] png = service.renderChart(108L, since, until);

        assertDecodablePng(png);
        // AP·DB 두 role 모두 host 조회됨(이중 role 루프 입증)
        verify(repository).findHostsByPjtRoleRange(eq(108L), eq("AP"), any(), any());
        verify(repository).findHostsByPjtRoleRange(eq(108L), eq("DB"), any(), any());
        // host 없음 → row 조회 자체 미진입
        verify(repository, never()).findRangeByPjtRoleHost(anyLong(), anyString(), anyString(), any(), any());
    }

    // M2: host 있으나 rows 0 → rows.isEmpty() continue → dataset 0 → emptyChart
    @Test
    void renderChart_hostsButNoRows_returnsEmptyChartPng() throws IOException {
        when(repository.findHostsByPjtRoleRange(anyLong(), anyString(), any(), any()))
                .thenReturn(List.of("host1"));
        when(repository.findRangeByPjtRoleHost(anyLong(), anyString(), anyString(), any(), any()))
                .thenReturn(List.of());   // 행 없음 → continue

        byte[] png = service.renderChart(108L, since, until);

        assertDecodablePng(png);
        // host 루프 진입(row 조회 호출됨) 입증
        verify(repository, atLeastOnce())
                .findRangeByPjtRoleHost(eq(108L), anyString(), eq("host1"), any(), any());
    }
}
