package com.auth.service.system.schedule.config;

import com.auth.service.system.schedule.listener.SysJobExecutionListener;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 与 Spring 集成：SchedulerContext 注入容器、全局 Job 监听（日志与熔断）
 *
 * @author Bunny
 */
@EnableConfigurationProperties(ScheduleJobProperties.class)
@Configuration
public class ScheduleQuartzConfiguration {

	/**
	 * Quartz Scheduler 上下文中存放 Spring 容器的键名（与 Job 内读取一致）
	 */
	public static final String APPLICATION_CONTEXT_KEY = "applicationContext";

	/**
	 * 定制 Quartz Scheduler：暴露 Spring 容器供 Entry Job 使用，并挂载全局执行监听
	 * @param sysJobExecutionListener 任务执行监听（日志与熔断）
	 * @return 定制器
	 */
	@Bean
	public SchedulerFactoryBeanCustomizer scheduleQuartzCustomizer(SysJobExecutionListener sysJobExecutionListener) {
		return factory -> {
			factory.setApplicationContextSchedulerContextKey(APPLICATION_CONTEXT_KEY);
			factory.setGlobalJobListeners(sysJobExecutionListener);
			// 由 SysJobBootstrapSynchronizer 在对账完成后再 start，避免与删建 Trigger 竞态
			factory.setAutoStartup(false);
		};
	}

}
