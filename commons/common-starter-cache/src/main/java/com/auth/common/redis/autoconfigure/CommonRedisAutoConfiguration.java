package com.auth.common.redis.autoconfigure;

import com.auth.common.redis.config.JacksonConfig;
import com.auth.common.redis.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Redis 公共模块自动导入配置
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration(after = { JacksonAutoConfiguration.class })
@Import({ JacksonConfig.class, RedisConfig.class })
public class CommonRedisAutoConfiguration implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) {
		log.info("Common redis initialized successfully");
	}

}
