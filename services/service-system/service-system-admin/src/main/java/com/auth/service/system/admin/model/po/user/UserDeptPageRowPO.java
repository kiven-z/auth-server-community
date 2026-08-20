package com.auth.service.system.admin.model.po.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 用户部门关联分页行
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserDeptPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键 ID")
	private Long id;

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "部门 ID")
	private Long deptId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "部门本节点启用状态")
	private Boolean deptStatus;

	@Schema(title = "部门计算有效（本节点及全部祖先均启用）")
	private Boolean deptEffective;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

	@Schema(title = "备注")
	private String remark;

}
