package com.auth.service.system.schedule.task.support;

import com.auth.service.system.schedule.config.ScheduleQuartzConfiguration;
import lombok.experimental.UtilityClass;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

/**
 * Quartz Job 通用支持：从 SchedulerContext 获取 Spring 容器等
 *
 * @author Bunny
 */
@UtilityClass
public class QuartzJobSupport {

	/**
	 * 从 SchedulerContext 获取 Spring 容器等
	 * @param context JobExecutionContext
	 * @return ApplicationContext
	 * @throws JobExecutionException JobExecutionException
	 */
	public static ApplicationContext getApplicationContext(JobExecutionContext context) throws JobExecutionException {
		try {
			// 获取SchedulerContext
			Object app = context.getScheduler().getContext().get(ScheduleQuartzConfiguration.APPLICATION_CONTEXT_KEY);

			// 如果app是ApplicationContext，则返回
			if (app instanceof ApplicationContext ac) {
				return ac;
			}
		}
		catch (Exception e) {
			throw new JobExecutionException(e);
		}
		// 如果app不是ApplicationContext，则抛出异常
		throw new JobExecutionException("Scheduler 未注入 ApplicationContext");
	}

}
