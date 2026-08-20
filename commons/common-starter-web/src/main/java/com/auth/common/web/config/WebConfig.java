package com.auth.common.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Web 配置
 *
 * @author Bunny
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(@NonNull InterceptorRegistry registry) {
		LocaleChangeInterceptor interceptor = localeChangeInterceptor();
		registry.addInterceptor(interceptor);
	}

	/**
	 * 如果设置默认语言可能会导致语言识别失败
	 * @return 语言解析器
	 */
	@Bean
	public LocaleResolver localeResolver() {
		return new AcceptHeaderLocaleResolver();
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		// 添加白名单验证
		lci.setHttpMethods("GET", "POST");
		lci.setParamName("lang");
		return lci;
	}

}