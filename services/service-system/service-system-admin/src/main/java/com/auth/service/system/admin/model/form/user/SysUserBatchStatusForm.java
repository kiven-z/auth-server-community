package com.auth.service.system.admin.model.form.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户批量状态
 *
 * @author Bunny
 */
@Schema(name = "SysUserBatchStatusForm", title = "用户批量状态")
@Getter
@Setter
public class SysUserBatchStatusForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户主键列表", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotEmpty(message = "用户主键列表不能为空")
	private List<Long> ids;

	@Schema(title = "目标状态（0=禁用，1=正常，2=锁定）", requiredMode = Schema.RequiredMode.REQUIRED)
	@Max(value = 2, message = "目标状态值无效")
	@Min(value = 0, message = "目标状态值无效")
	@NotNull(message = "目标状态不能为空")
	private Integer status;

}
