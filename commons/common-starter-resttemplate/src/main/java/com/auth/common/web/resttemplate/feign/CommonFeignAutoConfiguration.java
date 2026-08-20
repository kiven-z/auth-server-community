package com.auth.common.web.resttemplate.feign;

import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * OpenFeign 全局装配：将远端 {@link com.auth.common.core.model.response.Result} 错误透传为业务异常
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration(beforeName = "org.springframework.cloud.openfeign.FeignAutoConfiguration")
@ConditionalOnClass(name = "feign.codec.ErrorDecoder")
public class CommonFeignAutoConfiguration {

	/**
	 * 全局 Feign 错误解码器
	 * @return Result 错误解码器
	 */
	@ConditionalOnMissingBean(ErrorDecoder.class)
	@Bean
	public ErrorDecoder resultFeignErrorDecoder() {
		log.info("Registering ResultFeignErrorDecoder for OpenFeign");
		return new ResultFeignErrorDecoder();
	}

}
