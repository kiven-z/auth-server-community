package com.auth.service.auth.support.login.ratelimit;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 *
 * @author Bunny
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

	/**
	 * 缓存 key 的Key的内容 前缀 + principal 主体表达式（支持SpEL） 例如：#email
	 * @return 缓存 key
	 */
	@Language("SpEL")
	String principal() default "";

}