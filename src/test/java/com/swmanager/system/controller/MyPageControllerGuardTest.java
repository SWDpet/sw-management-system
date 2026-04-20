package com.swmanager.system.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.swmanager.system.domain.User;
import com.swmanager.system.util.MaskingDetector;
import com.swmanager.system.util.SensitiveMask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MyPageController 가드 행위 검증 (S3-B FR-3 / NFR-3 / NFR-5).
 *
 * 컨트롤러 메서드 직접 호출 대신 가드 로직 단위만 동등 시뮬레이션:
 *   - 입력값/DB값/예상 동작 매트릭스
 *   - log.warn 검증 (값 미포함)
 *   - blockedFields warning message 형식
 *
 * 풀 MockMvc 통합은 Spring Security 의존성으로 별도 분리 (RUN_DB_TESTS).
 */
class MyPageControllerGuardTest {

    private MaskingDetector detector;
    private SensitiveMask sensitiveMask;
    private ListAppender<ILoggingEvent> appender;
    private Logger guardLogger;

    @BeforeEach
    void setUp() {
        sensitiveMask = new SensitiveMask();
        detector = new MaskingDetector();
        try {
            java.lang.reflect.Field f = MaskingDetector.class.getDeclaredField("sensitiveMask");
            f.setAccessible(true);
            f.set(detector, sensitiveMask);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        guardLogger = (Logger) LoggerFactory.getLogger(MyPageController.class);
        appender = new ListAppender<>();
        appender.start();
        guardLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        guardLogger.detachAppender(appender);
    }

    /**
     * 가드 로직을 컨트롤러와 동일하게 시뮬레이션.
     * (실제 컨트롤러 테스트는 Spring 컨텍스트 필요해 분리)
     */
    private GuardResult applyGuard(String tel, String mobile, String email, String ssn, String address, User dbUser) {
        List<String> blockedFields = new java.util.ArrayList<>();
        if (detector.isMaskedTel(tel, dbUser.getTel())) blockedFields.add("tel"); else dbUser.setTel(tel);
        if (detector.isMaskedTel(mobile, dbUser.getMobile())) blockedFields.add("mobile"); else dbUser.setMobile(mobile);
        if (detector.isMaskedEmail(email, dbUser.getEmail())) blockedFields.add("email"); else dbUser.setEmail(email);
        if (detector.isMaskedSsn(ssn, dbUser.getSsn())) blockedFields.add("ssn"); else dbUser.setSsn(ssn);
        if (detector.isMaskedAddress(address, dbUser.getAddress())) blockedFields.add("address"); else dbUser.setAddress(address);

        RedirectAttributesModelMap rttr = new RedirectAttributesModelMap();
        if (!blockedFields.isEmpty()) {
            org.slf4j.LoggerFactory.getLogger(MyPageController.class).warn(
                "MASKING_GUARD_BLOCKED: userid={} fields={}", dbUser.getUserid(), blockedFields);
            rttr.addFlashAttribute("warningMessage",
                "다음 필드는 마스킹된 값이 감지되어 변경되지 않았습니다: " + String.join(", ", blockedFields));
        }
        return new GuardResult(blockedFields, rttr, dbUser);
    }

    private static User user(String userid, String tel, String mobile, String email, String ssn, String address) {
        User u = new User();
        u.setUserid(userid);
        u.setTel(tel); u.setMobile(mobile); u.setEmail(email); u.setSsn(ssn); u.setAddress(address);
        return u;
    }

    static class GuardResult {
        List<String> blockedFields;
        RedirectAttributesModelMap rttr;
        User user;
        GuardResult(List<String> b, RedirectAttributesModelMap r, User u) { blockedFields=b; rttr=r; user=u; }
    }

    // ─────────────────────────────────
    // T-A: 모두 정상 입력 → 모두 저장 + warningMessage 없음
    // ─────────────────────────────────
    @Test
    void allNormalInput_savesAll_noWarning() {
        User dbUser = user("ukjin914", "070-1111-2222", "010-3333-4444", "old@x.com", "901201-1234567", "구주소");
        GuardResult r = applyGuard("070-7113-8093", "010-3056-2678", "ukjin914@uitgis.com", "901201-1234567", "신주소", dbUser);

        assertThat(r.blockedFields).isEmpty();
        assertThat(r.user.getTel()).isEqualTo("070-7113-8093");
        assertThat(r.user.getEmail()).isEqualTo("ukjin914@uitgis.com");
        assertThat(r.rttr.getFlashAttributes()).doesNotContainKey("warningMessage");
        assertThat(appender.list).noneMatch(e -> e.getLevel() == Level.WARN);
    }

    // ─────────────────────────────────
    // T-B: 모두 마스킹 입력 → 모두 차단 + warningMessage 에 5개 필드 모두
    // ─────────────────────────────────
    @Test
    void allMaskedFields_keepsDbValue_addsWarningMessage() {
        User dbUser = user("ukjin914", "070-7113-8093", "010-3056-2678", "ukjin914@uitgis.com", "901201-1234567", "서울 강남구");
        GuardResult r = applyGuard("070-****-8093", "010-****-2678", "u***@uitgis.com", "901201-1******", "*** ***", dbUser);

        assertThat(r.blockedFields).containsExactly("tel", "mobile", "email", "ssn", "address");
        // DB 값 유지
        assertThat(r.user.getTel()).isEqualTo("070-7113-8093");
        assertThat(r.user.getEmail()).isEqualTo("ukjin914@uitgis.com");
        // warningMessage 존재 + 5개 필드 모두 포함
        Object msg = r.rttr.getFlashAttributes().get("warningMessage");
        assertThat(msg).asString()
            .contains("tel").contains("mobile").contains("email").contains("ssn").contains("address");
    }

    // ─────────────────────────────────
    // T-C: 일부만 마스킹 (tel만) → tel 유지, 나머지 정상 변경
    // ─────────────────────────────────
    @Test
    void mixedMaskedAndNormal_savesNonMaskedOnly() {
        User dbUser = user("ukjin914", "070-7113-8093", "010-old-old", "old@x.com", "901201-1234567", "구주소");
        GuardResult r = applyGuard("070-****-8093", "010-9999-8888", "new@x.com", "901201-1234567", "신주소", dbUser);

        assertThat(r.blockedFields).containsExactly("tel");
        assertThat(r.user.getTel()).isEqualTo("070-7113-8093");        // 유지
        assertThat(r.user.getMobile()).isEqualTo("010-9999-8888");      // 변경
        assertThat(r.user.getEmail()).isEqualTo("new@x.com");           // 변경
        assertThat(r.user.getAddress()).isEqualTo("신주소");             // 변경
    }

    // ─────────────────────────────────
    // T-C2: NULL 입력 안전 통과 (NPE 없음, 가드 비발동)
    // ─────────────────────────────────
    @Test
    void nullInputs_passThrough_noBlocked() {
        User dbUser = user("ukjin914", "070-7113-8093", "010-3056-2678", "ukjin914@uitgis.com", null, null);
        // null 입력 → 가드 통과 (isMasked* 가 input==null 시 false 반환)
        // setXxx(null) 이 호출되어 DB 가 null 로 갱신되지만 가드 자체는 발동 X
        GuardResult r = applyGuard(null, null, null, null, null, dbUser);

        assertThat(r.blockedFields).isEmpty();
        assertThat(r.user.getTel()).isNull();
        assertThat(r.rttr.getFlashAttributes()).doesNotContainKey("warningMessage");
    }

    // ─────────────────────────────────
    // T-D: WARN 로그 검증 — userid + 필드명 포함, 값 미포함
    // ─────────────────────────────────
    @Test
    void blockedFields_logsWarnWithUseridAndFieldsOnly_noValues() {
        User dbUser = user("ukjin914", "070-7113-8093", "010-3056-2678", "ukjin914@uitgis.com", "901201-1234567", "서울");
        applyGuard("070-****-8093", "010-3056-2678", "ukjin914@uitgis.com", "901201-1234567", "서울", dbUser);

        assertThat(appender.list).anyMatch(e ->
            e.getLevel() == Level.WARN
            && e.getFormattedMessage().contains("MASKING_GUARD_BLOCKED")
            && e.getFormattedMessage().contains("ukjin914")
            && e.getFormattedMessage().contains("tel")
            // 값 (마스킹된 070-****-8093 등) 은 절대 포함 X
            && !e.getFormattedMessage().contains("****")
            && !e.getFormattedMessage().contains("8093"));
    }
}
