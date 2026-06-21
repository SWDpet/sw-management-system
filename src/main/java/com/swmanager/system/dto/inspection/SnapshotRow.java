package com.swmanager.system.dto.inspection;

/**
 * /api/inspect-snapshots (현장 수집 스냅샷 최신 1건/host) 응답 행 dto.
 *
 * 기존 InspectReportController 의 컨트롤러-로컬 LinkedHashMap 응답조립을 record 로 대체한다
 * (§6-4 inspect-report-rows-dto). 키셋·값 동치로 무손실. camelCase 키=컴포넌트명.
 * cpu/memory/disk 는 {@code extractSnapshotSpecs} 결과(allowlist 4필드, null 가능)를 호출부에서 주입한다.
 */
public record SnapshotRow(String serverRole, String hostName, String cpu, String memory, String disk) {
}
