package com.auth.service.system.admin.model.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 菜单详情（列表字段 + 绑定角色计数）
 *
 * @author Bunny
 */
@Schema(name = "SysMenuDetailVO", title = "菜单详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysMenuDetailVO extends SysMenuListVO {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "重定向")
	private String redirect;

	@Schema(title = "图标")
	private String icon;

	@Schema(title = "额外图标")
	private String extraIcon;

	@Schema(title = "是否显示父级菜单")
	private Boolean showParent;

	@Schema(title = "iframe 地址")
	private String frameSrc;

	@Schema(title = "iframe 首次加载动画")
	private Boolean frameLoading;

	@Schema(title = "Vue Transition 名称")
	private String transitionName;

	@Schema(title = "进场 animate.css")
	private String enterTransition;

	@Schema(title = "离场 animate.css")
	private String leaveTransition;

	@Schema(title = "禁止加入标签页")
	private Boolean hiddenTag;

	@Schema(title = "动态标签层级")
	private Integer dynamicLevel;

	@Schema(title = "激活路径")
	private String activePath;

	@Schema(title = "已绑定角色数")
	private Long boundRoleCount;

}
