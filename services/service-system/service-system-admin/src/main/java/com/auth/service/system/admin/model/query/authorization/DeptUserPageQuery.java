package com.auth.service.system.admin.model.query.authorization;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门关联用户分页查询
 *
 * @author Bunny
 */
@Schema(name = "DeptUserPageQuery", title = "部门关联用户分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DeptUserPageQuery extends PageQueryRequest {

	@Schema(title = "关键词（用户名/昵称）")
	private String keyword;

	@Schema(title = "用户状态（0=禁用，1=正常，2=锁定）")
	private Integer status;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

}
