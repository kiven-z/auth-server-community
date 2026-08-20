package com.auth.service.system.admin.model.query.authorization;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色已绑定权限分页查询
 *
 * @author Bunny
 */
@Schema(name = "RolePermissionPageQuery", title = "角色已绑定权限分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RolePermissionPageQuery extends PageQueryRequest {

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}
