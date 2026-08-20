package com.auth.service.system.admin.model.vo.menu;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 菜单列表行
 *
 * @author Bunny
 */
@Schema(name = "SysMenuListVO", title = "菜单列表行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysMenuListVO extends BaseResponse {

	@JsonStringFormat
	private Long id;

	@JsonStringFormat
	@Schema(title = "父菜单ID，顶级为 null")
	private Long parentId;

	@Schema(title = "路由名称")
	private String name;

	@Schema(title = "菜单标题")
	private String title;

	@Schema(title = "图标")
	private String icon;

	@Schema(title = "路由路径")
	private String path;

	@Schema(title = "前端组件路径")
	private String component;

	@Schema(title = "菜单类型：0 菜单 1 iframe 2 外链")
	private Integer menuType;

	@Schema(title = "状态：0 启用 1 禁用")
	private Boolean status;

	@Schema(title = "是否在菜单中显示")
	private Boolean showLink;

	@Schema(title = "登录可见（不校验角色）")
	private Boolean publicAccess;

	@Schema(title = "排序，越大越靠后")
	private Integer menuRank;

	@Schema(title = "是否缓存")
	private Boolean keepAlive;

	@Schema(title = "是否固定标签")
	private Boolean fixedTag;

	@Schema(title = "备注")
	private String remark;

}
