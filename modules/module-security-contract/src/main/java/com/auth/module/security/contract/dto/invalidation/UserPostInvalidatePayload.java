package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按岗位 ID 失效的业务键。
 *
 * @param postIds 岗位 ID 列表，非空
 * @author Bunny
 */
public record UserPostInvalidatePayload(List<Long> postIds) implements AuthorizationInvalidatePayload, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public UserPostInvalidatePayload {
		postIds = InvalidationPayloadSupport.copyNonEmpty(postIds, "postIds");
	}

	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.USER_POST;
	}

}
