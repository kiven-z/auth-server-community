package com.auth.service.system.admin.model.po.reference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色关联查询投影
 *
 * @author Bunny
 */
@Schema(name = "RoleReferencePO", title = "角色关联查询 PO")
@Getter
@Setter
public class RoleReferencePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色 ID")
	private Long id;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态（true=正常启用，false=停用）")
	private Boolean status;

}
