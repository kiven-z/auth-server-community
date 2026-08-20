package com.auth.service.system.admin.model.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 路由过渡动画
 *
 * @author Bunny
 */
@Schema(title = "路由过渡动画")
@Getter
@Setter
@ToString
public class RouteTransitionVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "Vue 内置 transition name")
	private String name;

	@Schema(title = "进场 animate.css 类名")
	private String enterTransition;

	@Schema(title = "离场 animate.css 类名")
	private String leaveTransition;

}
