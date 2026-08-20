package com.auth.service.system.admin.model.form.dept;

import com.auth.common.web.valid.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门新增/更新表单
 *
 * @author Bunny
 */
@Schema(name = "SysDeptForm", title = "部门保存表单")
@Getter
@Setter
public class SysDeptForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "部门主键，更新时必填")
	@NotNull(groups = UpdateGroup.class, message = "部门ID不能为空")
	private Long id;

	@Schema(title = "父部门 ID（0 表示顶级）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "父部门不能为空")
	private Long parentId;

	@Schema(title = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 100, message = "部门名称长度不能超过100个字符")
	@NotBlank(message = "部门名称不能为空")
	private String deptName;

	@Schema(title = "部门编码", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 128, message = "部门编码长度不能超过128个字符")
	@NotBlank(message = "部门编码不能为空")
	private String deptCode;

	@Schema(title = "启用状态（true=正常，false=停用）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "状态不能为空")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符")
	private String remark;

}
