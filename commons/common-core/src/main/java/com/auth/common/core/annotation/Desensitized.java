package com.auth.common.core.annotation;

import com.auth.common.core.desensitize.DesensitizedJsonSerializer;
import com.auth.common.core.desensitize.DesensitizedType;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;

/**
 * 响应 JSON 序列化时对字符串字段脱敏，基于 Hutool {@link cn.hutool.core.util.DesensitizedUtil}
 * <p>
 * 仅影响写出 JSON，不改变内存中的字段值
 * </p>
 *
 * @author Bunny
 */
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizedJsonSerializer.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Desensitized {

	/**
	 * @return 脱敏策略
	 */
	DesensitizedType value();

	/**
	 * 自定义脱敏时前缀保留长度；其他类型忽略
	 */
	int prefixLen() default 0;

	/**
	 * 自定义脱敏时后缀保留长度；其他类型忽略
	 */
	int suffixLen() default 0;

	/**
	 * 自定义脱敏时中间替换字符，默认 *
	 */
	String symbol() default "*";

	/**
	 * 仅 {@link DesensitizedType#ADDRESS} 生效：掩码末尾敏感字符长度；默认 8
	 */
	int addressSensitiveSize() default 8;

}
