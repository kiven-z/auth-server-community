package com.auth.service.auth.model.po.scope;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * user_scope 批量查询投影（含用户 ID）
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserScopeByUserPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户 ID
	 */
	private Long userId;

	/**
	 * 范围类型：ALL/SELF/DEPT/DEPT_AND_CHILD
	 */
	private String scopeType;

	/**
	 * scope_dept_ids JSON 字符串
	 */
	private String scopeDeptIds;

}
