package com.auth.service.system.schedule.model.enums;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.task.SysJobEntryJob;
import org.quartz.Job;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_TASK_TYPE_UNSUPPORTED;

/**
 * 定时任务类型
 *
 * @author Bunny
 */
public enum SysJobTaskType {

	/**
	 * Bean 调用模式：执行 invoke_target
	 */
	BEAN_INVOKE,

	/**
	 * 自定义类模式：执行 job_class（需实现 Quartz Job）
	 */
	CUSTOM_CLASS;

	/**
	 * 解析任务类型
	 * @param taskTypeRaw taskType 原始值
	 * @return 任务类型
	 */
	public static SysJobTaskType resolve(String taskTypeRaw) {
		try {
			return SysJobTaskType.valueOf(taskTypeRaw);
		}
		catch (Exception exception) {
			throw new SysJobException(JOB_TASK_TYPE_UNSUPPORTED, taskTypeRaw);
		}
	}

	/**
	 * 默认处理器编码
	 * @return 处理器编码
	 */
	public String defaultHandlerCode() {
		return switch (this) {
			case BEAN_INVOKE -> "beanInvokeHandler";
			case CUSTOM_CLASS -> "customClassHandler";
		};
	}

	/**
	 * 根据并发策略返回 Quartz 入口 Job 类型
	 * @param concurrent 是否允许并发
	 * @return 入口 Job 类
	 */
	public Class<? extends Job> resolveEntryJobClass(boolean concurrent) {
		return concurrent ? SysJobEntryJob.class : SysJobEntryJob.Disallow.class;
	}

}
