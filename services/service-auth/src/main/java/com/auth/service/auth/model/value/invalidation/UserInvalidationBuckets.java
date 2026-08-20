package com.auth.service.auth.model.value.invalidation;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 用户失效分桶
 *
 * @author Bunny
 */
@Value
@Accessors(fluent = true)
public class UserInvalidationBuckets {

	/**
	 * 需要 perm_version + 1 并刷新画像的用户 ID
	 */
	Set<Long> versionBumpUserIds;

	/**
	 * 仅需驱逐 Redis 画像的用户 ID
	 */
	Set<Long> evictOnlyUserIds;

	/**
	 * 构建分桶并规范化集合为不可变副本。
	 * @param versionBumpUserIds 版本递增用户 ID
	 * @param evictOnlyUserIds 仅驱逐用户 ID
	 */
	@Builder
	public UserInvalidationBuckets(Set<Long> versionBumpUserIds, Set<Long> evictOnlyUserIds) {
		this.versionBumpUserIds = Set.copyOf(Objects.requireNonNullElse(versionBumpUserIds, Set.of()));
		this.evictOnlyUserIds = Set.copyOf(Objects.requireNonNullElse(evictOnlyUserIds, Set.of()));
	}

	/**
	 * 空分桶
	 * @return 无任何用户
	 */
	public static UserInvalidationBuckets empty() {
		return UserInvalidationBuckets.builder().build();
	}

	/**
	 * 合并后的全部用户 ID（去重）
	 * @return 所有涉及的用户 ID
	 */
	public Set<Long> allUserIds() {
		Set<Long> all = new HashSet<>(versionBumpUserIds);
		all.addAll(evictOnlyUserIds);
		return Set.copyOf(all);
	}

}
