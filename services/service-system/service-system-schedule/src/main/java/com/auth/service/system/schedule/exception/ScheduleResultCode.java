package com.auth.service.system.schedule.exception;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 定时任务 / Quartz / Job 配置结果码
 * <p>
 * 号段：216、220、226、228、239–241、243、246、248–249（217–219、224–225、235、244–245 已释放）
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum ScheduleResultCode implements SystemResultCode {

	/**
	 * invoke_target 非法（{0}=原因）
	 */
	JOB_INVOKE_TARGET_INVALID(422, 216, "JOB_INVOKE_TARGET_INVALID", "system.job.invoke_target.invalid"),

	/**
	 * Quartz 操作失败（{0}=register|update|delete|runOnce，{1}=详情）
	 */
	JOB_QUARTZ_OPERATION_FAILED(500, 220, "JOB_QUARTZ_OPERATION_FAILED", "system.job.quartz.operation_failed"),

	/**
	 * 任务分组不存在或未启用
	 */
	JOB_GROUP_NOT_AVAILABLE(422, 226, "JOB_GROUP_NOT_AVAILABLE", "system.job.group.not_available"),

	/**
	 * 任务类不可用（{0}=原因）
	 */
	JOB_CLASS_INVALID(422, 228, "JOB_CLASS_INVALID", "system.job.class.invalid"),

	/**
	 * 系统核心分组禁止删除
	 */
	JOB_GROUP_SYSTEM_PROTECTED(422, 239, "JOB_GROUP_SYSTEM_PROTECTED", "system.job.group.system_protected"),

	/**
	 * 分组下仍有任务
	 */
	JOB_GROUP_HAS_ACTIVE_JOBS(422, 240, "JOB_GROUP_HAS_ACTIVE_JOBS", "system.job.group.has_active_jobs"),

	/**
	 * 任务类型不支持
	 */
	JOB_TASK_TYPE_UNSUPPORTED(422, 241, "JOB_TASK_TYPE_UNSUPPORTED", "system.job.task_type.unsupported"),

	/**
	 * 调度时区不支持（{0}=ZoneId）
	 */
	JOB_TIME_ZONE_UNSUPPORTED(422, 249, "JOB_TIME_ZONE_UNSUPPORTED", "system.job.time_zone.unsupported"),

	/**
	 * 任务配置 JSON 非法（{0}=字段名，{1}=原因）
	 */
	JOB_CONFIG_INVALID(422, 243, "JOB_CONFIG_INVALID", "system.job.config.invalid"),

	/**
	 * 熔断后暂停 Quartz 任务失败（{0}=jobGroup，{1}=jobName）
	 */
	JOB_CIRCUIT_BREAKER_PAUSE_FAILED(500, 246, "JOB_CIRCUIT_BREAKER_PAUSE_FAILED",

			"system.job.circuit_breaker.pause_failed"),

	/**
	 * 任务正在执行或排队中，禁止立即执行
	 */
	JOB_BUSY(422, 248, "JOB_BUSY", "system.job.busy");

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	ScheduleResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
