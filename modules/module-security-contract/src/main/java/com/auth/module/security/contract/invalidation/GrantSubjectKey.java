package com.auth.module.security.contract.invalidation;

import com.auth.module.security.contract.grant.GrantTableSubjectType;

import java.io.Serial;
import java.io.Serializable;

/**
 * grant_table 授权主体键
 *
 * @param subjectType 主体类型
 * @param subjectId 主体 ID
 * @author Bunny
 */
public record GrantSubjectKey(GrantTableSubjectType subjectType, Long subjectId) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public GrantSubjectKey {
		if (subjectType == null) {
			throw new IllegalArgumentException("subjectType must not be null");
		}
		if (subjectId == null) {
			throw new IllegalArgumentException("subjectId must not be null");
		}
	}

}
