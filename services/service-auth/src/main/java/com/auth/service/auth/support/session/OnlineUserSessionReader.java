package com.auth.service.auth.support.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.auth.model.value.session.OnlineUserPageSlice;
import com.auth.service.auth.model.value.session.OnlineUserPageSlice.OnlineUserEntry;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongPredicate;

/**
 * 在线用户 ZSet 只读查询
 *
 * @author Bunny
 */
@Repository
public class OnlineUserSessionReader {

	/**
	 * 在线用户 ZSet 扫描批次大小
	 */
	private static final int ONLINE_USER_SCAN_BATCH = 64;

	/**
	 * 单页最大条数
	 */
	private static final int MAX_PAGE_SIZE = 100;

	private final UserSessionRedisStore userSessionRedisStore;

	private final OnlineUserZSetStore onlineUserZSetStore;

	public OnlineUserSessionReader(UserSessionRedisStore userSessionRedisStore,
			OnlineUserZSetStore onlineUserZSetStore) {
		this.userSessionRedisStore = userSessionRedisStore;
		this.onlineUserZSetStore = onlineUserZSetStore;
	}

	public OnlineUserPageSlice pageOnlineUsers(int pageIndex, int pageSize, Set<Long> allowedUserIds) {
		int normalizedPageIndex = Math.max(1, pageIndex);
		int normalizedPageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);

		LongPredicate filter = userId -> allowedUserIds == null || allowedUserIds.contains(userId);
		OnlineUserScanResult scanResult = scanOnlineUsers(filter, normalizedPageIndex, normalizedPageSize);
		return OnlineUserPageSlice.builder().total(scanResult.matchedCount()).users(scanResult.pageRecords()).build();
	}

	/**
	 * 扫描在线用户
	 * @param filter 过滤器
	 * @param pageIndex 页索引
	 * @param pageSize 页大小
	 * @return 扫描结果
	 */
	@NotNull
	private OnlineUserScanResult scanOnlineUsers(LongPredicate filter, int pageIndex, int pageSize) {
		int toSkip = (pageIndex - 1) * pageSize;
		List<OnlineUserEntry> pageRecords = new ArrayList<>(pageSize);

		long zcard = onlineUserZSetStore.totalCount();
		if (zcard <= 0) {
			return OnlineUserScanResult.builder().matchedCount(0L).pageRecords(pageRecords).build();
		}

		OnlineUserPageCollector collector = new OnlineUserPageCollector(toSkip, pageSize, pageRecords);
		long matchedCount = 0L;
		long rank = 0L;

		while (rank < zcard) {
			Set<String> members = onlineUserZSetStore.reverseRange(rank, rank + ONLINE_USER_SCAN_BATCH - 1L);

			if (CollUtil.isEmpty(members)) {
				break;
			}

			matchedCount += collectOnlineUserBatch(members, filter, collector);
			rank += ONLINE_USER_SCAN_BATCH;
		}
		return OnlineUserScanResult.builder().matchedCount(matchedCount).pageRecords(pageRecords).build();
	}

	/**
	 * 收集在线用户批次
	 * @param members 成员
	 * @param filter 过滤器
	 * @param collector 收集器
	 * @return 匹配数量
	 */
	private long collectOnlineUserBatch(Set<String> members, LongPredicate filter, OnlineUserPageCollector collector) {
		long batchMatched = 0L;
		for (String userIdText : members) {
			batchMatched += tryCollectOnlineUser(userIdText, filter, collector);
		}
		return batchMatched;
	}

	/**
	 * 尝试收集在线用户
	 * @param userIdText 用户 ID
	 * @param filter 过滤器
	 * @param collector 收集器
	 * @return 收集结果
	 */
	private int tryCollectOnlineUser(String userIdText, LongPredicate filter, OnlineUserPageCollector collector) {
		if (CharSequenceUtil.isBlank(userIdText)) {
			return 0;
		}

		Optional<OnlineUserEntry> entry = loadOnlineUserEntry(userIdText);
		if (entry.isEmpty() || !filter.test(entry.get().userId())) {
			return 0;
		}

		collector.accept(entry.get());
		return 1;
	}

	/**
	 * 加载在线用户条目
	 * @param userIdText 用户 ID
	 * @return 在线用户条目
	 */
	private Optional<OnlineUserEntry> loadOnlineUserEntry(String userIdText) {
		long userId;
		try {
			userId = Long.parseLong(userIdText);
		}
		catch (NumberFormatException ex) {
			onlineUserZSetStore.removeByMember(userIdText);
			return Optional.empty();
		}

		userSessionRedisStore.cleanupStaleActiveSessions(userId);

		long sessionCount = userSessionRedisStore.countActiveSessions(userId);
		if (sessionCount <= 0) {
			onlineUserZSetStore.removeByMember(userIdText);
			return Optional.empty();
		}

		Double score = onlineUserZSetStore.score(userId);
		long lastLoginAt = score != null ? score.longValue() : 0L;
		return Optional.of(OnlineUserEntry.builder()
			.userId(userId)
			.lastLoginAt(lastLoginAt)
			.activeSessionCount((int) sessionCount)
			.build());
	}

	private static final class OnlineUserPageCollector {

		private final int toSkip;

		private final int pageSize;

		private final List<OnlineUserEntry> pageRecords;

		private int skipped;

		private OnlineUserPageCollector(int toSkip, int pageSize, List<OnlineUserEntry> pageRecords) {
			this.toSkip = toSkip;
			this.pageSize = pageSize;
			this.pageRecords = pageRecords;
		}

		private void accept(OnlineUserEntry entry) {
			if (skipped < toSkip) {
				skipped++;
				return;
			}
			if (pageRecords.size() < pageSize) {
				pageRecords.add(entry);
			}
		}

	}

	@Value
	@Builder
	@Accessors(fluent = true)
	private static class OnlineUserScanResult {

		long matchedCount;

		List<OnlineUserEntry> pageRecords;

	}

}
