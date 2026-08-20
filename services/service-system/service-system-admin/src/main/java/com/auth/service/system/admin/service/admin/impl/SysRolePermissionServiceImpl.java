package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.mapper.admin.role.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.entity.SysRolePermissionEntity;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.service.admin.SysRolePermissionService;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色权限分配服务实现
 *
 * @author Bunny
 */

@RequiredArgsConstructor
@Slf4j
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermissionEntity>
		implements SysRolePermissionService {

	private final RbacReferenceChecker rbacReferenceChecker;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PermissionReferenceVO> listAssignedPermissions(Long roleId) {
		rbacReferenceChecker.getExisting(roleId);
		List<PermissionReferencePO> permissionList = baseMapper.selectAssignedPermissionsByRoleId(roleId);

		return ReferenceConverter.INSTANCE.toPermissionReferenceList(permissionList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void assignPermissions(Long roleId, List<Long> permissionIds) {
		SysRoleEntity role = rbacReferenceChecker.getExisting(roleId);

		List<Long> ids = permissionIds.stream().distinct().toList();
		Long operatorId = SecurityUserUtils.getUserId();

		baseMapper.deleteByRoleId(role.getId());
		if (CollUtil.isNotEmpty(ids)) {
			rbacReferenceChecker.requireActivePermissionIds(ids);
			baseMapper.batchInsertRolePermissions(role.getId(), ids, operatorId, operatorId);
		}

		roleInvalidationTrigger.submitByRoleCodes(List.of(role.getRoleCode()), "assign-permissions");
		log.info("Role permissions replaced: roleId={}, count={}", role.getId(), ids.size());
	}

}
