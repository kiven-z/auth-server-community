package com.auth.service.system.admin.model.form.post;

import com.auth.common.web.valid.group.CreateGroup;
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
 * 岗位新增/更新表单
 *
 * @author Bunny
 */
@Schema(name = "SysPostForm", title = "岗位保存表单")
@Getter
@Setter
public class SysPostForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "岗位主键，更新时必填")
	@NotNull(groups = UpdateGroup.class, message = "岗位ID不能为空")
	private Long id;

	@Schema(title = "所属部门ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "部门不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private Long deptId;

	@Schema(title = "岗位编码", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 64, message = "岗位编码长度不能超过64个字符", groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "岗位编码不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String postCode;

	@Schema(title = "岗位名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 128, message = "岗位名称长度不能超过128个字符", groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "岗位名称不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String postName;

	@Schema(title = "启用状态（true=启用，false=停用）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "状态不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符", groups = { CreateGroup.class, UpdateGroup.class })
	private String remark;

}
