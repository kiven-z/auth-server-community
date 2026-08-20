package com.auth.common.data.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link UserStatus} 解析与登录门禁语义
 */
@DisplayName("UserStatus 用户账号状态")
class UserStatusTest {

	@Test
	@DisplayName("ofNullable：合法 code 解析为对应枚举")
	void ofNullable_knownCodes_shouldResolve() {
		assertEquals(UserStatus.DISABLED, UserStatus.ofNullable(UserStatus.DISABLED.getCode()));
		assertEquals(UserStatus.NORMAL, UserStatus.ofNullable(UserStatus.NORMAL.getCode()));
		assertEquals(UserStatus.LOCKED, UserStatus.ofNullable(UserStatus.LOCKED.getCode()));
	}

	@Test
	@DisplayName("ofNullable：未知 code 返回 null")
	void ofNullable_unknown_shouldReturnNull() {
		assertNull(UserStatus.ofNullable(99));
	}

	@Test
	@DisplayName("仅 NORMAL 允许进入登录凭证校验")
	void allowsLogin_onlyNormal() {
		assertFalse(UserStatus.DISABLED.allowsLogin());
		assertTrue(UserStatus.NORMAL.allowsLogin());
		assertFalse(UserStatus.LOCKED.allowsLogin());
	}

}
