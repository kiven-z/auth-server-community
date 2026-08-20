package com.auth.service.system.admin.model.po.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位关键词搜索查询行
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysPostSearchItemPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "岗位ID")
	private Long id;

	@Schema(title = "所属部门ID")
	private Long deptId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}
