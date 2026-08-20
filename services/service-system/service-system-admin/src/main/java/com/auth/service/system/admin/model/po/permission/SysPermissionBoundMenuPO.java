package com.auth.service.system.admin.model.po.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色已绑定菜单行
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionBoundMenuPO", title = "角色已绑定菜单 PO")
@Getter
@Setter
public class SysPermissionBoundMenuPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "菜单ID")
	private Long id;

	@Schema(title = "菜单标题")
	private String title;

	@Schema(title = "路由名称")
	private String name;

	@Schema(title = "路由路径")
	private String path;

	@Schema(title = "菜单类型：0 菜单 1 iframe 2 外链")
	private Integer menuType;

	@Schema(title = "启用状态（true=启用菜单，false=停用）")
	private Boolean status;

}
