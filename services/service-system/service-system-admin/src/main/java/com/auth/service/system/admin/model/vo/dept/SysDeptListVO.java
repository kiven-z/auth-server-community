package com.auth.service.system.admin.model.vo.dept;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 部门列表行
 *
 * @author Bunny
 */
@Schema(name = "SysDeptListVO", title = "部门列表行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysDeptListVO extends BaseResponse {

	@JsonStringFormat
	private Long id;

	@JsonStringFormat
	@Schema(title = "父部门ID，0 表示顶级")
	private Long parentId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "状态：true 正常 false 禁用")
	private Boolean status;

	@Schema(title = "计算有效（本节点及全部祖先均启用）")
	private Boolean effective;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	private String remark;

}
