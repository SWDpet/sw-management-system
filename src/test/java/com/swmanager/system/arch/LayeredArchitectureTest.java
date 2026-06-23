package com.swmanager.system.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 아키텍처 불변식 게이트 (품질 로드맵: 아키텍처 A).
 * "등급은 게이트가 강제하는 불변식"(QUALITY_CHARTER §0) — 레이어 경계·도메인 순수성·순환의존 0 을 ArchUnit 으로 고정.
 *
 * 통과 규칙은 하드게이트. 컨트롤러→Repository 직접접근(저장소의 의도적 read 패턴)은
 * {@code ControllerRepositoryRatchetTest} 로 count 동결(신규금지).
 *
 * 2026-06-23: config↔service 순환 해소(CustomUserDetails·SecurityLoginProperties 를 security/ 로 이동) → R1 순환 게이트 추가.
 */
@AnalyzeClasses(
        packages = "com.swmanager.system",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class LayeredArchitectureTest {

    // R1. 패키지 슬라이스 순환의존 0
    @ArchTest
    static final ArchRule no_package_cycles =
            slices().matching("com.swmanager.system.(*)..").should().beFreeOfCycles();

    // R2. 도메인 순수성: 엔티티는 controller/service/dto 에 의존하지 않음
    @ArchTest
    static final ArchRule domain_should_not_depend_on_upper_layers =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..controller..", "..service..", "..dto..");

    // R3. 상향 의존 금지: 컨트롤러는 service/repository 에게 의존받지 않음
    @ArchTest
    static final ArchRule controllers_should_not_be_depended_on =
            noClasses().that().resideInAPackage("..service..").or().resideInAPackage("..repository..")
                    .should().dependOnClassesThat().resideInAPackage("..controller..");

    // R4. Repository 는 인터페이스
    @ArchTest
    static final ArchRule repositories_should_be_interfaces =
            classes().that().resideInAPackage("..repository..").and().haveSimpleNameEndingWith("Repository")
                    .should().beInterfaces();

    // R5. 명명: controller 패키지 최상위 클래스는 *Controller
    @ArchTest
    static final ArchRule controllers_named_controller =
            classes().that().resideInAPackage("..controller..").and().areNotInterfaces().and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Controller");
}
