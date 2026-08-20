package com.auth.service.system.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * system-admin 通用异步线程池
 *
 * @author Bunny
 */
@Configuration
public class AdminAsyncConfiguration {

	/**
	 * 核心线程数
	 */
	private static final int CORE_POOL_SIZE = 2;

	/**
	 * 最大线程数
	 */
	private static final int MAX_POOL_SIZE = 8;

	/**
	 * 队列容量
	 */
	private static final int QUEUE_CAPACITY = 200;

	/**
	 * system-admin 通用异步执行器（头像清理等后台任务共用）
	 * @return 执行器
	 */
	@Bean(name = "adminAsyncExecutor")
	public Executor adminAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(CORE_POOL_SIZE);
		executor.setMaxPoolSize(MAX_POOL_SIZE);
		executor.setQueueCapacity(QUEUE_CAPACITY);
		executor.setThreadNamePrefix("admin-async-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}

}
