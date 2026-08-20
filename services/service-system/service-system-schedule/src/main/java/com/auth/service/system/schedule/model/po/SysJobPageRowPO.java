package com.auth.service.system.schedule.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 定时任务分页行
 *
 * @author Bunny
 */
@Schema(name = "SysJobPageRowPO", title = "定时任务分页行")
@Getter
@Setter
@ToString
public class SysJobPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "白名单任务类")
	private String jobClass;

	@Schema(title = "任务类型（BEAN_INVOKE/CUSTOM_CLASS）")
	private String taskType;

	@Schema(title = "执行处理器编码")
	private String handlerCode;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "Cron")
	private String cronExpression;

	@Schema(title = "调度时区（IANA ZoneId）")
	private String timeZone;

	@Schema(title = "错失策略")
	private Integer misfirePolicy;

	@Schema(title = "是否并发（true=允许并发执行，false=禁止）")
	private Boolean concurrent;

	@Schema(title = "开始时间")
	private LocalDateTime startTime;

	@Schema(title = "结束时间")
	private LocalDateTime endTime;

	@Schema(title = "运行状态（true=正常调度，false=暂停）")
	private Boolean status;

	@Schema(title = "执行参数 JSON，写入 JobDataMap")
	private String jobParams;

	@Schema(title = "扩展载荷 JSON")
	private String payloadJson;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}
