package com.auth.service.auth.service;

import com.auth.service.auth.model.vo.authorization.EffectiveCodesVO;

import java.util.Collection;

/**
 * 管理端授权画像运维：按用户重建并写入 Redis
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

	/**
	 * 刷新用户授权画像缓存
	 * @param userIds 用户 ID 列表
	 */
	void refreshUserCache(Collection<Long> userIds);

}
