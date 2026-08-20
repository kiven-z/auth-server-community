package com.auth.service.system.admin.model.query.user;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户部门关联分页查询
 *
 * @author Bunny
 */
@Schema(name = "UserDeptPageQuery", title = "用户部门关联分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserDeptPageQuery extends PageQueryRequest {

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

}
