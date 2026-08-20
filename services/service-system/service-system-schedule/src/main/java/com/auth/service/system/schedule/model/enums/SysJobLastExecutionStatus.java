package com.auth.service.system.schedule.model.enums;

/**
 * 定时任务最近一次执行结果
 *
 * @author Bunny
 */
public enum SysJobLastExecutionStatus {

	/**
	 * 最近一次执行成功
	 */
	SUCCESS,

	/**
	 * 最近一次执行失败
	 */
	FAILED,

	/**
	 * 无可用执行日志（未执行或未开启成功日志）
	 */
	UNKNOWN

}
