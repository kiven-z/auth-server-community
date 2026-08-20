package com.auth.service.auth.model.po.scope;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * user_scope 只读查询投影
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserScopePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "范围类型：ALL/SELF/DEPT/DEPT_AND_CHILD")
	private String scopeType;

	@Schema(title = "scope_dept_ids JSON 字符串")
	private String scopeDeptIds;

}
