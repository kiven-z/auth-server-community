package com.auth.service.system.authorization.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationEventPageVO;

/**
 * 授权失效幂等事件运维查询服务
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationEventQueryService {

	/**
	 * 分页查询幂等事件
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PageResponse<AuthorizationInvalidationEventPageVO> getPage(AuthorizationInvalidationEventQuery query);

	/**
	 * 查询幂等事件详情
	 * @param id 主键
	 * @return 详情
	 */
	AuthorizationInvalidationEventDetailVO getDetail(Long id);

}
