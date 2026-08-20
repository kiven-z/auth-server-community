package com.auth.service.auth.model.value.login;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 登出审计上下文：撤销会话后供编排层写入单条登出日志
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class LogoutSessionHint {

	/**
	 * 用户 ID
	 */
	Long userId;

	/**
	 * 会话 ID
	 */
	String jti;

}
