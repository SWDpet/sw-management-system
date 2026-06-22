package com.swmanager.system.dto.inspection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SiteNotMappedError 직렬화 골든 테스트 (qrbatch-adminuser-rows-dto §6-4, C1).
 *
 * InspectionQrBatchController.handleSiteNotMapped 의 컨트롤러-로컬 LinkedHashMap(error/site/hint)
 * 응답조립을 record 로 바꾸면서 422 응답 JSON 무손실(키셋·값)을 고정한다. JsonNode tree 동치.
 */
class SiteNotMappedErrorTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void siteNotMappedError_matchesLegacy() {
        String site = "108";
        String hint = "사업관리 화면에서 해당 사이트의 site_code 를 설정하세요";
        Map<String, Object> legacy = new LinkedHashMap<>();
        legacy.put("error", "site_not_mapped");
        legacy.put("site", site);
        legacy.put("hint", hint);

        JsonNode a = om.valueToTree(new SiteNotMappedError("site_not_mapped", site, hint));
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
        assertThat(a.size()).isEqualTo(3);
        assertThat(a.get("error").asText()).isEqualTo("site_not_mapped");
    }
}
