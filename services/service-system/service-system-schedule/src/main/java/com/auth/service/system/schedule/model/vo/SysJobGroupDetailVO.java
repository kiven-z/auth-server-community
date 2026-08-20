package com.auth.service.system.schedule.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 任务分组详情
 *
 * @author Bunny
 */
@Schema(name = "SysJobGroupDetailVO", title = "任务分组详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysJobGroupDetailVO extends BaseResponse {

	@Schema(title = "分组编码")
	private String groupCode;

	@Schema(title = "显示名称")
	private String groupName;

	@Schema(title = "描述")
	private String description;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

	@Schema(title = "是否系统内置")
	private Boolean isSystem;

	@Schema(title = "排序号")
	private Integer orderNum;

}
