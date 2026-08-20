package com.auth.module.security.contract.dto.invalidation;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权失效统一响应（HTTP / Feign 共用）。
 *
 * @param impactedUserCount 影响面反查得到的去重用户数
 * @param versionBumpedCount 实际递增 perm_version 的用户数
 * @param profileRefreshedCount 成功重建并写入 Redis 画像的用户数
 * @param profileEvictedCount 成功驱逐 Redis 画像的用户数
 * @author Bunny
 */
public record AuthorizationInvalidateResponse(int impactedUserCount, int versionBumpedCount, int profileRefreshedCount,
		int profileEvictedCount) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 无受影响用户时的结果。
	 * @return 零影响结果
	 */
	public static AuthorizationInvalidateResponse empty() {
		return new AuthorizationInvalidateResponse(0, 0, 0, 0);
	}

}
