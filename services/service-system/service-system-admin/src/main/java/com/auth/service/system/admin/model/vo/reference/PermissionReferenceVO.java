package com.auth.service.system.admin.model.vo.reference;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限关联回显基础字段
 *
 * @author Bunny
 */
@Schema(name = "PermissionReferenceVO", title = "权限关联回显")
@Getter
@Setter
@ToString
public class PermissionReferenceVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "权限 ID")
	private Long id;

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}
