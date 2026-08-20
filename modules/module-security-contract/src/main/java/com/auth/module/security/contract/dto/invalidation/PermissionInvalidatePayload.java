package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按权限码失效的业务键。
 *
 * @param permissionCodes 权限码列表，非空
 * @author Bunny
 */
public record PermissionInvalidatePayload(
		List<String> permissionCodes) implements AuthorizationInvalidatePayload, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public PermissionInvalidatePayload {
		permissionCodes = InvalidationPayloadSupport.copyNonEmpty(permissionCodes, "permissionCodes");
	}

	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.PERMISSION;
	}

}
