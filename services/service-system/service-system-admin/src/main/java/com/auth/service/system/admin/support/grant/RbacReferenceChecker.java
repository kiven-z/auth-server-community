package com.auth.service.system.admin.support.grant;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.mapper.admin.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RBAC 授权写入前的关联引用存在性与启用状态校验
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class RbacReferenceChecker {

	private final SysPermissionMapper sysPermissionMapper;

	private final SysRoleMapper sysRoleMapper;

	/**
	 * 权限 ID 均存在且已启用
	 * @param permissionIds 待写入权限 ID
	 */
	public void requireActivePermissionIds(List<Long> permissionIds) {
		if (CollUtil.isEmpty(permissionIds)) {
			return;
		}

		List<Long> distinctIds = permissionIds.stream().distinct().toList();
		Set<Long> activeIds = new HashSet<>(sysPermissionMapper.selectActivePermissionIds(distinctIds));
		if (activeIds.size() != distinctIds.size()) {
			Long invalidId = distinctIds.stream()
				.filter(id -> !activeIds.contains(id))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("active permission mismatch without invalid id"));
			log.warn("Grant reference invalid: id={}", invalidId);
			throw new SystemBusinessException(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		}
	}

	/**
	 * 读取已存在角色，不存在时抛出业务异常
	 * @param roleId 角色 ID
	 * @return 角色实体
	 */
	public SysRoleEntity getExisting(Long roleId) {
		SysRoleEntity role = sysRoleMapper.selectById(roleId);
		if (role == null) {
			log.warn("role not found: id={}", roleId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}
		return role;
	}

	/**
	 * 写入前：角色 ID 均存在且已启用
	 * @param roleIds 待写入角色 ID
	 * @param invalidCode 校验失败时抛出的结果码
	 */
	public void requireExistingEnabledRoleIds(List<Long> roleIds, SystemResultCode invalidCode) {
		if (CollUtil.isEmpty(roleIds)) {
			return;
		}
		List<Long> distinctIds = roleIds.stream().distinct().toList();
		Set<Long> activeIds = new HashSet<>(sysRoleMapper.selectActiveRoleIds(distinctIds));
		if (activeIds.size() != distinctIds.size()) {
			Long invalidId = distinctIds.stream()
				.filter(id -> !activeIds.contains(id))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("active role mismatch without invalid id"));
			log.warn("Grant reference invalid: id={}, code={}", invalidId, invalidCode);
			throw new SystemBusinessException(invalidCode);
		}
	}

}
