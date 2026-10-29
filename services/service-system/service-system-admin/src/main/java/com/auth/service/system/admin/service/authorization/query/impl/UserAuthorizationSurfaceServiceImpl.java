package com.auth.service.system.admin.service.authorization.query.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.grant.GrantTableSubjectType;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.mapper.authorization.GrantBindingQueryMapper;
import com.auth.service.system.admin.mapper.authorization.UserEffectiveAuthorizationQueryMapper;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.auth.service.system.admin.model.query.authorization.UserEffectivePermissionPageQuery;
import com.auth.service.system.admin.model.query.authorization.UserEffectiveRolePageQuery;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.authorization.query.UserAuthorizationSurfaceService;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户授权面只读服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserAuthorizationSurfaceServiceImpl implements UserAuthorizationSurfaceService {

	private final UserReferenceChecker userReferenceChecker;

	private final GrantBindingQueryMapper grantBindingQueryMapper;

	private final UserEffectiveAuthorizationQueryMapper userEffectiveAuthorizationQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<RoleReferenceVO> pageDirectRoles(Long userId, SubjectRolePageQuery query) {
		userReferenceChecker.getExistingActive(userId);
		Page<RoleReferencePO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<RoleReferencePO> page = grantBindingQueryMapper.selectBoundRolesBySubjectPage(pageParams,
				GrantTableSubjectType.USER.name(), userId, query);

		IPage<RoleReferenceVO> convert = page.convert(ReferenceConverter.INSTANCE::toRoleReference);
		return PageResponse.of(convert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<RoleReferenceVO> pageEffectiveRoles(Long userId, UserEffectiveRolePageQuery query) {
		userReferenceChecker.getExistingActive(userId);
		Page<RoleReferencePO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<RoleReferencePO> page = userEffectiveAuthorizationQueryMapper.selectEffectiveRolesByUserIdPage(pageParams,
				userId, query);

		IPage<RoleReferenceVO> convert = page.convert(ReferenceConverter.INSTANCE::toRoleReference);
		return PageResponse.of(convert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<PermissionReferenceVO> pageEffectivePermissions(Long userId,
			UserEffectivePermissionPageQuery query) {
		userReferenceChecker.getExistingActive(userId);
		Page<PermissionReferencePO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<PermissionReferencePO> page = userEffectiveAuthorizationQueryMapper
			.selectEffectivePermissionsByUserIdPage(pageParams, userId, query);

		IPage<PermissionReferenceVO> convert = page.convert(ReferenceConverter.INSTANCE::toPermissionReference);
		return PageResponse.of(convert);
	}

}
