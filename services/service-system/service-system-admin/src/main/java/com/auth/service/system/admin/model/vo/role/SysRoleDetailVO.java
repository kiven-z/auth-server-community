package com.auth.service.system.admin.model.vo.role;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色详情 VO（标量 + 授权关系计数）
 *
 * @author Bunny
 */
@Schema(name = "SysRoleDetailVO", title = "角色详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysRoleDetailVO extends BaseResponse {

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态（true=正常启用，false=停用）")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "绑定权限数")
	private Long permissionCount;

	@Schema(title = "绑定菜单数")
	private Long menuCount;

	@Schema(title = "授权用户数")
	private Long grantUserCount;

}
