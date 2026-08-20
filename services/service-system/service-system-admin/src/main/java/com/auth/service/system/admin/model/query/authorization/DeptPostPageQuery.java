package com.auth.service.system.admin.model.query.authorization;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门下属岗位分页查询
 *
 * @author Bunny
 */
@Schema(name = "DeptPostPageQuery", title = "部门下属岗位分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DeptPostPageQuery extends PageQueryRequest {

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}
