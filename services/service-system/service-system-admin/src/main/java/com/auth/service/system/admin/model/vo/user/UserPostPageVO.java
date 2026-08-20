package com.auth.service.system.admin.model.vo.user;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户岗位关联分页行
 *
 * @author Bunny
 */
@Schema(name = "UserPostPageVO", title = "用户岗位关联分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class UserPostPageVO extends BaseResponse {

	@JsonStringFormat
	@Schema(title = "用户 ID")
	private Long userId;

	@JsonStringFormat
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

	@Schema(title = "备注")
	private String remark;

}
