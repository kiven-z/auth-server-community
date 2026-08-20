package com.auth.service.auth.support.session;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Duration;

/**
 * 刷新令牌原子旋转入参
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class RefreshRotateCommand {

	/**
	 * 会话唯一标识
	 */
	String jti;

	/**
	 * 请求 refresh 的哈希
	 */
	String requestRefreshHash;

	/**
	 * 新 refresh 哈希
	 */
	String newRefreshHash;

	/**
	 * 新 refresh 过期时间（毫秒）
	 */
	long newRefreshExpiresAtMs;

	/**
	 * 会话 TTL
	 */
	Duration ttl;

	/**
	 * 上一轮 refresh 复用宽限（毫秒）
	 */
	long graceMs;

	/**
	 * 候选 access（ROTATED 时写入 last*）
	 */
	String accessToken;

	/**
	 * 候选 refresh（ROTATED 时写入 last*）
	 */
	String refreshToken;

	/**
	 * 候选 access 过期时间（毫秒）
	 */
	long accessExpiresAtMs;

}
