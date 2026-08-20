package com.auth.service.system.schedule.model.vo;

import com.auth.service.system.schedule.model.enums.SysJobLastExecutionStatus;

import java.time.Instant;

/**
 * 可补充最近一次执行结果的视图（分页行 / 详情共用）
 *
 * @author Bunny
 */
public interface SysJobLastExecutionView {

	/**
	 * 任务主键
	 * @return 主键
	 */
	Long getId();

	/**
	 * 写入最近一次执行结果
	 * @param status 执行结果
	 */
	void setLastExecutionStatus(SysJobLastExecutionStatus status);

	/**
	 * 写入最近一次执行时间
	 * @param time 执行时间
	 */
	void setLastExecutionTime(Instant time);

}
