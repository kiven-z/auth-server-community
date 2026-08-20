package com.auth.service.system.authorization.model.constants;

import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import lombok.experimental.UtilityClass;

/**
 * authorization / admin 域专有操作审计「小模块」编码。
 * <p>
 * 跨能力共享资源码见 {@link PlatformBizCodes}。
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class AuthorizationAuditBizModule {

	/**
	 * 菜单
	 */
	public static final String SYS_MENU = "SYS_MENU";

	/**
	 * 菜单角色
	 */
	public static final String SYS_MENU_ROLE = "SYS_MENU_ROLE";

	/**
	 * 角色权限
	 */
	public static final String SYS_ROLE_PERMISSION = "SYS_ROLE_PERMISSION";

	/**
	 * 角色数据范围
	 */
	public static final String SYS_ROLE_SCOPE = "SYS_ROLE_SCOPE";

	/**
	 * 用户数据范围
	 */
	public static final String SYS_USER_SCOPE = "SYS_USER_SCOPE";

	/**
	 * 部门闭包
	 */
	public static final String DEPT_CLOSURE = "DEPT_CLOSURE";

	/**
	 * 授权主体
	 */
	public static final String GRANT_TABLE = "GRANT_TABLE";

	/**
	 * 授权失效 Outbox 投递队列
	 */
	public static final String AUTH_INVALIDATION_OUTBOX = "AUTH_INVALIDATION_OUTBOX";

	/**
	 * 授权失效幂等事件
	 */
	public static final String AUTH_INVALIDATION_EVENT = "AUTH_INVALIDATION_EVENT";

}
