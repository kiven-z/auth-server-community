package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按部门 ID 失效的业务键（含部门成员及继承关系反查）。
 *
 * @param deptIds 部门 ID 列表，非空
 * @author Bunny
 */
public record UserDeptInvalidatePayload(List<Long> deptIds) implements AuthorizationInvalidatePayload, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public UserDeptInvalidatePayload {
		deptIds = InvalidationPayloadSupport.copyNonEmpty(deptIds, "deptIds");
	}

	@Override
	public AuthorizationChangeKind kind() {
		return AuthorizationChangeKind.USER_DEPT;
	}

}
