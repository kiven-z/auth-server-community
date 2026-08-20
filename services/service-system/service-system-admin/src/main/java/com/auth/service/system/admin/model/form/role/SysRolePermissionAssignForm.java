package com.auth.service.system.admin.model.form.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 角色分配权限表单（全量替换）
 *
 * @author Bunny
 */
@Schema(name = "SysRolePermissionAssignForm", title = "角色权限分配表单")
@Getter
@Setter
public class SysRolePermissionAssignForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限 ID 列表（全量替换；空数组表示清空该角色全部权限）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "权限 ID 列表不能为空")
	private List<Long> permissionIds;

}
