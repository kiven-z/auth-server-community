package com.auth.service.auth.model.constants;

import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import lombok.experimental.UtilityClass;

/**
 * auth 服务域专有操作审计「小模块」编码。
 * <p>
 * 跨能力共享资源码见 {@link PlatformBizCodes}。
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class AuthAuditBizModule {

	/**
	 * 用户会话
	 */
	public static final String AUTH_SESSION = "AUTH_SESSION";

}
