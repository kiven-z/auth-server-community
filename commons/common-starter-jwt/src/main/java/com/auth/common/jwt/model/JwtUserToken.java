package com.auth.common.jwt.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Access Token 载荷映射 / 签发入参
 *
 * @author Bunny
 */
@Getter
@Setter
@Builder
public class JwtUserToken {

	private Long userId;

	/**
	 * 签发者：指明该 JWT 的签发方（例如身份认证服务或应用标识）
	 */
	private String iss;

	/**
	 * 主题：通常表示 Token 所针对的用户或实体（例如用户 ID、设备 ID）
	 */
	private String sub;

	/**
	 * Jwt Id JWT jti（JWT ID）用于将 access token 与服务端会话（sessionId）绑定
	 */
	private String jti;

	/**
	 * 预期接收者 对应 JWT aud（单值场景下写入一个 audience 字符串）
	 */
	private String audience;

}
