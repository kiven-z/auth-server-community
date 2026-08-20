package com.auth.service.system.schedule.model.form;

import com.auth.common.web.valid.group.CreateGroup;
import com.auth.service.system.schedule.validation.form.ValidSysJobCreateForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 新增定时任务（JobKey 创建后不可改）
 *
 * @author Bunny
 */
@ValidSysJobCreateForm(groups = CreateGroup.class)
@Schema(name = "SysJobCreateForm", title = "新增定时任务")
@Getter
@Setter
public class SysJobCreateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务名称")
	@Size(max = 64)
	@NotBlank(message = "任务名称不能为空", groups = CreateGroup.class)
	private String jobName;

	@Schema(title = "任务分组编码")
	@Size(max = 64)
	@NotBlank(groups = CreateGroup.class)
	private String jobGroup;

	@Schema(title = "白名单任务类全限定名")
	@Size(max = 255)
	private String jobClass;

	@Schema(title = "任务类型")
	@Size(max = 32)
	@NotBlank(groups = CreateGroup.class)
	private String taskType;

	@Schema(title = "执行处理器编码")
	@Size(max = 64)
	private String handlerCode;

	@Schema(title = "调用目标")
	@Size(max = 500)
	private String invokeTarget;

	@Schema(title = "Cron 表达式")
	@Size(max = 255)
	@NotBlank(groups = CreateGroup.class)
	private String cronExpression;

	@Schema(title = "调度时区（IANA ZoneId，默认 Asia/Shanghai）")
	@Size(max = 64)
	private String timeZone;

	@Schema(title = "错失策略（1/2/3）")
	@NotNull(groups = CreateGroup.class)
	private Integer misfirePolicy;

	@Schema(title = "是否并发（0否 1是）")
	@NotNull(groups = CreateGroup.class)
	private Boolean concurrent;

	@Schema(title = "开始时间")
	private LocalDateTime startTime;

	@Schema(title = "结束时间")
	private LocalDateTime endTime;

	@Schema(title = "运行状态（true=正常调度，false=暂停）")
	@NotNull(groups = CreateGroup.class)
	private Boolean status;

	@Schema(title = "执行参数 JSON")
	private String jobParams;

	@Schema(title = "扩展载荷 JSON")
	private String payloadJson;

	@Schema(title = "备注")
	@Size(max = 500)
	private String remark;

}
