package com.auth.service.system.admin.model.po.reference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门关联查询投影
 *
 * @author Bunny
 */
@Schema(name = "DeptReferencePO", title = "部门关联查询 PO")
@Getter
@Setter
public class DeptReferencePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "部门 ID")
	private Long id;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "启用状态（true=正常，false=禁用）")
	private Boolean status;

}
