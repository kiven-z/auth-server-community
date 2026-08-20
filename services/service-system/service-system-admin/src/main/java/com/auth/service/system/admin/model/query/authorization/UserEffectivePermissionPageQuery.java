package com.auth.service.system.admin.model.query.authorization;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户生效权限分页查询
 *
 * @author Bunny
 */
@Schema(name = "UserEffectivePermissionPageQuery", title = "用户生效权限分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserEffectivePermissionPageQuery extends PageQueryRequest {

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

}
