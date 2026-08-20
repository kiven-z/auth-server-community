package com.auth.service.system.schedule.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import com.auth.service.system.schedule.model.enums.SysJobLastExecutionStatus;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 定时任务详情
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysJobDetailVO extends BaseResponse implements SysJobLastExecutionView {

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "任务分组名称")
	private String jobGroupName;

	@Schema(title = "任务类")
	private String jobClass;

	@Schema(title = "任务类型")
	private String taskType;

	@Schema(title = "执行处理器")
	private String handlerCode;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "Cron 表达式")
	private String cronExpression;

	@Schema(title = "调度时区（IANA ZoneId）")
	private String timeZone;

	@Schema(title = "错失策略")
	private Integer misfirePolicy;

	@Schema(title = "是否并发")
	private Boolean concurrent;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "开始时间")
	private LocalDateTime startTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "结束时间")
	private LocalDateTime endTime;

	@Schema(title = "运行状态")
	private Boolean status;

	@Schema(title = "执行参数 JSON")
	private String jobParams;

	@Schema(title = "扩展载荷 JSON")
	private String payloadJson;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "上次触发")
	private Instant previousFireTime;

	@Schema(title = "下次触发")
	private Instant nextFireTime;

	@Schema(title = "运行态")
	private SysJobQuartzRuntimeStatus quartzRuntimeStatus;

	@Schema(title = "本次开始执行时间")
	private Instant quartzFireTime;

	@Schema(title = "最近结果")
	private SysJobLastExecutionStatus lastExecutionStatus;

	@Schema(title = "最近执行时间")
	private Instant lastExecutionTime;

}
