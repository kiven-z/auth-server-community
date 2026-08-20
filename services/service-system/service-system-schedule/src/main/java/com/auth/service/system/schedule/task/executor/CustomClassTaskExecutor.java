package com.auth.service.system.schedule.task.executor;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.support.QuartzJobSupport;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 自定义 Job 类模式执行器
 *
 * @author Bunny
 */
@Slf4j
@Component
public class CustomClassTaskExecutor implements SysJobTaskExecutor {

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.CUSTOM_CLASS;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// 获取JobDataMap
		JobDataMap jobDataMap = context.getMergedJobDataMap();
		String jobClassName = jobDataMap.getString(SysJobQuartzDataKeys.JOB_CLASS);
		if (CharSequenceUtil.isBlank(jobClassName)) {
			throw new JobExecutionException("job_class 为空");
		}

		try {
			// 获取ApplicationContext
			ApplicationContext app = QuartzJobSupport.getApplicationContext(context);

			// 获取Class
			Class<?> raw = Class.forName(jobClassName);
			if (!Job.class.isAssignableFrom(raw)) {
				throw new JobExecutionException("job_class 未实现 org.quartz.Job: " + jobClassName);
			}

			// 创建Job
			Class<? extends Job> target = (Class<? extends Job>) raw;
			Job job = app.getAutowireCapableBeanFactory().createBean(target);
			job.execute(context);
		}
		catch (JobExecutionException exception) {
			throw exception;
		}
		catch (Exception exception) {
			log.warn("自定义 Job 执行失败 jobClass={} jobKey={}", jobClassName, context.getJobDetail().getKey(), exception);
			throw new JobExecutionException(exception);
		}
	}

}
