package com.auth.service.system.schedule.runner;

import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.support.quartz.SysJobScheduleReconciler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link SysJobBootstrapSynchronizer} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobBootstrapSynchronizer 启动同步")
@ExtendWith(MockitoExtension.class)
class SysJobBootstrapSynchronizerTest {

	@Mock
	private SysJobScheduleReconciler sysJobScheduleReconciler;

	@Mock
	private Scheduler scheduler;

	@Mock
	private ScheduledExecutorService bootstrapExecutor;

	@Mock
	@SuppressWarnings("rawtypes")
	private ScheduledFuture bootstrapFuture;

	private ScheduleJobProperties scheduleJobProperties;

	private SysJobBootstrapSynchronizer sysJobBootstrapSynchronizer;

	private ApplicationReadyEvent applicationReadyEvent;

	@BeforeEach
	void setUp() {
		scheduleJobProperties = new ScheduleJobProperties();
		scheduleJobProperties.setBootstrapDelaySeconds(0);
		sysJobBootstrapSynchronizer = new SysJobBootstrapSynchronizer(sysJobScheduleReconciler, scheduler,
				scheduleJobProperties, bootstrapExecutor);
		ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
		applicationReadyEvent = new ApplicationReadyEvent(mock(SpringApplication.class), new String[] {}, context,
				Duration.ZERO);
	}

	@Test
	@DisplayName("应用就绪后应按配置延迟调度对账任务")
	void onApplicationReadySchedulesBootstrapWithConfiguredDelay() throws SchedulerException {
		scheduleJobProperties.setBootstrapDelaySeconds(30);
		doReturn(bootstrapFuture).when(bootstrapExecutor).schedule(any(Runnable.class), eq(30L), eq(TimeUnit.SECONDS));

		sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent);

		verify(bootstrapExecutor).schedule(any(Runnable.class), eq(30L), eq(TimeUnit.SECONDS));
		verify(sysJobScheduleReconciler, never()).reconcileAll();
		verify(scheduler, never()).isStarted();
	}

	@Test
	@DisplayName("延迟任务执行时应先全量对账再启动调度器")
	void bootstrapReconcilesThenStartsScheduler() throws SchedulerException {
		when(scheduler.isStarted()).thenReturn(false);
		Runnable bootstrapTask = captureBootstrapTask();

		bootstrapTask.run();

		verify(sysJobScheduleReconciler).reconcileAll();
		verify(scheduler).start();
	}

	@Test
	@DisplayName("调度器已启动时不应重复 start")
	void bootstrapSkipsStartWhenSchedulerAlreadyStarted() throws SchedulerException {
		when(scheduler.isStarted()).thenReturn(true);
		Runnable bootstrapTask = captureBootstrapTask();

		bootstrapTask.run();

		verify(sysJobScheduleReconciler).reconcileAll();
		verify(scheduler, never()).start();
	}

	@Test
	@DisplayName("对账失败时仍应启动调度器且不向外抛出")
	void bootstrapStartsSchedulerEvenWhenReconcileFails() throws SchedulerException {
		when(scheduler.isStarted()).thenReturn(false);
		doThrow(new RuntimeException("reconcile failed")).when(sysJobScheduleReconciler).reconcileAll();
		Runnable bootstrapTask = captureBootstrapTask();

		bootstrapTask.run();

		verify(scheduler).start();
	}

	@Test
	@DisplayName("调度器启动失败时仅记录日志且不向外抛出")
	void bootstrapLogsSchedulerStartFailureWithoutThrowing() throws SchedulerException {
		when(scheduler.isStarted()).thenReturn(false);
		doThrow(new SchedulerException("start failed")).when(scheduler).start();
		Runnable bootstrapTask = captureBootstrapTask();

		bootstrapTask.run();

		verify(scheduler).start();
	}

	@Test
	@DisplayName("重复 Ready 事件不应重复调度")
	void onApplicationReadySchedulesOnlyOnce() {
		doReturn(bootstrapFuture).when(bootstrapExecutor)
			.schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS));

		sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent);
		sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent);

		verify(bootstrapExecutor, times(1)).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
	}

	@Test
	@DisplayName("销毁时应取消未执行任务并关闭执行器")
	void destroyCancelsFutureAndShutsDownExecutor() {
		doReturn(bootstrapFuture).when(bootstrapExecutor)
			.schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS));
		sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent);

		sysJobBootstrapSynchronizer.destroy();

		verify(bootstrapFuture).cancel(false);
		verify(bootstrapExecutor).shutdownNow();
	}

	private Runnable captureBootstrapTask() {
		ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
		doReturn(bootstrapFuture).when(bootstrapExecutor)
			.schedule(runnableCaptor.capture(), anyLong(), eq(TimeUnit.SECONDS));
		sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent);
		return runnableCaptor.getValue();
	}

}
