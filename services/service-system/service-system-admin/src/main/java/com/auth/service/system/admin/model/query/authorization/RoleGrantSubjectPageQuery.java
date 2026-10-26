package com.auth.service.system.admin.model.query.authorization;

import com.auth.common.core.model.query.PageQueryRequest;
import com.auth.module.security.contract.grant.GrantTableSubjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色授权对象分页查询
 *
 * @author Bunny
 */
@Schema(name = "RoleGrantSubjectPageQuery", title = "角色授权对象分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RoleGrantSubjectPageQuery extends PageQueryRequest {

	@Schema(title = "主体类型（USER / DEPT / POST）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "主体类型不能为空")
	private GrantTableSubjectType subjectType;

	@Schema(title = "关键词（用户名/昵称、部门名/编码、岗位名/编码）")
	private String keyword;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}
