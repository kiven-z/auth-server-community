package com.auth.service.system.authorization.dispatch;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 事务提交后触发 auth 同步投递的事件载体。
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class AuthorizationInvalidationDispatchEvent {

	/**
	 * Outbox 主键
	 */
	Long outboxId;

}
