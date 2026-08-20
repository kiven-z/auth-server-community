package com.auth.service.system.schedule.support.quartz;

import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import static com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys.TRIGGER_SUFFIX;

/**
 * 根据 {@link JobEntity} 构建 Quartz {@link CronTrigger}
 *
 * @author Bunny
 */
@Component
public class SysJobQuartzTriggerFactory {

	private static CronScheduleBuilder applyMisfire(CronScheduleBuilder cron, Integer policy) {
		int resolvedPolicy = Objects.requireNonNullElse(policy, 2);
		return switch (resolvedPolicy) {
			case 1 -> cron.withMisfireHandlingInstructionIgnoreMisfires();
			case 3 -> cron.withMisfireHandlingInstructionDoNothing();
			// 默认 case 2
			default -> cron.withMisfireHandlingInstructionFireAndProceed();
		};
	}

	/**
	 * 构建 CronTrigger
	 * @param job 任务实体
	 * @param jobKey 任务键
	 * @return CronTrigger
	 */
	public CronTrigger build(JobEntity job, JobKey jobKey) {
		String jobName = job.getJobName();
		String jobGroup = job.getJobGroup();
		ZoneId zoneId = ZoneId.of(SysJobTimeZone.normalize(job.getTimeZone()));
		TimeZone timeZone = TimeZone.getTimeZone(zoneId);

		// 构建 CronScheduleBuilder（按任务时区解释墙钟）
		CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule(job.getCronExpression()).inTimeZone(timeZone);
		cron = applyMisfire(cron, job.getMisfirePolicy());

		// 构建 TriggerBuilder
		TriggerBuilder<CronTrigger> triggerBuilder = TriggerBuilder.newTrigger()
			.withIdentity(jobName + TRIGGER_SUFFIX, jobGroup)
			.forJob(jobKey)
			.withSchedule(cron);

		// 设置开始时间
		if (job.getStartTime() != null) {
			triggerBuilder.startAt(Date.from(job.getStartTime().atZone(zoneId).toInstant()));
		}

		// 设置结束时间
		if (job.getEndTime() != null) {
			triggerBuilder.endAt(Date.from(job.getEndTime().atZone(zoneId).toInstant()));
		}
		return triggerBuilder.build();
	}

}
