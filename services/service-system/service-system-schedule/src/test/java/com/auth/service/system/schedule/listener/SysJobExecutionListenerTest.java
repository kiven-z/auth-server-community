package com.auth.service.system.schedule.listener;

import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.service.LogJobService;
import com.auth.service.system.schedule.support.SysJobCircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link SysJobExecutionListener} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobExecutionListener 任务执行监听")
@ExtendWith(MockitoExtension.class)
class SysJobExecutionListenerTest {

	@Mock
	private LogJobService logJobService;

	@Mock
	private SysJobCircuitBreaker sysJobCircuitBreaker;

	@Mock
	private ScheduleJobProperties scheduleJobProperties;

	@InjectMocks
	private SysJobExecutionListener sysJobExecutionListener;

	@Test
	@DisplayName("jobWasExecuted：执行失败时应记录日志并触发熔断评估")
	void jobWasExecuted_onFailure_recordsLogAndEvaluatesCircuitBreaker() {
		when(scheduleJobProperties.isSuccessEnabled()).thenReturn(false);

		JobExecutionContext context = mock(JobExecutionContext.class);
		JobDetail jobDetail = mock(JobDetail.class);
		JobKey jobKey = JobKey.jobKey("demoJob", "DEFAULT");
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(SysJobQuartzDataKeys.SYS_JOB_ID, "1");
		jobDataMap.put(SysJobQuartzDataKeys.INVOKE_TARGET, "demoBean.run()");
		jobDataMap.put(SysJobQuartzDataKeys.TRIGGER_TYPE, SysJobQuartzDataKeys.TRIGGER_TYPE_SCHEDULE);

		when(context.getJobDetail()).thenReturn(jobDetail);
		when(jobDetail.getKey()).thenReturn(jobKey);
		when(context.getMergedJobDataMap()).thenReturn(jobDataMap);

		JobExecutionException jobException = new JobExecutionException("boom");
		sysJobExecutionListener.jobToBeExecuted(context);
		sysJobExecutionListener.jobWasExecuted(context, jobException);

		verify(logJobService).recordFailure(eq(1L), eq("demoJob"), eq("DEFAULT"), eq("demoBean.run()"),
				eq(SysJobQuartzDataKeys.TRIGGER_TYPE_SCHEDULE), org.mockito.ArgumentMatchers.anyLong(),
				eq(jobException));
		verify(sysJobCircuitBreaker).evaluateAfterFailure(1L, "demoJob", "DEFAULT");
	}

	@Test
	@DisplayName("jobWasExecuted：执行成功且未开启成功日志时不写库")
	void jobWasExecuted_onSuccess_withoutSuccessLog_skipsPersistence() {
		when(scheduleJobProperties.isSuccessEnabled()).thenReturn(false);

		JobExecutionContext context = mock(JobExecutionContext.class);
		JobDetail jobDetail = mock(JobDetail.class);
		JobKey jobKey = JobKey.jobKey("demoJob", "DEFAULT");
		JobDataMap jobDataMap = new JobDataMap();

		when(context.getJobDetail()).thenReturn(jobDetail);
		when(jobDetail.getKey()).thenReturn(jobKey);
		when(context.getMergedJobDataMap()).thenReturn(jobDataMap);

		sysJobExecutionListener.jobToBeExecuted(context);
		sysJobExecutionListener.jobWasExecuted(context, null);

		verifyNoInteractions(logJobService);
		verifyNoInteractions(sysJobCircuitBreaker);
	}

}
