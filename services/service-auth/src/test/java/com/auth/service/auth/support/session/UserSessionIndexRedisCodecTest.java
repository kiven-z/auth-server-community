package com.auth.service.auth.support.session;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.TestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link UserSessionIndexRedisCodec} 会话 Hash 编解码单元测试
 */
@DisplayName("UserSessionIndexRedisCodec 会话 Hash 编解码")
class UserSessionIndexRedisCodecTest {

	@Test
	@DisplayName("asStringHash：将 Object Map 转为字符串 Hash")
	void asStringHash_convertsObjectMap() {
		Map<Object, Object> raw = Map.of("userId", 1L, "ipAddress", "10.0.0.1");

		Map<String, String> hash = UserSessionIndexRedisCodec.asStringHash(raw);

		assertThat(hash).containsEntry("userId", "1").containsEntry("ipAddress", "10.0.0.1");
	}

	@Test
	@DisplayName("toSessionIndex：纯字符串 Hash 正确映射字段")
	void toSessionIndex_mapsPlainStringHash() {
		Map<String, String> hash = Map.of("userId", String.valueOf(TestConstants.USER_ID), "ipAddress", "10.0.0.1",
				"loginAt", "1700000000000", "rememberMe", "true");

		UserSessionIndex session = UserSessionIndexRedisCodec.toSessionIndex(TestConstants.JTI, hash, null);

		assertThat(session.getSessionId()).isEqualTo(TestConstants.JTI);
		assertThat(session.getUserId()).isEqualTo(TestConstants.USER_ID);
		assertThat(session.getIpAddress()).isEqualTo("10.0.0.1");
		assertThat(session.getLoginAt()).isEqualTo(1_700_000_000_000L);
		assertThat(session.getRememberMe()).isTrue();
	}

	@Test
	@DisplayName("buildRegisterScriptArgs：生成纯字符串 ARGV 供 sessionRedisTemplate 执行")
	void buildRegisterScriptArgs_returnsStringArgv() {
		UserSessionIndex index = new UserSessionIndex();
		index.setUserId(TestConstants.USER_ID);
		index.setSessionId(TestConstants.JTI);
		index.setIpAddress("10.0.0.1");
		index.setLoginAt(1_700_000_000_000L);
		index.setRememberMe(true);
		index.setRefreshTokenExpiresAt(System.currentTimeMillis() + 3_600_000L);

		Object[] args = UserSessionIndexRedisCodec.buildRegisterScriptArgs(index);

		assertThat(args).hasSizeGreaterThanOrEqualTo(6);
		assertThat(args[0]).isEqualTo(TestConstants.JTI);
		assertThat(args[1]).isEqualTo("1700000000000");
		// TTL 按剩余秒数向下取整，设置过期时间与计算之间可能相差 1ms，允许 3599~3600
		assertThat(Long.parseLong((String) args[2])).isBetween(3599L, 3600L);
		assertThat(args[3]).isEqualTo(String.valueOf(TestConstants.USER_ID));
		assertThat(args[4]).isInstanceOf(String.class);
	}

}
