package com.auth.module.security.contract.invalidation;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 授权失效业务键（密封类型）：不同 {@link AuthorizationChangeKind} 使用不同子类型。
 *
 * @author Bunny
 */
public sealed interface AuthorizationInvalidatePayload extends Serializable
		permits AuthorizationInvalidatePayload.Role, AuthorizationInvalidatePayload.Permission,
		AuthorizationInvalidatePayload.Grant, AuthorizationInvalidatePayload.User {

	/**
	 * 校验列表非 null 且非空，并返回不可变拷贝。
	 * @param values 待校验列表
	 * @param fieldName 字段名
	 * @param <T> 元素类型
	 * @return 不可变列表
	 */
	private static <T> List<T> copyNonEmpty(List<T> values, String fieldName) {
		if (values == null || values.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " must not be null or empty");
		}
		return List.copyOf(values);
	}

	/**
	 * 本负载对应的变更维度。
	 * @return 变更维度
	 */
	AuthorizationChangeKind kind();

	/**
	 * 按角色码失效的业务键
	 */
	record Role(List<String> roleCodes) implements AuthorizationInvalidatePayload {

		@Serial
		private static final long serialVersionUID = 1L;

		public Role {
			roleCodes = copyNonEmpty(roleCodes, "roleCodes");
		}

		@Override
		public AuthorizationChangeKind kind() {
			return AuthorizationChangeKind.ROLE;
		}

	}

	/**
	 * 按权限码失效的业务键
	 */
	record Permission(List<String> permissionCodes) implements AuthorizationInvalidatePayload {

		@Serial
		private static final long serialVersionUID = 1L;

		public Permission {
			permissionCodes = copyNonEmpty(permissionCodes, "permissionCodes");
		}

		@Override
		public AuthorizationChangeKind kind() {
			return AuthorizationChangeKind.PERMISSION;
		}

	}

	/**
	 * 按 grant_table 授权主体失效的业务键
	 */
	record Grant(List<GrantSubjectKey> subjects) implements AuthorizationInvalidatePayload {

		@Serial
		private static final long serialVersionUID = 1L;

		public Grant {
			subjects = copyNonEmpty(subjects, "subjects");
		}

		@Override
		public AuthorizationChangeKind kind() {
			return AuthorizationChangeKind.GRANT;
		}

	}

	/**
	 * 按用户 ID 直连失效的业务键
	 */
	record User(List<Long> userIds) implements AuthorizationInvalidatePayload {

		@Serial
		private static final long serialVersionUID = 1L;

		public User {
			userIds = copyNonEmpty(userIds, "userIds");
		}

		@Override
		public AuthorizationChangeKind kind() {
			return AuthorizationChangeKind.USER;
		}

	}

}
