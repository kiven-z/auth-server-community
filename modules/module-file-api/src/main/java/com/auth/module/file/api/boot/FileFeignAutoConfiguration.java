package com.auth.module.file.api.boot;

import com.auth.module.file.api.feign.SystemFileRecordFeignClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 文件服务 Feign 客户端与跨服务 Port 装配
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignClient")
@EnableFeignClients(clients = { SystemFileRecordFeignClient.class })
public class FileFeignAutoConfiguration {

}
