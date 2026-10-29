package com.auth.module.security.contract.profile.datascope;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.profile.ScopeGrant;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 宽松合并为一条，供角色级 role_scope 画像构建使用
 *
 * @author Bunny
 */
@UtilityClass
public class ScopeGrantMerger {

	/**
	 * 宽松合并为一条，优先级从高到低：
	 * <ul>
	 * <li>任一条 ALL → ALL</li>
	 * <li>IDS / IDS_AND_CHILD → values 取并集；含 IDS_AND_CHILD → IDS_AND_CHILD，否则 → IDS</li>
	 * <li>仅有 SELF → SELF</li>
	 * </ul>
	 * @param grants 授权片段集合
	 * @return 合并结果；无可合并项时返回 null
	 */
	public static ScopeGrant mergeLoose(Collection<ScopeGrant> grants) {
		if (CollUtil.isEmpty(grants)) {
			return null;
		}
		boolean anyAll = false;
		boolean anySelf = false;
		boolean anyIdsAndChild = false;
		boolean restrictiveWithoutIds = false;
		LinkedHashSet<Long> unionIds = new LinkedHashSet<>();
		for (ScopeGrant grant : grants) {
			if (grant == null || grant.getScopeType() == null) {
				continue;
			}
			switch (grant.getScopeType()) {
				case ALL -> anyAll = true;
				case SELF -> anySelf = true;
				case IDS -> restrictiveWithoutIds |= mergeValues(grant, unionIds);
				case IDS_AND_CHILD -> {
					anyIdsAndChild = true;
					restrictiveWithoutIds |= mergeValues(grant, unionIds);
				}
				default -> anySelf = true;
			}
		}

		if (anyAll) {
			return ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build();
		}
		if (CollUtil.isNotEmpty(unionIds)) {
			DataScopeStorageType mergedType = anyIdsAndChild ? DataScopeStorageType.IDS_AND_CHILD
					: DataScopeStorageType.IDS;
			return ScopeGrant.builder().scopeType(mergedType).values(List.copyOf(unionIds)).build();
		}
		if (restrictiveWithoutIds) {
			DataScopeStorageType mergedType = anyIdsAndChild ? DataScopeStorageType.IDS_AND_CHILD
					: DataScopeStorageType.IDS;
			return ScopeGrant.builder().scopeType(mergedType).values(List.of()).build();
		}
		if (anySelf) {
			return ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build();
		}
		return null;
	}

	/**
	 * 合并节点 ID；空列表记为受限且无 ID
	 * @param grant 单条授权
	 * @param unionIds 并集
	 * @return values 为空时为 true
	 */
	private static boolean mergeValues(ScopeGrant grant, LinkedHashSet<Long> unionIds) {
		List<Long> vals = grant.getValues();
		if (CollUtil.isEmpty(vals)) {
			return true;
		}
		unionIds.addAll(vals);
		return false;
	}

}
