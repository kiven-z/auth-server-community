package com.auth.service.system.admin.model.vo.permission;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 权限分页行
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionPageVO", title = "权限分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysPermissionPageVO extends BaseResponse {

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "启用状态（true=正常启用，false=停用）")
	private Boolean status;

}
