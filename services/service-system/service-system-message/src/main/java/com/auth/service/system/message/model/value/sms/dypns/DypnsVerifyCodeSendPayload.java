package com.auth.service.system.message.model.value.sms.dypns;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Dypns 验证码短信发送就绪参数
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class DypnsVerifyCodeSendPayload {

	/**
	 * 厂商侧模板编码
	 */
	String providerTemplateCode;

	/**
	 * 模板变量 JSON
	 */
	String templateParam;

}
