package com.auth.service.auth.model.value.session;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 在线用户分页切片（Port 读模型）
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class OnlineUserPageSlice {

	/**
	 * 匹配用户总数
	 */
	long total;

	/**
	 * 当前页用户条目
	 */
	List<OnlineUserEntry> users;

	/**
	 * 在线用户索引条目
	 *
	 * @author Bunny
	 */
	@Value
	@Builder
	@Accessors(fluent = true)
	public static class OnlineUserEntry {

		/**
		 * 用户 ID
		 */
		long userId;

		/**
		 * 最近登录时间（毫秒）
		 */
		long lastLoginAt;

		/**
		 * 活跃会话数
		 */
		int activeSessionCount;

	}

}
