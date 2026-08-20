package com.auth.service.auth.support.authorization;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.DataScopeMapper;
import com.auth.service.auth.mapper.UserAuthorizationGrantMapper;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.po.authorization.UserGrantCodeRowPO;
import com.auth.service.auth.model.po.scope.RoleScopePO;
import com.auth.service.auth.model.po.scope.UserScopeByUserPO;
import com.auth.service.auth.model.po.scope.UserScopePO;
import com.auth.service.auth.model.value.authorization.UserAccountSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 授权画像查询：聚合用户快照、角色权限与数据范围，构建 {@link AuthProfile}。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Repository
public class AuthProfileRepository {

	private final UserAuthorizationGrantMapper userAuthorizationGrantMapper;

	private final DeptDataScopeResolver deptDataScopeResolver;

	private final UserMapper userMapper;

	private final DataScopeMapper dataScopeMapper;

	/**
	 * 根据用户 ID 构建授权画像。
	 * @param userId 用户 ID
	 * @return 授权画像
	 */
	public AuthProfile buildByUserId(Long userId) {
		return buildByUserIds(List.of(userId)).stream()
			.findFirst()
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USER_NOT_FOUND));
	}

	/**
	 * 批量构建授权画像（存在且可加载的用户）。
	 * @param userIds 用户 ID
	 * @return 授权画像列表
	 */
	public List<AuthProfile> buildByUserIds(Collection<Long> userIds) {
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return List.of();
		}

		List<UserEntity> users = userMapper.selectByIds(ids);
		if (CollUtil.isEmpty(users)) {
			return List.of();
		}

		Map<Long, UserAccountSnapshot> accounts = users.stream()
			.map(UserAccountSnapshot::from)
			.collect(Collectors.toMap(UserAccountSnapshot::id, Function.identity(), (left, right) -> left));
		List<Long> existingUserIds = ids.stream().filter(accounts::containsKey).toList();
		if (CollUtil.isEmpty(existingUserIds)) {
			return List.of();
		}

		List<UserGrantCodeRowPO> roleRows = userAuthorizationGrantMapper.selectRoleRowsByUserIds(existingUserIds);
		Map<Long, List<String>> rolesByUser = groupCodesByUser(roleRows);
		List<UserGrantCodeRowPO> permissionRows = userAuthorizationGrantMapper
			.selectPermissionRowsByUserIds(existingUserIds);
		Map<Long, List<String>> permissionsByUser = groupCodesByUser(permissionRows);

		Map<Long, UserScopePO> userScopesByUserId = dataScopeMapper.selectByUserIds(existingUserIds)
			.stream()
			.collect(Collectors.toMap(UserScopeByUserPO::getUserId, this::toUserScope, (left, right) -> left));

		// 超管跳过；已有 user_scope 的不需要再查 role_scope
		Set<String> roleCodesForScope = existingUserIds.stream()
			.filter(userId -> !PermissionConstant.isSuperAdmin(userId))
			.filter(userId -> !userScopesByUserId.containsKey(userId))
			.flatMap(userId -> rolesByUser.getOrDefault(userId, List.of()).stream())
			.collect(Collectors.toCollection(LinkedHashSet::new));
		List<RoleScopePO> roleScopes = CollUtil.isEmpty(roleCodesForScope) ? List.of()
				: dataScopeMapper.selectByRoleCodes(new ArrayList<>(roleCodesForScope));

		List<Long> nonSuperAdminUserIds = existingUserIds.stream()
			.filter(userId -> !PermissionConstant.isSuperAdmin(userId))
			.toList();
		Map<Long, ScopeGrant> deptScopeByUser = deptDataScopeResolver.resolveEffectiveGrants(nonSuperAdminUserIds,
				rolesByUser, userScopesByUserId, roleScopes);

		return existingUserIds.stream()
			.map(userId -> assembleProfile(accounts.get(userId), rolesByUser.getOrDefault(userId, List.of()),
					permissionsByUser.getOrDefault(userId, List.of()), deptScopeByUser.get(userId)))
			.toList();
	}

	/**
	 * 套用超管/管理员特权后组装画像。
	 */
	private AuthProfile assembleProfile(UserAccountSnapshot account, List<String> roles, List<String> permissions,
			ScopeGrant deptScope) {
		long userId = account.id();
		boolean superAdmin = PermissionConstant.isSuperAdmin(userId);
		boolean adminRole = PermissionConstant.isAdminRole(roles);

		List<String> resolvedRoles = roles;
		List<String> resolvedPermissions = permissions;
		ScopeGrant resolvedDeptScope = deptScope;
		if (superAdmin) {
			resolvedRoles = PermissionConstant.ADMIN_ROLES;
			resolvedPermissions = PermissionConstant.ADMIN_WILDCARD_PERMISSIONS;
			resolvedDeptScope = ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).values(List.of()).build();
		}
		else if (adminRole) {
			resolvedRoles = PermissionConstant.ADMIN_ROLES;
			resolvedPermissions = PermissionConstant.ADMIN_WILDCARD_PERMISSIONS;
		}
		if (!superAdmin && resolvedDeptScope == null) {
			resolvedDeptScope = ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build();
		}

		return AuthProfile.builder()
			.userId(account.id())
			.username(account.username())
			.roles(resolvedRoles)
			.permissions(resolvedPermissions)
			.deptScope(resolvedDeptScope)
			.permVersion(account.permVersion())
			.build();
	}

	private Map<Long, List<String>> groupCodesByUser(List<UserGrantCodeRowPO> rows) {
		if (CollUtil.isEmpty(rows)) {
			return Map.of();
		}
		return rows.stream()
			.filter(Objects::nonNull)
			.filter(row -> row.getUserId() != null && CharSequenceUtil.isNotBlank(row.getCode()))
			.collect(Collectors.groupingBy(UserGrantCodeRowPO::getUserId,
					Collectors.mapping(UserGrantCodeRowPO::getCode, Collectors.toList())));
	}

	private UserScopePO toUserScope(UserScopeByUserPO row) {
		UserScopePO scope = new UserScopePO();
		scope.setScopeType(row.getScopeType());
		scope.setScopeDeptIds(row.getScopeDeptIds());
		return scope;
	}

}