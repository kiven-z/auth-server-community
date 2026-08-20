package com.auth.service.system.admin.model.vo.post;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import com.auth.service.system.admin.model.vo.reference.DeptReferenceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 岗位详情 VO（标量 + 所属部门快照 + 授权关系计数）
 *
 * @author Bunny
 */
@Schema(name = "SysPostDetailVO", title = "岗位详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysPostDetailVO extends BaseResponse {

	@JsonStringFormat
	@Schema(title = "所属部门ID")
	private Long deptId;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

	@Schema(title = "计算有效", description = "本节点启用且所属部门计算有效")
	private Boolean effective;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "所属部门快照")
	private DeptReferenceVO boundDept;

	@Schema(title = "绑定用户数", description = "含岗位已停用")
	private Long boundUserCount;

}
