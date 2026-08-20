package com.auth.service.system.architecture;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.exception.ValidationExceptionHandler;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * service-system 模块化单体架构约束（ArchUnit）。
 * <p>
 * 扫描 classpath 上全部 com.auth.service.system 域代码（含子模块 jar）。
 * </p>
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.service.system", importOptions = ImportOption.DoNotIncludeTests.class)
class SystemArchitectureTest {

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_mappers = noClasses().that()
		.resideInAPackage("..controller..")
		.should()
		.dependOnClassesThat()
		.haveSimpleNameEndingWith("Mapper");

	@ArchTest
	static final ArchRule business_exception_handlers_must_not_extend_validation_handler = noClasses().that()
		.resideInAPackage("..common.exception..")
		.and()
		.areAnnotatedWith(RestControllerAdvice.class)
		.should()
		.beAssignableTo(ValidationExceptionHandler.class);

	/**
	 * 常规 JSON API 返回 {@link Result}；二进制文件下载（如 Excel 导出）允许 {@link ResponseEntity}。
	 */
	@ArchTest
	static final ArchRule controller_public_api_methods_return_result = methods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage("..controller..")
		.and()
		.arePublic()
		.and()
		.doNotHaveName("equals")
		.and()
		.doNotHaveName("hashCode")
		.and()
		.doNotHaveName("toString")
		.and()
		.doNotHaveRawReturnType(ResponseEntity.class)
		.should()
		.haveRawReturnType(Result.class);

	/**
	 * com.auth.service.system.common 包仅允许出现在 common 模块，admin 模块不得残留同名包。
	 */
	@ArchTest
	static final ArchRule common_package_must_not_reside_in_admin_module = classes().that()
		.resideInAPackage("com.auth.service.system.common..")
		.should(new ArchCondition<>("not originate from service-system-admin module") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents events) {
				javaClass.getSource().ifPresent(source -> {
					if (source.toString().contains("service-system-admin")) {
						events.add(SimpleConditionEvent.violated(javaClass,
								javaClass.getFullName() + " must live in service-system-common, not admin"));
					}
				});
			}
		});

	private static final String SYSTEM_ROOT = "..service.system..";

	/**
	 * 允许通过 Starter 注入 {@link RestTemplate}，禁止在模块内 new RestTemplate() 重复造轮子。
	 */
	@ArchTest
	static final ArchRule must_not_instantiate_rest_template = noClasses().that()
		.resideInAPackage(SYSTEM_ROOT)
		.should()
		.callConstructor(RestTemplate.class);

	@ArchTest
	static final ArchRule no_printStackTrace = noMethods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage(SYSTEM_ROOT)
		.should()
		.haveName("printStackTrace");

	private static final String ADMIN = "com.auth.service.system.admin..";

	private static final String SCHEDULE = "com.auth.service.system.schedule..";

	/**
	 * schedule 调用 message 时只允许依赖 message.service 包（Maven 模块依赖已约束模块级边界）。
	 */
	@ArchTest
	static final ArchRule schedule_must_not_depend_on_message_internals = noClasses().that()
		.resideInAPackage(SCHEDULE)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("com.auth.service.system.message.controller..", "com.auth.service.system.message.channel..",
				"com.auth.service.system.message.config..", "com.auth.service.system.message.convert..",
				"com.auth.service.system.message.dispatch..", "com.auth.service.system.message.mapper..",
				"com.auth.service.system.message.model..", "com.auth.service.system.message.support..");

	private static final String MESSAGE = "com.auth.service.system.message..";

	@ArchTest
	static final ArchRule admin_must_not_depend_on_message_or_schedule = noClasses().that()
		.resideInAPackage(ADMIN)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(MESSAGE, SCHEDULE);

	@ArchTest
	static final ArchRule message_and_schedule_must_not_use_feign_client = noClasses().that()
		.resideInAnyPackage(MESSAGE, SCHEDULE)
		.should()
		.dependOnClassesThat()
		.resideInAPackage("com.auth.service.system.authorization.feign..");

	private static final String AUTHORIZATION_ROOT = "com.auth.service.system.authorization";

	@ArchTest
	static final ArchRule authorization_must_not_depend_on_admin = noClasses().that()
		.resideInAPackage(AUTHORIZATION_ROOT + "..")
		.should()
		.dependOnClassesThat()
		.resideInAPackage(ADMIN);

	private static final DescribedPredicate<JavaClass> FORBIDDEN_DEPENDENCIES_FOR_MESSAGE = new DescribedPredicate<>(
			"admin or authorization") {
		@Override
		public boolean test(JavaClass javaClass) {
			String pkg = javaClass.getPackageName();
			return pkg.startsWith("com.auth.service.system.admin") || pkg.startsWith(AUTHORIZATION_ROOT);
		}
	};

	/**
	 * message 不得依赖 admin / authorization；与 schedule 的集成由 Maven 模块依赖约束。
	 */
	@ArchTest
	static final ArchRule message_must_not_depend_on_other_domains = noClasses().that()
		.resideInAPackage(MESSAGE)
		.should()
		.dependOnClassesThat(FORBIDDEN_DEPENDENCIES_FOR_MESSAGE);

	private static final String AUTHORIZATION_DISPATCH_TRIGGER = AUTHORIZATION_ROOT + ".dispatch.trigger";

	private static final String AUTHORIZATION_DISPATCH_QUERY = AUTHORIZATION_ROOT + ".dispatch.query";

	private static final DescribedPredicate<JavaClass> AUTHORIZATION_INTERNALS_FOR_ADMIN = new DescribedPredicate<>(
			"authorization internals except dispatch trigger and query facades") {
		@Override
		public boolean test(JavaClass javaClass) {
			String pkg = javaClass.getPackageName();
			if (!pkg.startsWith(AUTHORIZATION_ROOT)) {
				return false;
			}
			return !(pkg.startsWith(AUTHORIZATION_DISPATCH_TRIGGER) || pkg.startsWith(AUTHORIZATION_DISPATCH_QUERY));
		}
	};

	/**
	 * admin 访问 authorization 仅允许依赖对外门面：dispatch.trigger（写）与 dispatch.query（读）。
	 */
	@ArchTest
	static final ArchRule admin_authorization_coupling_only_via_facade = noClasses().that()
		.resideInAPackage(ADMIN)
		.should()
		.dependOnClassesThat(AUTHORIZATION_INTERNALS_FOR_ADMIN);

	private static final String AUTHORIZATION_FEIGN = AUTHORIZATION_ROOT + ".feign..";

	@ArchTest
	static final ArchRule admin_must_not_depend_on_authorization_feign = noClasses().that()
		.resideInAPackage(ADMIN)
		.should()
		.dependOnClassesThat()
		.resideInAPackage(AUTHORIZATION_FEIGN);

	private static final DescribedPredicate<JavaClass> SERVICE_SYSTEM_FEIGN_CLIENT = new DescribedPredicate<>(
			"FeignClient targeting service-system") {
		@Override
		public boolean test(JavaClass javaClass) {
			if (!javaClass.isAnnotatedWith(FeignClient.class)) {
				return false;
			}
			FeignClient feignClient = javaClass.getAnnotationOfType(FeignClient.class);
			return "service-system".equals(feignClient.name());
		}
	};

	/**
	 * service-system 进程内不得通过 Feign 回调自身（应使用 *-api Port 本地实现）。
	 */
	@ArchTest
	static final ArchRule system_modules_must_not_use_self_feign = noClasses().that()
		.resideInAPackage("com.auth.service.system..")
		.should()
		.dependOnClassesThat(SERVICE_SYSTEM_FEIGN_CLIENT);

	private static final String MODULE_FILE_FEIGN = "com.auth.module.file.api.feign..";

	@ArchTest
	static final ArchRule admin_must_not_depend_on_module_file_feign = noClasses().that()
		.resideInAPackage(ADMIN)
		.should()
		.dependOnClassesThat()
		.resideInAPackage(MODULE_FILE_FEIGN);

	private static final String ADMIN_FEIGN = ADMIN + "feign..";

	@ArchTest
	static final ArchRule admin_must_not_depend_on_admin_feign = noClasses().that()
		.resideInAPackage(ADMIN)
		.should()
		.dependOnClassesThat()
		.resideInAPackage(ADMIN_FEIGN);

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
