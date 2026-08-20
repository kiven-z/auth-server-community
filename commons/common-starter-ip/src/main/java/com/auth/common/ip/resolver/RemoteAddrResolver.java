package com.auth.common.ip.resolver;

import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * 使用{@link HttpServletRequest#getRemoteAddr()}的回退解析器
 *
 * @author Bunny
 */
public class RemoteAddrResolver implements ClientIpResolver {

	/**
	 * 解析HTTP请求的原始客户端IP字符串
	 * @param request HTTP请求
	 * @return 原始客户端IP字符串
	 */
	@Override
	public Optional<String> resolve(HttpServletRequest request) {
		// 获取远程地址
		String remoteAddr = request.getRemoteAddr();
		// 如果远程地址为空，则返回空
		if (CharSequenceUtil.isBlank(remoteAddr)) {
			return Optional.empty();
		}

		// 返回修剪后的远程地址
		return Optional.of(CharSequenceUtil.trim(remoteAddr));
	}

}
