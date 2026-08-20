package com.auth.gateway.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.client.RestTemplate;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * auth-gateway 模块架构约束（ArchUnit）。 网关为 WebFlux 响应式栈，全局错误由 AbstractErrorWebExceptionHandler
 * 处理，无 @RestControllerAdvice。
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.gateway", importOptions = ImportOption.DoNotIncludeTests.class)
class GatewayArchitectureTest {

	private static final String GATEWAY_ROOT = "..gateway..";

	@ArchTest
	static final ArchRule must_not_declare_rest_template = noClasses().that()
		.resideInAPackage(GATEWAY_ROOT)
		.should()
		.dependOnClassesThat()
		.areAssignableTo(RestTemplate.class);

	@ArchTest
	static final ArchRule no_printStackTrace = noMethods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage(GATEWAY_ROOT)
		.should()
		.haveName("printStackTrace");

}
