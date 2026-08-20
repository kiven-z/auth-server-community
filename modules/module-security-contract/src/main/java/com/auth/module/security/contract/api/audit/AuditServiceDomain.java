package com.auth.module.security.contract.api.audit;

/**
 * 操作审计大模块，用于
 * {@link com.auth.module.security.contract.annotation.OperationLog#serviceDomain()}。
 *
 * @author Bunny
 */
public enum AuditServiceDomain {

	/**
	 * system 服务（系统管理、用户部门菜单等）
	 */
	SYSTEM,

	/**
	 * auth 服务（认证、令牌等）
	 */
	AUTH,

	/**
	 * example 演示 / 集成验证服务
	 */
	EXAMPLE

}
