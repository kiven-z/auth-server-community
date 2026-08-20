package com.auth.module.security.datapermission.architecture;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.springframework.context.annotation.Bean;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * 数据权限模块架构约束
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.module.security.datapermission",
		importOptions = ImportOption.DoNotIncludeTests.class)
@DisplayName("data-permission 架构约束")
class DataPermissionArchitectureTest {

	/**
	 * 外层 MybatisPlusInterceptor 只能由 BaseMybatisPlusConfig 注册；本模块只贡献 InnerInterceptor
	 */
	@ArchTest
	static final ArchRule must_not_register_mybatis_plus_interceptor_bean = methods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage("com.auth.module.security.datapermission..")
		.and()
		.areAnnotatedWith(Bean.class)
		.should()
		.haveRawReturnType(DescribedPredicate.not(JavaClass.Predicates.assignableTo(MybatisPlusInterceptor.class)))
		.because("Duplicate MybatisPlusInterceptor beans cause double LIMIT; contribute InnerInterceptor only")
		.allowEmptyShould(true);

}
