package com.auth.service.system.admin.model.query.authorization;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 主体已授角色分页查询
 *
 * @author Bunny
 */
@Schema(name = "SubjectRolePageQuery", title = "主体已授角色分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SubjectRolePageQuery extends PageQueryRequest {

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}
