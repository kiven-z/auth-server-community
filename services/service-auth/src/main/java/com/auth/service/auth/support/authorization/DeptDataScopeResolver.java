package com.auth.service.auth.support.authorization;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.api.datascope.DeptScopeMerger;
import com.auth.service.auth.mapper.DeptClosureMapper;
import com.auth.service.auth.model.po.scope.DeptClosureDescendantRowPO;
import com.auth.service.auth.model.po.scope.RoleScopePO;
import com.auth.service.auth.model.po.scope.UserScopePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门维数据权限生效解析：user_scope 覆盖 role_scope，多角色宽松合并
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class DeptDataScopeResolver {

	private final DeptClosureMapper deptClosureMapper;

	/**
	 * 生成 SELF 数据权限
	 * @return SELF 数据权限
	 */
	private static ScopeGrant selfGrant() {
		return ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build();
	}

	/**
	 * 批量解析用户数据权限（调用方预加载 user_scope 与 role_scope）
	 * @param userIds 用户 ID
	 * @param rolesByUser 用户角色码
	 * @param userScopesByUserId 用户级覆盖范围
	 * @param allRoleScopes 本批涉及角色的范围配置
	 * @return 用户 ID → 生效数据权限
	 */
	public Map<Long, ScopeGrant> resolveEffectiveGrants(Collection<Long> userIds, Map<Long, List<String>> rolesByUser,
			Map<Long, UserScopePO> userScopesByUserId, List<RoleScopePO> allRoleScopes) {
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return Map.of();
		}

		Set<Long> anchorDeptIds = collectDeptAndChildAnchors(userScopesByUserId, allRoleScopes, rolesByUser, ids);
		Map<Long, List<Long>> descendantsByAncestor = loadDescendantsByAncestor(anchorDeptIds);

		Map<Long, ScopeGrant> grants = new HashMap<>(ids.size());
		for (Long userId : ids) {
			UserScopePO userScope = userScopesByUserId.get(userId);
			if (userScope != null) {
				ScopeGrant grant = materializeRow(userScope.getScopeType(), userScope.getScopeDeptIds(),
						descendantsByAncestor);
				log.debug("user_scope 生效: userId={}, scopeType={}", userId, grant.getScopeType());
				grants.put(userId, grant);
				continue;
			}

			List<String> roleCodes = rolesByUser.getOrDefault(userId, List.of());
			List<RoleScopePO> roleScopes = filterRoleScopes(roleCodes, allRoleScopes);
			grants.put(userId, resolveFromRoleScopes(userId, roleScopes, descendantsByAncestor));
		}
		return Map.copyOf(grants);
	}

	private ScopeGrant resolveFromRoleScopes(Long userId, List<RoleScopePO> roleScopes,
			Map<Long, List<Long>> descendantsByAncestor) {
		if (CollUtil.isEmpty(roleScopes)) {
			log.debug("无 role_scope: userId={}, 默认 SELF", userId);
			return selfGrant();
		}

		List<ScopeGrant> grants = roleScopes.stream()
			.map(scope -> materializeRow(scope.getScopeType(), scope.getScopeDeptIds(), descendantsByAncestor))
			.toList();
		ScopeGrant merged = DeptScopeMerger.mergeLoose(grants);
		if (merged == null) {
			log.debug("role_scope 合并为空: userId={}, 默认 SELF", userId);
			return selfGrant();
		}
		log.debug("role_scope 合并生效: userId={}, scopeType={}", userId, merged.getScopeType());
		return merged;
	}

	private List<RoleScopePO> filterRoleScopes(List<String> roleCodes, List<RoleScopePO> allRoleScopes) {
		if (CollUtil.isEmpty(roleCodes) || CollUtil.isEmpty(allRoleScopes)) {
			return List.of();
		}
		Set<String> roleCodeSet = new HashSet<>(roleCodes);
		return allRoleScopes.stream().filter(scope -> roleCodeSet.contains(scope.getRoleCode())).toList();
	}

	private Set<Long> collectDeptAndChildAnchors(Map<Long, UserScopePO> userScopesByUserId,
			List<RoleScopePO> allRoleScopes, Map<Long, List<String>> rolesByUser, List<Long> userIds) {
		Set<Long> anchors = new LinkedHashSet<>();
		for (Long userId : userIds) {
			UserScopePO userScope = userScopesByUserId.get(userId);
			if (userScope != null) {
				collectAnchors(userScope.getScopeType(), userScope.getScopeDeptIds(), anchors);
				continue;
			}
			List<String> roleCodes = rolesByUser.getOrDefault(userId, List.of());
			filterRoleScopes(roleCodes, allRoleScopes)
				.forEach(scope -> collectAnchors(scope.getScopeType(), scope.getScopeDeptIds(), anchors));
		}
		return anchors;
	}

	private void collectAnchors(String scopeTypeRaw, String scopeDeptIdsRaw, Set<Long> anchors) {
		DataScopeStorageType storageType = DataScopeStorageType.parse(scopeTypeRaw);
		if (storageType != DataScopeStorageType.DEPT_AND_CHILD) {
			return;
		}
		anchors.addAll(JSONUtil.toList(scopeDeptIdsRaw, Long.class));
	}

	private Map<Long, List<Long>> loadDescendantsByAncestor(Set<Long> anchorDeptIds) {
		if (CollUtil.isEmpty(anchorDeptIds)) {
			return Map.of();
		}
		List<DeptClosureDescendantRowPO> rows = deptClosureMapper
			.selectDescendantRowsByAncestorIds(new ArrayList<>(anchorDeptIds));
		if (CollUtil.isEmpty(rows)) {
			return Map.of();
		}
		return rows.stream()
			.collect(Collectors.groupingBy(DeptClosureDescendantRowPO::getAncestorId,
					Collectors.mapping(DeptClosureDescendantRowPO::getDescendantId, Collectors.toList())));
	}

	/**
	 * 将数据库中的数据转换为 ScopeGrant 对象
	 * @param scopeTypeRaw 数据库中的 scope_type 字段
	 * @param scopeDeptIdsRaw 数据库中的 scope_dept_ids 字段
	 * @param descendantsByAncestor 预加载的闭包后代
	 * @return ScopeGrant 对象
	 */
	private ScopeGrant materializeRow(String scopeTypeRaw, String scopeDeptIdsRaw,
			Map<Long, List<Long>> descendantsByAncestor) {
		DataScopeStorageType storageType = DataScopeStorageType.parse(scopeTypeRaw);
		if (storageType == null) {
			return selfGrant();
		}

		List<Long> anchorDeptIds = JSONUtil.toList(scopeDeptIdsRaw, Long.class);
		List<Long> valuesForRedis = resolveValuesForRedis(storageType, anchorDeptIds, descendantsByAncestor);
		return switch (storageType) {
			case ALL -> ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build();
			case DEPT -> ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(valuesForRedis).build();
			case DEPT_AND_CHILD ->
				ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT_AND_CHILD).values(valuesForRedis).build();
			default -> selfGrant();
		};
	}

	/**
	 * 将数据库中的 scope_dept_ids 字段转换为 Long 列表
	 * @param storageType 数据范围存储类型
	 * @param anchorDeptIds 数据库中的 scope_dept_ids 字段
	 * @param descendantsByAncestor 预加载的闭包后代
	 * @return Long 列表
	 */
	private List<Long> resolveValuesForRedis(DataScopeStorageType storageType, List<Long> anchorDeptIds,
			Map<Long, List<Long>> descendantsByAncestor) {
		if (storageType != DataScopeStorageType.DEPT_AND_CHILD) {
			return anchorDeptIds;
		}

		if (CollUtil.isEmpty(anchorDeptIds)) {
			return List.of();
		}

		LinkedHashSet<Long> union = new LinkedHashSet<>(anchorDeptIds);
		for (Long anchor : anchorDeptIds) {
			union.addAll(descendantsByAncestor.getOrDefault(anchor, List.of()));
		}
		return List.copyOf(union);
	}

}
