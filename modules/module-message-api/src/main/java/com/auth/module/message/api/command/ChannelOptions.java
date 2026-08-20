package com.auth.module.message.api.command;

import com.auth.module.message.api.model.dingtalk.DingTalkChannelOptions;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.module.message.api.model.inapp.InAppChannelOptions;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * 渠道扩展参数
 *
 * @author Bunny
 */
@JsonSubTypes({
		// 邮件多态标识
		@JsonSubTypes.Type(value = EmailChannelOptions.class, name = EmailChannelOptions.TYPE),
		// 钉钉多态标识
		@JsonSubTypes.Type(value = DingTalkChannelOptions.class, name = DingTalkChannelOptions.TYPE),
		// 站内信多态标识
		@JsonSubTypes.Type(value = InAppChannelOptions.class, name = InAppChannelOptions.TYPE) })
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface ChannelOptions extends Serializable {

}
