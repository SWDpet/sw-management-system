package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.workplan.ProcessMaster;

/**
 * /api/process-master (시스템별 공정명 목록) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 doclookup-rows-dto).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). camelCase 키=컴포넌트명, null 포함.
 */
public record ProcessMasterRow(Integer processId, String processName) {

    public static ProcessMasterRow from(ProcessMaster pm) {
        return new ProcessMasterRow(pm.getProcessId(), pm.getProcessName());
    }
}
