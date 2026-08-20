package com.auth.service.auth.support.session;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * 刷新令牌原子旋转结果（REUSED 时携带上一轮签发的令牌）
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class RefreshRotateResult {

	RefreshRotateOutcome outcome;

	String accessToken;

	String refreshToken;

	Instant accessExpiresAt;

}
