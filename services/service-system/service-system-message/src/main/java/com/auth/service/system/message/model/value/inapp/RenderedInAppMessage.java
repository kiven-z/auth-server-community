package com.auth.service.system.message.model.value.inapp;

import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.model.enums.MessageSendSourceType;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 站内信直发定稿快照：渲染正文 + 渠道选项 + 来源
 *
 * @author Bunny
 */
@Value
@Builder(toBuilder = true)
@Accessors(fluent = true)
public class RenderedInAppMessage {

	/**
	 * 模板主键（模板发送时有值；自定义正文为空）
	 */
	Long templateId;

	/**
	 * 标题
	 */
	String title;

	/**
	 * 正文
	 */
	String content;

	/**
	 * 正文类型
	 */
	MessageContentType contentType;

	/**
	 * 场景编码（模板发送时有值）
	 */
	String sceneCode;

	/**
	 * 业务小类
	 */
	Long categoryId;

	/**
	 * 跳转链接（来自渠道选项，可空）
	 */
	String linkUrl;

	/**
	 * 发送来源：TEMPLATE / SYSTEM
	 */
	MessageSendSourceType sourceType;

}
