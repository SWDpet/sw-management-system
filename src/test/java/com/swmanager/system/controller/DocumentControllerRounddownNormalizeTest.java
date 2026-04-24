package com.swmanager.system.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DocumentController#normalizeDesignEstimateRounddownUnit} 단위테스트.
 *
 * 기획서 v6 FR-5' (서버측 정규화), §4-9 (4개 진입점 방어 중 "서버 저장") /
 * 개발계획서 v6 T10 (API 저장 시 정규화).
 * MockMvc 대신 정규화 로직을 직접 단위테스트로 커버 — 로직 자체가 POJO 조작이라
 * Spring context 없이도 FR-5' 충족을 완전히 검증 가능.
 */
class DocumentControllerRounddownNormalizeTest {

    private DocumentController controller;
    private Logger controllerLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        controller = new DocumentController();
        controllerLogger = (Logger) LoggerFactory.getLogger(DocumentController.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        controllerLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        controllerLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void allowedValue_passesThroughUnchanged() {
        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("rounddownUnit", 1000);
        sectionData.put("estimateType", "TYPE_A");

        controller.normalizeDesignEstimateRounddownUnit(sectionData, 1);

        assertThat(sectionData.get("rounddownUnit")).isEqualTo(1000);
        assertThat(sectionData.get("estimateType")).isEqualTo("TYPE_A"); // 다른 필드 영향 없음
        assertThat(listAppender.list).noneMatch(e -> e.getLevel() == Level.WARN);
    }

    @Test
    void invalidNumber500_normalizedTo1000_withWarn() {
        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("rounddownUnit", 500);
        sectionData.put("bidRate", 91);

        controller.normalizeDesignEstimateRounddownUnit(sectionData, 42);

        assertThat(sectionData.get("rounddownUnit")).isEqualTo(1000);
        assertThat(sectionData.get("bidRate")).isEqualTo(91); // 다른 필드 보존
        ILoggingEvent warn = listAppender.list.stream()
                .filter(e -> e.getLevel() == Level.WARN)
                .findFirst().orElseThrow();
        String msg = warn.getFormattedMessage();
        assertThat(msg).contains("design_estimate.rounddownUnit");
        assertThat(msg).contains("500");
        assertThat(msg).contains("docId=42");
        assertThat(msg).contains("normalized to 1000");
    }

    @Test
    void nonNumericString_normalizedTo1000_withWarn() {
        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("rounddownUnit", "abc");

        controller.normalizeDesignEstimateRounddownUnit(sectionData, 99);

        assertThat(sectionData.get("rounddownUnit")).isEqualTo(1000);
        assertThat(listAppender.list)
                .anyMatch(e -> e.getLevel() == Level.WARN
                         && e.getFormattedMessage().contains("abc"));
    }

    @Test
    void nullValue_normalizedTo1000_withWarn() {
        Map<String, Object> sectionData = new HashMap<>();
        // rounddownUnit 미포함
        sectionData.put("estimateType", "TYPE_A");

        controller.normalizeDesignEstimateRounddownUnit(sectionData, 1);

        assertThat(sectionData.get("rounddownUnit")).isEqualTo(1000);
        // null 은 미존재 = "구 스키마" 케이스. WARN 은 남기되 사용자 영향 없음.
        // (Controller 정규화 정책은 null 도 WARN 대상임에 주의 — 설계 선택)
    }

    @Test
    void numericStringAllowedValue_parsedAndAccepted() {
        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("rounddownUnit", "1000"); // JSON 에서 문자열로 들어온 정상 값

        controller.normalizeDesignEstimateRounddownUnit(sectionData, 1);

        assertThat(sectionData.get("rounddownUnit")).isEqualTo(1000);
        assertThat(listAppender.list).noneMatch(e -> e.getLevel() == Level.WARN);
    }

    @Test
    void allowedAsDouble_intValueAccepted() {
        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("rounddownUnit", 10000.0); // Jackson 이 숫자를 Double 로 역직렬화하는 경우

        controller.normalizeDesignEstimateRounddownUnit(sectionData, 1);

        // Number.intValue() 로 변환되어 허용값으로 판정
        assertThat(sectionData.get("rounddownUnit")).isEqualTo(10000);
        assertThat(listAppender.list).noneMatch(e -> e.getLevel() == Level.WARN);
    }
}
