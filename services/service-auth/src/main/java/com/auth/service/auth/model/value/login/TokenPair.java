package com.auth.service.auth.model.value.login;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * 访问令牌与刷新令牌值对象
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class TokenPair {

	/**
	 * 访问令牌
	 */
	String accessToken;

	/**
	 * 刷新令牌
	 */
	String refreshToken;

	/**
	 * 访问令牌过期时间
	 */
	Instant accessExpiresAt;

}
