package com.swmanager.system.service.teammonitor;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * 가상 팀 status 파일 변경 감지 추상화.
 * (sprint team-monitor-dashboard, 개발계획 Step 2-1)
 * (sprint team-monitor-wildcard-watcher: 와일드카드 + meta reload + team deleted 콜백 추가)
 *
 * 구현체:
 *  - JavaNioWatcher  : WatchService 기반 (NIO).
 *  - PollingWatcher  : lastModifiedTime 폴링 (fallback).
 */
public interface TeamStatusWatcher {

    void start();

    void stop();

    boolean isAlive();

    /** 마지막으로 변경 이벤트가 발화된 시각. 한 번도 없었으면 null. */
    Instant lastEventAt();

    /**
     * 변경된 팀 이름 콜백 등록 (CREATE/MODIFY).
     * 팀 이름은 디렉토리 스캔 결과 — 정해진 5팀에 한정되지 않음 (FR-1/2).
     */
    void subscribe(Consumer<String> onChangedTeam);

    /**
     * teams.json 메타데이터 변경 콜백 등록 (FR-2-d / FR-4-c).
     * Service 가 이 콜백을 받아 모든 emitter 에 snapshot 재발행.
     */
    void subscribeMetaChange(Runnable onMetaChange);

    /**
     * status 파일 삭제 (팀 삭제) 콜백 등록 (C3-01-B).
     * Service 가 이 콜백을 받아 latestCache 정리 + snapshot 재발행.
     */
    void subscribeTeamDeleted(Consumer<String> onTeamDeleted);

    /** 마지막 에러 메시지 (헬스 표기용). 없으면 null. */
    String lastError();

    /** watcher 가 사용 중인 모드 ("nio" | "polling"). */
    String mode();

    /** 재시작 누적 횟수. */
    int restartCount();
}
