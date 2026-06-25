package com.swmanager.system.dto;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.InfraLinkApi;
import com.swmanager.system.domain.InfraLinkUpis;
import com.swmanager.system.domain.InfraServer;
import com.swmanager.system.domain.InfraSoftware;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * InfraDTO 변환 로직 단위테스트 (커버리지 상향 beyond-A, 순수·mock 무관).
 * fromEntity 의 마스킹(mask true/false)·중첩 변환·maskKey 경계를 고정한다.
 */
class InfraDTOTest {

    private Infra sampleInfra() {
        Infra infra = new Infra();
        infra.setInfraId(1L);
        infra.setInfraType("UPIS");
        infra.setCityNm("서울특별시");
        infra.setSysNm("도시계획정보체계");

        InfraSoftware sw = new InfraSoftware();
        sw.setSwId(10L); sw.setSwNm("PostgreSQL"); sw.setSwAccPw("dbpass");
        InfraServer server = new InfraServer();
        server.setServerId(100L); server.setHostName("host01"); server.setAccPw("serverpass");
        server.setSoftwares(new ArrayList<>(List.of(sw)));
        infra.setServers(new ArrayList<>(List.of(server)));

        InfraLinkUpis upis = new InfraLinkUpis();
        upis.setLinkId(200L); upis.setGpkiPw("gpkisecret"); upis.setMinwonKey("minwonsecret");
        infra.setUpisLinks(new ArrayList<>(List.of(upis)));

        InfraLinkApi api = new InfraLinkApi();
        api.setApiId(300L);
        api.setNaverNewsKey("ABCDEFGH");                 // 8글자 → maskKey: "ABCD********"
        api.setNaverNewsExpDt(LocalDate.of(2026, 12, 31));
        infra.setApiLinks(new ArrayList<>(List.of(api)));
        return infra;
    }

    @Test
    void fromEntity_null_returnsNull() {
        assertThat(InfraDTO.fromEntity(null, true)).isNull();
    }

    @Test
    void fromEntity_mask_masksSecrets() {
        InfraDTO dto = InfraDTO.fromEntity(sampleInfra(), true);
        assertThat(dto.getServers().get(0).getAccPw()).isEqualTo("********");
        assertThat(dto.getServers().get(0).getSoftwares().get(0).getSwAccPw()).isEqualTo("********");
        assertThat(dto.getUpisLinks().get(0).getGpkiPw()).isEqualTo("********");
        assertThat(dto.getUpisLinks().get(0).getMinwonKey()).isEqualTo("********");
        // maskKey: 8글자 → 앞4글자 + "****" + "****"
        assertThat(dto.getApiLinks().get(0).getNaverNewsKey()).isEqualTo("ABCD********");
        assertThat(dto.getApiLinks().get(0).getNaverNewsExpDt()).isEqualTo("2026-12-31"); // LocalDate.toString()
    }

    @Test
    void fromEntity_noMask_keepsRaw() {
        InfraDTO dto = InfraDTO.fromEntity(sampleInfra(), false);
        assertThat(dto.getServers().get(0).getAccPw()).isEqualTo("serverpass");
        assertThat(dto.getServers().get(0).getSoftwares().get(0).getSwAccPw()).isEqualTo("dbpass");
        assertThat(dto.getUpisLinks().get(0).getGpkiPw()).isEqualTo("gpkisecret");
        assertThat(dto.getApiLinks().get(0).getNaverNewsKey()).isEqualTo("ABCDEFGH");
    }

    @Test
    void fromEntity_nullCollections_keepNull() {
        Infra infra = new Infra();
        infra.setInfraId(2L);
        infra.setServers(null); infra.setUpisLinks(null); infra.setApiLinks(null);
        InfraDTO dto = InfraDTO.fromEntity(infra, true);
        assertThat(dto.getServers()).isNull();
        assertThat(dto.getUpisLinks()).isNull();
        assertThat(dto.getApiLinks()).isNull();
    }

    @Test
    void maskKey_boundaries_viaApi() {
        Infra infra = new Infra();
        InfraLinkApi api = new InfraLinkApi();
        api.setApiId(1L);
        api.setNaverNewsKey("AB");        // 길이 ≤4 → "****"
        api.setKrasKey(null);             // null → "****"
        infra.setApiLinks(new ArrayList<>(List.of(api)));
        InfraDTO dto = InfraDTO.fromEntity(infra, true);
        assertThat(dto.getApiLinks().get(0).getNaverNewsKey()).isEqualTo("****");
        assertThat(dto.getApiLinks().get(0).getKrasKey()).isEqualTo("****");
        assertThat(dto.getApiLinks().get(0).getNaverNewsExpDt()).isNull();  // expDt null → null
    }
}
