package com.auth.service.system.schedule.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 定时任务执行日志
 *
 * @author Bunny
 */
@TableName("log_job")
@Getter
@Setter
@Accessors(chain = true)
public class LogJobEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务名")
	private String jobName;

	@Schema(title = "任务ID")
	private Long jobId;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "执行报告")
	private String jobMessage;

	@Schema(title = "触发类型（SCHEDULE/MANUAL）")
	private String triggerType;

	@Schema(title = "执行是否成功")
	private Boolean status;

	@Schema(title = "异常信息")
	private String exceptionInfo;

	@Schema(title = "耗时毫秒")
	private Long elapsedTime;

}
