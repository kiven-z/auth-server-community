package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;

import java.io.Serial;
import java.io.Serializable;

/**
 * grant_table 授权主体键（USER / DEPT / POST + 主键）。
 *
 * @param subjectType 主体类型
 * @param subjectId 主体 ID
 * @author Bunny
 */
public record GrantSubjectKey(GrantTableSubjectType subjectType, Long subjectId) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public GrantSubjectKey {
		subjectType = InvalidationPayloadSupport.requireNonNull(subjectType, "subjectType");
		subjectId = InvalidationPayloadSupport.requireNonNull(subjectId, "subjectId");
	}

}
