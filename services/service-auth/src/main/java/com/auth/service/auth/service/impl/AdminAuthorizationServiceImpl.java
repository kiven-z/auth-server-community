package com.auth.service.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
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
 * 管理端授权画像运维实现：查询生效码并刷新画像缓存
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AdminAuthorizationServiceImpl implements AdminAuthorizationService {

	private final AuthProfileRepository authProfileRepository;

	private final AuthProfileMaterializationService authProfileMaterializationService;

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
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
