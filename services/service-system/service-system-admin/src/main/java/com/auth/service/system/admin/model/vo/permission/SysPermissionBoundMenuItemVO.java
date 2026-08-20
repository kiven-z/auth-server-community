package com.auth.service.system.admin.model.vo.permission;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色详情
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionBoundMenuItemVO", title = "角色已绑定菜单项")
@Getter
@Setter
@ToString
public class SysPermissionBoundMenuItemVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
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
