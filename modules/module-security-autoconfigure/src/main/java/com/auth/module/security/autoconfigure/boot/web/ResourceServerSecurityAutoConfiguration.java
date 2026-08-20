package com.auth.module.security.autoconfigure.boot.web;

import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.module.security.autoconfigure.config.security.SecurityConfigProperties;
import com.auth.module.security.autoconfigure.config.user.UserConfigProperties;
import com.auth.module.security.autoconfigure.pipeline.filter.TokenAuthenticationFilter;
import com.auth.module.security.autoconfigure.security.AnnotationOverridingAuthorizationManager;
import com.auth.module.security.autoconfigure.service.AuthProfileCacheService;
import com.auth.module.security.autoconfigure.service.PermissionService;
import com.auth.module.security.autoconfigure.web.ResourceServerAccessDeniedHandler;
import com.auth.module.security.autoconfigure.web.ResourceServerAuthenticationEntryPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Servlet 资源服务器场景下的 Spring Security 自动配置
 *
 * @author Bunny
 */
@Slf4j
@ConditionalOnClass({ HttpSecurity.class, JwtTokenProvider.class })
@EnableConfigurationProperties({ SecurityConfigProperties.class, UserConfigProperties.class, })
@Import({
		// SpEL @PreAuthorize("@auth.decide('...')") 等
		PermissionService.class,
		// 获取Redis信息
		AuthProfileCacheService.class,
		// 认证失败、权限不足处理
		ResourceServerAuthenticationEntryPoint.class,
		// 403 处理
		ResourceServerAccessDeniedHandler.class, })
@Configuration
@EnableMethodSecurity
public class ResourceServerSecurityAutoConfiguration {

	/**
	 * 密码加密器，Spring推荐的方式
	 * @return 密码加密器
	 */
	@ConditionalOnMissingBean
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	/**
	 * 注解管理，项目规定：注解权限高于路径配置
	 */
	@ConditionalOnMissingBean
	@Bean
	AnnotationOverridingAuthorizationManager annotationOverridingAuthorizationManager(
			SecurityConfigProperties properties) {
		return new AnnotationOverridingAuthorizationManager(properties);
	}

	@ConditionalOnMissingBean
	@Bean
	SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http, TokenAuthenticationFilter tokenFilter,
			ResourceServerAuthenticationEntryPoint entryPoint, ResourceServerAccessDeniedHandler deniedHandler,
			AnnotationOverridingAuthorizationManager authorizationManager) throws Exception {
		// 前后端分离不需要会话
		http.sessionManagement(SessionManagementConfigurer::disable);
		// 前后端分离不需要 CSRF
		http.csrf(AbstractHttpConfigurer::disable);
		// 禁用 HTTP 基本认证
		http.httpBasic(AbstractHttpConfigurer::disable);
		// 禁用表单登录
		http.formLogin(AbstractHttpConfigurer::disable);
		// 禁用退出登录
		http.logout(AbstractHttpConfigurer::disable);
		// 跨域访问权限，如果需要可以关闭后自己配置跨域访问
		http.cors(AbstractHttpConfigurer::disable);

		// 异常处理
		http.exceptionHandling(eh ->
		// 请求未授权接口 和 没有权限访问
		eh.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler));

		// 添加令牌认证过滤器
		http.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

		// 授权：单一 AuthorizationManager，避免多段 requestMatchers 与「注解压过路径」顺序歧义
		http.authorizeHttpRequests(auth -> auth.anyRequest().access(authorizationManager));

		return http.build();
	}

}
