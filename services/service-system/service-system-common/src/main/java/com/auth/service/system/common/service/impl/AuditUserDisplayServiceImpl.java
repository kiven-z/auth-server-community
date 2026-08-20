package com.auth.service.system.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.common.mapper.AuditUserDisplayMapper;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 审计用户展示名服务实现
 *
 * @author Bunny
 */
@Service
public class AuditUserDisplayServiceImpl implements AuditUserDisplayService {

	private final AuditUserDisplayMapper auditUserDisplayMapper;

	public AuditUserDisplayServiceImpl(AuditUserDisplayMapper auditUserDisplayMapper) {
		this.auditUserDisplayMapper = auditUserDisplayMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Long, String> mapUsernamesByIds(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Collections.emptyMap();
		}
		List<Long> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(distinctIds)) {
			return Collections.emptyMap();
		}
		return auditUserDisplayMapper.selectByIds(distinctIds)
			.stream()
			.collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends BaseResponse> void enrichAuditUsernames(IPage<T> page, ToLongFunction<T> extraUserIdGetter,
			BiConsumer<T, String> extraUsernameSetter) {
		if (page == null) {
			return;
		}
		enrichAuditUsernames(page.getRecords(), extraUserIdGetter, extraUsernameSetter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends BaseResponse> void enrichAuditUsernames(List<T> records, ToLongFunction<T> extraUserIdGetter,
			BiConsumer<T, String> extraUsernameSetter) {
		enrichAuditUsernamesInternal(records, extraUserIdGetter, extraUsernameSetter);
	}

	/**
	 * 填充审计用户名，并可选填充额外用户字段
	 * @param records 行列表
	 * @param extraUserIdGetter 额外用户 ID 读取
	 * @param extraUsernameSetter 额外用户名写入
	 * @param <T> 行类型
	 */
	private <T extends BaseResponse> void enrichAuditUsernamesInternal(List<? extends T> records,
			ToLongFunction<T> extraUserIdGetter, BiConsumer<T, String> extraUsernameSetter) {
		if (CollUtil.isEmpty(records)) {
			return;
		}

		List<Long> userIds = records.stream()
			.flatMap(vo -> collectUserIds(vo, extraUserIdGetter))
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		Map<Long, String> usernameMap = mapUsernamesByIds(userIds);

		for (T vo : records) {
			vo.setCreatedByName(usernameMap.get(vo.getCreatedBy()));
			vo.setUpdatedByName(usernameMap.get(vo.getUpdatedBy()));
			if (extraUserIdGetter != null && extraUsernameSetter != null) {
				long extraUserId = extraUserIdGetter.applyAsLong(vo);
				if (extraUserId != 0L) {
					extraUsernameSetter.accept(vo, usernameMap.get(extraUserId));
				}
			}
		}
	}

	/**
	 * 收集单条记录涉及的用户 ID（审计列 + 可选额外列）
	 * @param vo 行对象
	 * @param extraUserIdGetter 额外用户 ID 读取
	 * @param <T> 行类型
	 * @return 用户 ID 流
	 */
	private <T extends BaseResponse> Stream<Long> collectUserIds(T vo, ToLongFunction<T> extraUserIdGetter) {
		Stream.Builder<Long> builder = Stream.builder();
		builder.add(vo.getCreatedBy());
		builder.add(vo.getUpdatedBy());
		if (extraUserIdGetter != null) {
			long extraUserId = extraUserIdGetter.applyAsLong(vo);
			if (extraUserId != 0L) {
				builder.add(extraUserId);
			}
		}
		return builder.build();
	}

}
