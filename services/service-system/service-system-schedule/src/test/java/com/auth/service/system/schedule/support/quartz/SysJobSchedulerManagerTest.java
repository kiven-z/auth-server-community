package com.auth.service.system.schedule.support.quartz;

import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.entity.JobEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysJobSchedulerManager} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobSchedulerManager Quartz 调度写门面")
@ExtendWith(MockitoExtension.class)
class SysJobSchedulerManagerTest {

	@Mock
	private Scheduler scheduler;

	@Mock
	private SysJobQuartzJobFactory sysJobQuartzJobFactory;

	@Mock
	private SysJobQuartzTriggerFactory sysJobQuartzTriggerFactory;

	@InjectMocks
	private SysJobSchedulerManager sysJobSchedulerManager;

	private static JobEntity enabledJob() {
		JobEntity job = new JobEntity();
		job.setId(1L);
		job.setJobName("demo");
		job.setJobGroup("G");
		job.setTaskType("BEAN_INVOKE");
		job.setInvokeTarget("demo.run()");
		job.setCronExpression("0 0 12 * * ?");
		job.setStatus(true);
		job.setConcurrent(false);
		return job;
	}

	@Test
	@DisplayName("pauseJob：任务存在时应调用 scheduler.pauseJob")
	void pauseJob_whenJobExists_pausesSchedulerJob() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("demoJob", "DEFAULT");
		when(scheduler.checkExists(jobKey)).thenReturn(true);

		sysJobSchedulerManager.pauseJob("demoJob", "DEFAULT");

		verify(scheduler).pauseJob(jobKey);
	}

	@Test
	@DisplayName("pauseGroup：分组编码有效时应调用 scheduler.pauseJobs")
	void pauseGroup_whenGroupCodePresent_pausesSchedulerGroup() throws SchedulerException {
		sysJobSchedulerManager.pauseGroup("G");

		verify(scheduler).pauseJobs(GroupMatcher.groupEquals("G"));
	}

	@Test
	@DisplayName("scheduleOrReplace：暂停且 Quartz 无任务时不注册调度")
	void scheduleOrReplace_whenPausedAndMissing_doesNotSchedule() throws SchedulerException {
		JobEntity job = new JobEntity();
		job.setJobName("demo");
		job.setJobGroup("G");
		job.setStatus(false);
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(false);

		sysJobSchedulerManager.scheduleOrReplace(job);

		verify(scheduler).checkExists(jobKey);
		verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	@DisplayName("scheduleOrReplace：暂停且 Quartz 仍有任务时应 pauseJob")
	void scheduleOrReplace_whenPausedAndExists_pausesJob() throws SchedulerException {
		JobEntity job = new JobEntity();
		job.setJobName("demo");
		job.setJobGroup("G");
		job.setStatus(false);
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(true);

		sysJobSchedulerManager.scheduleOrReplace(job);

		verify(scheduler).pauseJob(jobKey);
		verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	@DisplayName("pauseJob：任务不存在时不调用 scheduler.pauseJob")
	void pauseJob_whenJobMissing_skipsPause() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("missing", "DEFAULT");
		when(scheduler.checkExists(jobKey)).thenReturn(false);

		sysJobSchedulerManager.pauseJob("missing", "DEFAULT");

		verify(scheduler).checkExists(jobKey);
		verify(scheduler, never()).pauseJob(any());
	}

	@Test
	@DisplayName("scheduleOrReplace：启用且 Quartz 无任务时应注册调度")
	void scheduleOrReplace_whenEnabledAndMissing_schedulesJob() throws SchedulerException {
		JobEntity job = enabledJob();
		JobKey jobKey = JobKey.jobKey("demo", "G");
		JobDetail jobDetail = mock(JobDetail.class);
		CronTrigger trigger = mock(CronTrigger.class);
		when(scheduler.checkExists(jobKey)).thenReturn(false);
		when(sysJobQuartzJobFactory.build(job)).thenReturn(jobDetail);
		when(sysJobQuartzTriggerFactory.build(job, jobKey)).thenReturn(trigger);

		sysJobSchedulerManager.scheduleOrReplace(job);

		verify(scheduler).scheduleJob(jobDetail, trigger);
	}

	@Test
	@DisplayName("scheduleOrReplace：启用且配置一致且未暂停时应跳过删建")
	void scheduleOrReplace_whenEnabledAndMatches_skipsReplace() throws SchedulerException {
		JobEntity job = enabledJob();
		JobKey jobKey = JobKey.jobKey("demo", "G");
		TriggerKey triggerKey = TriggerKey.triggerKey("demo" + SysJobQuartzDataKeys.TRIGGER_SUFFIX, "G");

		JobDataMap dataMap = new JobDataMap();
		dataMap.put(SysJobQuartzDataKeys.SYS_JOB_ID, "1");
		dataMap.put(SysJobQuartzDataKeys.INVOKE_TARGET, "demo.run()");
		dataMap.put(SysJobQuartzDataKeys.JOB_CLASS, null);
		dataMap.put(SysJobQuartzDataKeys.TASK_TYPE, "BEAN_INVOKE");
		dataMap.put(SysJobQuartzDataKeys.HANDLER_CODE, "beanInvoke");
		dataMap.put(SysJobQuartzDataKeys.PAYLOAD_JSON, null);
		dataMap.put(SysJobQuartzDataKeys.JOB_PARAMS, null);

		JobDetail jobDetail = mock(JobDetail.class);
		doReturn(Job.class).when(jobDetail).getJobClass();
		when(jobDetail.getJobDataMap()).thenReturn(dataMap);

		CronTrigger cronTrigger = mock(CronTrigger.class);
		when(cronTrigger.getCronExpression()).thenReturn("0 0 12 * * ?");
		when(cronTrigger.getMisfireInstruction()).thenReturn(Trigger.MISFIRE_INSTRUCTION_SMART_POLICY);
		when(cronTrigger.getEndTime()).thenReturn(null);

		when(scheduler.checkExists(jobKey)).thenReturn(true);
		when(scheduler.getTrigger(triggerKey)).thenReturn(cronTrigger);
		when(scheduler.getTriggerState(triggerKey)).thenReturn(Trigger.TriggerState.NORMAL);
		when(scheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
		when(sysJobQuartzJobFactory.build(job)).thenReturn(jobDetail);
		when(sysJobQuartzTriggerFactory.build(job, jobKey)).thenReturn(cronTrigger);

		sysJobSchedulerManager.scheduleOrReplace(job);

		verify(scheduler, never()).deleteJob(any());
		verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	@DisplayName("scheduleOrReplace：启用但 Trigger 已暂停时应删后建")
	void scheduleOrReplace_whenEnabledButPaused_replacesJob() throws SchedulerException {
		JobEntity job = enabledJob();
		JobKey jobKey = JobKey.jobKey("demo", "G");
		TriggerKey triggerKey = TriggerKey.triggerKey("demo" + SysJobQuartzDataKeys.TRIGGER_SUFFIX, "G");
		JobDetail jobDetail = mock(JobDetail.class);
		CronTrigger cronTrigger = mock(CronTrigger.class);
		CronTrigger newTrigger = mock(CronTrigger.class);

		when(scheduler.checkExists(jobKey)).thenReturn(true);
		when(scheduler.getTrigger(triggerKey)).thenReturn(cronTrigger);
		when(scheduler.getTriggerState(triggerKey)).thenReturn(Trigger.TriggerState.PAUSED);
		when(sysJobQuartzJobFactory.build(job)).thenReturn(jobDetail);
		when(sysJobQuartzTriggerFactory.build(job, jobKey)).thenReturn(newTrigger);

		sysJobSchedulerManager.scheduleOrReplace(job);

		verify(scheduler).deleteJob(jobKey);
		verify(scheduler).scheduleJob(jobDetail, newTrigger);
	}

	@Test
	@DisplayName("scheduleOrReplace：实体已清空 endTime 但 Quartz 仍有结束时间时应删后建")
	void scheduleOrReplace_whenEndTimeCleared_replacesJob() throws SchedulerException {
		JobEntity job = enabledJob();
		JobKey jobKey = JobKey.jobKey("demo", "G");
		TriggerKey triggerKey = TriggerKey.triggerKey("demo" + SysJobQuartzDataKeys.TRIGGER_SUFFIX, "G");

		JobDataMap dataMap = new JobDataMap();
		dataMap.put(SysJobQuartzDataKeys.SYS_JOB_ID, "1");
		dataMap.put(SysJobQuartzDataKeys.INVOKE_TARGET, "demo.run()");
		dataMap.put(SysJobQuartzDataKeys.JOB_CLASS, null);
		dataMap.put(SysJobQuartzDataKeys.TASK_TYPE, "BEAN_INVOKE");
		dataMap.put(SysJobQuartzDataKeys.HANDLER_CODE, "beanInvoke");
		dataMap.put(SysJobQuartzDataKeys.PAYLOAD_JSON, null);
		dataMap.put(SysJobQuartzDataKeys.JOB_PARAMS, null);

		JobDetail jobDetail = mock(JobDetail.class);
		doReturn(Job.class).when(jobDetail).getJobClass();
		when(jobDetail.getJobDataMap()).thenReturn(dataMap);

		CronTrigger existingTrigger = mock(CronTrigger.class);
		when(existingTrigger.getCronExpression()).thenReturn("0 0 12 * * ?");
		when(existingTrigger.getMisfireInstruction()).thenReturn(Trigger.MISFIRE_INSTRUCTION_SMART_POLICY);
		when(existingTrigger.getEndTime()).thenReturn(new Date());

		CronTrigger newTrigger = mock(CronTrigger.class);
		when(scheduler.checkExists(jobKey)).thenReturn(true);
		when(scheduler.getTrigger(triggerKey)).thenReturn(existingTrigger);
		when(scheduler.getTriggerState(triggerKey)).thenReturn(Trigger.TriggerState.NORMAL);
		when(scheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
		when(sysJobQuartzJobFactory.build(job)).thenReturn(jobDetail);
		when(sysJobQuartzTriggerFactory.build(job, jobKey)).thenReturn(existingTrigger, newTrigger);

		sysJobSchedulerManager.scheduleOrReplace(job);

		verify(scheduler).deleteJob(jobKey);
		verify(scheduler).scheduleJob(jobDetail, newTrigger);
	}

	@Test
	@DisplayName("deleteJob：任务存在时应删除")
	void deleteJob_whenExists_deletes() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(true);

		sysJobSchedulerManager.deleteJob("demo", "G");

		verify(scheduler).deleteJob(jobKey);
	}

	@Test
	@DisplayName("deleteJob：任务不存在时不调用 deleteJob")
	void deleteJob_whenMissing_skipsDelete() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(false);

		sysJobSchedulerManager.deleteJob("demo", "G");

		verify(scheduler, never()).deleteJob(any());
	}

	@Test
	@DisplayName("triggerOnce：JobDetail 不存在时应先补建再手动触发")
	void triggerOnce_whenMissing_addsDetailThenTriggers() throws SchedulerException {
		JobEntity job = enabledJob();
		JobKey jobKey = JobKey.jobKey("demo", "G");
		JobDetail jobDetail = mock(JobDetail.class);
		when(scheduler.checkExists(jobKey)).thenReturn(false);
		when(sysJobQuartzJobFactory.build(job)).thenReturn(jobDetail);

		sysJobSchedulerManager.triggerOnce(job);

		verify(scheduler).addJob(jobDetail, false);
		verify(scheduler).triggerJob(eq(jobKey), argThat(map -> SysJobQuartzDataKeys.TRIGGER_TYPE_MANUAL
			.equals(map.getString(SysJobQuartzDataKeys.TRIGGER_TYPE))));
	}

	@Test
	@DisplayName("triggerOnce：JobDetail 已存在时应直接手动触发")
	void triggerOnce_whenExists_triggersOnly() throws SchedulerException {
		JobEntity job = enabledJob();
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(true);

		sysJobSchedulerManager.triggerOnce(job);

		verify(scheduler, never()).addJob(any(), anyBoolean());
		verify(scheduler).triggerJob(eq(jobKey), any(JobDataMap.class));
	}

}
