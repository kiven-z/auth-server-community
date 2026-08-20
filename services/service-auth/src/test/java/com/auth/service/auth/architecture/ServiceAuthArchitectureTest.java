package com.auth.service.auth.architecture;

import com.auth.common.core.model.response.Result;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.springframework.web.client.RestTemplate;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * service-auth 模块轻量架构约束（ArchUnit）。
 * <p>
 * 仅保留与跨模块契约、统一 API 响应相关的底线检查，不强制分层与目录目标态。
 * </p>
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.service.auth", importOptions = ImportOption.DoNotIncludeTests.class)
@DisplayName("service-auth 架构约束（底线）")
class ServiceAuthArchitectureTest {

	private static final String AUTH_ROOT = "..service.auth..";

	@ArchTest
	static final ArchRule no_printStackTrace = noMethods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage(AUTH_ROOT)
		.should()
		.haveName("printStackTrace");

	@ArchTest
	static final ArchRule must_not_declare_rest_template = noClasses().that()
		.resideInAPackage(AUTH_ROOT)
		.should()
		.dependOnClassesThat()
		.areAssignableTo(RestTemplate.class);

	private static final String AUTH_CONTROLLER = "..controller..";

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_mappers = noClasses().that()
		.resideInAPackage(AUTH_CONTROLLER)
		.should()
		.dependOnClassesThat()
		.haveSimpleNameEndingWith("Mapper");

	@ArchTest
	static final ArchRule controller_public_api_methods_return_result = methods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage(AUTH_CONTROLLER)
		.and()
		.arePublic()
		.and()
		.doNotHaveName("equals")
		.and()
		.doNotHaveName("hashCode")
		.and()
		.doNotHaveName("toString")
		.should()
		.haveRawReturnType(Result.class);

	private static final String SYSTEM_IMPLEMENTATION = "com.auth.service.system..";

	@ArchTest
	static final ArchRule must_not_depend_on_system_implementation = noClasses().that()
		.resideInAPackage(AUTH_ROOT)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(SYSTEM_IMPLEMENTATION);

	private static final String DATA_SCOPE_ANNOTATION = "com.auth.module.security.datapermission.annotation.DataScope";

	/**
	 * @DataScope 按 MappedStatement ID 解析，仅 Mapper 方法生效。
	 */
	@ArchTest
	static final ArchRule data_scope_only_on_mapper_methods = methods().that()
		.areAnnotatedWith(DATA_SCOPE_ANNOTATION)
		.should()
		.beDeclaredInClassesThat()
		.haveSimpleNameEndingWith("Mapper")
		.because("@DataScope is resolved via MappedStatement ID and only takes effect on Mapper methods")
		.allowEmptyShould(true);

}
