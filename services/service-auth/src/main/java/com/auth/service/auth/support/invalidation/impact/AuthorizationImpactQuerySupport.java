package com.auth.service.auth.support.invalidation.impact;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.auth.mapper.AuthorizationImpactMapper;
import com.auth.service.auth.util.BatchPartition;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * 授权影响面查询：按角色、权限、grant、部门、岗位等
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
	 * 按角色码反查所有可能持有该角色的用户 ID。
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
	 * 按 grant_table 授权主体反查受影响用户 ID（仅 USER）。
	 * @param subjects 授权主体键列表
	 * @return 去重后的用户 ID 集合
	 */
	public Set<Long> findUserIdsByGrantSubjects(Collection<GrantSubjectKey> subjects) {
		List<Long> userSubjectIds = subjects.stream()
			.filter(subject -> subject.subjectType() == GrantTableSubjectType.USER)
			.map(GrantSubjectKey::subjectId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		if (userSubjectIds.isEmpty()) {
			return Set.of();
		}

		Set<Long> userIds = new HashSet<>();
		queryLongKeysInBatches(userSubjectIds, authorizationImpactMapper::selectUserIdsByGrantUserSubjectIds, userIds);
		return Set.copyOf(userIds);
	}

	/**
	 * 按权限码反查受影响用户 ID。
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

	/**
	 * 按 Long 型键分批反查用户 ID 并去重。
	 * @param keys 查询键
	 * @param queryFn 分批查询函数
	 * @return 去重后的用户 ID 集合
	 */
	public Set<Long> findUserIdsByLongKeys(Collection<Long> keys, UnaryOperator<List<Long>> queryFn) {
		List<Long> ids = keys.stream().filter(Objects::nonNull).distinct().toList();
		if (ids.isEmpty()) {
			return Set.of();
		}

		Set<Long> userIds = new HashSet<>();
		queryLongKeysInBatches(ids, queryFn, userIds);
		return Set.copyOf(userIds);
	}

	private void queryLongKeysInBatches(List<Long> keys, UnaryOperator<List<Long>> queryFn, Set<Long> accumulator) {
		for (List<Long> chunk : BatchPartition.partitionIds(keys, BatchSizes.SIZE_500)) {
			accumulator.addAll(queryFn.apply(chunk));
		}
	}

	private void queryStringKeysInBatches(List<String> keys, Function<List<String>, List<Long>> queryFn,
			Set<Long> accumulator) {
		for (List<String> chunk : BatchPartition.partitionStrings(keys, BatchSizes.SIZE_500)) {
			accumulator.addAll(queryFn.apply(chunk));
		}
	}

	private void accumulateBridgedStringsInBatches(List<String> keys, UnaryOperator<List<String>> queryFn,
			Set<String> accumulator) {
		for (List<String> chunk : BatchPartition.partitionStrings(keys, BatchSizes.SIZE_500)) {
			accumulator.addAll(queryFn.apply(chunk));
		}
	}

}
