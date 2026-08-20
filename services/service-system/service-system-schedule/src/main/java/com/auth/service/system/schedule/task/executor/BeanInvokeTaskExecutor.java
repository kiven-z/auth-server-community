package com.auth.service.system.schedule.task.executor;

import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.support.QuartzBeanInvoker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * Bean 调用模式执行器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class BeanInvokeTaskExecutor implements SysJobTaskExecutor {

	private final QuartzBeanInvoker quartzBeanInvoker;

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.BEAN_INVOKE;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			String invokeTarget = context.getMergedJobDataMap().getString(SysJobQuartzDataKeys.INVOKE_TARGET);
			String out = quartzBeanInvoker.invoke(invokeTarget);
			JobDetail jobDetail = context.getJobDetail();
			log.debug("Quartz bean job ok jobKey={} result={}", jobDetail.getKey(), out);
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

}
