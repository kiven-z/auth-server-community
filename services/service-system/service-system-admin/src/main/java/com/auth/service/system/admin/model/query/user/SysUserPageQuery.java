package com.auth.service.system.admin.model.query.user;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "SysUserPageQuery", title = "用户分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysUserPageQuery extends PageQueryRequest {

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "手机号")
	private String phone;

	@Schema(title = "邮箱")
	private String email;

	@Schema(title = "工号")
	private String employeeNo;

	@Schema(title = "账号状态（0=禁用，1=正常，2=锁定）")
	private Integer status;

	@Schema(title = "部门ID")
	private Long deptId;

}
