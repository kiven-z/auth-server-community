package com.auth.service.system.admin.service.authorization.query.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.mapper.authorization.RolePermissionBindingQueryMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.authorization.query.PermissionAuthorizationSurfaceService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限授权面只读服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class PermissionAuthorizationSurfaceServiceImpl implements PermissionAuthorizationSurfaceService {

	private final SysPermissionMapper sysPermissionMapper;

	private final RolePermissionBindingQueryMapper rolePermissionBindingQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<RoleReferenceVO> pageRoles(Long permissionId, SubjectRolePageQuery query) {
		SysPermissionEntity entity = sysPermissionMapper.selectById(permissionId);
		if (entity == null) {
			log.warn("permission not found: id={}", permissionId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}
		long total = rolePermissionBindingQueryMapper.countRolesByPermissionId(permissionId, query);

		Page<RoleReferencePO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize(), total, false);
		IPage<RoleReferencePO> page = rolePermissionBindingQueryMapper.selectRolesByPermissionIdPage(pageParams,
				permissionId, query);

		IPage<RoleReferenceVO> convert = page.convert(ReferenceConverter.INSTANCE::toRoleReference);
		return PageResponse.of(convert);
	}

}
