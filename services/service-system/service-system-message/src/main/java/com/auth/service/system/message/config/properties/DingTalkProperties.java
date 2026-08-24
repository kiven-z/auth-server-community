package com.auth.service.system.message.config.properties;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.config.MessageChannelCapability;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import static com.auth.service.system.message.support.template.MessageConfigAssertions.assertNotBlank;
import static com.auth.service.system.message.support.template.MessageConfigAssertions.assertNotNull;

/**
 * 钉钉工作通知配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.ding-talk")
@RefreshScope
@Configuration
public class DingTalkProperties implements MessageChannelCapability {

	/**
	 * 钉钉渠道开关
	 */
	private boolean enabled = true;

	@Nullable
	private Long agentId;

	@Nullable
	private String clientId;

	@Nullable
	private String clientSecret;

	/**
	 * 未指定正文格式时的默认值
	 */
	private MessageContentType defaultMessageType = MessageContentType.TEXT;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel channel() {
		return MessageChannel.DING_TALK;
	}

	/**
	 * 断言发送钉钉消息所需配置齐全
	 */
	public void assertSendRequiredConfigured() {
		assertNotBlank(clientId, "钉钉 ClientId");
		assertNotBlank(clientSecret, "钉钉 ClientSecret");
		assertNotNull(agentId, "钉钉 AgentId");
	}

}
