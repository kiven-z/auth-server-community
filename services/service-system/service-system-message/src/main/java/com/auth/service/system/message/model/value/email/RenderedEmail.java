package com.auth.service.system.message.model.value.email;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 邮件模板渲染结果
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class RenderedEmail {

	/**
	 * 渲染后的主题
	 */
	String subject;

	/**
	 * 渲染后的正文
	 */
	String body;

}
