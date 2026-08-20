package com.auth.common.data.model.enums;

import lombok.Getter;

/**
 * 审计元数据字段枚举 用于标识实体的审计字段（创建人、创建时间等）
 *
 * @author Bunny
 */
@Getter
public enum DatabaseAuditMetaFieldEnum {

	/**
	 * 创建用户
	 */
	CREATED_BY("createdBy"),

	/**
	 * 修改用户
	 */
	UPDATED_BY("updatedBy"),

	/**
	 * 创建时间
	 */
	CREATED_AT("createdAt"),

	/**
	 * 修改时间
	 */
	UPDATED_AT("updatedAt");

	private final String columnName;

	DatabaseAuditMetaFieldEnum(String columnName) {
		this.columnName = columnName;
	}

}
