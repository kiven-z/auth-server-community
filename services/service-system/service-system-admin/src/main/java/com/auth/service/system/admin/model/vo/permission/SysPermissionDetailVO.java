package com.auth.service.system.admin.model.vo.permission;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 权限详情 VO（标量 + 绑定角色计数）
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionDetailVO", title = "权限详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysPermissionDetailVO extends BaseResponse {

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "已绑定角色数")
	private Long boundRoleCount;

}
