package com.auth.service.system.schedule.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 任务日志分页查询
 *
 * @author Bunny
 */
@Schema(name = "LogJobQuery", title = "任务日志查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LogJobQuery extends PageQueryRequest {

	@Schema(title = "任务 ID")
	private Long jobId;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "触发类型（SCHEDULE/MANUAL）")
	private String triggerType;

	@Schema(title = "耗时（ms）")
	private Long elapsedTime;

	@Schema(title = "执行是否成功（1=仅成功，0=仅失败）")
	private Boolean status;

}
