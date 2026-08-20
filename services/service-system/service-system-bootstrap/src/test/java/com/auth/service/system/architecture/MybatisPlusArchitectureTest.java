package com.auth.service.system.architecture;

import com.auth.common.data.config.BaseMybatisPlusConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.springframework.context.annotation.Bean;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * MyBatis-Plus 插件注册架构约束
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth", importOptions = ImportOption.DoNotIncludeTests.class)
@DisplayName("MyBatis-Plus 架构约束")
class MybatisPlusArchitectureTest {

	/**
	 * 全应用仅允许 BaseMybatisPlusConfig 声明 MybatisPlusInterceptor @Bean
	 */
	@ArchTest
	static final ArchRule mybatis_plus_interceptor_bean_only_in_base_config = methods().that()
		.areAnnotatedWith(Bean.class)
		.and()
		.haveRawReturnType(MybatisPlusInterceptor.class)
		.should()
		.beDeclaredInClassesThat()
		.areAssignableTo(BaseMybatisPlusConfig.class)
		.because("Only one MybatisPlusInterceptor bean; extras must be InnerInterceptor")
		.allowEmptyShould(true);

}
