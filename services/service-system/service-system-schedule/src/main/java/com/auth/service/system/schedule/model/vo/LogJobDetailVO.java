package com.auth.service.system.schedule.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 任务日志详情
 *
 * @author Bunny
 */
@Schema(name = "LogJobDetailVO", title = "任务日志详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogJobDetailVO extends BaseResponse {

	@Schema(title = "任务名称")
	private String jobName;

	@JsonStringFormat
	@Schema(title = "任务ID")
	private Long jobId;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "触发类型（SCHEDULE/MANUAL）")
	private String triggerType;

	@Schema(title = "执行报告")
	private String jobMessage;

	@Schema(title = "执行是否成功（1=成功，0=失败）")
	private Boolean status;

	@Schema(title = "异常信息")
	private String exceptionInfo;

	@Schema(title = "耗时毫秒")
	private Long elapsedTime;

}
