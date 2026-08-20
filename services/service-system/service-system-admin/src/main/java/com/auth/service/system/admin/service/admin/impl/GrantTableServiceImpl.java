package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.mapper.admin.role.GrantTableMapper;
import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.admin.GrantTableService;
import com.auth.service.system.admin.support.grant.GrantTableSubjectExistenceVerifier;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
