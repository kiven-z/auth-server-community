package com.auth.service.system.admin.service.authorization.query.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.grant.GrantTableSubjectType;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.convert.authorization.AuthorizationSurfaceConverter;
import com.auth.service.system.admin.mapper.authorization.GrantBindingQueryMapper;
import com.auth.service.system.admin.mapper.authorization.MenuRoleBindingQueryMapper;
import com.auth.service.system.admin.mapper.authorization.RolePermissionBindingQueryMapper;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundMenuPO;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.query.authorization.RoleGrantSubjectPageQuery;
import com.auth.service.system.admin.model.query.authorization.RoleMenuPageQuery;
import com.auth.service.system.admin.model.query.authorization.RolePermissionPageQuery;
import com.auth.service.system.admin.model.vo.authorization.GrantSubjectItemRow;
import com.auth.service.system.admin.model.vo.authorization.RoleAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionBoundMenuItemVO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.service.authorization.query.RoleAuthorizationSurfaceService;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色授权面只读服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class RoleAuthorizationSurfaceServiceImpl implements RoleAuthorizationSurfaceService {

	private final RbacReferenceChecker rbacReferenceChecker;

	private final RolePermissionBindingQueryMapper rolePermissionBindingQueryMapper;

	private final MenuRoleBindingQueryMapper menuRoleBindingQueryMapper;

	private final GrantBindingQueryMapper grantBindingQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RoleAuthorizationSummaryVO getAuthorizationSummary(Long roleId) {
		rbacReferenceChecker.getExisting(roleId);

		RoleAuthorizationSummaryVO summary = new RoleAuthorizationSummaryVO();
		summary.setPermissionCount(rolePermissionBindingQueryMapper.countPermissionsByRoleId(roleId, null));
		summary.setMenuCount(menuRoleBindingQueryMapper.countMenusByRoleId(roleId, null));
		summary.setGrantUserCount(
				grantBindingQueryMapper.countSubjectsByRoleIdAndType(roleId, GrantTableSubjectType.USER.name(), null));
		summary.setGrantDeptCount(
				grantBindingQueryMapper.countSubjectsByRoleIdAndType(roleId, GrantTableSubjectType.DEPT.name(), null));
		summary.setGrantPostCount(
				grantBindingQueryMapper.countSubjectsByRoleIdAndType(roleId, GrantTableSubjectType.POST.name(), null));
		return summary;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<PermissionReferenceVO> pagePermissions(Long roleId, RolePermissionPageQuery query) {
		rbacReferenceChecker.getExisting(roleId);
		long total = rolePermissionBindingQueryMapper.countPermissionsByRoleId(roleId, query);

		Page<PermissionReferencePO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize(), total, false);
		IPage<PermissionReferencePO> page = rolePermissionBindingQueryMapper.selectPermissionsByRoleIdPage(pageParams,
				roleId, query);

		IPage<PermissionReferenceVO> convert = page.convert(ReferenceConverter.INSTANCE::toPermissionReference);
		return PageResponse.of(convert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysPermissionBoundMenuItemVO> pageMenus(Long roleId, RoleMenuPageQuery query) {
		rbacReferenceChecker.getExisting(roleId);
		long total = menuRoleBindingQueryMapper.countMenusByRoleId(roleId, query);

		Page<SysPermissionBoundMenuPO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize(), total, false);
		IPage<SysPermissionBoundMenuPO> page = menuRoleBindingQueryMapper.selectMenusByRoleIdPage(pageParams, roleId,
				query);

		IPage<SysPermissionBoundMenuItemVO> convert = page
			.convert(AuthorizationSurfaceConverter.INSTANCE::toBoundMenuItem);
		return PageResponse.of(convert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<GrantSubjectItemRow> pageGrantSubjects(Long roleId, RoleGrantSubjectPageQuery query) {
		rbacReferenceChecker.getExisting(roleId);
		String type = query.getSubjectType().name();
		long total = grantBindingQueryMapper.countSubjectsByRoleIdAndType(roleId, type, query);

		Page<GrantSubjectItemRow> pageParams = new Page<>(query.getPageIndex(), query.getPageSize(), total, false);
		IPage<GrantSubjectItemRow> page = grantBindingQueryMapper.selectSubjectsByRoleIdAndTypePage(pageParams, roleId,
				type, query);
		return PageResponse.of(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GrantSubjectItemRow> listGrantSubjectSeeds(Long roleId, GrantTableSubjectType subjectType) {
		rbacReferenceChecker.getExisting(roleId);
		List<GrantSubjectItemRow> list = grantBindingQueryMapper.selectSubjectsByRoleIdAndType(roleId,
				subjectType.name());
		return CollUtil.emptyIfNull(list);
	}

}
