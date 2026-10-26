package com.auth.module.security.contract.api.authorization;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 用户授权画像
 *
 * @author Bunny
 */
@Getter
@Builder
@Jacksonized
public class AuthProfile implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private final Long userId;

	private final String username;

	@Builder.Default
	private final List<String> roles = Collections.emptyList();

	@Builder.Default
	private final List<String> permissions = Collections.emptyList();

	/**
	 * 部门维数据范围
	 * <p>
	 * 用户可覆盖角色范围，优先级：user_scope > role_scope
	 * </p>
	 */
	private final ScopeGrant deptScope;

	/**
	 * 权限/画像版本号
	 * <p>
	 * 用于与 Redis 中的 permVersion 对比
	 * </p>
	 */
	private final Long permVersion;

}
