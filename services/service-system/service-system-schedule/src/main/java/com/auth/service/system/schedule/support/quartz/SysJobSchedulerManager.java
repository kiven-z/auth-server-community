package com.auth.service.system.schedule.support.quartz;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTimeZone;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * Quartz 调度写门面：按业务意图注册、暂停、删除、立即触发
 *
 * @author Bunny
 */
@Component
public class SysJobSchedulerManager {

	private static final String[] JOB_DATA_COMPARE_KEYS = { SysJobQuartzDataKeys.SYS_JOB_ID,
			SysJobQuartzDataKeys.INVOKE_TARGET, SysJobQuartzDataKeys.JOB_CLASS, SysJobQuartzDataKeys.TASK_TYPE,
			SysJobQuartzDataKeys.HANDLER_CODE, SysJobQuartzDataKeys.PAYLOAD_JSON, SysJobQuartzDataKeys.JOB_PARAMS };

	/**
	 * 启用任务不应处于的 Trigger 状态
	 */
	private static final Set<Trigger.TriggerState> NON_MATCHING_TRIGGER_STATES = Set.of(Trigger.TriggerState.PAUSED,
			Trigger.TriggerState.ERROR);

	private final Scheduler scheduler;

	private final SysJobQuartzJobFactory sysJobQuartzJobFactory;

	private final SysJobQuartzTriggerFactory sysJobQuartzTriggerFactory;

	public SysJobSchedulerManager(@Lazy Scheduler scheduler, SysJobQuartzJobFactory sysJobQuartzJobFactory,
			SysJobQuartzTriggerFactory sysJobQuartzTriggerFactory) {
		this.scheduler = scheduler;
		this.sysJobQuartzJobFactory = sysJobQuartzJobFactory;
		this.sysJobQuartzTriggerFactory = sysJobQuartzTriggerFactory;
	}

	/**
	 * 按当前实体状态同步调度：启用时差量注册（未变更则跳过删建）；停用时仅暂停 Quartz
	 * @param job 任务实体
	 */
	public void scheduleOrReplace(JobEntity job) throws SchedulerException {
		String jobName = job.getJobName();
		String jobGroup = job.getJobGroup();
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

		if (job.getStatus() != null && !job.getStatus()) {
			pauseJob(jobName, jobGroup);
			return;
		}

		if (matchesExisting(job, jobKey)) {
			return;
		}

		if (scheduler.checkExists(jobKey)) {
			scheduler.deleteJob(jobKey);
		}
		JobDetail detail = sysJobQuartzJobFactory.build(job);
		CronTrigger trigger = sysJobQuartzTriggerFactory.build(job, jobKey);
		scheduler.scheduleJob(detail, trigger);
	}

	/**
	 * 暂停单个任务（不存在则忽略）
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 */
	public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		if (scheduler.checkExists(jobKey)) {
			scheduler.pauseJob(jobKey);
		}
	}

	/**
	 * 暂停整个分组下的所有任务（仅暂停 Quartz 调度状态，不删除 Trigger）
	 * @param groupCode 分组编码
	 */
	public void pauseGroup(String groupCode) throws SchedulerException {
		if (CharSequenceUtil.isBlank(groupCode)) {
			return;
		}
		scheduler.pauseJobs(GroupMatcher.groupEquals(groupCode));
	}

	/**
	 * 删除 Quartz 中的任务（不存在则忽略）
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 */
	public void deleteJob(String jobName, String jobGroup) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		if (scheduler.checkExists(jobKey)) {
			scheduler.deleteJob(jobKey);
		}
	}

	/**
	 * 立即触发一次：若尚无 JobDetail 则先补建（不建 Cron），再按手动类型触发
	 * @param job 任务实体
	 */
	public void triggerOnce(JobEntity job) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
		if (!scheduler.checkExists(jobKey)) {
			scheduler.addJob(sysJobQuartzJobFactory.build(job), false);
		}

		JobDataMap onceMap = new JobDataMap();
		onceMap.put(SysJobQuartzDataKeys.TRIGGER_TYPE, SysJobQuartzDataKeys.TRIGGER_TYPE_MANUAL);
		scheduler.triggerJob(jobKey, onceMap);
	}

	/**
	 * 判断 Quartz 现态是否已与业务实体一致（启用态、未暂停、Job/Trigger 关键字段相同）
	 * @param job 任务实体
	 * @param jobKey 任务关键字
	 * @return 是否匹配
	 */
	private boolean matchesExisting(JobEntity job, JobKey jobKey) throws SchedulerException {
		if (!scheduler.checkExists(jobKey)) {
			return false;
		}

		TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName() + SysJobQuartzDataKeys.TRIGGER_SUFFIX,
				job.getJobGroup());
		Trigger existingTrigger = scheduler.getTrigger(triggerKey);
		if (!(existingTrigger instanceof CronTrigger existingCron)) {
			return false;
		}

		Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
		if (NON_MATCHING_TRIGGER_STATES.contains(triggerState)) {
			return false;
		}

		JobDetail expectedDetail = sysJobQuartzJobFactory.build(job);
		JobDetail existingDetail = scheduler.getJobDetail(jobKey);
		if (existingDetail == null || !Objects.equals(expectedDetail.getJobClass(), existingDetail.getJobClass())) {
			return false;
		}

		JobDataMap expectedMap = expectedDetail.getJobDataMap();
		JobDataMap existingMap = existingDetail.getJobDataMap();
		for (String key : JOB_DATA_COMPARE_KEYS) {
			if (!Objects.equals(Objects.toString(expectedMap.get(key), null),
					Objects.toString(existingMap.get(key), null))) {
				return false;
			}
		}

		CronTrigger expectedTrigger = sysJobQuartzTriggerFactory.build(job, jobKey);
		if (!Objects.equals(expectedTrigger.getCronExpression(), existingCron.getCronExpression())
				|| expectedTrigger.getMisfireInstruction() != existingCron.getMisfireInstruction()
				|| !Objects.equals(expectedTrigger.getTimeZone(), existingCron.getTimeZone())) {
			return false;
		}

		ZoneId zoneId = ZoneId.of(SysJobTimeZone.normalize(job.getTimeZone()));

		// start 未配置时忽略 Quartz 默认 startAt(now)
		LocalDateTime expectedStart = job.getStartTime();
		Date existingStart = existingCron.getStartTime();
		if (expectedStart != null && (existingStart == null
				|| Date.from(expectedStart.atZone(zoneId).toInstant()).getTime() != existingStart.getTime())) {
			return false;
		}

		// end 未配置时要求 Quartz 也无结束时间，避免清空 endTime 后仍跳过重建
		LocalDateTime expectedEnd = job.getEndTime();
		Date existingEnd = existingCron.getEndTime();
		if (expectedEnd == null) {
			return existingEnd == null;
		}
		return existingEnd != null
				&& Date.from(expectedEnd.atZone(zoneId).toInstant()).getTime() == existingEnd.getTime();
	}

}
