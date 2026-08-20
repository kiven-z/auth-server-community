package com.auth.service.system.schedule.runner;

import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.support.quartz.SysJobScheduleReconciler;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 应用就绪后延迟将 job 对账至 Quartz，再启动调度器（业务表为单一事实来源）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SysJobBootstrapSynchronizer {

	private final SysJobScheduleReconciler sysJobScheduleReconciler;

	private final Scheduler scheduler;

	private final ScheduleJobProperties scheduleJobProperties;

	private final ScheduledExecutorService bootstrapExecutor;

	private final AtomicBoolean bootstrapScheduled = new AtomicBoolean(false);

	private final AtomicReference<ScheduledFuture<?>> bootstrapFuture = new AtomicReference<>();

	@Autowired
	public SysJobBootstrapSynchronizer(SysJobScheduleReconciler sysJobScheduleReconciler, @Lazy Scheduler scheduler,
			ScheduleJobProperties scheduleJobProperties) {
		this(sysJobScheduleReconciler, scheduler, scheduleJobProperties, newSingleThreadBootstrapExecutor());
	}

	SysJobBootstrapSynchronizer(SysJobScheduleReconciler sysJobScheduleReconciler, Scheduler scheduler,
			ScheduleJobProperties scheduleJobProperties, ScheduledExecutorService bootstrapExecutor) {
		this.sysJobScheduleReconciler = sysJobScheduleReconciler;
		this.scheduler = scheduler;
		this.scheduleJobProperties = scheduleJobProperties;
		this.bootstrapExecutor = bootstrapExecutor;
	}

	private static ScheduledExecutorService newSingleThreadBootstrapExecutor() {
		return Executors.newSingleThreadScheduledExecutor(runnable -> {
			Thread thread = new Thread(runnable, "sys-job-bootstrap");
			thread.setDaemon(true);
			return thread;
		});
	}

	/**
	 * 应用就绪后异步延迟对账，避免阻塞 Ready 事件链路
	 * @param event 应用就绪事件
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady(ApplicationReadyEvent event) {
		if (!bootstrapScheduled.compareAndSet(false, true)) {
			return;
		}

		int delaySeconds = scheduleJobProperties.getBootstrapDelaySeconds();
		log.info("Quartz bootstrap scheduled after {}s", delaySeconds);
		bootstrapFuture.set(bootstrapExecutor.schedule(() -> {
			try {
				log.info("Syncing Quartz from job before scheduler start");
				sysJobScheduleReconciler.reconcileAll();
				log.info("job reconciled to Quartz");
			}
			catch (RuntimeException exception) {
				log.error("Failed to reconcile job to Quartz during bootstrap", exception);
			}
			finally {
				try {
					if (!scheduler.isStarted()) {
						scheduler.start();
						log.info("Quartz scheduler started after job bootstrap sync");
					}
				}
				catch (SchedulerException exception) {
					// 异步线程中仅记录，避免未捕获异常淹没启动失败原因
					log.error("Failed to start Quartz scheduler after bootstrap sync", exception);
				}
			}
		}, delaySeconds, TimeUnit.SECONDS));
	}

	/**
	 * 关闭时取消未执行的启动对账，避免关停后仍写 Quartz
	 */
	@PreDestroy
	public void destroy() {
		ScheduledFuture<?> future = bootstrapFuture.get();
		if (future != null) {
			future.cancel(false);
		}
		bootstrapExecutor.shutdownNow();
	}

}
