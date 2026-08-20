package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按 grant_table 授权主体失效的业务键。
 *
 * @param subjects 授权主体键列表，非空
 * @author Bunny
 */
public record GrantInvalidatePayload(
		List<GrantSubjectKey> subjects) implements AuthorizationInvalidatePayload, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public GrantInvalidatePayload {
		subjects = InvalidationPayloadSupport.copyNonEmpty(subjects, "subjects");
	}

	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.GRANT;
	}

}
