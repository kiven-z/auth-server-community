package com.auth.service.system.admin.service.authorization.query.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.mapper.authorization.MenuRoleBindingQueryMapper;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.authorization.query.MenuAuthorizationSurfaceService;
import com.auth.service.system.admin.support.menu.MenuReferenceChecker;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 菜单授权面只读服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MenuAuthorizationSurfaceServiceImpl implements MenuAuthorizationSurfaceService {

	private final MenuReferenceChecker menuReferenceChecker;

	private final MenuRoleBindingQueryMapper menuRoleBindingQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<RoleReferenceVO> pageRoles(Long menuId, SubjectRolePageQuery query) {
		menuReferenceChecker.getExisting(menuId);
		long total = menuRoleBindingQueryMapper.countRolesByMenuId(menuId, query);

		Page<RoleReferencePO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize(), total, false);
		IPage<RoleReferencePO> page = menuRoleBindingQueryMapper.selectRolesByMenuIdPage(pageParams, menuId, query);

		IPage<RoleReferenceVO> convert = page.convert(ReferenceConverter.INSTANCE::toRoleReference);
		return PageResponse.of(convert);
	}

}
