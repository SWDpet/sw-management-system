package com.swmanager.system.license.sync;

import com.swmanager.system.license.domain.LicenseRegistry;
import com.swmanager.system.license.domain.LicenseSyncHistory;
import com.swmanager.system.license.repository.LicenseRegistryRepository;
import com.swmanager.system.license.repository.LicenseSyncHistoryRepository;
import com.swmanager.system.license.service.LicenseRegistryService;
import com.swmanager.system.license.service.LicenseRegistryService.UpsertDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * License4J 매월 연동 오케스트레이션 (P6).
 *
 * run(): 동시실행 가드 → 이력 RUNNING → pre-sync 스냅샷(커밋) → Derby 사본 읽기
 *        → 매핑/preflight → 공유 upsert 코어(배치, 트랜잭션) → 이력 마감.
 * 롤백 기준: 건별 실패=PARTIAL(성공분 유지) / 치명적 실패=FAIL(업서트 배치 트랜잭션 자동 롤백, 대장 불변)
 *           + pre-sync 스냅샷은 별도 커밋되어 감사·수동 복원 안전망.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseSyncService {

    private final DerbyLicenseReader derbyReader;
    private final LicenseFieldMapper mapper;
    private final LicenseRegistryService registryService;
    private final LicenseRegistryRepository registryRepository;
    private final LicenseSyncHistoryRepository syncHistoryRepository;
    private final JdbcTemplate jdbcTemplate;

    /** 업서트 배치 트랜잭션을 위한 self-proxy */
    @Autowired @Lazy
    private LicenseSyncService self;

    /** 집계 */
    static class Counters {
        int total, neu, updated, duplicate, fail;
        List<String> errors = new ArrayList<>();
    }

    /**
     * 연동 1회 실행.
     * @param runType     AUTO(스케줄러) / MANUAL(ADMIN)
     * @param triggeredBy 수동=사용자ID / 자동=SYSTEM
     */
    public LicenseSyncHistory run(String runType, String triggeredBy) {
        // 1차(빠른) 가드 + 2차 원자 가드(uq_license_sync_running 부분 유니크 인덱스).
        if (syncHistoryRepository.countByStatus("RUNNING") > 0)
            throw new IllegalStateException("이미 진행 중인 라이선스 연동이 있습니다.");

        LicenseSyncHistory h = new LicenseSyncHistory();
        h.setRunType(runType);
        h.setStatus("RUNNING");
        h.setStartedAt(LocalDateTime.now());
        h.setTriggeredBy(triggeredBy);
        try {
            h = syncHistoryRepository.saveAndFlush(h);   // 제약 즉시 검사
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("이미 진행 중인 라이선스 연동이 있습니다.");
        }

        String snapshotId = UUID.randomUUID().toString();
        try {
            // 1) pre-sync 스냅샷 (별도 커밋 — 감사/안전망)
            int snap = takeSnapshot(snapshotId, h.getId());
            h.setSnapshotId(snapshotId);
            log.info("pre-sync 스냅샷 {}건 (snapshotId={})", snap, snapshotId);

            // 2) Derby 사본 복사 + 읽기
            DerbyLicenseReader.ReadResult rr = derbyReader.copyAndRead(LocalDateTime.now());
            h.setSourceSnapshotPath(rr.backupPath().toString());

            // 3) 매핑 + 공유 upsert (배치 트랜잭션)
            Counters c = self.applyUpserts(rr.rows(), triggeredBy);

            h.setTotalCount(c.total);
            h.setNewCount(c.neu);
            h.setUpdatedCount(c.updated);
            h.setDuplicateCount(c.duplicate);
            h.setFailCount(c.fail);
            h.setStatus(c.fail > 0 ? "PARTIAL" : "SUCCESS");
            h.setMessage(buildMessage(c));
            log.info("연동 완료 - 총:{} 신규:{} 갱신:{} 중복:{} 실패:{}",
                    c.total, c.neu, c.updated, c.duplicate, c.fail);

        } catch (Exception e) {
            h.setStatus("FAIL");
            h.setMessage("치명적 실패: " + safeMsg(e));   // 업서트 배치는 트랜잭션 롤백되어 대장 불변
            log.error("라이선스 연동 실패", e);
        } finally {
            h.setFinishedAt(LocalDateTime.now());
            h = syncHistoryRepository.save(h);
        }
        return h;
    }

    /**
     * 매핑 + 공유 upsert 코어 호출 + 배치 저장 (원자적).
     * 치명적 예외 시 전체 롤백 → 대장 불변. (건별 매핑/판정 실패는 fail 집계 후 계속)
     */
    @Transactional
    public Counters applyUpserts(List<Map<String, Object>> rows, String triggeredBy) {
        Counters c = new Counters();
        c.total = rows.size();
        List<LicenseRegistry> toSave = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            LicenseFieldMapper.MapResult mr = mapper.map(row);
            if (!mr.isOk()) {
                c.fail++;
                if (c.errors.size() < 50) c.errors.add(mr.error());
                continue;
            }
            UpsertDecision d = registryService.classifyUpsert(mr.registry(), triggeredBy);
            switch (d.outcome()) {
                case NEW       -> { toSave.add(d.toSave()); c.neu++; }
                case UPDATED   -> { toSave.add(d.toSave()); c.updated++; }
                case DUPLICATE -> c.duplicate++;
                case FAILED    -> c.fail++;
            }
        }
        if (!toSave.isEmpty()) registryRepository.saveAll(toSave);
        return c;
    }

    /**
     * license_registry 전량 → license_registry_backup (jsonb payload). 반환=백업 행수.
     * 단일 INSERT…SELECT 문이라 JdbcTemplate autocommit 으로 원자적(별도 @Transactional 불필요).
     */
    public int takeSnapshot(String snapshotId, Long historyId) {
        return jdbcTemplate.update(
            "INSERT INTO license_registry_backup " +
            "(snapshot_id, snapshot_at, sync_history_id, registry_id, license_id, product_id, payload) " +
            "SELECT ?, now(), ?, id, license_id, product_id, to_jsonb(lr) " +
            "FROM license_registry lr",
            snapshotId, historyId);
    }

    /**
     * 스냅샷에서 대장 전량 복원 (P6 disaster-recovery). 트랜잭션 내 전량 교체.
     * 정상 경로의 1차 안전장치는 applyUpserts 트랜잭션 자동 롤백이며, 본 메서드는 수동 복구용.
     * @return 복원된 행수
     */
    @Transactional
    public int restoreFromSnapshot(String snapshotId) {
        jdbcTemplate.update("DELETE FROM license_registry");
        return jdbcTemplate.update(
            "INSERT INTO license_registry " +
            "SELECT (jsonb_populate_record(null::license_registry, payload)).* " +
            "FROM license_registry_backup WHERE snapshot_id = ?",
            snapshotId);
    }

    private String buildMessage(Counters c) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("총 %d / 신규 %d / 갱신 %d / 중복 %d / 실패 %d",
                c.total, c.neu, c.updated, c.duplicate, c.fail));
        if (!c.errors.isEmpty()) {
            sb.append(" | 실패사유(최대50): ");
            sb.append(String.join("; ", c.errors));
        }
        return sb.length() > 4000 ? sb.substring(0, 4000) : sb.toString();
    }

    private String safeMsg(Exception e) {
        String m = e.getMessage();
        return m == null ? e.getClass().getSimpleName() : m;
    }
}
