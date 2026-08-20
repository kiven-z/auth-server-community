package com.auth.service.system.schedule.support;

import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * Quartz 运行时状态快照（不含失败日志补充）
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class SysJobQuartzRuntimeSnapshot {

	/**
	 * 运行时状态
	 */
	SysJobQuartzRuntimeStatus status;

	/**
	 * 开始执行时间
	 */
	Instant fireTime;

	/**
	 * 构建快照
	 * @param status 运行时状态
	 * @param fireTime 开始执行时间
	 * @return 快照
	 */
	public static SysJobQuartzRuntimeSnapshot of(SysJobQuartzRuntimeStatus status, Instant fireTime) {
		return SysJobQuartzRuntimeSnapshot.builder().status(status).fireTime(fireTime).build();
	}

}
