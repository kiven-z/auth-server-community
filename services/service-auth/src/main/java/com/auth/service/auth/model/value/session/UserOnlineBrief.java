package com.auth.service.auth.model.value.session;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 在线会话列表所需的用户展示快照
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class UserOnlineBrief {

	/**
	 * 用户 ID
	 */
	Long id;

	/**
	 * 用户名
	 */
	String username;

	/**
	 * 昵称
	 */
	String nickname;

}
