package com.swmanager.system.testsupport;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 엔드포인트 인벤토리 net (net 인프라 #3, QUALITY_CHARTER §4-1).
 *
 * 전 컨트롤러의 (HTTP 메서드 + full URL) 집합을 baseline 으로 박제한다.
 * 컨트롤러 분리(strangler)·리팩토링으로 <b>URL 표면이 바뀌면</b>(이동·삭제·메서드/경로 변경) 빌드 실패로 잡는다.
 *
 * Spring 컨텍스트 없이 classpath 스캔만 사용(가벼움, Docker·DB 불필요).
 * baseline: {@code src/test/resources/golden/endpoint-inventory.txt} (없으면 첫 실행에 기록 → 커밋).
 */
class EndpointInventoryTest {

    private static final String BASE_PKG = "com.swmanager.system";
    private static final Path BASELINE = Path.of("src", "test", "resources", "golden", "endpoint-inventory.txt");

    @Test
    void urlSurface_matchesBaseline() throws Exception {
        TreeSet<String> inventory = collectEndpoints();
        String current = String.join("\n", inventory) + "\n";

        if (!Files.exists(BASELINE)) {
            if (GoldenSnapshot.isRecordMode()) {
                Files.createDirectories(BASELINE.getParent());
                Files.writeString(BASELINE, current);
                System.out.println("[EndpointInventory] baseline 기록: " + BASELINE.toAbsolutePath()
                        + " (" + inventory.size() + " endpoints) — 커밋 필요");
                return;
            }
            throw new AssertionError("endpoint baseline 없음 (" + BASELINE
                    + "). 최초 기록: GOLDEN_RECORD=1 로 1회 실행 후 커밋.");
        }
        String expected = Files.readString(BASELINE).strip();
        assertThat(current.strip())
                .as("URL 표면이 baseline 과 달라짐 — 엔드포인트 이동/삭제/경로·메서드 변경. "
                        + "의도된 변경이면 endpoint-inventory.txt 갱신.")
                .isEqualTo(expected);
    }

    private TreeSet<String> collectEndpoints() throws ClassNotFoundException {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        TreeSet<String> out = new TreeSet<>();
        for (var bd : scanner.findCandidateComponents(BASE_PKG)) {
            Class<?> clazz = Class.forName(bd.getBeanClassName());
            RequestMapping classRm = AnnotatedElementUtils.findMergedAnnotation(clazz, RequestMapping.class);
            List<String> prefixes = pathsOf(classRm);
            for (Method m : clazz.getDeclaredMethods()) {
                RequestMapping rm = AnnotatedElementUtils.findMergedAnnotation(m, RequestMapping.class);
                if (rm == null) continue;
                List<String> methods = httpMethods(rm);
                String produces = producesSuffix(rm);  // 응답 형식 표면(codex): text/html 등 보존 추적
                for (String prefix : prefixes) {
                    for (String sub : pathsOf(rm)) {
                        String full = join(prefix, sub);
                        for (String httpM : methods) {
                            out.add(httpM + " " + full + produces);
                        }
                    }
                }
            }
        }
        return out;
    }

    private static List<String> pathsOf(RequestMapping rm) {
        if (rm == null) return List.of("");
        String[] p = rm.path().length > 0 ? rm.path() : rm.value();
        if (p.length == 0) return List.of("");
        return new ArrayList<>(List.of(p));
    }

    /** produces 가 지정된 경우만 " [produces=...]" 접미사(정렬). 기본(빈값)이면 표기 안 함. */
    private static String producesSuffix(RequestMapping rm) {
        String[] p = rm.produces();
        if (p == null || p.length == 0) return "";
        return " [produces=" + String.join(",", new TreeSet<>(List.of(p))) + "]";
    }

    private static List<String> httpMethods(RequestMapping rm) {
        RequestMethod[] ms = rm.method();
        if (ms.length == 0) return List.of("ANY");
        List<String> out = new ArrayList<>();
        for (RequestMethod m : ms) out.add(m.name());
        return out;
    }

    private static String join(String prefix, String sub) {
        String a = prefix == null ? "" : prefix.trim();
        String b = sub == null ? "" : sub.trim();
        if (b.isEmpty()) return a.isEmpty() ? "/" : a;
        if (a.isEmpty()) return b.startsWith("/") ? b : "/" + b;
        String left = a.endsWith("/") ? a.substring(0, a.length() - 1) : a;
        String right = b.startsWith("/") ? b : "/" + b;
        return left + right;
    }
}
