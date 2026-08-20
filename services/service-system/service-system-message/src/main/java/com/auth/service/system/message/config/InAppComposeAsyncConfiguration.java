package com.auth.service.system.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 站内信群发异步线程池
 *
 * @author Bunny
 */
@Configuration
public class InAppComposeAsyncConfiguration {

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
	 * 站内信群发专用线程池
	 * @return 执行器
	 */
	@Bean(name = "inAppComposeExecutor")
	public Executor inAppComposeExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(CORE_POOL_SIZE);
		executor.setMaxPoolSize(MAX_POOL_SIZE);
		executor.setQueueCapacity(QUEUE_CAPACITY);
		executor.setThreadNamePrefix("in-app-compose-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}

}
