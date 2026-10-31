package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.common.core.constants.BatchSizes;
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
	public List<SysRoleScopeVO> getByRoleId(Long roleId) {
		rbacReferenceChecker.getExisting(roleId);
		return super.list(Wrappers.<RoleScopeEntity>lambdaQuery().eq(RoleScopeEntity::getRoleId, roleId)).stream()
			.map(entity -> {
				String scopeIdsRaw = entity.getScopeIds();
				List<Long> scopeIds = CharSequenceUtil.isBlank(scopeIdsRaw) ? List.of()
						: JSONUtil.toList(scopeIdsRaw, Long.class);
				SysRoleScopeVO vo = new SysRoleScopeVO();
				vo.setId(entity.getId());
				vo.setRoleId(entity.getRoleId());
				vo.setDimension(entity.getDimension());
				vo.setScopeType(entity.getScopeType());
				vo.setScopeIds(scopeIds);
				vo.setRemark(entity.getRemark());
				return vo;
			})
			.toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void replace(Long roleId, List<SysDataScopeForm> forms) {
		SysRoleEntity role = rbacReferenceChecker.getExisting(roleId);
		List<DataScopeFormSupport.ResolvedDataScope> resolved = dataScopeFormSupport.resolveAll(forms);

		int removed = baseMapper.delete(Wrappers.<RoleScopeEntity>lambdaQuery().eq(RoleScopeEntity::getRoleId, roleId));
		if (!resolved.isEmpty()) {
			List<RoleScopeEntity> entities = resolved.stream().map(item -> {
				RoleScopeEntity entity = new RoleScopeEntity();
				entity.setRoleId(roleId);
				entity.setDimension(item.dimension().name());
				entity.setScopeType(item.scopeType().name());
				entity.setScopeIds(CollUtil.isEmpty(item.scopeIds()) ? null : JSONUtil.toJsonStr(item.scopeIds()));
				entity.setRemark(item.remark());
				return entity;
			}).toList();
			super.saveBatch(entities, BatchSizes.SIZE_500);
		}
		if (removed > 0 || !resolved.isEmpty()) {
			roleInvalidationTrigger.submitByRoleCodes(List.of(role.getRoleCode()), "replace-role-scope");
		}
	}

}
