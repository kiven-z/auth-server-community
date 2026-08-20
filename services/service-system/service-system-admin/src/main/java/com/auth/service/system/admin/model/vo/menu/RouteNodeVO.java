package com.auth.service.system.admin.model.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 动态路由节点
 *
 * @author Bunny
 */
@Schema(title = "动态路由节点")
@Getter
@Setter
@ToString
public class RouteNodeVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "路径")
	private String path;

	@Schema(title = "路由 name")
	private String name;

	@Schema(title = "重定向")
	private String redirect;

	@Schema(title = "组件路径（Vite glob）")
	private String component;

	@Schema(title = "元信息")
	private RouteMetaVO meta;

	@Schema(title = "子路由")
	private List<RouteNodeVO> children;

}
