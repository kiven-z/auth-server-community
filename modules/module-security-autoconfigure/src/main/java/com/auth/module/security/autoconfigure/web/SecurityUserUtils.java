package com.auth.module.security.autoconfigure.web;

import com.auth.module.security.autoconfigure.security.SecurityRequestAttributes;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 从Spring Security上下文中读取当前的{@link AuthProfile}(资源服务器流)
 *
 * @author Bunny
 */
@UtilityClass
public class SecurityUserUtils {

	/**
	 * 获取当前登录用户
	 * @return 当前登录用户，如果未认证或主体不是{@link AuthProfile}，则返回null
	 */
	public static AuthProfile currentAuthProfile() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		Object principal = authentication.getPrincipal();
		return principal instanceof AuthProfile profile ? profile : null;
	}

	/**
	 * 获取当前用户ID
	 * @return 当前用户ID，如果未认证或主体不是{@link AuthProfile}，则返回null
	 */
	public static Long getUserId() {
		AuthProfile profile = currentAuthProfile();
		return profile != null ? profile.getUserId() : null;
	}

	/**
	 * 获取当前用户名
	 * @return 当前用户名，如果未认证或主体不是{@link AuthProfile}，则返回null
	 */
	public static String getUsername() {
		AuthProfile profile = currentAuthProfile();
		return profile != null ? profile.getUsername() : null;
	}

	/**
	 * 获取当前会话 ID（JWT jti）
	 * @return 会话 ID，未设置请求属性时返回 null
	 */
	public static String getSessionId() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			return null;
		}

		HttpServletRequest request = attrs.getRequest();
		Object attribute = request.getAttribute(SecurityRequestAttributes.SESSION_ID);
		return attribute instanceof String sessionId ? sessionId : null;
	}

}
