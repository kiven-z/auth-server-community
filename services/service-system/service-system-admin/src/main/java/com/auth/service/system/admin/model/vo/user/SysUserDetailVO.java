package com.auth.service.system.admin.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户详情 VO（档案 + 授权关系计数）
 *
 * @author Bunny
 */
@Schema(name = "SysUserDetailVO", title = "用户详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysUserDetailVO extends SysUserProfileVO {

	@Schema(title = "直连角色数")
	private Long directRoleCount;

	@Schema(title = "生效角色数")
	private Long effectiveRoleCount;

	@Schema(title = "生效权限数")
	private Long effectivePermissionCount;

}
