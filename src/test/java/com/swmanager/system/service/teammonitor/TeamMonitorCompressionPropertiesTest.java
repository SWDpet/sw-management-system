package com.swmanager.system.service.teammonitor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T20 회귀 방지 (codex 후속 개선 2).
 *
 * application.properties 의 server.compression.mime-types 가 SSE (text/event-stream) 를
 * 절대 포함하지 않음을 빌드시 단정.
 * 누군가 실수로 추가하면 PR 차단.
 */
@SpringBootTest(properties = {
        "spring.main.web-application-type=servlet",
        "server.port=0"
})
class TeamMonitorCompressionPropertiesTest {

    @Autowired
    ServerProperties serverProperties;

    @Test
    void compressionMimeTypes_mustNotContain_textEventStream() {
        String[] mimeTypes = serverProperties.getCompression().getMimeTypes();
        assertThat(mimeTypes).isNotNull();
        assertThat(mimeTypes)
                .as("server.compression.mime-types 에 text/event-stream 포함 시 SSE 가 압축되어 실시간성 깨짐. application.properties 확인.")
                .noneMatch(mt -> mt != null && mt.toLowerCase().contains("event-stream"));
    }
}
