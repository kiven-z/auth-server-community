package com.auth.service.system.admin.model.vo.user;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户部门关联分页行
 *
 * @author Bunny
 */
@Schema(name = "UserDeptPageVO", title = "用户部门关联分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class UserDeptPageVO extends BaseResponse {

	@JsonStringFormat
	@Schema(title = "用户 ID")
	private Long userId;

	@JsonStringFormat
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

	@Schema(title = "备注")
	private String remark;

}
