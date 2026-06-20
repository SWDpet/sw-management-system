package com.swmanager.system.testsupport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * main 자바 소스 워커 (ratchet 게이트용). target/ 등 생성물 제외(QUALITY_CHARTER ratchet 함정 #2).
 */
public final class MainSources {

    private MainSources() {}

    public static final Path ROOT = Path.of("src", "main", "java");

    public static List<Path> allJava() {
        try (Stream<Path> s = Files.walk(ROOT)) {
            return s.filter(p -> p.toString().endsWith(".java"))
                    .filter(Files::isRegularFile)
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("main 소스 walk 실패", e);
        }
    }

    public static List<String> lines(Path p) {
        try {
            return Files.readAllLines(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("읽기 실패: " + p, e);
        }
    }

    /** 패키지 경로 판별(슬래시 무관). */
    public static boolean isUnder(Path p, String segment) {
        return p.toString().replace('\\', '/').contains("/" + segment + "/");
    }
}
