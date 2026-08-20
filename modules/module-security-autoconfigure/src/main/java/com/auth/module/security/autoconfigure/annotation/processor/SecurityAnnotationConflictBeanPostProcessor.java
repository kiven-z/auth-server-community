package com.auth.module.security.autoconfigure.annotation.processor;

import cn.hutool.core.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

/**
 * 在 Bean 初始化完成后扫描 RestController 依次执行所有 SecurityAnnotationConflictRule 在启动期发现非法注解组合时立即失败
 *
 * @author Bunny
 */
public class SecurityAnnotationConflictBeanPostProcessor implements BeanPostProcessor {

	private final List<SecurityAnnotationConflictRule> rules;

	public SecurityAnnotationConflictBeanPostProcessor(List<SecurityAnnotationConflictRule> rules) {
		this.rules = ArrayUtil.isEmpty(rules) ? List.of() : List.copyOf(rules);
	}

	/**
	 * 在 Bean 初始化后处理
	 * @param bean Bean
	 * @param beanName Bean 名称
	 * @return Bean
	 * @throws BeansException Beans 异常
	 */
	@Override
	public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
		Class<?> type = ClassUtils.getUserClass(bean);
		if (!AnnotatedElementUtils.hasAnnotation(type, RestController.class)) {
			return bean;
		}

		validateElement(type, type.getName());
		Arrays.stream(type.getMethods())
			.filter(m -> m.getDeclaringClass() != Object.class)
			.forEach(m -> validateElement(m, m.toGenericString()));

		return bean;
	}

	/**
	 * 验证注解元素
	 * @param element 注解元素
	 * @param target 目标
	 */
	private void validateElement(AnnotatedElement element, String target) {
		for (SecurityAnnotationConflictRule rule : rules) {
			rule.validate(element, target);
		}
	}

}
