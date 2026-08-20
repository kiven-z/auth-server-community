package com.auth.module.security.contract.api.authorization;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 用户授权画像（契约层，缓存/JWT 下游可消费的稳定模型）
 *
 * <p>
 * 注意：该模型不绑定 Redis/Spring/MyBatis 等基础设施，序列化应保持稳定可演进
 * </p>
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
	 * 部门维数据范围（登录解析：user_scope 优先，否则 role_scope 合并）
	 */
	private final ScopeGrant deptScope;

	/**
	 * 权限/画像版本号（用于与 Redis 中的 permVersion 对比）
	 */
	private final Long permVersion;

}
