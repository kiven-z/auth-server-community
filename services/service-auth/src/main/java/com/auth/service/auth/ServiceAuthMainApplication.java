package com.auth.service.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 认证服务启动类
 *
 * @author Bunny
 */
@SpringBootApplication
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class ServiceAuthMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceAuthMainApplication.class, args);
	}

}
