package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.admin.GrantTableService;
import com.auth.service.system.admin.service.admin.SysUserRoleService;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户直连角色授权服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserRoleServiceImpl implements SysUserRoleService {

	private final UserReferenceChecker userReferenceChecker;

	private final RbacReferenceChecker rbacReferenceChecker;

	private final GrantTableService grantTableService;

	private final UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RoleReferenceVO> listAssignedRoles(Long userId) {
		return grantTableService.listAssignedRoles(GrantTableSubjectType.USER, userId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceUserRoles(Long userId, GrantTableAssignRoleForm form) {
		userReferenceChecker.getExistingActive(userId);
		userReferenceChecker.requireOperable(List.of(userId));

		List<Long> roleIds = CollUtil.emptyIfNull(form.getRoleIds()).stream().distinct().toList();
		rbacReferenceChecker.requireExistingEnabledRoleIds(roleIds, SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		grantTableService.replaceSubjectRoleGrants(GrantTableSubjectType.USER.name(), userId, roleIds);

		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "replace-roles");
	}

}
