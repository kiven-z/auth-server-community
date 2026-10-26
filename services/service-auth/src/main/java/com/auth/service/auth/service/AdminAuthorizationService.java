package com.auth.service.auth.service;

import com.auth.module.security.contract.api.AuthProfile;
import com.auth.service.auth.model.vo.authorization.EffectiveCodesVO;

import java.util.Collection;

/**
 * 管理端授权画像运维：按用户重建并写入 Redis
 *
 * @author Bunny
 */
public interface AdminAuthorizationService {

	/**
	 * 按用户 ID 加载授权画像（供内部接口查询生效角色码与权限码）
	 * @param userId 用户 ID
	 * @return 授权画像
	 */
	AuthProfile loadAuthProfile(Long userId);

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
