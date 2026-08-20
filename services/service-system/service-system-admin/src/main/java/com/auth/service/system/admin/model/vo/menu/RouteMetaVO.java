package com.auth.service.system.admin.model.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 动态路由 meta
 *
 * @author Bunny
 */
@Schema(title = "路由 Meta")
@Getter
@Setter
@ToString
public class RouteMetaVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "标题")
	private String title;

	@Schema(title = "图标")
	private String icon;

	@Schema(title = "是否在菜单展示")
	private Boolean showLink;

	@Schema(title = "登录可见（不校验角色）")
	private Boolean publicAccess;

	@Schema(title = "排序，越大越靠后（顶级路由）")
	private Integer rank;

	@Schema(title = "页面可见角色编码（绑定角色或祖先壳冒泡）")
	private List<String> roles;

	@Schema(title = "额外图标")
	private String extraIcon;

	@Schema(title = "是否显示父级菜单")
	private Boolean showParent;

	@Schema(title = "是否缓存")
	private Boolean keepAlive;

	@Schema(title = "iframe 地址")
	private String frameSrc;

	@Schema(title = "iframe 首次加载动画")
	private Boolean frameLoading;

	@Schema(title = "过渡动画")
	private RouteTransitionVO transition;

	@Schema(title = "禁止加入标签页")
	private Boolean hiddenTag;

	@Schema(title = "动态路由标签层级")
	private Integer dynamicLevel;

	@Schema(title = "激活菜单 path")
	private String activePath;

	@Schema(title = "固定标签")
	private Boolean fixedTag;

}
