package com.swmanager.system.testsupport;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 소스 위생 게이트 (S-tier). main 자바 소스에 제어문자 손상(NUL 등)이 없어야 한다.
 *
 * 배경: OpsKbController 의 문자열 리터럴에 NUL 바이트(\\0)가 박혀 있어 ripgrep 이 binary 로
 * 오인하고 도구가 막힌 사례(2026-06-20 수정). 재발 방지로 빌드가 차단한다.
 */
class SourceHygieneTest {

    @Test
    void noControlCharCorruption_inMainSources() throws Exception {
        List<String> bad = new ArrayList<>();
        for (Path p : MainSources.allJava()) {
            byte[] bytes = Files.readAllBytes(p);
            for (byte b : bytes) {
                // 허용: 일반 출력 가능 문자 + 탭(9)·LF(10)·CR(13). 그 외 제어문자(특히 NUL=0)는 손상.
                if (b == 0) { bad.add(p + " — NUL(\\0) 포함"); break; }
            }
        }
        assertThat(bad)
                .as("소스에 NUL 바이트 손상. 인코딩 사고로 문자가 \\0 으로 깨진 것 — 해당 리터럴 복구 필요.")
                .isEmpty();
    }
}
