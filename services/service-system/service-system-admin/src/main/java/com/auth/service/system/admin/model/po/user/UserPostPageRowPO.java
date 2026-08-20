package com.auth.service.system.admin.model.po.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 用户岗位关联分页行
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserPostPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键 ID")
	private Long id;

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "岗位 ID")
	private Long postId;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "岗位本节点启用状态")
	private Boolean postStatus;

	@Schema(title = "岗位计算有效（本节点启用且所属部门计算有效）")
	private Boolean postEffective;

	@Schema(title = "是否主岗位")
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
