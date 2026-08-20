package com.auth.module.security.autoconfigure.config.user;

/**
 * 会话限制策略 当用户达到最大会话数上限时，应该采取的策略
 *
 * @author Bunny
 */
public enum SessionLimitStrategy {

	/**
	 * 达到上限时踢出最旧会话（默认）
	 */
	EVICT_OLDEST,

	/**
	 * 达到上限时拒绝本次登录
	 */
	REJECT_LOGIN

}