package com.auth.service.auth.support.authorization;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.security.contract.profile.ScopeGrant;
import com.auth.module.security.contract.profile.datascope.DataScopeDimension;
import com.auth.module.security.contract.profile.datascope.DataScopeStorageType;
import com.auth.module.security.contract.profile.datascope.ScopeGrantMerger;
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
 * 数据权限生效解析
 * <p>
 * 同一维内 user_scope 覆盖 role_scope，多角色宽松合并
 * </p>
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class DataScopeResolver {

	private final DeptClosureMapper deptClosureMapper;

	/**
	 * 生成 SELF 数据权限
	 * @return SELF 数据权限
	 */
	private static ScopeGrant selfGrant() {
		return ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build();
	}

	private static DataScopeDimension parseDimension(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return DataScopeDimension.valueOf(raw.strip().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static List<Long> parseIds(String raw) {
		if (raw == null || raw.isBlank()) {
			return List.of();
		}
		List<Long> ids = JSONUtil.toList(raw, Long.class);
		return ids == null ? List.of() : ids;
	}

	/**
	 * 批量解析用户数据权限（调用方预加载 user_scope 与 role_scope）
	 * @param userIds 用户 ID
	 * @param rolesByUser 用户角色码
	 * @param userScopesByUserId 用户级覆盖范围（同一用户可有多维）
	 * @param allRoleScopes 本批涉及角色的范围配置
	 * @return 用户 ID → 维度 → 生效数据权限；未出现的维度不写入
	 */
	public Map<Long, Map<DataScopeDimension, ScopeGrant>> resolveEffectiveGrants(Collection<Long> userIds,
			Map<Long, List<String>> rolesByUser, Map<Long, List<UserScopePO>> userScopesByUserId,
			List<RoleScopePO> allRoleScopes) {
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return Map.of();
		}

		Set<Long> anchorIds = collectIdsAndChildAnchors(userScopesByUserId, allRoleScopes, rolesByUser, ids);
		Map<Long, List<Long>> descendantsByAncestor = loadDescendantsByAncestor(anchorIds);

		Map<Long, Map<DataScopeDimension, ScopeGrant>> grants = new HashMap<>(ids.size());
		for (Long userId : ids) {
			Map<DataScopeDimension, UserScopePO> userByDimension = indexUserScopes(
					userScopesByUserId.getOrDefault(userId, List.of()));
			List<RoleScopePO> roleScopes = filterRoleScopes(rolesByUser.getOrDefault(userId, List.of()), allRoleScopes);
			Map<DataScopeDimension, List<RoleScopePO>> roleByDimension = indexRoleScopes(roleScopes);

			Set<DataScopeDimension> dimensions = new LinkedHashSet<>();
			dimensions.addAll(userByDimension.keySet());
			dimensions.addAll(roleByDimension.keySet());

			Map<DataScopeDimension, ScopeGrant> resolved = new LinkedHashMap<>();
			for (DataScopeDimension dimension : dimensions) {
				UserScopePO userScope = userByDimension.get(dimension);
				if (userScope != null) {
					ScopeGrant grant = materializeRow(dimension, userScope.getScopeType(), userScope.getScopeIds(),
							descendantsByAncestor);
					log.debug("user_scope 生效: userId={}, dimension={}, scopeType={}", userId, dimension,
							grant.getScopeType());
					resolved.put(dimension, grant);
					continue;
				}
				resolved.put(dimension, resolveFromRoleScopes(userId, dimension, roleByDimension.get(dimension),
						descendantsByAncestor));
			}
			grants.put(userId, Map.copyOf(resolved));
		}
		return Map.copyOf(grants);
	}

	private ScopeGrant resolveFromRoleScopes(Long userId, DataScopeDimension dimension, List<RoleScopePO> roleScopes,
			Map<Long, List<Long>> descendantsByAncestor) {
		if (CollUtil.isEmpty(roleScopes)) {
			log.debug("无 role_scope: userId={}, dimension={}, 默认 SELF", userId, dimension);
			return selfGrant();
		}

		List<ScopeGrant> grants = roleScopes.stream()
			.map(scope -> materializeRow(dimension, scope.getScopeType(), scope.getScopeIds(), descendantsByAncestor))
			.toList();
		ScopeGrant merged = ScopeGrantMerger.mergeLoose(grants);
		if (merged == null) {
			log.debug("role_scope 合并为空: userId={}, dimension={}, 默认 SELF", userId, dimension);
			return selfGrant();
		}
		log.debug("role_scope 合并生效: userId={}, dimension={}, scopeType={}", userId, dimension, merged.getScopeType());
		return merged;
	}

	private List<RoleScopePO> filterRoleScopes(List<String> roleCodes, List<RoleScopePO> allRoleScopes) {
		if (CollUtil.isEmpty(roleCodes) || CollUtil.isEmpty(allRoleScopes)) {
			return List.of();
		}
		Set<String> roleCodeSet = new HashSet<>(roleCodes);
		return allRoleScopes.stream().filter(scope -> roleCodeSet.contains(scope.getRoleCode())).toList();
	}

	private Map<DataScopeDimension, UserScopePO> indexUserScopes(List<UserScopePO> scopes) {
		if (CollUtil.isEmpty(scopes)) {
			return Map.of();
		}
		Map<DataScopeDimension, UserScopePO> indexed = new LinkedHashMap<>();
		for (UserScopePO scope : scopes) {
			if (scope == null) {
				continue;
			}
			DataScopeDimension dimension = parseDimension(scope.getDimension());
			if (dimension != null) {
				indexed.putIfAbsent(dimension, scope);
			}
		}
		return indexed;
	}

	private Map<DataScopeDimension, List<RoleScopePO>> indexRoleScopes(List<RoleScopePO> scopes) {
		if (CollUtil.isEmpty(scopes)) {
			return Map.of();
		}
		Map<DataScopeDimension, List<RoleScopePO>> indexed = new LinkedHashMap<>();
		for (RoleScopePO scope : scopes) {
			if (scope == null) {
				continue;
			}
			DataScopeDimension dimension = parseDimension(scope.getDimension());
			if (dimension == null) {
				continue;
			}
			indexed.computeIfAbsent(dimension, key -> new ArrayList<>()).add(scope);
		}
		return indexed;
	}

	private Set<Long> collectIdsAndChildAnchors(Map<Long, List<UserScopePO>> userScopesByUserId,
			List<RoleScopePO> allRoleScopes, Map<Long, List<String>> rolesByUser, List<Long> userIds) {
		Set<Long> anchors = new LinkedHashSet<>();
		for (Long userId : userIds) {
			Map<DataScopeDimension, UserScopePO> userByDimension = indexUserScopes(
					userScopesByUserId.getOrDefault(userId, List.of()));
			for (UserScopePO scope : userByDimension.values()) {
				collectAnchors(scope.getDimension(), scope.getScopeType(), scope.getScopeIds(), anchors);
			}
			List<RoleScopePO> roleScopes = filterRoleScopes(rolesByUser.getOrDefault(userId, List.of()), allRoleScopes);
			for (RoleScopePO scope : roleScopes) {
				DataScopeDimension dimension = parseDimension(scope.getDimension());
				if (dimension != null && userByDimension.containsKey(dimension)) {
					continue;
				}
				collectAnchors(scope.getDimension(), scope.getScopeType(), scope.getScopeIds(), anchors);
			}
		}
		return anchors;
	}

	private void collectAnchors(String dimensionRaw, String scopeTypeRaw, String scopeIdsRaw, Set<Long> anchors) {
		DataScopeDimension dimension = parseDimension(dimensionRaw);
		if (dimension == null || !dimension.isTree()) {
			return;
		}
		if (DataScopeStorageType.parse(scopeTypeRaw) != DataScopeStorageType.IDS_AND_CHILD) {
			return;
		}
		anchors.addAll(parseIds(scopeIdsRaw));
	}

	private Map<Long, List<Long>> loadDescendantsByAncestor(Set<Long> anchorIds) {
		if (CollUtil.isEmpty(anchorIds)) {
			return Map.of();
		}
		List<DeptClosureDescendantRowPO> rows = deptClosureMapper
			.selectDescendantRowsByAncestorIds(new ArrayList<>(anchorIds));
		if (CollUtil.isEmpty(rows)) {
			return Map.of();
		}
		return rows.stream()
			.collect(Collectors.groupingBy(DeptClosureDescendantRowPO::getAncestorId,
					Collectors.mapping(DeptClosureDescendantRowPO::getDescendantId, Collectors.toList())));
	}

	/**
	 * 将库表行转换为 {@link ScopeGrant}
	 * @param dimension 数据维；仅树形维展开 IDS_AND_CHILD
	 * @param scopeTypeRaw 数据库中的 scope_type
	 * @param scopeIdsRaw 数据库中的 scope_ids
	 * @param descendantsByAncestor 预加载的闭包后代
	 * @return ScopeGrant
	 */
	private ScopeGrant materializeRow(DataScopeDimension dimension, String scopeTypeRaw, String scopeIdsRaw,
			Map<Long, List<Long>> descendantsByAncestor) {
		DataScopeStorageType storageType = DataScopeStorageType.parse(scopeTypeRaw);
		if (storageType == null) {
			return selfGrant();
		}

		List<Long> anchorIds = parseIds(scopeIdsRaw);
		List<Long> values = expandValues(dimension, storageType, anchorIds, descendantsByAncestor);
		return switch (storageType) {
			case ALL -> ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build();
			case IDS -> ScopeGrant.builder().scopeType(DataScopeStorageType.IDS).values(values).build();
			case IDS_AND_CHILD ->
				ScopeGrant.builder().scopeType(DataScopeStorageType.IDS_AND_CHILD).values(values).build();
			default -> selfGrant();
		};
	}

	/**
	 * 树形维的 IDS_AND_CHILD 展开为锚点及其后代；其余原样返回锚点
	 * @param dimension 数据维
	 * @param storageType 范围类型
	 * @param anchorIds 锚点 ID
	 * @param descendantsByAncestor 预加载的闭包后代
	 * @return 写入画像的 ID 列表
	 */
	private List<Long> expandValues(DataScopeDimension dimension, DataScopeStorageType storageType,
			List<Long> anchorIds, Map<Long, List<Long>> descendantsByAncestor) {
		if (storageType != DataScopeStorageType.IDS_AND_CHILD || !dimension.isTree()) {
			return anchorIds;
		}
		if (CollUtil.isEmpty(anchorIds)) {
			return List.of();
		}

		LinkedHashSet<Long> union = new LinkedHashSet<>(anchorIds);
		for (Long anchor : anchorIds) {
			union.addAll(descendantsByAncestor.getOrDefault(anchor, List.of()));
		}
		return List.copyOf(union);
	}

}
