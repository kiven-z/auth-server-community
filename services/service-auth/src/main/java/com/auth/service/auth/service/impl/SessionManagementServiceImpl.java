package com.auth.service.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.convert.UserSessionConverter;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.query.OnlineUserPageQuery;
import com.auth.service.auth.model.value.session.OnlineUserPageSlice;
import com.auth.service.auth.model.value.session.OnlineUserPageSlice.OnlineUserEntry;
import com.auth.service.auth.model.value.session.UserOnlineBrief;
import com.auth.service.auth.model.vo.OnlineUserPageVO;
import com.auth.service.auth.model.vo.UserSessionVO;
import com.auth.service.auth.service.SessionManagementService;
import com.auth.service.auth.support.session.OnlineUserSessionReader;
import com.auth.service.auth.support.session.UserSessionRedisStore;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 会话管理实现：踢人、活跃会话与在线用户查询
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SessionManagementServiceImpl implements SessionManagementService {

	private final UserSessionRedisStore userSessionRedisStore;

	private final OnlineUserSessionReader onlineUserSessionReader;

	private final UserMapper userMapper;

	/**
	 * 分页查询在线用户
	 * @param query HTTP 查询条件
	 * @return 分页 VO
	 */
	@Override
	public PageResponse<OnlineUserPageVO> getOnlineUserPage(OnlineUserPageQuery query) {
		int pageIndex = Objects.requireNonNullElse(query.getPageIndex(), 1);
		int pageSize = Objects.requireNonNullElse(query.getPageSize(), 30);

		Set<Long> allowedUserIds = Optional.ofNullable(query.getUserId()).map(Set::of).orElse(null);

		OnlineUserPageSlice slice = onlineUserSessionReader.pageOnlineUsers(pageIndex, pageSize, allowedUserIds);
		Set<Long> userIds = slice.users().stream().map(OnlineUserEntry::userId).collect(Collectors.toSet());
		Map<Long, UserOnlineBrief> briefMap = loadOnlineBriefByIds(userIds);

		List<OnlineUserPageVO> records = slice.users().stream().map(entry -> {
			OnlineUserPageVO vo = new OnlineUserPageVO();
			vo.setUserId(entry.userId());
			vo.setActiveSessionCount(entry.activeSessionCount());
			vo.setLastLoginAt(entry.lastLoginAt());

			UserOnlineBrief brief = briefMap.get(entry.userId());
			if (brief != null) {
				vo.setUsername(brief.username());
				vo.setNickname(brief.nickname());
			}
			return vo;
		}).toList();

		return PageResponse.of((long) pageIndex, (long) pageSize, slice.total(), records);
	}

	private Map<Long, UserOnlineBrief> loadOnlineBriefByIds(Set<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Collections.emptyMap();
		}

		QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
		wrapper.in("id", userIds).select("id", "username", "nickname");
		return userMapper.selectList(wrapper)
			.stream()
			.collect(Collectors.toMap(UserEntity::getId,
					entity -> UserOnlineBrief.builder()
						.id(entity.getId())
						.username(entity.getUsername())
						.nickname(entity.getNickname())
						.build(),
					(left, right) -> left));
	}

	/**
	 * 踢出指定会话
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	@Override
	public void kickSession(long userId, String sessionId) {
		userSessionRedisStore.terminateSession(userId, sessionId);
	}

	/**
	 * 踢出用户全部会话
	 * @param userId 用户 ID
	 */
	@Override
	public void kickAllSessions(long userId) {
		userSessionRedisStore.terminateAllSessions(userId);
	}

	/**
	 * 批量踢出用户全部会话
	 * @param userIds 用户 ID 列表
	 */
	@Override
	public void kickAllSessions(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return;
		}
		userIds.stream().filter(Objects::nonNull).forEach(userId -> {
			try {
				userSessionRedisStore.terminateAllSessions(userId);
			}
			catch (RuntimeException ex) {
				log.warn("Failed to kick all sessions for userId={}: {}", userId, ex.getMessage());
			}
		});
	}

	/**
	 * 查询用户活跃会话列表
	 * @param userId 用户 ID
	 * @return 活跃会话 VO 列表
	 */
	@Override
	public List<UserSessionVO> listActiveSessions(long userId) {
		List<UserSessionIndex> indexes = userSessionRedisStore.listUserSessions(userId);
		return UserSessionConverter.INSTANCE.toVoList(indexes);
	}

}
