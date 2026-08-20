package com.auth.service.system.schedule.support.quartz;

import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.support.SysJobQuartzRuntimeSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobQuartzRuntimeReader} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobQuartzRuntimeReader Quartz 运行时读取")
@ExtendWith(MockitoExtension.class)
class SysJobQuartzRuntimeReaderTest {

	@Mock
	private Scheduler scheduler;

	@InjectMocks
	private SysJobQuartzRuntimeReader sysJobQuartzRuntimeReader;

	@Test
	@DisplayName("resolveRuntimeSnapshot：Job 不存在时返回 NOT_REGISTERED")
	void resolveRuntimeSnapshotWhenMissingReturnsNotRegistered() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("missing", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(false);
		when(scheduler.getCurrentlyExecutingJobs()).thenReturn(Collections.emptyList());

		SysJobQuartzRuntimeSnapshot snapshot = sysJobQuartzRuntimeReader.resolveRuntimeSnapshot("missing", "G");

		assertThat(snapshot.status()).isEqualTo(SysJobQuartzRuntimeStatus.NOT_REGISTERED);
	}

	@Test
	@DisplayName("resolveRuntimeSnapshot：正在执行时返回 RUNNING")
	void resolveRuntimeSnapshotWhenRunningReturnsRunning() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(true);
		JobExecutionContext context = mock(JobExecutionContext.class);
		JobDetail jobDetail = mock(JobDetail.class);
		when(context.getJobDetail()).thenReturn(jobDetail);
		when(jobDetail.getKey()).thenReturn(jobKey);
		when(context.getFireTime()).thenReturn(new Date());
		when(scheduler.getCurrentlyExecutingJobs()).thenReturn(List.of(context));

		SysJobQuartzRuntimeSnapshot snapshot = sysJobQuartzRuntimeReader.resolveRuntimeSnapshot("demo", "G");

		assertThat(snapshot.status()).isEqualTo(SysJobQuartzRuntimeStatus.RUNNING);
		assertThat(snapshot.fireTime()).isNotNull();
	}

	@Test
	@DisplayName("resolveRuntimeSnapshot：Trigger BLOCKED 时返回 PENDING")
	void resolveRuntimeSnapshotWhenBlockedReturnsPending() throws SchedulerException {
		JobKey jobKey = JobKey.jobKey("demo", "G");
		when(scheduler.checkExists(jobKey)).thenReturn(true);
		when(scheduler.getCurrentlyExecutingJobs()).thenReturn(Collections.emptyList());
		Trigger trigger = mock(Trigger.class);
		TriggerKey triggerKey = TriggerKey.triggerKey("demo_TRIGGER", "G");
		when(trigger.getKey()).thenReturn(triggerKey);
		when(scheduler.getTriggersOfJob(jobKey)).thenAnswer(invocation -> List.of(trigger));
		when(scheduler.getTriggerState(triggerKey)).thenReturn(Trigger.TriggerState.BLOCKED);

		SysJobQuartzRuntimeSnapshot snapshot = sysJobQuartzRuntimeReader.resolveRuntimeSnapshot("demo", "G");

		assertThat(snapshot.status()).isEqualTo(SysJobQuartzRuntimeStatus.PENDING);
	}

}
