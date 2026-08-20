package com.auth.module.security.autoconfigure.annotation.processor;

import lombok.Getter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通用「互斥注解组」规则：给定一组注解类型（含 Spring 元数据合并）命中多于一个，则启动失败
 *
 * @author Bunny
 */
public final class MutuallyExclusiveAnnotationsRule implements SecurityAnnotationConflictRule {

	private final String groupName;

	@Getter
	private final Set<Class<? extends Annotation>> group;

	/**
	 * @param groupName 规则名称，仅用于异常信息
	 * @param group 互斥组内各注解类型，至少 2 个才有意义
	 */
	public MutuallyExclusiveAnnotationsRule(String groupName, Set<Class<? extends Annotation>> group) {
		Assert.hasText(groupName, "groupName");
		Assert.notEmpty(group, "group must not be empty");
		this.groupName = groupName;
		this.group = Set.copyOf(group);
	}

	/**
	 * 验证互斥注解组
	 * @param element 注解元素
	 * @param target 目标
	 */
	@Override
	public void validate(AnnotatedElement element, String target) {
		// 获取注解元素上存在的注解类型
		List<Class<? extends Annotation>> present = group.stream()
			.filter(annType -> AnnotatedElementUtils.hasAnnotation(element, annType))
			.toList();

		// 如果注解类型数量小于等于1，表示当前注解没有冲突
		if (present.size() <= 1) {
			return;
		}

		String joined = present.stream().map(Class::getName).collect(Collectors.joining(", "));
		throw new IllegalStateException("Invalid security annotations (group '" + groupName + "'): at most one of ["
				+ joined + "] may be present on " + target);
	}

}
