package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.mapper.admin.role.GrantTableMapper;
import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.form.role.SysRoleGrantSubjectsForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.admin.GrantTableService;
import com.auth.service.system.admin.support.grant.GrantTableSubjectExistenceVerifier;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.GrantAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * grant_table 读写与主体角色授权编排实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class GrantTableServiceImpl extends ServiceImpl<GrantTableMapper, GrantTableEntity>
		implements GrantTableService {

	private final GrantTableSubjectExistenceVerifier subjectExistenceVerifier;

	private final UserReferenceChecker userReferenceChecker;

	private final RbacReferenceChecker rbacReferenceChecker;

	private final GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RoleReferenceVO> listAssignedRoles(GrantTableSubjectType subjectType, Long subjectId) {
		subjectExistenceVerifier.requireExistingActive(subjectType, subjectId);

		return listBoundRoles(subjectType, subjectId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RoleReferenceVO> listBoundRoles(GrantTableSubjectType subjectType, Long subjectId) {
		List<RoleReferencePO> poList = baseMapper.selectAssignedRolesBySubject(subjectType.name(), subjectId);

		return ReferenceConverter.INSTANCE.toRoleReferenceList(poList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceGrantSubjectsByRoleId(Long roleId, SysRoleGrantSubjectsForm form) {
		rbacReferenceChecker.getExisting(roleId);

		GrantTableSubjectType subjectType = form.getSubjectType();
		List<Long> subjectIds = CollUtil.emptyIfNull(form.getSubjectIds())
			.stream()
			.filter(Objects::nonNull)
			.distinct()
			.toList();

		if (subjectType == GrantTableSubjectType.USER && CollUtil.isNotEmpty(subjectIds)) {
			userReferenceChecker.requireOperable(subjectIds);
		}
		for (Long subjectId : subjectIds) {
			subjectExistenceVerifier.requireExistingActive(subjectType, subjectId);
		}

		List<Long> oldIds = CollUtil
			.emptyIfNull(baseMapper.selectSubjectIdsByRoleIdAndSubjectType(roleId, subjectType.name()));
		baseMapper.deleteByRoleIdAndSubjectType(roleId, subjectType.name());
		if (CollUtil.isNotEmpty(subjectIds)) {
			List<GrantTableEntity> entities = subjectIds.stream().map(subjectId -> {
				GrantTableEntity entity = new GrantTableEntity();
				entity.setSubjectType(subjectType.name());
				entity.setSubjectId(subjectId);
				entity.setRoleId(roleId);
				return entity;
			}).toList();
			super.saveBatch(entities);
		}

		List<GrantSubjectKey> impacted = Stream.concat(oldIds.stream(), subjectIds.stream())
			.distinct()
			.map(subjectId -> new GrantSubjectKey(subjectType, subjectId))
			.toList();
		if (CollUtil.isNotEmpty(impacted)) {
			grantInvalidationTrigger.submitByGrantSubjects(impacted, "replace-grant-subjects");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceOrgSubjectRoles(GrantTableSubjectType subjectType, Long subjectId,
			GrantTableAssignRoleForm form) {
		subjectExistenceVerifier.requireExistingActive(subjectType, subjectId);
		List<Long> roleIds = CollUtil.emptyIfNull(form.getRoleIds()).stream().distinct().toList();

		rbacReferenceChecker.requireExistingEnabledRoleIds(roleIds, SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		replaceSubjectRoleGrants(subjectType.name(), subjectId, roleIds);
		grantInvalidationTrigger.submitByGrantSubjects(List.of(new GrantSubjectKey(subjectType, subjectId)),
				"replace-roles");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceSubjectRoleGrants(String subjectType, Long subjectId, List<Long> roleIds) {
		baseMapper.deleteBySubjectIds(subjectType, List.of(subjectId));
		if (CollUtil.isEmpty(roleIds)) {
			return;
		}

		List<GrantTableEntity> entities = roleIds.stream().map(roleId -> {
			GrantTableEntity entity = new GrantTableEntity();
			entity.setSubjectType(subjectType);
			entity.setSubjectId(subjectId);
			entity.setRoleId(roleId);
			return entity;
		}).toList();
		super.saveBatch(entities);
	}

}
