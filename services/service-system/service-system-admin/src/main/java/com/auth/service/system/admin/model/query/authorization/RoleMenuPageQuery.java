package com.auth.service.system.admin.model.query.authorization;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色已绑定菜单分页查询
 *
 * @author Bunny
 */
@Schema(name = "RoleMenuPageQuery", title = "角色已绑定菜单分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RoleMenuPageQuery extends PageQueryRequest {

	@Schema(title = "菜单标题")
	private String title;

	@Schema(title = "路由名称")
	private String name;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}
