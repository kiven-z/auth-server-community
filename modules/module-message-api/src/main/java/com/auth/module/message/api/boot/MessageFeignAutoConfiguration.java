package com.auth.module.message.api.boot;

import com.auth.module.message.api.feign.SystemMessageSendFeignClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 当 classpath 存在 OpenFeign 时，注册共享的 {@link SystemMessageSendFeignClient}。
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignClient")
@EnableFeignClients(clients = SystemMessageSendFeignClient.class)
public class MessageFeignAutoConfiguration {

}
