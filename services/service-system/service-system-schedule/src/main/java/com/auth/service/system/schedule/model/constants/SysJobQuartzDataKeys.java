package com.auth.service.system.schedule.model.constants;

import lombok.experimental.UtilityClass;

/**
 * Quartz {@link org.quartz.JobDataMap} 中与业务任务相关的键名约定
 *
 * @author Bunny
 */
@UtilityClass
public class SysJobQuartzDataKeys {

	/**
	 * 业务表 job.id，用于日志与熔断关联
	 */
	public static final String SYS_JOB_ID = "sysJobId";

	/**
	 * 调用目标字符串（Bean 方法）
	 */
	public static final String INVOKE_TARGET = "invokeTarget";

	/**
	 * 白名单任务类全限定名
	 */
	public static final String JOB_CLASS = "jobClass";

	/**
	 * 任务类型（BEAN_INVOKE/CUSTOM_CLASS）
	 */
	public static final String TASK_TYPE = "taskType";

	/**
	 * 处理器编码
	 */
	public static final String HANDLER_CODE = "handlerCode";

	/**
	 * 执行参数 JSON 原文（供内置 Job 解析嵌套结构）
	 */
	public static final String JOB_PARAMS = "jobParams";

	/**
	 * 扩展载荷 JSON
	 */
	public static final String PAYLOAD_JSON = "payloadJson";

	/**
	 * 触发类型
	 */
	public static final String TRIGGER_TYPE = "triggerType";

	/**
	 * 定时触发
	 */
	public static final String TRIGGER_TYPE_SCHEDULE = "SCHEDULE";

	/**
	 * 手动触发
	 */
	public static final String TRIGGER_TYPE_MANUAL = "MANUAL";

	/**
	 * Trigger 名称规则：jobName + "_TRIGGER"
	 */
	public static final String TRIGGER_SUFFIX = "_TRIGGER";

	/**
	 * bean名称
	 */
	public static final String BEAN_NAME = "beanName";

	/**
	 * 方法名称
	 */
	public static final String METHOD_NAME = "methodName";

}
