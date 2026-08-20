package com.auth.service.system.message.config.properties;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.config.MessageChannelCapability;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件渠道开关配置（凭证由 Spring Mail 托管）
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.email")
@RefreshScope
@Configuration
public class EmailChannelProperties implements MessageChannelCapability {

	/**
	 * 邮件渠道开关
	 */
	private boolean enabled = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel channel() {
		return MessageChannel.EMAIL;
	}

}
