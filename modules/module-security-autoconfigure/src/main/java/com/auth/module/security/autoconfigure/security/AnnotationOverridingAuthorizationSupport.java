package com.auth.module.security.autoconfigure.security;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

/**
 * AnnotationOverridingAuthorizationManager 工具类
 *
 * @author Bunny
 */
@UtilityClass
public class AnnotationOverridingAuthorizationSupport {

	/**
	 * 将路径模式列表转换为 RequestMatcher 列表
	 * @param patterns 路径模式列表
	 * @return RequestMatcher 列表
	 */
	public static List<RequestMatcher> toPathMatchers(List<String> patterns) {
		PathPatternRequestMatcher.Builder matcherBuilder = PathPatternRequestMatcher.withDefaults();
		return CollUtil.emptyIfNull(patterns)
			.stream()
			.filter(CharSequenceUtil::isNotBlank)
			.<RequestMatcher>map(pattern -> matcherBuilder.matcher(pattern.trim()))
			.toList();
	}

	/**
	 * 匿名主体不算已认证
	 * @param authentication 认证
	 * @return 是否认证
	 */
	public static boolean isNonAnonymousAuthenticated(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		return !(authentication instanceof AnonymousAuthenticationToken);
	}

}
