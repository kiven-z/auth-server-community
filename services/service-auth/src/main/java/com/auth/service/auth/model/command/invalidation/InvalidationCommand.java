package com.auth.service.auth.model.command.invalidation;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.invalidation.AuthorizationInvalidatePayload;
import lombok.Builder;
import lombok.experimental.Accessors;

/**
 * 授权失效命令（应用层入参）：携带契约 Payload 与幂等事件 ID
 *
 * @param eventId 业务事件 ID，供幂等去重
 * @param payload 类型化失效业务键
 * @author Bunny
 */
@Accessors(fluent = true)
public record InvalidationCommand(String eventId, AuthorizationInvalidatePayload payload) {

	/**
	 * 构建失效命令并校验必填字段
	 * @param eventId 事件 ID
	 * @param payload 失效业务键
	 */
	@Builder
	public InvalidationCommand {
		if (CharSequenceUtil.isBlank(eventId)) {
			throw new IllegalArgumentException("事件ID (eventId) 不能为空");
		}
		if (payload == null) {
			throw new IllegalArgumentException("失效业务键 (payload) 不能为空");
		}
	}

}
