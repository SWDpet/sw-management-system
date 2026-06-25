package com.swmanager.system.dto;

import com.swmanager.system.domain.SwProject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SwProjectDTO 변환 로직 단위테스트 (커버리지 상향 beyond-A, 순수).
 * fromEntity/toEntity 매핑 고정. ⚠ statNm/maintTypeNm/personNm 은 조인 전용(미매핑),
 * toEntity 는 regDt 미세팅 → 라운드트립 단언에서 제외.
 */
class SwProjectDTOTest {

    @Test
    void fromEntity_null_returnsNull() {
        assertThat(SwProjectDTO.fromEntity(null)).isNull();
    }

    @Test
    void fromEntity_mapsRepresentativeFields() {
        SwProject e = new SwProject();
        e.setProjId(5L); e.setYear(2026); e.setProjNm("밀양 GIS 유지보수");
        e.setContAmt(100_000_000L); e.setMaintType("HW"); e.setStat("진행");
        e.setCityNm("경상남도"); e.setDistNm("밀양시");

        SwProjectDTO dto = SwProjectDTO.fromEntity(e);

        assertThat(dto.getProjId()).isEqualTo(5L);
        assertThat(dto.getYear()).isEqualTo(2026);
        assertThat(dto.getProjNm()).isEqualTo("밀양 GIS 유지보수");
        assertThat(dto.getContAmt()).isEqualTo(100_000_000L);
        assertThat(dto.getMaintType()).isEqualTo("HW");
        assertThat(dto.getStat()).isEqualTo("진행");
        assertThat(dto.getCityNm()).isEqualTo("경상남도");
        assertThat(dto.getDistNm()).isEqualTo("밀양시");
    }

    @Test
    void toEntity_mapsBack_roundTripExcludingUnmapped() {
        SwProject e = new SwProject();
        e.setProjId(7L); e.setYear(2025); e.setProjNm("강진 점검");
        e.setContAmt(50L); e.setStat("완료"); e.setMaintType("SW");

        SwProject back = SwProjectDTO.fromEntity(e).toEntity();

        assertThat(back.getProjId()).isEqualTo(7L);
        assertThat(back.getYear()).isEqualTo(2025);
        assertThat(back.getProjNm()).isEqualTo("강진 점검");
        assertThat(back.getContAmt()).isEqualTo(50L);
        assertThat(back.getStat()).isEqualTo("완료");
        assertThat(back.getMaintType()).isEqualTo("SW");
        // 미매핑(statNm/maintTypeNm/personNm) · toEntity 미세팅(regDt) 은 단언 제외
    }
}
