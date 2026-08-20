package com.auth.service.system.admin.model.po.reference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限关联查询投影
 *
 * @author Bunny
 */
@Schema(name = "PermissionReferencePO", title = "权限关联查询 PO")
@Getter
@Setter
public class PermissionReferencePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限 ID")
	private Long id;

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}
