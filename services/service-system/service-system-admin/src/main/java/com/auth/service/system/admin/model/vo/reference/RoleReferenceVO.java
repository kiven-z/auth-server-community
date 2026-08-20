package com.auth.service.system.admin.model.vo.reference;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色关联回显基础字段
 *
 * @author Bunny
 */
@Schema(name = "RoleReferenceVO", title = "角色关联回显")
@Getter
@Setter
@ToString
public class RoleReferenceVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "角色 ID")
	private Long id;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态（true=正常启用，false=停用）")
	private Boolean status;

}
