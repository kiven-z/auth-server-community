package com.auth.service.system.admin.model.vo.role;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色分页行
 *
 * @author Bunny
 */
@Schema(name = "SysRolePageVO", title = "角色分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysRolePageVO extends BaseResponse {

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态（true=正常启用，false=停用）")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

}
