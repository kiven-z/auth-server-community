package com.auth.service.system.admin.model.vo.authorization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色授权面摘要
 *
 * @author Bunny
 */
@Schema(name = "RoleAuthorizationSummaryVO", title = "角色授权面摘要")
@Getter
@Setter
@ToString
public class RoleAuthorizationSummaryVO {

	@Schema(title = "绑定权限数")
	private Long permissionCount;

	@Schema(title = "绑定菜单数")
	private Long menuCount;

	@Schema(title = "授权用户数")
	private Long grantUserCount;

}
