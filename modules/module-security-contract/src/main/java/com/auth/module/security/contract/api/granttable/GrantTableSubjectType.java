package com.auth.module.security.contract.api.granttable;

import lombok.Getter;

/**
 * 统一授权主体类型
 *
 * @author Bunny
 */
@Getter
public enum GrantTableSubjectType {

	/**
	 * 用户
	 */
	USER("用户");

	private final String description;

	GrantTableSubjectType(String description) {
		this.description = description;
	}

}
