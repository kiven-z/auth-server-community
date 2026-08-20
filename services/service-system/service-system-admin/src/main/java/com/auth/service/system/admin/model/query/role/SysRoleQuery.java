package com.auth.service.system.admin.model.query.role;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "SysRoleQuery", title = "角色分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysRoleQuery extends PageQueryRequest {

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态（true=正常启用，false=停用）")
	private Boolean status;

}
