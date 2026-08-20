package com.auth.service.system.schedule.support.quartz;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.support.SysJobQuartzRuntimeSnapshot;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * 读取 Quartz 运行时快照与触发时间（只读，不参与调度写入）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SysJobQuartzRuntimeReader {

	private final Scheduler scheduler;

	public SysJobQuartzRuntimeReader(@Lazy Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * Quartz {@link Date} → {@link Instant}
	 * @param date Quartz 时间
	 * @return Instant，空则 null
	 */
	private static Instant toInstant(Date date) {
		return date == null ? null : date.toInstant();
	}

	/**
	 * 解析单个任务的 Quartz 运行时状态
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 * @return 运行时快照
	 */
	public SysJobQuartzRuntimeSnapshot resolveRuntimeSnapshot(String jobName, String jobGroup) {
		try {
			JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
			return resolveRuntimeSnapshot(jobKey, collectRunningJobKeys());
		}
		catch (SchedulerException exception) {
			log.warn("解析 Quartz 运行时状态失败: {} / {}", jobGroup, jobName, exception);
			return SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.ERROR, null);
		}
	}

	/**
	 * 查询任务触发时间
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 * @return 触发时间
	 */
	public FireTimes getFireTimes(String jobName, String jobGroup) {
		try {
			// 构建任务键
			JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
			if (!scheduler.checkExists(jobKey)) {
				return FireTimes.empty();
			}

			// 获取任务的触发器
			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
			if (CollUtil.isEmpty(triggers)) {
				return FireTimes.empty();
			}

			// 获取触发器的触发时间
			Trigger trigger = triggers.get(0);
			return FireTimes.builder()
				.previousFireTime(toInstant(trigger.getPreviousFireTime()))
				.nextFireTime(toInstant(trigger.getNextFireTime()))
				.build();
		}
		catch (Exception exception) {
			log.warn("查询触发时间失败: {} / {}", jobGroup, jobName, exception);
			return FireTimes.empty();
		}
	}

	/**
	 * 收集正在运行的任务键
	 * @return 正在运行的任务键
	 * @throws SchedulerException SchedulerException
	 */
	private Set<JobKey> collectRunningJobKeys() throws SchedulerException {
		// 收集正在运行的任务键
		Set<JobKey> runningJobKeys = new HashSet<>();
		for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
			runningJobKeys.add(context.getJobDetail().getKey());
		}
		return runningJobKeys;
	}

	/**
	 * 解析任务的 Quartz 运行时状态
	 * @param jobKey 任务键
	 * @param runningJobKeys 正在运行的任务键
	 * @return 运行时状态快照
	 * @throws SchedulerException SchedulerException
	 */
	private SysJobQuartzRuntimeSnapshot resolveRuntimeSnapshot(JobKey jobKey, Set<JobKey> runningJobKeys)
			throws SchedulerException {
		// 验证任务是否存在
		if (!scheduler.checkExists(jobKey)) {
			return SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.NOT_REGISTERED, null);
		}

		// 验证任务是否正在运行
		if (runningJobKeys.contains(jobKey)) {
			Instant fireTime = resolveRunningFireTime(jobKey);
			return SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.RUNNING, fireTime);
		}

		// 解析任务的触发状态
		SysJobQuartzRuntimeStatus triggerStatus = resolveTriggerStatus(jobKey);
		return SysJobQuartzRuntimeSnapshot.of(Objects.requireNonNullElse(triggerStatus, SysJobQuartzRuntimeStatus.IDLE),
				null);
	}

	/**
	 * 解析正在运行的任务的触发时间
	 * @param jobKey 任务键
	 * @return 触发时间
	 * @throws SchedulerException SchedulerException
	 */
	private Instant resolveRunningFireTime(JobKey jobKey) throws SchedulerException {
		// 解析正在运行的任务的触发时间
		for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
			if (jobKey.equals(context.getJobDetail().getKey())) {
				return toInstant(context.getFireTime());
			}
		}
		return null;
	}

	/**
	 * 解析任务的 Quartz 触发状态
	 * @param jobKey 任务键
	 * @return 触发状态
	 * @throws SchedulerException SchedulerException
	 */
	private SysJobQuartzRuntimeStatus resolveTriggerStatus(JobKey jobKey) throws SchedulerException {
		// 解析任务的触发状态
		List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
		if (CollUtil.isEmpty(triggers)) {
			return null;
		}

		// 解析任务的触发状态
		boolean paused = false;
		for (Trigger trigger : triggers) {
			Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());
			if (state == Trigger.TriggerState.BLOCKED) {
				return SysJobQuartzRuntimeStatus.PENDING;
			}

			if (state == Trigger.TriggerState.ERROR) {
				return SysJobQuartzRuntimeStatus.ERROR;
			}

			if (state == Trigger.TriggerState.PAUSED) {
				paused = true;
			}
		}

		// 解析任务的触发状态
		if (paused) {
			return SysJobQuartzRuntimeStatus.PAUSED;
		}
		return null;
	}

	/**
	 * 触发时间值对象
	 */
	@Value
	@Builder
	@Accessors(fluent = true)
	public static class FireTimes {

		/**
		 * 上次触发时间
		 */
		Instant previousFireTime;

		/**
		 * 下次触发时间
		 */
		Instant nextFireTime;

		/**
		 * 空触发时间
		 * @return 无触发时间
		 */
		public static FireTimes empty() {
			return FireTimes.builder().build();
		}

	}

}
