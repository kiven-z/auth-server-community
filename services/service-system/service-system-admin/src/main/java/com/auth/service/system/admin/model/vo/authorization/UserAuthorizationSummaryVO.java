package com.auth.service.system.admin.model.vo.authorization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户授权面摘要
 *
 * @author Bunny
 */
@Schema(name = "UserAuthorizationSummaryVO", title = "用户授权面摘要")
@Getter
@Setter
@ToString
public class UserAuthorizationSummaryVO {

	@Schema(title = "有效任职部门数")
	private Long deptCount;

	@Schema(title = "有效任职岗位数")
	private Long postCount;

	@Schema(title = "直连角色数")
	private Long directRoleCount;

	@Schema(title = "生效角色数")
	private Long effectiveRoleCount;

	@Schema(title = "生效权限数")
	private Long effectivePermissionCount;

}
