package com.auth.service.auth.support.session;

/**
 * 刷新令牌原子旋转结果
 *
 * @author Bunny
 */
public enum RefreshRotateOutcome {

	/**
	 * 当前 hash 命中并已旋转
	 */
	ROTATED,

	/**
	 * grace 内复用上一轮令牌
	 */
	REUSED,

	/**
	 * 会话不存在或 refresh 已过期
	 */
	EXPIRED,

	/**
	 * hash 不匹配（疑似盗用或越代重放）
	 */
	MISMATCH

}
