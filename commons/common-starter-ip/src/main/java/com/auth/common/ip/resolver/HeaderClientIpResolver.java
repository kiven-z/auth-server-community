package com.auth.common.ip.resolver;

import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * 从单个头解析客户端IP
 *
 * @author Bunny
 */
public class HeaderClientIpResolver implements ClientIpResolver {

	private static final String UNKNOWN = "unknown";

	private final String headerName;

	private final boolean xForwardedForStyle;

	public HeaderClientIpResolver(String headerName, boolean xForwardedForStyle) {
		this.headerName = headerName;
		this.xForwardedForStyle = xForwardedForStyle;
	}

	/**
	 * 判断值是否为未知
	 * @param value 值
	 * @return 是否为未知
	 */
	private static boolean isUnknown(String value) {
		return CharSequenceUtil.equalsIgnoreCase(UNKNOWN, value);
	}

	/**
	 * 解析HTTP请求的原始客户端IP字符串
	 * @param request HTTP请求
	 * @return 原始客户端IP字符串
	 */
	@Override
	public Optional<String> resolve(HttpServletRequest request) {
		// 获取头部的值
		String value = request.getHeader(headerName);
		// 如果值为空，则返回空
		if (CharSequenceUtil.isBlank(value)) {
			return Optional.empty();
		}

		// 如果头部的值不是X-Forwarded-For风格，则修剪值
		if (!xForwardedForStyle) {
			String trimmed = CharSequenceUtil.trim(value);
			// 如果修剪后的值是未知，则返回空
			return isUnknown(trimmed) ? Optional.empty() : Optional.of(trimmed);
		}

		// 分割值
		String[] parts = CharSequenceUtil.splitToArray(value, ",");
		if (parts == null) {
			return Optional.empty();
		}

		// 遍历分割后的值
		for (String part : parts) {
			if (CharSequenceUtil.isBlank(part)) {
				continue;
			}

			// 修剪值
			String trimmed = CharSequenceUtil.trim(part);
			if (!isUnknown(trimmed)) {
				// 如果修剪后的值不是未知，则返回
				return Optional.of(trimmed);
			}
		}

		// 如果所有分割后的值都是未知，则返回空
		return Optional.empty();
	}

}
