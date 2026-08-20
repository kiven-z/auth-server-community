package com.auth.service.system.schedule.model.po;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 任务日志分页行
 *
 * @author Bunny
 */
@Schema(name = "LogJobPageRowPO", title = "任务日志分页行")
@Getter
@Setter
@ToString
public class LogJobPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "任务ID")
	private Long jobId;

	@Schema(title = "任务名称")
	private String jobName;

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

	@Schema(title = "耗时毫秒")
	private Long elapsedTime;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@JsonStringFormat
	@Schema(title = "创建用户")
	private Long createdBy;

	@JsonStringFormat
	@Schema(title = "更新用户")
	private Long updatedBy;

}
