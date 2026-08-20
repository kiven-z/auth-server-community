package com.auth.module.security.contract.api.audit;

import lombok.experimental.UtilityClass;

/**
 * 跨能力共享的平台资源身份码。
 * <p>
 * 仅收录会被审计、导出、授权失效等 ≥2 个能力复用的稳定资源码；服务域专有编码仍由各服务本地常量维护。
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class PlatformBizCodes {

	/**
	 * 用户
	 */
	public static final String SYS_USER = "SYS_USER";

	/**
	 * 岗位
	 */
	public static final String SYS_POST = "SYS_POST";

	/**
	 * 角色
	 */
	public static final String SYS_ROLE = "SYS_ROLE";

	/**
	 * 部门
	 */
	public static final String SYS_DEPT = "SYS_DEPT";

	/**
	 * 权限
	 */
	public static final String SYS_PERMISSION = "SYS_PERMISSION";

	/**
	 * 授权画像（跨服务刷新编排）
	 */
	public static final String AUTH_PROFILE = "AUTH_PROFILE";

}
