package com.auth.common.web.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Objects;

/**
 * Spring上下文持有者 用于在非Spring管理的类中获取Spring容器中的Bean实例， 提供静态方法访问Spring
 * ApplicationContext和MessageSource功能
 *
 * @author Bunny
 */
public final class SpringContextHolder {

	/**
	 * Spring 应用上下文实例
	 */
	private static ApplicationContext applicationContext;

	private SpringContextHolder() {
	}

	/**
	 * 获取配置属性值
	 * @param key 属性键名
	 * @return 属性对应的值，如果不存在则返回null
	 */
	public static String getProperty(String key) {
		assertContextInitialized();
		return applicationContext.getEnvironment().getProperty(key);
	}

	/**
	 * 根据消息代码获取国际化消息
	 * @param code 消息代码
	 * @param args 消息参数
	 * @param locale 语言环境
	 * @return 国际化消息字符串
	 */
	public static String getMessage(String code, Object[] args, Locale locale) {
		assertContextInitialized();
		MessageSource messageSource = applicationContext.getBean(MessageSource.class);
		return messageSource.getMessage(code, args, locale);
	}

	/**
	 * 根据消息代码获取国际化消息，支持默认消息
	 * @param code 消息代码
	 * @param args 消息参数
	 * @param defaultMessage 默认消息
	 * @param locale 语言环境
	 * @return 国际化消息字符串，如果找不到对应代码则返回默认消息
	 */
	public static String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		assertContextInitialized();
		MessageSource messageSource = applicationContext.getBean(MessageSource.class);
		return messageSource.getMessage(code, args, defaultMessage, locale);
	}

	private static void assertContextInitialized() {
		if (Objects.isNull(applicationContext)) {
			throw new IllegalStateException("ApplicationContext has not been initialized.");
		}
	}

	/**
	 * 注册 Spring 应用上下文
	 * @param context Spring 应用上下文实例
	 */
	public static void registerApplicationContext(ApplicationContext context) {
		applicationContext = context;
	}

}
