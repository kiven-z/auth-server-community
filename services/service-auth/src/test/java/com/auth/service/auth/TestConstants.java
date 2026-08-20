package com.auth.service.auth;

/**
 * 测试常量
 *
 * @author Bunny
 */
public final class TestConstants {

	/**
	 * 固定用户 ID
	 */
	public static final long USER_ID = 10001L;

	/**
	 * 固定会话 jti
	 */
	public static final String JTI = "test-jti";

	/**
	 * 固定 refresh token
	 */
	public static final String REFRESH_TOKEN = "test-refresh-token";

	/**
	 * 固定 access token
	 */
	public static final String ACCESS_TOKEN = "test-access-token";

	/**
	 * 禁止实例化
	 */
	private TestConstants() {
	}

}
