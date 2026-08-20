package com.auth.service.auth.service;

import com.auth.service.auth.model.vo.authorization.EffectiveCodesVO;

/**
 * 管理端授权查询：生效角色码与权限码
 *
 * @author Bunny
 */
public interface AdminAuthorizationService {

	/**
	 * 查询用户生效角色码与权限码
	 * @param userId 用户 ID
	 * @return 生效码 VO
	 */
	EffectiveCodesVO getEffectiveCodes(Long userId);

}
