package com.auth.module.security.datapermission.context;

import com.auth.module.security.contract.api.authorization.AuthProfile;

/**
 * 提供当前 {@link AuthProfile} 快照
 *
 * @author Bunny
 */
@FunctionalInterface
public interface LoginUserProvider {

	/**
	 * 获取当前登录用户
	 * @return 当前登录用户
	 */
	AuthProfile currentUser();

}
