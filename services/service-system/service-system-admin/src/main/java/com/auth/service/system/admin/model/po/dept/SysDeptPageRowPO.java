package com.auth.service.system.admin.model.po.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 部门分页行
 *
 * @author Bunny
 */
@Schema(name = "SysDeptPageRowPO", title = "部门分页行")
@Getter
@Setter
@ToString
public class SysDeptPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "父部门ID，0 表示顶级")
	private Long parentId;

	@Schema(title = "父部门编码")
	private String parentDeptCode;

	@Schema(title = "父部门名称")
	private String parentDeptName;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "启用状态", description = "true=正常，false=禁用")
	private Boolean status;

	@Schema(title = "计算有效", description = "本节点及全部祖先均启用")
	private Boolean effective;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}
