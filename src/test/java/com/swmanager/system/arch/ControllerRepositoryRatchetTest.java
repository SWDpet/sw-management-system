package com.swmanager.system.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 컨트롤러→Repository 직접접근 부채 ratchet (아키텍처 B→A — 신규 무증가).
 *
 * 본 저장소는 컨트롤러가 단순 read 를 위해 Repository 를 직접 호출하는 실용 패턴이 광범위(현 baseline).
 * 엄격 레이어링(서비스 경유) 전면 전환은 코드품질 대형작업이므로, 여기서는 **총량을 baseline 으로 동결**하고
 * 감소만 허용한다(MapDebt/GiantClass ratchet 과 동일 철학 — QUALITY_CHARTER §0).
 *
 * 갱신(감소 시): {@code GOLDEN_RECORD=1 ./mvnw test -Dtest=ControllerRepositoryRatchetTest} 후 baseline 커밋.
 */
class ControllerRepositoryRatchetTest {

    private static final Path BASELINE =
            Path.of("src", "test", "resources", "golden", "controller-repo-arch-baseline.txt");

    private static final ArchRule CONTROLLER_NO_REPO =
            noClasses().that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..repository..");

    @Test
    void controllerRepositoryAccess_doesNotGrow() throws IOException {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.swmanager.system");

        EvaluationResult result = CONTROLLER_NO_REPO.evaluate(classes);
        int current = result.getFailureReport().getDetails().size();

        if ("1".equals(System.getenv("GOLDEN_RECORD"))) {
            Files.createDirectories(BASELINE.getParent());
            Files.writeString(BASELINE, current + "\n");
            return;
        }

        if (!Files.exists(BASELINE)) {
            throw new AssertionError("controller-repo baseline 없음. GOLDEN_RECORD=1 로 1회 기록 후 커밋.");
        }
        int baseline = Integer.parseInt(Files.readString(BASELINE).strip());

        if (current < baseline) { // 감소분 tighten
            Files.writeString(BASELINE, current + "\n");
        }

        assertThat(current)
                .as("컨트롤러→Repository 직접접근이 baseline(%d)을 초과(%d). 신규 직접접근 금지 — "
                        + "서비스 경유로 추가하거나, 정당한 감소면 GOLDEN_RECORD=1 로 baseline 갱신.", baseline, current)
                .isLessThanOrEqualTo(baseline);
    }
}
