package com.auth.module.security.autoconfigure.boot.pipeline;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 「注解优先」安全认证流水线的 Bean 装配（聚合入口）
 *
 * @author Bunny
 */
@Import({
		// 安全需求解析
		SecurityRequirementResolverConfiguration.class,
		// 会话策略
		SessionPolicyConfiguration.class,
		// 请求认证器装配
		RequestAuthenticatorConfiguration.class,
		// 认证执行
		SecurityAuthExecutorConfiguration.class,
		// 审计兜底
		AuthorizationAuditAutoConfiguration.class,
		// 操作日志 SPI 与切面
		OperationLogAutoConfiguration.class,
		// 过滤器入口
		TokenAuthenticationFilterConfiguration.class })
@Configuration
public class SecurityAuthenticationPipelineConfiguration {

}
