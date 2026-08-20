package com.auth.module.security.datapermission.context;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import lombok.extern.slf4j.Slf4j;

/**
 * 反射式提供者，当 Spring SecurityContext 存在时，从其中读取 AuthProfile
 *
 * @author Bunny
 */
@Slf4j
public class SecurityContextLoginUserProvider implements LoginUserProvider {

	/**
	 * 获取当前登录用户
	 * @return 当前登录用户
	 */
	@Override
	public AuthProfile currentUser() {
		return SecurityUserUtils.currentAuthProfile();
	}

}
