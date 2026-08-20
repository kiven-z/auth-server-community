package com.auth.service.system.message.model.form.inapp;

import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

import static com.auth.common.web.validation.ValidationPatterns.UPPER_ALNUM_UNDERSCORE;

/**
 * 站内信业务分类新增/更新表单
 *
 * @author Bunny
 */
@Schema(name = "InAppMessageCategoryForm", title = "站内信业务分类表单")
@Getter
@Setter
public class InAppMessageCategoryForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键", description = "更新必填")
	@NotNull(groups = UpdateGroup.class, message = "更新时ID不能为空")
	private Long id;

	@Schema(title = "父分类 ID", description = "0 或空=大类；非 0=小类，须指向大类")
	private Long parentId;

	@Schema(title = "分类码")
	@Size(max = 64, groups = { CreateGroup.class, UpdateGroup.class })
	@Pattern(regexp = UPPER_ALNUM_UNDERSCORE, message = "仅允许大写字母、数字、下划线",
			groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "分类码不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String code;

	@Schema(title = "展示名")
	@Size(max = 64, groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "展示名不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String name;

	@Schema(title = "同级排序")
	private Integer sortOrder;

	@Schema(title = "启用状态", description = "true=启用，false=停用")
	@NotNull(message = "启用状态不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private Boolean status;

	@Schema(title = "备注")
	@Size(max = 500, groups = { CreateGroup.class, UpdateGroup.class })
	private String remark;

}
