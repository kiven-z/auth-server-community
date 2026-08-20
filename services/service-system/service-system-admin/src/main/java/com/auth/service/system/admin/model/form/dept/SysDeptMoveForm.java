package com.auth.service.system.admin.model.form.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门移动
 *
 * @author Bunny
 */
@Schema(name = "SysDeptMoveForm", title = "部门移动表单")
@Getter
@Setter
public class SysDeptMoveForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "部门主键", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "部门ID不能为空")
	private Long id;

	@Schema(title = "新父部门ID，0 表示挂到顶级", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "新父部门不能为空")
	private Long parentId;

}
