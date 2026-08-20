package com.auth.service.system.admin.model.po.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单与角色编码关联行（持久层查询投影，供动态路由批量加载）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuRoleLinkRow", title = "菜单-角色关联行")
@Getter
@Setter
public class SysMenuRoleLinkRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "菜单ID")
	private Long menuId;

	@Schema(title = "角色编码")
	private String roleCode;

}
