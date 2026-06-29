package com.swmanager.system.license.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * License4J 매월 말 자동 연동 스케줄러 (P7, FR-1).
 *
 * Spring cron 은 'L'(말일) 미지원 → 28~31일에 깨워서 isLastDayOfMonth 가드.
 * license4j.sync.enabled=false 로 비활성 가능(롤백/유지보수 안전).
 * 실행 주체 = 시스템(SYSTEM).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LicenseSyncScheduler {

    private final LicenseSyncService syncService;

    @Value("${license4j.sync.enabled:false}")
    private boolean enabled;

    /** 기본: 매월 28~31일 02:00 (Asia/Seoul) 깨움 → 말일에만 실행 */
    @Scheduled(cron = "${license4j.sync.cron:0 0 2 28-31 * *}", zone = "Asia/Seoul")
    public void monthEndSync() {
        if (!enabled) {
            log.debug("license4j 연동 스케줄러 비활성(license4j.sync.enabled=false) — skip");
            return;
        }
        LocalDate today = LocalDate.now();
        if (today.getDayOfMonth() != today.lengthOfMonth()) {
            log.debug("말일 아님({}) — skip", today);
            return;
        }
        log.info("=== License4J 월말 자동 연동 시작 ({}) ===", today);
        try {
            syncService.run("AUTO", "SYSTEM");
        } catch (Exception e) {
            log.error("월말 자동 연동 실패", e);
        }
    }
}
