package com.auth.service.system.admin.model.query.menu;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 菜单扁平列表查询
 *
 * @author Bunny
 */
@Schema(name = "SysMenuListQuery", title = "菜单列表查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysMenuQuery extends PageQueryRequest {

	@Schema(title = "路由名称")
	private String name;

	@Schema(title = "菜单标题")
	private String title;

	@Schema(title = "菜单类型：0 菜单 1 iframe 2 外链")
	private Integer menuType;

	@Schema(title = "启用状态（true=启用菜单，false=停用）")
	private Boolean status;

	@Schema(title = "组件路径")
	private String component;

}
