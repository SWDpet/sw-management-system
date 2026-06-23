package com.swmanager.system.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 아키텍처 불변식 게이트 (품질 로드맵: 아키텍처 B→A).
 * "등급은 게이트가 강제하는 불변식"(QUALITY_CHARTER §0) — 레이어 경계·도메인 순수성을 ArchUnit 으로 고정.
 *
 * 통과 규칙은 하드게이트. 컨트롤러→Repository 직접접근(저장소의 의도적 read 패턴)은
 * {@code ControllerRepositoryRatchetTest} 로 count 동결(신규금지).
 *
 * ⚠ 알려진 순환의존 1건(config↔service): 보안핸들러(config)가 LogService/LoginAttemptService 호출 ↔
 *   service 가 {@code CustomUserDetails}·{@code SecurityLoginProperties}(config 소재) 참조. 근본해소 = 이 둘을
 *   security/ 패키지로 이동(단 CustomUserDetails 는 세션 직렬화 대상 → 배포 시 전원 재로그인 부수효과 → 사용자 승인 사안).
 *   해소 후 `slices().beFreeOfCycles()` 게이트 추가 예정.
 */
@AnalyzeClasses(
        packages = "com.swmanager.system",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class LayeredArchitectureTest {

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
