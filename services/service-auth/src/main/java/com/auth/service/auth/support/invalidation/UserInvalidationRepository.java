package com.auth.service.auth.support.invalidation;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.po.user.UserInvalidationStatePO;
import com.auth.service.auth.util.BatchPartition;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 用户失效状态读写：分桶前加载状态、分批递增 perm_version。
 *
 * @author Bunny
 */
@Repository
public class UserInvalidationRepository {

	private final UserMapper userMapper;

	public UserInvalidationRepository(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	/**
	 * 批量加载用户失效分桶所需状态。
	 * @param userIds 用户 ID 集合
	 * @return 状态快照列表
	 */
	public List<UserInvalidationStatePO> loadByUserIds(Collection<Long> userIds) {
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return List.of();
		}

		List<UserInvalidationStatePO> projections = new ArrayList<>();
		for (List<Long> batch : BatchPartition.partitionIds(ids, BatchSizes.SIZE_500)) {
			List<UserInvalidationStatePO> batchProjections = userMapper.selectInvalidationStatesByUserIds(batch);
			if (CollUtil.isNotEmpty(batchProjections)) {
				projections.addAll(batchProjections);
			}
		}
		if (CollUtil.isEmpty(projections)) {
			return List.of();
		}

		return projections.stream()
			.filter(Objects::nonNull)
			.filter(projection -> projection.getUserId() != null)
			.toList();
	}

	/**
	 * 分批递增 perm_version
	 * @param userIds 待递增用户 ID
	 * @return 实际更新行数合计
	 */
	public int incrementPermVersionInBatches(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return 0;
		}

		int totalUpdated = 0;
		for (List<Long> batch : BatchPartition.partitionIds(userIds, BatchSizes.SIZE_500)) {
			totalUpdated += userMapper.incrementPermVersionByUserIds(batch);
		}
		return totalUpdated;
	}

}
