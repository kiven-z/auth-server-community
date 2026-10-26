package com.auth.service.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.AuthProfile;
import com.auth.service.auth.model.vo.authorization.EffectiveCodesVO;
import com.auth.service.auth.service.AdminAuthorizationService;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.invalidation.AuthProfileMaterializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 管理端授权画像运维实现：按用户重建并写入 Redis
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AdminAuthorizationServiceImpl implements AdminAuthorizationService {

	private final AuthProfileRepository authProfileRepository;

	private final AuthProfileMaterializationService authProfileMaterializationService;

	/**
	 * 按用户 ID 加载授权画像（供内部接口查询生效角色码与权限码）
	 * @param userId 用户 ID
	 * @return 授权画像
	 */
	@Override
	public AuthProfile loadAuthProfile(Long userId) {
		return authProfileRepository.buildByUserId(userId);
	}

	/**
	 * 查询用户生效角色码与权限码
	 * @param userId 用户 ID
	 * @return 生效码 VO
	 */
	@Override
	public EffectiveCodesVO getEffectiveCodes(Long userId) {
		List<AuthProfile> profiles = authProfileRepository.buildByUserIds(List.of(userId));
		EffectiveCodesVO vo = new EffectiveCodesVO();
		if (profiles.isEmpty()) {
			vo.setRoleCodes(List.of());
			vo.setPermissionCodes(List.of());
			return vo;
		}

		AuthProfile profile = profiles.get(0);
		vo.setRoleCodes(profile.getRoles());
		vo.setPermissionCodes(profile.getPermissions());
		return vo;
	}

	/**
	 * 刷新用户授权画像缓存
	 * @param userIds 用户 ID 列表
	 */
	@Override
	public void refreshUserCache(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return;
		}

		List<Long> normalizedUserIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (normalizedUserIds.isEmpty()) {
			return;
		}

		authProfileMaterializationService.refreshInBatches(normalizedUserIds);
	}

}
