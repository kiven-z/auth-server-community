package com.auth.module.security.autoconfigure.boot;

import com.auth.common.jwt.autoconfigure.JwtAutoConfiguration;
import com.auth.module.security.autoconfigure.boot.integration.MybatisPlusAuditFillAutoConfiguration;
import com.auth.module.security.autoconfigure.boot.pipeline.SecurityAuthenticationPipelineConfiguration;
import com.auth.module.security.autoconfigure.boot.validation.SecurityAnnotationConflictAutoConfiguration;
import com.auth.module.security.autoconfigure.boot.web.ResourceServerSecurityAutoConfiguration;
import com.auth.module.security.autoconfigure.web.exception.SecurityExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 安全启动器引入当前所有的配置信息
 *
 * @author Bunny
 */
@AutoConfiguration(after = { WebMvcAutoConfiguration.class, JwtAutoConfiguration.class })
@Import({
		// 资源配置包含 SpringSecurity 配置
		ResourceServerSecurityAutoConfiguration.class,
		// 内部注解定义等
		SecurityAuthenticationPipelineConfiguration.class,
		// 启动期安全注解冲突校验自动配置
		SecurityAnnotationConflictAutoConfiguration.class,
		// Mybatis Plus 配置
		MybatisPlusAuditFillAutoConfiguration.class,
		// 异常捕获
		SecurityExceptionHandler.class, })
public class ModuleSecurityAutoConfiguration {

}
