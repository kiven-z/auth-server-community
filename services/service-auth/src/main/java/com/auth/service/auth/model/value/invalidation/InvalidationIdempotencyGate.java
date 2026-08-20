package com.auth.service.auth.model.value.invalidation;

import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * eventId 幂等门禁结果：一次调用表达「直接返回 / 等待 / 继续执行」
 *
 * @author Bunny
 */
public sealed interface InvalidationIdempotencyGate permits InvalidationIdempotencyGate.Completed,
		InvalidationIdempotencyGate.InProgress, InvalidationIdempotencyGate.Claimed {

	/**
	 * 事件已处理完成，直接返回已存结果
	 *
	 * @author Bunny
	 */
	@Value
	@Builder
	@Accessors(fluent = true)
	class Completed implements InvalidationIdempotencyGate {

		/**
		 * 已存执行结果摘要
		 */
		AuthorizationInvalidateResponse response;

	}

	/**
	 * 其它 worker 正在处理，调用方宜稍后重试（Outbox 补偿）
	 *
	 * @author Bunny
	 */
	@Value
	@Builder
	@Accessors(fluent = true)
	class InProgress implements InvalidationIdempotencyGate {

	}

	/**
	 * 本线程已抢占，可执行失效流水线
	 *
	 * @author Bunny
	 */
	@Value
	@Builder
	@Accessors(fluent = true)
	class Claimed implements InvalidationIdempotencyGate {

	}

}
