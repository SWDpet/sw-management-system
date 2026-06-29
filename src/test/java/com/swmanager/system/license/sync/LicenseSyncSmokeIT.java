package com.swmanager.system.license.sync;

import com.swmanager.system.license.domain.LicenseSyncHistory;
import com.swmanager.system.license.repository.LicenseRegistryBackupRepository;
import com.swmanager.system.license.repository.LicenseRegistryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * License4J 연동 end-to-end 스모크 (P10, T-1/T-5 실측).
 *
 * 실제 빈(DerbyLicenseReader→LicenseFieldMapper→LicenseSyncService)을 띄워
 * 라이선스 서버(.188) Derby 사본 읽기 → 매핑 → 공유 upsert → pre-sync 스냅샷 → 이력 기록을
 * 운영DB 대상으로 1회 실행. **운영 대장에 실제 반영**(멱등 + 백업).
 *
 * 게이트: 환경변수 RUN_L4J_SYNC_SMOKE=1 + DB 좌표 + L4J_SYNC_DERBY_UNC/BACKUP_DIR 필요.
 * 미설정 시 스킵(일반 CI 무영향).
 */
@SpringBootTest   // MOCK 웹환경(기본) — SecurityConfig 의 MvcRequestMatcher 가 MVC 컨텍스트 필요
class LicenseSyncSmokeIT {

    @DynamicPropertySource
    static void syncProps(DynamicPropertyRegistry r) {
        r.add("license4j.sync.enabled", () -> "false");   // 스케줄러 자동발화 방지(수동 호출만)
        r.add("license4j.sync.derby-unc", () -> envOr("L4J_SYNC_DERBY_UNC", ""));
        r.add("license4j.sync.backup-dir", () -> envOr("L4J_SYNC_BACKUP_DIR", ""));
    }

    private static String envOr(String k, String def) {
        String v = System.getenv(k);
        return v == null ? def : v;
    }

    @Autowired LicenseSyncService syncService;
    @Autowired LicenseRegistryRepository registryRepository;
    @Autowired LicenseRegistryBackupRepository backupRepository;

    @Test
    void endToEndSync() {
        assumeTrue("1".equals(System.getenv("RUN_L4J_SYNC_SMOKE")), "RUN_L4J_SYNC_SMOKE!=1 → 스킵");

        long before = registryRepository.count();
        System.out.println("[SMOKE] 연동 전 대장 = " + before);

        LicenseSyncHistory h = syncService.run("MANUAL", "smoke-it");

        System.out.printf("[SMOKE] 상태=%s 총=%d 신규=%d 갱신=%d 중복=%d 실패=%d%n",
                h.getStatus(), h.getTotalCount(), h.getNewCount(),
                h.getUpdatedCount(), h.getDuplicateCount(), h.getFailCount());
        System.out.println("[SMOKE] 메시지=" + h.getMessage());
        System.out.println("[SMOKE] 백업본 경로=" + h.getSourceSnapshotPath());

        // 치명 실패가 아니어야 함
        assertThat(h.getStatus()).isIn("SUCCESS", "PARTIAL");
        // pre-sync 스냅샷이 연동 전 대장 건수만큼 남아야 함 (T-5)
        assertThat(backupRepository.countBySnapshotId(h.getSnapshotId())).isEqualTo(before);
        // 총 처리 = 신규+갱신+중복+실패
        assertThat(h.getTotalCount())
                .isEqualTo(h.getNewCount() + h.getUpdatedCount() + h.getDuplicateCount() + h.getFailCount());
    }
}
