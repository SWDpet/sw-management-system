package com.swmanager.system.arch;

import com.swmanager.system.service.LogService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * S9 CI 게이트 — Deprecated LogService.log(String,String,String) 신규 호출 차단.
 *
 * 이 규칙이 실패하면 빌드도 실패. Enum 오버로드 log(String, AccessActionType, String)
 * 는 시그니처가 다르므로 이 규칙에 걸리지 않음 (비오탐).
 */
@AnalyzeClasses(
        packages = "com.swmanager.system",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class LogServiceUsageArchTest {

    @ArchTest
    static final ArchRule no_deprecated_log_string_overload =
            noClasses().should().callMethod(
                    LogService.class, "log", String.class, String.class, String.class
            ).because("S9: logService.log(String, String, String) is @Deprecated; "
                    + "use log(String, AccessActionType, String) instead.");
}
