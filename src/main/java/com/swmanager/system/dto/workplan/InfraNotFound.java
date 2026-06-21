package com.swmanager.system.dto.workplan;

/**
 * /api/infra-find 미발견 응답 dto — `{found:false}` 단일키.
 *
 * 발견({@link InfraFindResult}, 6키)과 키셋이 달라(미발견은 found 1키만) 별도 타입으로 분리한다
 * (§6-4 doclookup-workplan-rows-dto, codex P2). 현행 미발견 HashMap(`{found:false}`)과 무손실 동치.
 */
public record InfraNotFound(boolean found) {

    public static InfraNotFound instance() {
        return new InfraNotFound(false);
    }
}
