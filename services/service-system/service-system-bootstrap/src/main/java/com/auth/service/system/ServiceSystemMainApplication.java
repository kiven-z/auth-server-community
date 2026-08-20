package com.auth.service.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 系统服务启动类
 *
 * @author Bunny
 */
@SpringBootApplication
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class ServiceSystemMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceSystemMainApplication.class, args);
	}

}
