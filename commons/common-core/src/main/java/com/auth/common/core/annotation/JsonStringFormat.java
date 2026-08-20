package com.auth.common.core.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于将数值类型序列化为字符串格式 适用于需要将Long、Integer等数值类型以字符串形式返回给前端的场景，
 * 以避免JavaScript处理大数值时出现精度丢失的问题
 *
 * @author Bunny
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
@JacksonAnnotationsInside
@JsonSerialize(using = ToStringSerializer.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonStringFormat {

}