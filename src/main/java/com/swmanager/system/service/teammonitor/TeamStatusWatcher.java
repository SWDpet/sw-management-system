package com.swmanager.system.service.teammonitor;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * 가상 팀 status 파일 변경 감지 추상화.
 * (sprint team-monitor-dashboard, 개발계획 Step 2-1)
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

    /** 변경된 팀 이름(planner|db|developer|codex) 콜백 등록. */
    void subscribe(Consumer<String> onChangedTeam);

    /** 마지막 에러 메시지 (헬스 표기용). 없으면 null. */
    String lastError();

    /** watcher 가 사용 중인 모드 ("nio" | "polling"). */
    String mode();

    /** 재시작 누적 횟수. */
    int restartCount();
}
