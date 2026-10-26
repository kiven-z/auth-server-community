package com.auth.module.security.contract.invalidation;

/**
 * 授权失效业务键（密封类型）：不同 {@link AuthorizationChangeKind} 使用不同子类型。
 *
 * @author Bunny
 */
public sealed interface AuthorizationInvalidatePayload permits RoleInvalidatePayload, PermissionInvalidatePayload,
		GrantInvalidatePayload, UserPostInvalidatePayload, UserInvalidatePayload {

	/**
	 * 本负载对应的变更维度。
	 * @return 变更维度
	 */
	AuthorizationChangeKind kind();

}
