package com.auth.service.system.message.config;

import com.auth.module.message.api.channel.MessageChannel;

/**
 * 消息渠道运行时能力
 *
 * @author Bunny
 */
public interface MessageChannelCapability {

	/**
	 * 获取逻辑渠道
	 * @return 逻辑渠道
	 */
	MessageChannel channel();

	/**
	 * 是否允许发送
	 * @return true-允许发送，false-渠道关闭
	 */
	boolean isEnabled();

}
