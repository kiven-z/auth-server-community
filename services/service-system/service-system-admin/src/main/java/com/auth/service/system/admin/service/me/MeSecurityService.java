package com.auth.service.system.admin.service.me;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.me.MeLoginLogPageQuery;
import com.auth.service.system.admin.model.vo.me.MeLoginLogPageVO;
import com.auth.service.system.admin.model.vo.me.MeUserSessionVO;

import java.util.List;

/**
 * 个人中心安全活动服务：会话查询/踢出与登录日志
 *
 * @author Bunny
 */
public interface MeSecurityService {

	/**
	 * 查询当前用户活跃会话（标记当前设备）
	 * @return 会话列表
	 */
	List<MeUserSessionVO> listMySessions();

	/**
	 * 踢出当前用户指定会话
	 * @param sessionId 会话 ID（jti）
	 */
	void kickSession(String sessionId);

	/**
	 * 分页查询当前用户登录日志（近 180 天）
	 * @param query 客户端查询条件（loginType、loginResult）
	 * @return 精简分页数据
	 */
	PageResponse<MeLoginLogPageVO> getLoginLogPage(MeLoginLogPageQuery query);

}
