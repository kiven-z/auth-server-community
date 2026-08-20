package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.convert.admin.role.SysRoleScopeConverter;
import com.auth.service.system.admin.mapper.admin.role.RoleScopeMapper;
import com.auth.service.system.admin.model.entity.RoleScopeEntity;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.role.SysRoleScopeVO;
import com.auth.service.system.admin.service.admin.SysRoleScopeService;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.admin.support.scope.DataScopeFormSupport;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色数据范围服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysRoleScopeServiceImpl extends ServiceImpl<RoleScopeMapper, RoleScopeEntity>
		implements SysRoleScopeService {

	private final RbacReferenceChecker rbacReferenceChecker;

	private final DataScopeFormSupport dataScopeFormSupport;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysRoleScopeVO getByRoleId(Long roleId) {
		rbacReferenceChecker.getExisting(roleId);
		RoleScopeEntity entity = super.getOne(
				Wrappers.<RoleScopeEntity>lambdaQuery().eq(RoleScopeEntity::getRoleId, roleId));
		return SysRoleScopeConverter.toVo(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void upsert(Long roleId, SysDataScopeForm form) {
		SysRoleEntity role = rbacReferenceChecker.getExisting(roleId);
		DataScopeFormSupport.ResolvedDataScope resolved = dataScopeFormSupport.resolve(form);

		RoleScopeEntity existing = super.getOne(
				Wrappers.<RoleScopeEntity>lambdaQuery().eq(RoleScopeEntity::getRoleId, roleId));
		RoleScopeEntity entity = existing != null ? existing : new RoleScopeEntity();
		if (existing == null) {
			entity.setRoleId(roleId);
		}
		SysRoleScopeConverter.applyForm(entity, form, resolved.scopeType().name(), resolved.scopeDeptIds());
		super.saveOrUpdate(entity);

		roleInvalidationTrigger.submitByRoleCodes(List.of(role.getRoleCode()), "upsert-role-scope");
	}

}
