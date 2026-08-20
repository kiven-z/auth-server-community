package com.auth.module.security.contract.spi;

import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;

/**
 * 授权审计记录处理器（SPI）
 *
 * <p>
 * 业务服务如需将授权审计事件持久化（DB / ES / 日志），实现此接口并注册为 Spring Bean autoconfigure 层提供
 *
 * @ConditionalOnMissingBean 空实现，确保不接入时不会 NPE
 * </p>
 *
 * <p>
 * 建议实现方使用 @Async 异步执行，避免阻塞主请求链路
 * </p>
 *
 * <pre>
 * &#64;Component
 * public class MyAuditHandler implements AuthorizationAuditHandler {
 *
 *
 *     &#64;Async
 *
 *
 * &#64;Override
 *     public void handle(SecurityAuthorizationAuditPayloadEvent event) {
 *         // 持久化逻辑
 *     }
 * }
 * </pre>
 * @author Bunny
 */
@FunctionalInterface
public interface AuthorizationAuditHandler {

	/**
	 * 处理授权审计事件
	 * @param event 授权审计负载
	 */
	void handle(SecurityAuthorizationAuditPayloadEvent event);

}
