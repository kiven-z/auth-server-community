package com.auth.service.system.schedule.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 任务分组分页查询
 *
 * @author Bunny
 */
@Schema(name = "SysJobGroupQuery", title = "任务分组查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysJobGroupQuery extends PageQueryRequest {

	@Schema(title = "分组编码")
	private String groupCode;

	@Schema(title = "显示名称")
	private String groupName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}
