package com.auth.module.security.autoconfigure.security;

import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 安全模块写入 {@link jakarta.servlet.http.HttpServletRequest} 的请求属性键
 *
 * @author Bunny
 */
@UtilityClass
public final class SecurityRequestAttributes {

	/**
	 * 解析后的接口安全级别（{@link SecurityRequirement} 枚举）
	 */
	public static final String REQUIREMENT = "com.auth.security.requirement";

	/**
	 * 外部 Bearer 认证后的当前会话 ID（JWT jti），供业务层经 {@link RequestContextHolder} 读取
	 */
	public static final String SESSION_ID = "com.auth.security.session_id";

	/**
	 * Filter 写入的安全错误码，供统一错误响应读取
	 */
	public static final String SECURITY_ERROR = "security.error";

}
