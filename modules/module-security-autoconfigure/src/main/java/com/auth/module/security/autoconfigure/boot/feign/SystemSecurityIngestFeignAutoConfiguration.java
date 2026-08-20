package com.auth.module.security.autoconfigure.boot.feign;

import com.auth.module.security.autoconfigure.feign.SystemSecurityIngestFeignClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 当 classpath 存在 OpenFeign 时，注册共享
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignClient")
@EnableFeignClients(clients = SystemSecurityIngestFeignClient.class)
public class SystemSecurityIngestFeignAutoConfiguration {

}
