package com.auth.service.auth.service.impl;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.model.vo.authorization.EffectiveCodesVO;
import com.auth.service.auth.service.AdminAuthorizationService;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端授权查询实现：生效角色码与权限码
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AdminAuthorizationServiceImpl implements AdminAuthorizationService {

	private final AuthProfileRepository authProfileRepository;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EffectiveCodesVO getEffectiveCodes(Long userId) {
		AuthProfile profile = authProfileRepository.buildByUserId(userId);

		if (profile == null) {
			EffectiveCodesVO vo = new EffectiveCodesVO();
			vo.setRoleCodes(List.of());
			vo.setPermissionCodes(List.of());
			return vo;
		}

		EffectiveCodesVO vo = new EffectiveCodesVO();
		vo.setRoleCodes(profile.getRoles());
		vo.setPermissionCodes(profile.getPermissions());
		return vo;
	}

}
