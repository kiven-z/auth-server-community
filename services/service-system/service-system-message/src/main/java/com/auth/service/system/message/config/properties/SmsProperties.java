package com.auth.service.system.message.config.properties;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.config.MessageChannelCapability;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import static com.auth.service.system.message.support.template.MessageConfigAssertions.assertNotBlank;

/**
 * 短信渠道业务配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.sms")
@RefreshScope
@Configuration
public class SmsProperties implements MessageChannelCapability {

	/**
	 * 短信渠道开关
	 */
	private boolean enabled = true;

	private String region;

	private String endpoint;

	private String accessKeyId;

	private String accessKeySecret;

	/**
	 * 短信签名
	 */
	private String signName;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel channel() {
		return MessageChannel.SMS;
	}

	/**
	 * 断言发送短信所需配置齐全；发送时校验，防止未配置仍发起调用
	 */
	public void assertSendRequiredConfigured() {
		assertNotBlank(region, "短信区域");
		assertNotBlank(endpoint, "短信服务地址");
		assertNotBlank(accessKeyId, "短信 AccessKeyId");
		assertNotBlank(accessKeySecret, "短信 AccessKeySecret");
		assertNotBlank(signName, "短信签名");
	}

}
