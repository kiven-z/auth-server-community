package com.auth.service.system.admin.model.vo.reference.ext;

import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 菜单角色分配回显（在角色基础字段上扩展是否已分配）
 *
 * @author Bunny
 */
@Schema(name = "MenuAssignedRoleReferenceVO", title = "菜单已分配角色回显")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MenuAssignedRoleReferenceVO extends RoleReferenceVO {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "是否已分配给当前菜单")
	private Boolean assigned;

}
