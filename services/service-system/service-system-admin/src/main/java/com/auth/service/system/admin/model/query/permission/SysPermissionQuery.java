package com.auth.service.system.admin.model.query.permission;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionQuery", title = "权限分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysPermissionQuery extends PageQueryRequest {

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "启用状态（true=正常启用，false=停用）")
	private Boolean status;

}
