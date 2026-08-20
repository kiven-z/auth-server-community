package com.auth.service.system.schedule.task;

import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.executor.SysJobTaskExecutor;
import com.auth.service.system.schedule.task.support.QuartzJobSupport;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;

/**
 * 统一定时任务 Quartz 入口：按 taskType 委派 {@link SysJobTaskExecutor}
 *
 * @author Bunny
 */
@Slf4j
public class SysJobEntryJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			// 从 SchedulerContext 获取 Spring 容器
			ApplicationContext app = QuartzJobSupport.getApplicationContext(context);
			JobDataMap jobDataMap = context.getMergedJobDataMap();

			// 按 taskType 委派对应执行器
			SysJobTaskType taskType = SysJobTaskType.resolve(jobDataMap.getString(SysJobQuartzDataKeys.TASK_TYPE));
			SysJobTaskExecutorRegistry registry = app.getBean(SysJobTaskExecutorRegistry.class);
			registry.resolve(taskType).execute(context);
		}
		catch (JobExecutionException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

	/**
	 * 禁止并发版本：仅通过注解控制并发
	 */
	@DisallowConcurrentExecution
	public static class Disallow extends SysJobEntryJob {

	}

}
