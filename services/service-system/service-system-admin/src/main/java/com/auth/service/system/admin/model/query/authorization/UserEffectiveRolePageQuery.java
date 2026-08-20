package com.auth.service.system.admin.model.query.authorization;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户生效角色分页查询
 *
 * @author Bunny
 */
@Schema(name = "UserEffectiveRolePageQuery", title = "用户生效角色分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserEffectiveRolePageQuery extends PageQueryRequest {

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

}
