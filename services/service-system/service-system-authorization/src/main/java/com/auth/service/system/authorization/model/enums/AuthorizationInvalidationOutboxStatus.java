package com.auth.service.system.authorization.model.enums;

/**
 * 授权失效 Outbox 投递状态
 *
 * @author Bunny
 */
public enum AuthorizationInvalidationOutboxStatus {

	/**
	 * 待投递或待重试
	 */
	PENDING,

	/**
	 * 补偿任务已抢占，处理中
	 */
	PROCESSING,

	/**
	 * auth 已成功处理
	 */
	SUCCESS,

	/**
	 * 投递失败，等待下次重试
	 */
	FAILED,

	/**
	 * 超过最大重试次数，需人工介入
	 */
	DEAD

}
