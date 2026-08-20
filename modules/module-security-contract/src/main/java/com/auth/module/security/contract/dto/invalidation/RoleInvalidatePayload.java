package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按角色码失效的业务键。
 *
 * @param roleCodes 角色编码列表，非空
 * @author Bunny
 */
public record RoleInvalidatePayload(List<String> roleCodes) implements AuthorizationInvalidatePayload, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public RoleInvalidatePayload {
		roleCodes = InvalidationPayloadSupport.copyNonEmpty(roleCodes, "roleCodes");
	}

	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.ROLE;
	}

}
