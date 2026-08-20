package com.auth.service.system.admin.model.form.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户岗位关联更新表单（不含岗位 ID）
 *
 * @author Bunny
 */
@Schema(name = "UserPostRelationUpdateForm", title = "用户岗位关联更新")
@Getter
@Setter
public class UserPostRelationUpdateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "是否主岗位", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "是否主岗位不能为空")
	private Boolean isPrimary;

	@Schema(title = "备注")
	@Size(max = 255, message = "备注长度不能超过255个字符")
	private String remark;

}
