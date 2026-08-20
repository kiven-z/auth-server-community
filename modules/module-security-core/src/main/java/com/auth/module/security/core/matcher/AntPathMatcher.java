package com.auth.module.security.core.matcher;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Ant 路径匹配工具类
 *
 * @author Bunny
 */
@UtilityClass
public class AntPathMatcher {

	/**
	 * 匹配任意 Ant 路径
	 * @param uri 请求 URI
	 * @param patterns 匹配路径
	 * @return 是否匹配
	 */
	public static boolean matchesAnyAnt(String uri, List<String> patterns) {
		// 请求路径为空、匹配路径为空，返回 false
		if (CharSequenceUtil.isBlank(uri) || ArrayUtil.isEmpty(patterns)) {
			return false;
		}

		// 开始寻找匹配的 Ant 路径
		org.springframework.util.AntPathMatcher matcher = new org.springframework.util.AntPathMatcher();
		for (String pattern : patterns) {
			if (StringUtils.hasText(pattern) && matcher.match(pattern, uri)) {
				return true;
			}
		}
		return false;
	}

}
