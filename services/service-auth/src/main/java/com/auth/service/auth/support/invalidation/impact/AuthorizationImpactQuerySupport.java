package com.auth.service.auth.support.invalidation.impact;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.module.security.contract.grant.GrantTableSubjectType;
import com.auth.module.security.contract.invalidation.GrantSubjectKey;
import com.auth.service.auth.mapper.AuthorizationImpactMapper;
import com.auth.service.auth.util.BatchPartition;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 授权影响面查询：按角色、权限、授权主体反查用户 成员展开读 v_user_grant_subjects
 *
 * @author Bunny
 */
@Repository
public class AuthorizationImpactQuerySupport {

	private final AuthorizationImpactMapper authorizationImpactMapper;

	public AuthorizationImpactQuerySupport(AuthorizationImpactMapper authorizationImpactMapper) {
		this.authorizationImpactMapper = authorizationImpactMapper;
	}

	/**
	 * 按角色码反查所有可能持有该角色的用户 ID
	 * @param roleCodes 角色编码
	 * @return 去重后的用户 ID 集合
	 */
	public Set<Long> findUserIdsByRoleCodes(Collection<String> roleCodes) {
		List<String> codes = roleCodes.stream().filter(Objects::nonNull).distinct().toList();
		if (codes.isEmpty()) {
			return Set.of();
		}

		Set<Long> userIds = new HashSet<>();
		for (List<String> chunk : BatchPartition.partitionStrings(codes, BatchSizes.SIZE_500)) {
			userIds.addAll(authorizationImpactMapper.selectUserIdsByRoleCodes(chunk));
		}
		return Set.copyOf(userIds);
	}

	/**
	 * 按授权主体反查成员用户 ID
	 * @param subjects 授权主体键列表
	 * @return 去重后的用户 ID 集合
	 */
	public Set<Long> findUserIdsByGrantSubjects(Collection<GrantSubjectKey> subjects) {
		Map<GrantTableSubjectType, List<Long>> idsByType = new EnumMap<>(GrantTableSubjectType.class);
		for (GrantSubjectKey subject : subjects) {
			idsByType.computeIfAbsent(subject.subjectType(), type -> new ArrayList<>()).add(subject.subjectId());
		}

		Set<Long> userIds = new HashSet<>();
		for (Map.Entry<GrantTableSubjectType, List<Long>> entry : idsByType.entrySet()) {
			List<Long> ids = entry.getValue().stream().distinct().toList();
			String subjectType = entry.getKey().name();
			for (List<Long> chunk : BatchPartition.partitionIds(ids, BatchSizes.SIZE_500)) {
				userIds.addAll(authorizationImpactMapper.selectUserIdsByGrantSubjects(subjectType, chunk));
			}
		}
		return Set.copyOf(userIds);
	}

	/**
	 * 按权限码反查受影响用户 ID
	 * @param permissionCodes 权限码
	 * @return 去重后的用户 ID 集合
	 */
	public Set<Long> findUserIdsByPermissionCodes(Collection<String> permissionCodes) {
		List<String> codes = permissionCodes.stream().filter(Objects::nonNull).distinct().toList();
		if (codes.isEmpty()) {
			return Set.of();
		}

		Set<String> roleCodes = new LinkedHashSet<>();
		for (List<String> chunk : BatchPartition.partitionStrings(codes, BatchSizes.SIZE_500)) {
			roleCodes.addAll(authorizationImpactMapper.selectRoleCodesByPermissionCodes(chunk));
		}
		if (CollUtil.isEmpty(roleCodes)) {
			return Set.of();
		}

		return findUserIdsByRoleCodes(roleCodes);
	}

}
