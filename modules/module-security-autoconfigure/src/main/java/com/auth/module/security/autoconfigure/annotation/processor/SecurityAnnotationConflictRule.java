package com.auth.module.security.autoconfigure.annotation.processor;

import java.lang.reflect.AnnotatedElement;

/**
 * 启动期安全注解冲突校验规则：对 Controller 类或方法上的注解组合做 fail-fast 检查 项目自定义注解和本身注解不能冲突
 *
 * @author Bunny
 */
@FunctionalInterface
public interface SecurityAnnotationConflictRule {

	/**
	 * 校验 element 上的注解是否与当前规则冲突
	 * @param element 被校验的类或方法
	 * @param target 类全名或方法
	 * @throws IllegalStateException 存在冲突时抛出，阻止应用启动
	 */
	void validate(AnnotatedElement element, String target) throws IllegalStateException;

}
