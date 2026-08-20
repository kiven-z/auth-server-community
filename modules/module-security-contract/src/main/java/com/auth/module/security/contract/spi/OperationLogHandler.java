package com.auth.module.security.contract.spi;

import com.auth.module.security.contract.event.OperationLogPayloadEvent;

/**
 * 操作日志处理器（SPI）。业务服务实现本接口并注册为 Spring Bean，将负载持久化至 log_operation 等存储。
 *
 * <p>
 * autoconfigure 层提供 @ConditionalOnMissingBean 空实现，未接入时不产生 NPE。
 * </p>
 *
 * <p>
 * 建议实现类对 {@link #handle(OperationLogPayloadEvent)} 使用 @Async，避免阻塞 Web 线程。
 * </p>
 *
 * <pre>
 * &#64;Component
 * public class MyOperationLogHandler implements OperationLogHandler {
 *
 *     &#64;Async
 *     &#64;Override
 *     public void handle(OperationLogPayload payload) {
 *         // insert log_operation
 *     }
 * }
 * </pre>
 *
 * @author Bunny
 */
@FunctionalInterface
public interface OperationLogHandler {

	/**
	 * 处理操作日志负载。
	 * @param payload 负载
	 */
	void handle(OperationLogPayloadEvent payload);

}
