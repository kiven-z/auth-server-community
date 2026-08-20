package com.auth.service.system.admin.model.po.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门 ID 与岗位编码组成的查询键
 *
 * @author Bunny
 */
@Schema(name = "PostDeptCodePairPO", title = "岗位部门编码对 PO")
@Getter
@Setter
@EqualsAndHashCode(of = { "deptId", "postCode" })
public class PostDeptCodePairPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "部门 ID")
	private Long deptId;

	@Schema(title = "岗位编码")
	private String postCode;

}
