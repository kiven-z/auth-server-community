package com.auth.service.system.admin.model.form.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单移动
 *
 * @author Bunny
 */
@Schema(name = "SysMenuMoveForm", title = "菜单移动表单")
@Getter
@Setter
public class SysMenuMoveForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "新父菜单 ID，0 表示挂到顶级", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "新父菜单不能为空")
	private Long parentId;

}
