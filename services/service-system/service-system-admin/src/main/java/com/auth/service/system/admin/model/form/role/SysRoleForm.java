package com.auth.service.system.admin.model.form.role;

import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.convention.AuthCodeConvention;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色新增/更新表单
 *
 * @author Bunny
 */
@Schema(name = "SysRoleForm", title = "角色保存表单")
@Getter
@Setter
public class SysRoleForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色主键，更新时必填")
	@NotNull(groups = UpdateGroup.class, message = "角色ID不能为空")
	private Long id;

	@Schema(title = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 64, message = "角色编码长度不能超过64个字符")
	@Pattern(regexp = AuthCodeConvention.ROLE_CODE_REGEX, message = "角色编码格式不符合约定（全大写，仅字母与下划线，且首字符为大写字母）")
	@NotBlank(message = "角色编码不能为空")
	private String roleCode;

	@Schema(title = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 128, message = "角色名称长度不能超过128个字符")
	@NotBlank(message = "角色名称不能为空")
	private String roleName;

	@Schema(title = "启用状态（true=启用，false=停用）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "状态不能为空")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符")
	private String remark;

}
