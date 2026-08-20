package com.auth.service.auth.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.model.query.OnlineUserPageQuery;
import com.auth.service.auth.model.vo.OnlineUserPageVO;
import com.auth.service.auth.model.vo.UserSessionVO;

import java.util.Collection;
import java.util.List;

/**
 * 会话管理：踢人、活跃会话与在线用户查询
 *
 * @author Bunny
 */
public interface SessionManagementService {

	/**
	 * 分页查询在线用户
	 * @param query HTTP 查询条件
	 * @return 分页 VO
	 */
	PageResponse<OnlineUserPageVO> getOnlineUserPage(OnlineUserPageQuery query);

	/**
	 * 踢出指定会话
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	void kickSession(long userId, String sessionId);

	/**
	 * 踢出用户全部会话
	 * @param userId 用户 ID
	 */
	void kickAllSessions(long userId);

	/**
	 * 批量踢出用户全部会话
	 * @param userIds 用户 ID 列表
	 */
	void kickAllSessions(Collection<Long> userIds);

	/**
	 * 查询用户活跃会话列表
	 * @param userId 用户 ID
	 * @return 活跃会话 VO 列表
	 */
	List<UserSessionVO> listActiveSessions(long userId);

}
