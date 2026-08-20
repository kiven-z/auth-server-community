package com.auth.service.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 集成验证服务启动类（专用于安全 / 网关 / Feign 行为验证）
 *
 * @author Bunny
 */
@SpringBootApplication
@RefreshScope
@EnableFeignClients
@EnableAsync
public class ServiceExampleMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceExampleMainApplication.class, args);
	}

}
